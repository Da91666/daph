package com.dasea.daph.node.flink117.sql.general.connector

import com.dasea.daph.api.config.option.GlobalConfigOptions.DAPH_DT_MAPPING_PATH
import com.dasea.daph.flink117.api.node.strings.StringsMultipleInput
import com.dasea.daph.flink117.constants.FlinkSqlConstants.{NAME_DEFAULT_CATALOG, NAME_DEFAULT_DATABASE}
import com.dasea.daph.flink117.utils.{CatalogUtil, FlinkJdbcUtil, FlinkSqlUtil}
import com.dasea.daph.node.flink117.sql.general.{GaMultipleInputConfig, GeneralNode}
import com.dasea.daph.utils.{JdbcUtil, SQLUtil}
import org.apache.commons.lang3.StringUtils

class GaMultipleInput extends StringsMultipleInput with GeneralNode {
  override def in(): Map[String, Array[String]] = {
    val config = nodeConfig.asInstanceOf[GaMultipleInputConfig]
    val catalogName = config.catalogName
    val databaseName = config.databaseName
    val createSqls = config.createSqls
    val createConfigs = config.createConfigs
    val queryConfigs = config.queryConfigs
    val enableCatalogEDB = config.enableCatalogEDB
    val enableCatalogCDC = config.enableCatalogCDC
    val catalogExtraConfig = config.catalogExtraConfig
    val dtMappingFileRootPath = gc.globalConfig.getOrElse(DAPH_DT_MAPPING_PATH.key, mappingRootPath)

    su(config)
    CatalogUtil.logAndProduceCatalogDetails(tableEnv)

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
      val edbNumber = cec.getOrElse("daph.flink.edbNumber", "")
      val tableExpr = cec.getOrElse("daph.tableNames", "*")
      val onlyPrimaryTables = cec.getOrElse("daph.flink.onlyPrimaryTables", "false").toBoolean
      val onlyUnPrimaryTables = cec.getOrElse("daph.flink.onlyUnPrimaryTables", "false").toBoolean
      val allTableNames = {
        if (cec.contains("daph.schemaName")) tableEnv.listTables
          .filter(_.startsWith(s"${cec("daph.schemaName")}."))
        else tableEnv.listTables
      }
      val needTableNames = FlinkSqlUtil.getNeedTableNames(allTableNames, tableExpr)
      logger.info(s"cg-needTableNames: ${needTableNames.mkString("Array(", ", ", ")")}")

      def doJdbc(schemaName: String = null): Unit = {
        val jc = FlinkJdbcUtil.cggToJCgg(dbType, catalogConfig, databaseName, schemaName, enableCatalogCDC)
        val cc = jc ++ cec

        def createCDCSqls(): (Map[String, String], Map[String, String]) = {
          val ttt = needTableNames.map { tableName =>
            val rtn = if (schemaName == null) tableName else s"`$tableName`"
            val table = tableEnv.from(rtn)
            val fp = table.getResolvedSchema.toString
            s"$NAME_DEFAULT_CATALOG.$NAME_DEFAULT_DATABASE.$rtn" -> fp
          }.toMap
          val tnToFP = {
            if (onlyPrimaryTables) ttt.filter(_._2.contains("PRIMARY KEY"))
            else if (onlyUnPrimaryTables) ttt.filterNot(_._2.contains("PRIMARY KEY"))
            else ttt
          }
          logger.info(s"doJdbc-createCDCSqls-tnToFP:\n$tnToFP")

          val withConfig = cc.filterNot(_._1.startsWith("daph."))
          val tableKey = FlinkSqlUtil.specialKeys(cc("connector"))("tableKey")

          suDefault()

          tnToFP.foreach { case (tn, fieldPart) =>
            val rtn = {
              if (schemaName == null) tn.split("\\.").last
              else tn.replace("`", "").split("\\.").last
            }
            val rwc = {
              dbType match {
                case "postgresql" => withConfig + (tableKey -> rtn) + ("slot.name" -> rtn)
                case _ => withConfig + (tableKey -> rtn)
              }
            }
            val withPart = rwc.map { case (k, v) => s"'$k'='$v'" }
              .reduce((a, b) => s"$a,$b")
            val createSql = FlinkSqlUtil.createSqlString(tn, fieldPart, s"($withPart)", partitions)
            logger.info(s"doJdbc-createCDCSqls-createSql:\n$createSql")

            createTable(createSql)
          }

          (tnToFP, cc)
        }

        if (enableCatalogEDB) {
          if (enableCatalogCDC) gc.setGlobalValue(s"$id:daph-flink-edb-config$edbNumber", createCDCSqls())
          else {
            val ttt = needTableNames.map { tableName =>
              val rtn = if (schemaName == null) tableName else s"`$tableName`"
              val table = tableEnv.from(rtn)
              val fp = table.getResolvedSchema.toString
              s"$catalogName.$databaseName.$rtn" -> fp
            }.toMap
            val tnToFP = {
              if (onlyPrimaryTables) ttt.filter(_._2.contains("PRIMARY KEY"))
              else if (onlyUnPrimaryTables) ttt.filterNot(_._2.contains("PRIMARY KEY"))
              else ttt
            }
            logger.info(s"jdbc-un-enableCatalogCDC-tnToFP:\n$tnToFP")

            gc.setGlobalValue(s"$id:daph-flink-edb-config$edbNumber", (tnToFP, jc))
          }
        }
        else if (enableCatalogCDC) createCDCSqls()
      }

      def doUnJdbc(schemaName: String = null): Unit = {
        if (enableCatalogEDB) {
          val tnToFP = needTableNames.map { tableName =>
            val rtn = if (schemaName == null) tableName else s"`$tableName`"
            val table = tableEnv.from(rtn)
            val fp = table.getResolvedSchema.toString
            s"$catalogName.$databaseName.$rtn" -> fp
          }.toMap
          logger.info(s"doUnJdbc-enableCatalogEDB-tnToFP:\n$tnToFP")

          gc.setGlobalValue(s"$id:daph-flink-edb-config$edbNumber", (tnToFP, Map.empty[String, String]))
        }
      }

      dbType match {
        case "mysql" => doJdbc()
        case "postgresql" => doJdbc(cec("daph.schemaName"))
        case "hive" => doUnJdbc()
        case "iceberg" => doUnJdbc()
        case _ => logger.error("Unsupported catalog type!")
      }
      CatalogUtil.logAndProduceCatalogDetails(tableEnv)

      su(config)
      CatalogUtil.logAndProduceCatalogDetails(tableEnv)
    }

