package com.dasea.daph.node.flink117.sql.general.connector

import com.dasea.daph.api.config.option.GlobalConfigOptions.{DAPH_DAG_MODEL, DAPH_DT_MAPPING_PATH}
import com.dasea.daph.flink117.api.node.strings.StringsMultipleOutput
import com.dasea.daph.flink117.constants.FlinkSqlConstants.NAME_DEFAULT_CATALOG
import com.dasea.daph.flink117.utils.{CatalogUtil, FlinkJdbcUtil, FlinkSqlUtil}
import com.dasea.daph.node.flink117.sql.general.{GaMultipleOutputConfig, GeneralNode, InsertConfig}
import com.dasea.daph.utils._
import org.apache.flink.core.execution.JobClient

import java.util.UUID
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters.collectionAsScalaIterableConverter

class GaMultipleOutput extends StringsMultipleOutput with GeneralNode {
  override def out(lineToDS: Map[String, Array[String]]): Unit = {
    val config = nodeConfig.asInstanceOf[GaMultipleOutputConfig]
    val catalogName = config.catalogName
    val databaseName = config.databaseName
    val createSqls = config.createSqls
    val createConfigs = config.createConfigs
    val insertConfigs = config.insertConfigs
    val enableCatalogEDB = config.enableCatalogEDB
    val catalogExtraConfig = config.catalogExtraConfig
    val tmp = getExtraOptions.getOrElse(DAPH_DT_MAPPING_PATH.key, mappingRootPath)
    val dtMappingFileRootPath = gc.globalConfig.getOrElse(DAPH_DT_MAPPING_PATH.key, JsonUtil.convert(tmp, classOf[String]))
    val byMultiJobs = config.byMultiJobs
    var tableStructChangeGet = config.tableStructChangeGet
    val autoCreateTable = config.autoCreateTable
    val autoSingleInsertSql = config.autoSingleInsertSql
    var icBuffer = new ArrayBuffer[InsertConfig]
    icBuffer ++= insertConfigs

    su(config)
    CatalogUtil.logAndProduceCatalogDetails(tableEnv)

    def getTnToFPByInsertSqls(tns: Array[String] = Array.empty): Map[String, String] = {
      val filtered = filterIRSByTNS(icBuffer.toArray, tns)
      logger.debug(s"getTnToFPByInsertSqls-filtered:\n${filtered.mkString("Array(", ", ", ")")}")

      val res = filtered.map { ir =>
        val sql = ir.sql
        val primaryKeys = ir.primaryKeys
        val tn = SQLUtil.getTableName(sql)
        // TODO: 待解决
        val select = sql.split(tn).last.trim
        val table = tableEnv.sqlQuery(select)
        val schemaString = table.getResolvedSchema.toString
        val rs = {
          if (primaryKeys.isEmpty) schemaString
          else {
            val length = schemaString.length
            val ts = schemaString.substring(0, length - 1)
            s"$ts,PRIMARY KEY($primaryKeys) NOT ENFORCED)"
          }
        }

        tn -> rs
      }.toMap
      logger.info(s"getTnToFPByInsertSqls-res:\n$res")

      res
    }

    if (!catalogName.equals(NAME_DEFAULT_CATALOG)) {
      val catalogConfig = getCatalogConfig(catalogName)
      val dbType = catalogConfig("type") match {
        case "jdbc" => {
          catalogConfig("base-url") match {
            case a if a.startsWith("jdbc:mysql") => "mysql"
            case a if a.startsWith("jdbc:postgresql") => "postgresql"
          }
        }
        case _ => catalogConfig("type")
      }
      val cec = catalogExtraConfig + ("daph.dbType" -> dbType)

      val partitions = cec.getOrElse("daph.flink.partitions", "")
      val tableNameCase = cec.getOrElse("daph.tableNameCase", "")
      val columnNameCase = cec.getOrElse("daph.columnNameCase", "")

      def createJdbc(cc: Map[String, String], tnToFP: Map[String, String],
                     sourceConfig: Map[String, String] = Map.empty): Unit = {
        val createSqls = {
          val tns = tnToFP.keySet.map { tn =>
            if (tn.contains("`")) tn.replace("`", "").split("\\.").last
            else tn.split("\\.").last
          }
          logger.debug(s"cc-doJdbc-edb-tns: $tns")

          if (sourceConfig.nonEmpty) {
            FlinkJdbcUtil.createByDBConfig(dtMappingFileRootPath, sourceConfig, cc, tns.toArray)
          }
          else {
            FlinkJdbcUtil.createBySchemaString(dbType, dtMappingFileRootPath, tnToFP, cc)
          }
        }
        logger.info(s"cg-createJdbc-createSqls:\n$createSqls")

        FlinkJdbcUtil.createJdbcTable(cc, createSqls)
      }

      def createHive(tnToFP: Map[String, String], isRealTableName: Boolean = true,
                     prefix: String = "", suffix: String = ""): Unit = {
        val format = cec.getOrElse("daph.hive.format", "TextFile")
        val tw = cec.filterNot(_._1.startsWith("daph."))
        val wp = {
          if (tw.isEmpty) ""
          else tw.map { case (k, v) => s"'$k'='$v'" }
            .reduce((a, b) => s"$a,\n$b")
        }
        val dialect = cec.getOrElse("daph.hive.flinkDialect", "hive")
        val isHiveDialect = if (dialect.equals("hive")) true else false

        if (isHiveDialect) setDialect(dialect)

        val css = tnToFP.map { case (tn, fp) =>
          val rtn = {
            if (isRealTableName) tn
            else {
              if (tn.contains("`")) tn.replace("`", "").split("\\.").last
              else tn.split("\\.").last
            }
          }
          val t = TableUtil.getCaseName(tableNameCase, rtn)
          val tmp = fp.split("PRIMARY").head.split("CONSTRAINT").head.trim
          val len = tmp.length
          val rfp = tmp.substring(1, len - 1).replace("NOT NULL", "").replaceAll("""TIMESTAMP\(\d+\)""", "TIMESTAMP")
            .replace("""VARCHAR(2147483647)""", """STRING""")

          if (isHiveDialect) {
            if (wp.isEmpty)
              s"""CREATE TABLE if not exists $prefix$t$suffix (
                 |$rfp
                 |)
                 |STORED AS $format""".stripMargin
            else
              s"""CREATE TABLE if not exists $prefix$t$suffix (
                 |$rfp
                 |)
                 |STORED AS $format
                 |TBLPROPERTIES (
                 |$wp
                 |)""".stripMargin
          } else {
            FlinkSqlUtil.createSqlString(s"$prefix$t$suffix", s"($rfp)", s"($wp)", partitions)
          }
        }

        css.foreach { sql =>
          logger.info(s"cg-enableCatalogEDB-createHive-sql:\n$sql")
          createTable(sql)
        }

        setDialect("default")
      }

      def createIceberg(tnToFP: Map[String, String], isRealTableName: Boolean = true,
                        prefix: String = "", suffix: String = ""): Unit = {
        val wp = cec.filterNot(_._1.startsWith("daph."))
          .map { case (k, v) => s"'$k'='$v'" }
          .reduce((a, b) => s"$a,\n$b")

        val css = tnToFP.map { case (tn, fp) =>
          val rtn = {
            if (isRealTableName) tn
            else {
              if (tn.contains("`")) tn.replace("`", "").split("\\.").last
              else tn.split("\\.").last
            }
          }
          val t = TableUtil.getCaseName(tableNameCase, rtn)

          FlinkSqlUtil.createSqlString(s"$prefix$t$suffix", fp, s"($wp)", partitions)
        }

        css.foreach { sql =>
          logger.info(s"cg-createIceberg-sql:\n$sql")
          createTable(sql)
        }
      }

      if (enableCatalogEDB) {
        logger.info(s"cg-enableCatalogEDB")

        val dagModel = gc.globalConfig.getOrElse(DAPH_DAG_MODEL.key, DAPH_DAG_MODEL.default)
        val inNodeId = {
          if (dagModel.endsWith("-point-line")) {
            val inFlags = Seq("input", "in", "source", "src")
            gc.globalConfig.getNodeDescriptions
              .filter(desc => inFlags.exists(inf => desc.id.startsWith(inf)))
              .head.id
          }
          else cec("daph.flink.edbInNodeId")
        }
        val edbNumber = cec.getOrElse("daph.flink.edbNumber", "")
        val saveMode = cec.getOrElse("daph.flink.saveMode", "append")
        val (tnToFP, sourceConfig) = gc.getGlobalValue(s"$inNodeId:daph-flink-edb-config$edbNumber")
          .asInstanceOf[(Map[String, String], Map[String, String])]
        logger.debug(s"cg-enableCatalogEDB-(tnToFP, sourceConfig):\n$tnToFP\n$sourceConfig")

        val prefix = cec.getOrElse("daph.tableNamePrefix", "")
        val suffix = cec.getOrElse("daph.tableNameSuffix", "")

        dbType match {
          case "mysql" | "postgresql" =>
            val jcc = FlinkJdbcUtil.cggToJCgg(dbType, catalogConfig, databaseName)
            val cc = jcc ++ cec

            createJdbc(cc, tnToFP, sourceConfig)
          case "hive" =>
            createHive(tnToFP, isRealTableName = false, prefix, suffix)
          case "iceberg" => createIceberg(tnToFP, isRealTableName = false, prefix, suffix)
          case _ => logger.error("Unsupported catalog type!")
        }

        // 根据表名，获取表插入语句
        val inSqls = tnToFP.map { case (tn, _) =>
          val rtn = dbType match {
            case "postgresql" | "oracle" | "sqlserver" =>
              if (tn.contains("`")) {
                val sn = cec.getOrElse("daph.schemaName", "public")
                val tmp = tn.replace("`", "").split("\\.")
                val ttn = tmp(3)
                val t = TableUtil.getCaseName(tableNameCase, ttn)
                s"`$sn.$prefix$t$suffix`"
              } else {
                val ttn = tn.split("\\.").last
                val t = TableUtil.getCaseName(tableNameCase, ttn)
                s"$prefix$t$suffix"
              }
            case _ =>
              if (tn.contains("`")) {
                val tmp = tn.replace("`", "").split("\\.")
                val ttn = tmp(3)
                val t = TableUtil.getCaseName(tableNameCase, ttn)
                s"$prefix$t$suffix"
              } else {
                val ttn = tn.split("\\.").last
                val t = TableUtil.getCaseName(tableNameCase, ttn)
                s"$prefix$t$suffix"
              }
          }

          val sql = {
            if (columnNameCase.nonEmpty) {
              val scs = tableEnv.from(tn).getResolvedSchema.getColumnNames.toList
              val tcs = tableEnv.from(rtn).getResolvedSchema.getColumnNames.toList
              val cp = scs.zip(tcs).map { case (s, t) => s"$s AS $t" }.reduce((a, b) => s"$a,\n$b")
              FlinkJdbcUtil.createInsertSql(rtn, tn, saveMode, cp)
            } else {
              FlinkJdbcUtil.createInsertSql(rtn, tn, saveMode)
            }
          }
          logger.debug(s"cg-enableCatalogEDB-insertSql: $sql")

          sql
        }
        icBuffer ++= inSqls.map(InsertConfig(_))

        CatalogUtil.logAndProduceCatalogDetails(tableEnv)
      }
      else if (icBuffer.nonEmpty) {
        logger.info(s"cg-un-enableCatalogEDB")

        dbType match {
          case "mysql" | "postgresql" => {
            val jcc = FlinkJdbcUtil.cggToJCgg(dbType, catalogConfig, databaseName)
            val cc = jcc ++ cec
            val tnToFP = getTnToFPByInsertSqls()
            logger.debug(s"cg-un-enableCatalogEDB-tnToFP:\n$tnToFP")

            createJdbc(cc, tnToFP)
          }
          case "hive" => {
            val tnToFP = getTnToFPByInsertSqls()
            logger.debug(s"cg-un-enableCatalogEDB-tnToFP:\n$tnToFP")

            createHive(tnToFP)
          }
          case "iceberg" => {
            val tnToFP = getTnToFPByInsertSqls()
            logger.debug(s"cg-un-enableCatalogEDB-tnToFP:\n$tnToFP")

            createIceberg(tnToFP)
          }
          case _ => logger.error("Unsupported catalog type!")
        }
      }
    }

    if (createSqls.nonEmpty) {
      createSqls.foreach(createTable)
      CatalogUtil.logAndProduceCatalogDetails(tableEnv)
    }

    if (createConfigs.nonEmpty) {
      createConfigs.foreach { scc =>
        val createConfig = FlinkSqlUtil.getCC(scc)

        val dbType = createConfig("daph.dbType")
        val databaseName = createConfig.getOrElse("daph.databaseName", "")
        val tableExpr = createConfig.getOrElse("daph.tableNames", "*")
        val enableEDB = createConfig.getOrElse("daph.flink.enableEDB", "false").toBoolean
        val saveMode = createConfig.getOrElse("daph.flink.saveMode", "append")
        val autoSingleInsertSql = createConfig.getOrElse("daph.flink.autoSingleInsertSql", "false").toBoolean
        val inTableName = createConfig.getOrElse("daph.flink.inTableName", "")
        val primaryKeys = createConfig.getOrElse("daph.flink.primaryKeys", "")
        val partitions = createConfig.getOrElse("daph.flink.partitions", "")

        def doJdbc(schemaName: String = null, byConfig: Boolean = true): Unit = {
          val dc = FlinkJdbcUtil.daphConfig(createConfig)

          if (enableEDB) {
            val prefix = createConfig.getOrElse("daph.tableNamePrefix", "")
            val suffix = createConfig.getOrElse("daph.tableNameSuffix", "")
            val tableNameCase = createConfig.getOrElse("daph.tableNameCase", "")
            val columnNameCase = createConfig.getOrElse("daph.columnNameCase", "")
            val dagModel = gc.globalConfig.getOrElse(DAPH_DAG_MODEL.key, DAPH_DAG_MODEL.default)
            val inNodeId = {
              if (dagModel.endsWith("-point-line")) {
                val inFlags = Seq("input", "in", "source", "src")
                gc.globalConfig.getNodeDescriptions
                  .filter(desc => inFlags.exists(inf => desc.id.startsWith(inf)))
                  .head.id
              }
              else createConfig("daph.flink.edbInNodeId")
            }
            val edbNumber = createConfig.getOrElse("daph.flink.edbNumber", "")
            val (tf, sc) = gc.getGlobalValue(s"$inNodeId:daph-flink-edb-config$edbNumber")
              .asInstanceOf[(Map[String, String], Map[String, String])]
            logger.debug(s"cc-doJdbc-edb-(tf, sc):\n$tf\n$sc")

            val allTableNames = tf.keySet.map { tn =>
              if (tn.contains("`")) tn.replace("`", "").split("\\.").last
              else tn.split("\\.").last
            }.toArray
            val needTableNames = FlinkSqlUtil.getNeedTableNames(allTableNames, tableExpr)
            val tnToFP = tf.filter { case (tn, _) =>
              val rtn = {
                if (tn.contains("`")) tn.replace("`", "").split("\\.").last
                else tn.split("\\.").last
              }
              needTableNames.contains(rtn)
            }
            logger.debug(s"cc-doJdbc-edb-(tnToFP, sc):\n$tnToFP\n$sc")

            val createSqls = {
              if (sc.nonEmpty && byConfig) {
                val tns = tnToFP.keySet.map { tn =>
                  if (tn.contains("`")) tn.replace("`", "").split("\\.").last
                  else tn.split("\\.").last
                }
                logger.debug(s"cc-doJdbc-edb-tns: $tns")

                FlinkJdbcUtil.createByDBConfig(dtMappingFileRootPath, sc, createConfig, tns.toArray)
              } else {
                FlinkJdbcUtil.createBySchemaString(dbType, dtMappingFileRootPath, tnToFP, createConfig)
              }
            }
            logger.info(s"createConfigs-*-$inNodeId-$edbNumber-createSqls:\n$createSqls")
            FlinkJdbcUtil.createJdbcTable(createConfig, createSqls)

            val rc = {
              if (dbType.equals("doris") || dbType.equals("starrocks")) createConfig - "url"
              else createConfig
            }
            val rss = tnToFP.map { case (tn, fp) =>
              val st = {
                if (tn.contains("`")) tn.replace("`", "").split("\\.").last
                else tn.split("\\.").last
              }
              val t = TableUtil.getCaseName(tableNameCase, st)
              val ftn = s"$prefix$t$suffix"
              val dtn = s"daph_tmp_$ftn"
              val rdtn = if (schemaName == null) dtn else s"`$schemaName.$dtn`"

              val rcc = dbType match {
                case "doris" => {
                  val labelType = createConfig.getOrElse("daph.doris.labelType", "uuid")
                  val labelPrefix = labelType match {
                    case "tn" => ftn
                    case "uuid" =>
                      val uuid = UUID.randomUUID().toString
                      uuid.replace("-", "")
                  }
                  rc + ("table.identifier" -> s"$databaseName.$ftn") + ("sink.label-prefix" -> labelPrefix)
                }
                case "starrocks" => {
                  val labelType = createConfig.getOrElse("daph.starrocks.labelType", "uuid")
                  val labelPrefix = labelType match {
                    case "tn" => ftn
                    case "uuid" =>
                      val uuid = UUID.randomUUID().toString
                      uuid.replace("-", "")
                  }
                  rc + ("daph.tableNames" -> ftn) + ("sink.label-prefix" -> labelPrefix)
                }
                case _ => rc + ("daph.tableNames" -> ftn)
              }
              val wp = FlinkSqlUtil.withPart(rcc)
              val rfp = dbType match {
                case "doris" | "starrocks" => {
                  JdbcUtil.using(dc) { connection =>
                    FlinkJdbcUtil.fieldPartString(rcc, connection)
                  }
                }
                case _ => {
                  if (columnNameCase.nonEmpty) {
                    columnNameCase match {
                      case "lower" => fp.toLowerCase
                      case "upper" => fp.toUpperCase
                    }
                  } else fp
                }
              }
              val createSql = FlinkSqlUtil.createSqlString(rdtn, rfp, wp, partitions)
              logger.info(s"createConfigs-*-$inNodeId-$edbNumber-createSql:\n$createSql")
              createTable(createSql)

              val colPart = {
                val srcCols = tableEnv.from(tn).getResolvedSchema.getColumns.asScala.map { col =>
                  val cn = col.getName
                  val lcn = cn.toLowerCase
                  val ct = col.getDataType.toString
                  (lcn, cn, ct)
                }
                logger.debug(s"createConfigs-*-$inNodeId-$edbNumber-srcCols:\n$srcCols")
                val destCols = tableEnv.from(rdtn).getResolvedSchema.getColumns.asScala.map { col =>
                  val cn = col.getName
                  val ct = col.getDataType.toString
                  (cn, ct)
                }
                logger.debug(s"createConfigs-*-$inNodeId-$edbNumber-destCols:\n$destCols")
                destCols.map { case (cn, ct) =>
                  val three = srcCols.find(_._1.equals(cn.toLowerCase)).get
                  val scn = three._2
                  val sct = three._3
                  if (ct.equals(sct)) s"$scn AS $cn"
                  else s"CAST($scn AS $ct) AS $cn"
                }.reduce((a, b) => s"$a,\n$b")
              }
              val insertSql = FlinkJdbcUtil.createInsertSql(rdtn, tn, saveMode, colPart, dbType)
              logger.info(s"createConfigs-*-$inNodeId-$edbNumber-insertSql:\n$insertSql")
              insertSql
            }

            icBuffer ++= rss.map(InsertConfig(_))
          }
          else {
            val tns = if (tableExpr.contains(",")) tableExpr.split(",") else Array(tableExpr)
            val cc = createConfig + ("dtMappingFileRootPath" -> dtMappingFileRootPath)

            if (autoSingleInsertSql) {
              if (inTableName.nonEmpty) {
                val table = tableEnv.from(inTableName)
                val schemaString = table.getResolvedSchema.toString
                val fp = {
                  if (primaryKeys.isEmpty) schemaString
                  else {
                    val length = schemaString.length
                    val ts = schemaString.substring(0, length - 1)
                    s"$ts,PRIMARY KEY($primaryKeys) NOT ENFORCED)"
                  }
                }
                val tn2FP = Map(tableExpr -> fp)

                val createSqls = FlinkJdbcUtil.createBySchemaString(
                  dbType, dtMappingFileRootPath, tn2FP, createConfig, enableFix = false
                )
                logger.info(s"createConfigs-autoSingleInsertSql-createSql:\n$createSqls")

                FlinkJdbcUtil.createJdbcTable(createConfig, createSqls)

                val rc = {
                  if (dbType.equals("doris") || dbType.equals("starrocks")) cc - "url"
                  else cc
                }
                val tn = {
                  if (tableExpr.contains("`")) tableExpr.replace("`", "").split("\\.").last
                  else tableExpr.split("\\.").last
                }
                val tableKey = FlinkSqlUtil.specialKeys(createConfig("connector"))("tableKey")
                val rcc = {
                  if (dbType.equals("doris")) {
                    rc + (tableKey -> s"$databaseName.$tn")
                  } else {
                    rc + (tableKey -> tn)
                  }
                }
                val tableName = rcc.getOrElse("daph.flink.tableName", rcc("daph.tableNames"))
                //                val sn = if (rcc.contains("daph.schemaName")) rcc("daph.schemaName") else null
                //                val rtn = if (sn == null) tableName else s"`$sn.$tableName`"

                val rfp = JdbcUtil.using(dc) { connection =>
                  FlinkJdbcUtil.fieldPartString(rcc, connection)
                }
                val wp = FlinkSqlUtil.withPart(rcc)
                val createSql = FlinkSqlUtil.createSqlString(tableName, rfp, wp, partitions)
                logger.info(s"createConfigs-tns-$tn:\n$createSql")
                createTable(createSql)

                val colPart = {
                  val srcCols = tableEnv.from(inTableName).getResolvedSchema.getColumns.asScala.map { col =>
                    val cn = col.getName
                    val ct = col.getDataType.toString
                    (cn, ct)
                  }.toMap
                  logger.debug(s"createConfigs-autoSingleInsertSql-srcCols: $srcCols")
                  val destCols = tableEnv.from(tableName).getResolvedSchema.getColumns.asScala.map { col =>
                    val cn = col.getName
                    val ct = col.getDataType.toString
                    (cn, ct)
                  }
                  logger.debug(s"createConfigs-autoSingleInsertSql-destCols: $destCols")
                  destCols.map { case (cn, ct) =>
                    val sct = srcCols(cn).split("\\(").head
                    val tct = ct.split("\\(").head
                    if (tct.equals(sct)) cn
                    else s"CAST($cn AS $ct) AS $cn"
                  }.reduce((a, b) => s"$a,\n$b")
                }
                val insertSql = FlinkJdbcUtil.createInsertSql(tableName, inTableName, saveMode, colPart, dbType)
                logger.info(s"createConfigs-autoSingleInsertSql-insertSql:\n$insertSql")

                logger.debug(s"createConfigs-autoSingleInsertSql-icBuffer: $icBuffer")
                icBuffer = icBuffer.filterNot { ic =>
                  val tn = SQLUtil.getTableName(ic.sql)
                  logger.debug(s"createConfigs-autoSingleInsertSql-tableName: $tableName")
                  logger.debug(s"createConfigs-autoSingleInsertSql-tn: $tn")
                  tn.equals(tableName)
                }
                logger.debug(s"createConfigs-autoSingleInsertSql-icBuffer: $icBuffer")
                icBuffer += InsertConfig(insertSql)
                logger.debug(s"createConfigs-autoSingleInsertSql-icBuffer: $icBuffer")
              }
            }
            else {
              if (icBuffer.nonEmpty) {
                val tnToFP = getTnToFPByInsertSqls(tns)
                logger.info(s"createConfigs-tns-tnToFP:\n$tnToFP")
                val ttt = icBuffer.map { ir =>
                  val tn = SQLUtil.getTableName(ir.sql)
                  val st = {
                    if (tn.contains("`")) tn.replace("`", "").split("\\.").last
                    else tn.split("\\.").last
                  }
                  (tn, st, ir.primaryKeys)
                }.filter(e => tns.contains(e._2))
                val tn2Keys = ttt.map(e => e._1 -> e._3)
                val st2Keys = ttt.map(e => e._2 -> e._3)
                val rc = {
                  if (dbType.equals("doris") || dbType.equals("starrocks")) cc - "url"
                  else cc
                }

                val createSqls = FlinkJdbcUtil.createBySchemaString(
                  dbType, dtMappingFileRootPath, tnToFP,
                  createConfig, enableFix = false, tn2Keys.toMap
                )
                logger.info(s"createConfigs-tns-createSqls:\n$createSqls")
                FlinkJdbcUtil.createJdbcTable(createConfig, createSqls)

                JdbcUtil.using(dc) { connection =>
                  st2Keys.foreach { case (tn, keys) =>
                    val tableKey = FlinkSqlUtil.specialKeys(createConfig("connector"))("tableKey")
                    val rcc = {
                      if (dbType.equals("doris")) {
                        rc + (tableKey -> s"$databaseName.$tn")
                      } else {
                        rc + (tableKey -> tn)
                      }
                    }
                    val rrcc = if (keys.isEmpty) rcc else rcc + ("daph.flink.primaryKeys" -> keys)
                    val createSql = FlinkJdbcUtil.createCreateSql(rrcc, connection)
                    logger.info(s"createConfigs-tns-$tn:\n$createSql")

                    createTable(createSql)
                  }
                }
              }
            }
          }
        }

        def doKafka(): Unit = {
          if (enableEDB) {
            val prefix = createConfig.getOrElse("daph.tableNamePrefix", "")
            val suffix = createConfig.getOrElse("daph.tableNameSuffix", "")
            val saveMode = createConfig.getOrElse("daph.flink.saveMode", "append")
            val tableNameCase = createConfig.getOrElse("daph.tableNameCase", "")
            val columnNameCase = createConfig.getOrElse("daph.columnNameCase", "")
            val dagModel = gc.globalConfig.getOrElse(DAPH_DAG_MODEL.key, DAPH_DAG_MODEL.default)
            val inNodeId = {
              if (dagModel.endsWith("-point-line")) {
                val inFlags = Seq("input", "in", "source", "src")
                gc.globalConfig.getNodeDescriptions
                  .filter(desc => inFlags.exists(inf => desc.id.startsWith(inf)))
                  .head.id
              }
              else createConfig("daph.flink.edbInNodeId")
            }
            val edbNumber = createConfig.getOrElse("daph.flink.edbNumber", "")
            val (tf, sc) = gc.getGlobalValue(s"$inNodeId:daph-flink-edb-config$edbNumber")
              .asInstanceOf[(Map[String, String], Map[String, String])]
            logger.debug(s"cc-doKafka-edb-(tf, sc):\n$tf\n$sc")

            val allTableNames = tf.keySet.map { tn =>
              if (tn.contains("`")) tn.replace("`", "").split("\\.").last
              else tn.split("\\.").last
            }.toArray
            val needTableNames = FlinkSqlUtil.getNeedTableNames(allTableNames, tableExpr)
            val tnToFP = tf.filter { case (tn, _) =>
              val rtn = {
                if (tn.contains("`")) tn.replace("`", "").split("\\.").last
                else tn.split("\\.").last
              }
              needTableNames.contains(rtn)
            }
            logger.debug(s"cc-doKafka-edb-(tnToFP, sc):\n$tnToFP\n$sc")

            val topicConfigs = tnToFP.keySet.map { tn =>
              val arr = tn.replace("`", "").split("\\.")
              val front = arr.dropRight(1).reduce((a, b) => s"$a.$b")
              val st = arr.last
              val t = TableUtil.getCaseName(tableNameCase, st)
              val fst = s"$prefix$t$suffix"

              TopicConfig(s"$front.$fst")
            }.toList
            val kc = createConfig.filter(_._1.startsWith("properties."))
              .map { case (k, v) => k.replace("properties.", "") -> v }
            KafkaUtil.createTopics(topicConfigs, kc)

            val rc = createConfig
            val rss = tnToFP.map { case (tn, fp) =>
              val arr = tn.replace("`", "").split("\\.")
              val front = arr.dropRight(1).reduce((a, b) => s"$a.$b")
              val st = arr.last
              val t = TableUtil.getCaseName(tableNameCase, st)
              val fst = s"$prefix$t$suffix"
              val rcc = rc + ("daph.tableNames" -> s"$front.$fst")

              val rdtn = s"daph_tmp_$fst"
              val rfp = {
                if (columnNameCase.nonEmpty) {
                  columnNameCase match {
                    case "lower" => fp.toLowerCase
                    case "upper" => fp.toUpperCase
                  }
                } else fp
              }
              val wp = FlinkSqlUtil.withPart(rcc)
              val createSql = FlinkSqlUtil.createSqlString(rdtn, rfp, wp, partitions)
              logger.info(s"createConfigs-*-$inNodeId-$edbNumber-createSql:\n$createSql")
              createTable(createSql)

              if (columnNameCase.nonEmpty) {
                val tcs = tableEnv.from(rdtn).getResolvedSchema.getColumnNames.toList.map(_.toLowerCase)
                val scs = tableEnv.from(tn).getResolvedSchema.getColumnNames.toList
                val cp = scs.zip(tcs).map { case (s, t) => s"$s AS $t" }.reduce((a, b) => s"$a,\n$b")
                FlinkJdbcUtil.createInsertSql(rdtn, tn, saveMode, cp)
              } else {
                FlinkJdbcUtil.createInsertSql(rdtn, tn, saveMode)
              }
            }

            icBuffer ++= rss.map(InsertConfig(_))
          }
          else {
            val tns = if (tableExpr.contains(",")) tableExpr.split(",") else Array(tableExpr)

            if (icBuffer.nonEmpty) {

              def getTnToFPByInsertSqls(tns: Array[String] = Array.empty): Map[String, String] = {
                val filtered = {
                  if (tns.isEmpty) icBuffer
                  else icBuffer.filter { ir =>
                    var tableNameFromSql = SQLUtil.getTableName(ir.sql)
                    // remove `
                    if (tableNameFromSql.contains("`")) {
                      tableNameFromSql = tableNameFromSql.replace("`", "")
                    }
                    tns.contains(tableNameFromSql)
                  }
                }
                logger.debug(s"doKafka-getTnToFPByInsertSqls-filtered:\n$filtered")

                val res = filtered.map { ir =>
                  val sql = ir.sql
                  val primaryKeys = ir.primaryKeys
                  val tn = SQLUtil.getTableName(sql)
                  // TODO: 待解决
                  val select = sql.split(tn).last.trim
                  val table = tableEnv.sqlQuery(select)
                  val schemaString = table.getResolvedSchema.toString
                  val rs = {
                    if (primaryKeys.isEmpty) schemaString
                    else {
                      val length = schemaString.length
                      val ts = schemaString.substring(0, length - 1)
                      s"$ts,PRIMARY KEY($primaryKeys) NOT ENFORCED)"
                    }
                  }

                  val newTableName = if (tn.contains("`")) tn.replace("`", "" ) else tn
                  newTableName -> rs
                }.toMap
                logger.info(s"doKafka-getTnToFPByInsertSqls-res:\n$res")

                res
              }

              val tnToFP = getTnToFPByInsertSqls(tns)
              logger.info(s"createConfigs-doKafka-tns-tnToFP:\n$tnToFP")

              val topicConfigs = tnToFP.keySet.map(TopicConfig(_)).toList
              logger.debug(s"createConfigs-doKafka-tns-topicConfigs:\n$topicConfigs")

              val kc = createConfig.filter(_._1.startsWith("properties."))
                .map { case (k, v) => k.replace("properties.", "") -> v }
              KafkaUtil.createTopics(topicConfigs, kc)
              logger.info(s"createConfigs-doKafka-tns-createTopics Success")

              tnToFP.foreach { case (tn, fp) =>
                val fktn = createConfig.getOrElse("daph.flink.tableName", tn)

                val rcc = createConfig + ("daph.tableNames" -> tn)
                val wp = FlinkSqlUtil.withPart(rcc)

                val createSql = FlinkSqlUtil.createSqlString(fktn, fp, wp, partitions)
                logger.debug(s"createConfigs-tns-$tn:\n$createSql")

                createTable(createSql)
              }
            }
          }
        }

        dbType match {
          case "mysql" | "doris" | "starrocks" => doJdbc()
          case "postgresql" | "oracle" | "sqlserver" =>
            val schemaName = createConfig.getOrElse("daph.schemaName", null)
            doJdbc(schemaName = schemaName)
          case "hive" => doJdbc()
          case "kafka" => doKafka()
          case _ => logger.error("Unsupported database type!")
        }
      }

      CatalogUtil.logAndProduceCatalogDetails(tableEnv)
    }

    logger.info(s"icBuffer:\n$icBuffer")

    if (icBuffer.nonEmpty) {
      if (byMultiJobs) {
        val hasOrder = icBuffer.exists(ic => ic.order > -1)
        val sql2TR = {
          if (hasOrder) {
            val yes = icBuffer.toList.filter(ic => ic.order > -1)
              .map(ic => ic.order -> ic.sql).sortBy(_._1).map(_._2)
              .map(sql => sql -> tableEnv.executeSql(sql))
            val nos = icBuffer.toList.filterNot(ic => ic.order > -1)
              .map(_.sql)
              .map(sql => sql -> tableEnv.executeSql(sql))
            yes ++ nos
          } else {
            icBuffer.toList.map(_.sql)
              .map(sql => sql -> tableEnv.executeSql(sql))
          }
        }
        val sql2JC = mutable.Map[String, JobClient]()
        sql2JC ++= sql2TR.map { case (sql, tr) => sql -> tr.getJobClient.get() }.toMap

        def allTerminal = sql2JC
          .map(_._2.getJobStatus.get().isGloballyTerminalState)
          .reduce((a, b) => a && b)

        if (tableStructChangeGet) {
          while (!allTerminal) {
            val localTerminals = sql2JC.filter(_._2.getJobStatus.get().isTerminalState)
            localTerminals.foreach { case (sql, client) =>
              client.cancel()
              logger.warn(s"The sql[$sql] job canceled")
              sql2JC -= sql
            }
            Thread.sleep(5)
          }
        } else {
          while (!allTerminal) {
            val globalTerminals = sql2JC.filter(_._2.getJobStatus.get().isGloballyTerminalState).keys
            sql2JC --= globalTerminals
            Thread.sleep(5)
          }
        }
      }
      else {
        val sqlSet = tableEnv.createStatementSet()
        icBuffer.map(_.sql).foreach(sqlSet.addInsertSql)
        val res = sqlSet.execute()
        res.print()
      }
    }
  }

  private def filterIRSByTNS(irs: Array[InsertConfig], tns: Array[String]): Array[InsertConfig] = {
    if (tns.isEmpty) irs
    else irs.filter { ir =>
      val tn = SQLUtil.getTableName(ir.sql)
      val st = {
        if (tn.contains("`")) tn.replace("`", "").split("\\.").last
        else tn.split("\\.").last
      }
      tns.contains(st)
    }
  }

  override def getNodeConfigClass = classOf[GaMultipleOutputConfig]
}