    if (createSqls.nonEmpty) {
      createSqls.foreach(createTable)
      CatalogUtil.logAndProduceCatalogDetails(tableEnv)
    }

    if (createConfigs.nonEmpty) {
      createConfigs.foreach { scc =>
        val createConfig = FlinkSqlUtil.getCC(scc)
        val tableExpr = createConfig.getOrElse("daph.tableNames", "*")
        val enableEDB = createConfig.getOrElse("daph.flink.enableEDB", "false").toBoolean
        val partitions = createConfig.getOrElse("daph.flink.partitions", "")
        val tableKey = FlinkSqlUtil.specialKeys(createConfig("connector"))("tableKey")
        val cc = createConfig + ("dtMappingFileRootPath" -> dtMappingFileRootPath)
        val dc = FlinkJdbcUtil.daphConfig(cc)

        def doJdbc(schemaName: String = null): Unit = {
          JdbcUtil.using(dc) { conn =>
            if (enableEDB) {
              val onlyPrimaryTables = cc.getOrElse("daph.flink.onlyPrimaryTables", "false").toBoolean
              val onlyUnPrimaryTables = cc.getOrElse("daph.flink.onlyUnPrimaryTables", "false").toBoolean
              val edbNumber = cc.getOrElse("daph.flink.edbNumber", "")
              val allTableNames = FlinkJdbcUtil.getTableNames(cc - "daph.tableNames", conn)
              val needTableNames = FlinkSqlUtil.getNeedTableNames(allTableNames, tableExpr)
              logger.info(s"cc-doJdbc-needTableNames: ${needTableNames.mkString("Array(", ", ", ")")}")

              val ttt = needTableNames.map { tn =>
                val rcc = {
                  createConfig("daph.dbType") match {
                    case "postgresql" =>
                      if (cc("connector").equals("postgres-cdc") && !cc.contains("slot.name"))
                        cc + (tableKey -> tn) + ("daph.tableNames" -> tn) + ("slot.name" -> tn)
                      else
                        cc + (tableKey -> tn) + ("daph.tableNames" -> tn)
                    case "doris" =>
                      val databaseName = createConfig("daph.databaseName")
                      cc + (tableKey -> s"$databaseName.$tn") + ("daph.tableNames" -> tn)
                    case _ => cc + (tableKey -> tn) + ("daph.tableNames" -> tn)
                  }
                }
                logger.debug(s"cc-doJdbc-rcc: $rcc")
                val fp = FlinkJdbcUtil.fieldPartString(rcc, conn)
                val wp = FlinkSqlUtil.withPart(rcc)

                val rtn = if (schemaName == null) tn else s"`$schemaName.$tn`"
                val sql = FlinkSqlUtil.createSqlString(rtn, fp, wp, partitions)
                logger.info(s"createConfigs-*-$edbNumber:\n$sql")

                createTable(sql)

                s"$catalogName.$databaseName.$rtn" -> fp
              }.toMap
              val tnToFP = {
                if (onlyPrimaryTables) ttt.filter(_._2.contains("PRIMARY KEY"))
                else if (onlyUnPrimaryTables) ttt.filterNot(_._2.contains("PRIMARY KEY"))
                else ttt
              }

              gc.setGlobalValue(s"$id:daph-flink-edb-config$edbNumber", (tnToFP, cc - "daph.tableNames"))
            }
            else {
              val tns = if (tableExpr.contains(",")) tableExpr.split(",") else Array(tableExpr)
              tns.foreach { tn =>
                val rcc = cc + (tableKey -> tn)
                val sql = FlinkJdbcUtil.createCreateSql(rcc, conn)
                logger.info(s"createConfigs-tns-$tn:\n$sql")

                createTable(sql)
              }
            }
          }
        }

        createConfig("daph.dbType") match {
          case "mysql" | "hive" | "doris" | "starrocks" => doJdbc()
          case "postgresql" | "oracle" | "sqlserver" =>
            val schemaName = createConfig.getOrElse("daph.schemaName", null)
            doJdbc(schemaName)
          case "iceberg" =>
          case _ => logger.error("Unsupported database type!")
        }
      }

      CatalogUtil.logAndProduceCatalogDetails(tableEnv)
    }

    val res = {
      if (queryConfigs.nonEmpty) {
        if (queryConfigs.length == 1) {
          val results = queryConfigs.head.results
          val tns = results.map { rt =>
            val sql = rt.sql
            val tn =
              if (StringUtils.isEmpty(rt.resultTable)) SQLUtil.getTableName(sql)
              else rt.resultTable
            val table = tableEnv.sqlQuery(sql)
            tableEnv.createTemporaryView(tn, table)

            if (tn.split("\\.").length > 2) tn
            else s"$catalogName.$databaseName.$tn"
          }

          getOutLines.map(line => line -> tns)
        } else {
          queryConfigs.map { ls =>
            val outLine = ls.line
            val results = ls.results
            val tns = results.map { rt =>
              val sql = rt.sql
              val tn =
                if (StringUtils.isEmpty(rt.resultTable)) SQLUtil.getTableName(sql)
                else rt.resultTable
              val table = tableEnv.sqlQuery(sql)
              tableEnv.createTemporaryView(tn, table)

              if (tn.split("\\.").length > 2) tn
              else s"$catalogName.$databaseName.$tn"
            }

            outLine -> tns
          }
        }
      } else {
        val tables = tableEnv.listTables(catalogName, databaseName)
        val tns =
          if (tables.nonEmpty) tables.map { t =>
            if (t.split("\\.").length > 1) s"$catalogName.$databaseName.`$t`"
            else s"$catalogName.$databaseName.$t"
          }
          else Array.empty[String]

        getOutLines.map(line => line -> tns)
      }
    }
    res.toMap
  }

  override def getNodeConfigClass = classOf[GaMultipleInputConfig]
}
