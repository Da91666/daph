package com.dasea.daph.utils

import com.dasea.daph.api.config.option.GlobalConfigOptions.DAPH_HOME
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.scala.Logging

import java.sql.{Connection, DriverManager, PreparedStatement}
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object JdbcUtil extends Logging {
  def using[T](config: Map[String, String])(f: Connection => T): T = {
    val dn = loadDriver(config.getOrElse("daph.jdbcDriver", ""), config("daph.url"))
    logger.debug(s"JdbcUtil-using-loadDriver: $dn")
    val connection = jdbcConnection(config)
    usingConnection(connection) { connection =>
      f(connection)
    }
  }

  def createSqlString(tableName: String, cols: Array[(String, String)], config: Map[String, String] = Map.empty): String = {
    val dbType = config("daph.dbType")
    dbType match {
      case "doris" | "starrocks" => {
        val primaryKeyPart = config.getOrElse("primaryKeyPart", "")
        val indexPart = config.getOrElse(s"daph.$dbType.indexPart", "")
        val engineType = config.getOrElse(s"daph.$dbType.engineType", "olap")
        val keyType = config.getOrElse(s"daph.$dbType.keyType", "DUPLICATE")
        val keys = config.getOrElse(s"daph.$dbType.keys", "")
        val castType = config.getOrElse(s"daph.$dbType.castType", "VARCHAR(255)")
        val commentPart = config.getOrElse(s"daph.$dbType.commentPart", "")
        val partitionPart = config.getOrElse(s"daph.$dbType.partitionPart", "")
        val distPart = config.getOrElse(s"daph.$dbType.distPart", "")
        val bucketPart = config.getOrElse(s"daph.$dbType.bucketPart", "")
        val rollupPart = config.getOrElse(s"daph.$dbType.rollupPart", "")
        val orderPart = config.getOrElse(s"daph.$dbType.orderPart", "")
        val diffPart = if (dbType.equals("doris")) rollupPart else orderPart
        val properties = {
          val props = config.filter(_._1.contains(s"daph.$dbType.props."))
          if (props.nonEmpty) {
            props.map { case (k, v) =>
              val rk = k.split("\\.").last
              s""""$rk"="$v""""
            }.reduce((a, b) => s"$a,\n$b")
          } else s""""replication_num"="1""""
        }

        engineType match {
          case "olap" => {
            val realKeys = {
              val arr = if (keys.isEmpty) primaryKeyPart.split("PRIMARY KEY").last
                .replace("(", "").replace(")", "").replace("`", "")
                .split(",")
              else keys.split(",")
              arr.filterNot(StringUtils.isEmpty).map(_.trim)
            }
            logger.debug(s"realKeys: ${realKeys.mkString("Array(", ", ", ")")}")

            if (realKeys.isEmpty) {
              val fp = cols.map { case (k, v) => s"$k $v" }.reduce((a, b) => s"$a,\n$b")
              val rfp = if (indexPart.isEmpty) fp else s"$fp,\n$indexPart"
              val dp = if (distPart.isEmpty) "DISTRIBUTED BY RANDOM" else distPart

              s"""
                 |CREATE TABLE IF NOT EXISTS $tableName(
                 |$rfp
                 |)
                 |ENGINE=$engineType
                 |$commentPart
                 |$partitionPart
                 |$dp $bucketPart
                 |$diffPart
                 |PROPERTIES (\n$properties)
                 |""".stripMargin
            }
            else {
              val realKeyString = realKeys.reduce((a, b) => s"$a,$b")
              val nos = List("FLOAT", "DOUBLE", "STRING", "ARRAY", "STRUCT", "MAP")
              val keyCols = cols.filter(c => realKeys.contains(c._1))
              logger.debug(s"keyCols: ${keyCols.mkString("Array(", ", ", ")")}")
              val kcm = keyCols.map { case (cn, ct) =>
                val rct = ct.split(" ").head.split("\\(").head
                if (nos.contains(rct)) (cn, castType)
                else (cn, ct)
              }.toMap
              logger.debug(s"kcm: $kcm")
              val realKeyCols = realKeys.map(k => k -> kcm(k))
              logger.debug(s"realKeyCols: ${realKeyCols.mkString("Array(", ", ", ")")}")
              val tailCols = cols.diff(keyCols)
              logger.debug(s"tailCols: ${tailCols.mkString("Array(", ", ", ")")}")
              val realCols = realKeyCols ++ tailCols
              logger.debug(s"realCols: ${realCols.mkString("Array(", ", ", ")")}")
              val fp = realCols.map { case (k, v) => s"$k $v" }.reduce((a, b) => s"$a,\n$b")
              val rfp = if (indexPart.isEmpty) fp else s"$fp,\n$indexPart"

              // 整库：keyType，keys为空，distPart必须为空
              // 整表：若是olap引擎，distPart不能为空
              val dp = {
                if (distPart.isEmpty) s"DISTRIBUTED BY HASH($realKeyString)"
                else distPart
              }
              s"""
                 |CREATE TABLE IF NOT EXISTS $tableName(
                 |$rfp
                 |)
                 |ENGINE=$engineType
                 |$keyType KEY($realKeyString)
                 |$commentPart
                 |$partitionPart
                 |$dp $bucketPart
                 |$diffPart
                 |PROPERTIES (\n$properties)
                 |""".stripMargin
            }
          }
          case "mysql" => {
            val fp = cols.map { case (k, v) => s"$k $v" }.reduce((a, b) => s"$a,\n$b")
            s"""
               |CREATE EXTERNAL TABLE IF NOT EXISTS $tableName(
               |$fp
               |)
               |ENGINE=mysql
               |PROPERTIES (\n$properties)
               |""".stripMargin
          }
        }
      }
      case "postgresql" => {
        val basePart = cols.map { case (k, v) => s""""$k" $v""" }.reduce((a, b) => s"$a,$b")
        val colPart = {
          if (config.contains("primaryKeyPart")) {
            val pks = config("primaryKeyPart").split("\\(").last.replace(")", "").split(",")
              .map(k => s""""$k"""")
              .reduce((a, b) => s"$a,$b")
            s"($basePart, PRIMARY KEY($pks))"
          } else s"($basePart)"
        }
        s"CREATE TABLE if not exists $tableName $colPart"
      }
      case _ => {
        val basePart = cols.map { case (k, v) => s"$k $v" }.reduce((a, b) => s"$a,$b")
        val colPart = {
          if (config.contains("primaryKeyPart")) s"($basePart, ${config("primaryKeyPart")})"
          else s"($basePart)"
        }
        s"CREATE TABLE if not exists $tableName $colPart"
      }
    }
  }

  def executeSQLs(config: Map[String, String], connection: Connection): Boolean = {
    config("daph.dbType") match {
      case _ =>
        val st = connection.createStatement()
        val sqls = config("daph.sqls")
        logger.info(s"executeSQLs: $sqls")

        if (sqls.contains(";")) {
          sqls.split(";").map(st.execute).reduce((a, b) => a && b)
        } else {
          st.execute(sqls)
        }
    }
  }

  /**
   * 判断表是否存在，这里当前只支持oracle sqlserver
   *
   * @param schemaName schemaName
   * @param tableName tableName
   * @param sqlTempalate sql template
   * @param connection 连接信息
   * @return 有值返回true, 没有值，返回false
   */
  def checkTableExists(schemaName: String, tableName: String, sqlTempalate: String, connection: Connection): Boolean = {
    var pst: PreparedStatement = null
    try {
      pst = connection.prepareStatement(sqlTempalate)
      pst.setString(1, tableName)
      pst.setString(2, schemaName)
      pst.executeQuery.next
    } catch {
      case e: Exception => logger.error("Execute table exists failed", e)
        throw new RuntimeException("Execute table exists failed")
    } finally {
      if (pst != null) {
        pst.close()
      }
    }


  }


  def getTableNames(config: Map[String, String], connection: Connection): Array[String] = {
    val dbType = config("daph.dbType")

    dbType match {
      case _ =>
        val database = config("daph.databaseName")
        val schema =
          if (dbType.equals("mysql")) config.getOrElse("daph.schemaName", "%")
          else config.getOrElse("daph.schemaName", null)
        val pattern = config.getOrElse("daph.pattern", null)
        val meta = connection.getMetaData
        val rs = meta.getTables(database, schema, pattern, null)

        val kv = mutable.Map[String, String]()
        while (rs.next) {
          val tn = rs.getString("TABLE_NAME")
          val tt = rs.getString("TABLE_TYPE")

          kv += tn -> tt
        }

        kv.filter { case (_, v) => v.equals("TABLE") }.keySet.toArray
    }
  }

  def getSingleTableFields(config: Map[String, String], connection: Connection): Array[(String, Map[String, String])] = {
    val res = new ArrayBuffer[(String, Map[String, String])]
    val dbType = config("daph.dbType")
    dbType match {
      case _ =>
        val database = config("daph.databaseName")
        val schema = config.getOrElse("daph.schemaName", null)
        val tableName = config("daph.tableName")
        val pattern = config.getOrElse("daph.pattern", null)
        val meta = connection.getMetaData
        logger.debug(s"getSingleTableFields-config: $config")
        val crs = meta.getColumns(database, schema, tableName, pattern)
        while (crs.next) {
          val columnName = crs.getString("COLUMN_NAME")
          val columnType = crs.getString("TYPE_NAME")
          val columnSize = crs.getString("COLUMN_SIZE")
          val dd = crs.getString("DECIMAL_DIGITS")
          //          val columnDef = crs.getString("COLUMN_DEF")
          //          val dataType = crs.getString("DATA_TYPE")
          //          val isNullable = crs.getString("IS_NULLABLE")
          //          val nullable = crs.getString("NULLABLE")
          //          val isAuto = crs.getString("IS_AUTOINCREMENT")
          //          val remarks = crs.getString("REMARKS")

          val ct = columnType.toUpperCase
          val cs = dbType match {
            case _ =>
              ct match {
                case "VARCHAR" => if (columnSize.toInt > 65533) "65533" else columnSize
                case _ => columnSize
              }
          }

          res += columnName -> Map(
            "columnType" -> ct,
            "columnSize" -> cs,
            "dd" -> dd
          )
        }
    }

    res.toArray
  }

  def getSingleTablePrimaryKeys(config: Map[String, String], connection: Connection): Array[String] = {
    val res = new ArrayBuffer[String]
    config("daph.dbType") match {
      case _ =>
        val database = config("daph.databaseName")
        val schema = config.getOrElse("daph.schemaName", null)
        val tableName = config("daph.tableName")
        val meta = connection.getMetaData
        val rs = meta.getPrimaryKeys(database, schema, tableName)

        while (rs.next) {
          // 获取主键信息
          val columnName = rs.getString("COLUMN_NAME")
          res += columnName
        }
    }

    res.toArray
  }

  def getSingleTableUniqueKeys(config: Map[String, String], connection: Connection): Array[String] = {
    val res = new ArrayBuffer[String]
    config("daph.dbType") match {
      case _ =>
        val database = config("daph.databaseName")
        val schema = config.getOrElse("daph.schemaName", null)
        val tableName = config("daph.tableName")
        val meta = connection.getMetaData
        val rs = meta.getIndexInfo(database, schema, tableName, true, false)

        while (rs.next) {
          // 获取主键信息
          val columnName = rs.getString("COLUMN_NAME")
          res += columnName
        }
    }

    res.toArray
  }

  def getCreateSql(sourceTableName: String, dtMappingFileRootPath: String,
                   sourceDBConfig: Map[String, String], targetDBConfig: Map[String, String],
                   connection: Connection): String = {
    logger.debug(s"getCreateSql-sourceTableName: $sourceTableName")
    val sourceDBType = sourceDBConfig("daph.dbType")
    val targetDBType = targetDBConfig("daph.dbType")
    val targetDBName = targetDBConfig("daph.databaseName")
    val targetSNName = targetDBConfig.getOrElse("daph.schemaName", null)
    val prefix = targetDBConfig.getOrElse("daph.tableNamePrefix", "")
    val suffix = targetDBConfig.getOrElse("daph.tableNameSuffix", "")
    val tableNameCase = targetDBConfig.getOrElse("daph.tableNameCase", "")
    val columnNameCase = targetDBConfig.getOrElse("daph.columnNameCase", "")

    val sconfig = sourceDBConfig + ("daph.tableName" -> sourceTableName)
    val fields = {
      val tfs = getSingleTableFields(sconfig, connection)
      val tt = if (columnNameCase.nonEmpty) {
        columnNameCase match {
          case "lower" => tfs.map { case (k, v) => (k.toLowerCase, v) }
          case "upper" => tfs.map { case (k, v) => (k.toUpperCase, v) }
        }
      } else tfs

      tt.map { case (cn, ctc) =>
        val rctc = {
          if (ctc("columnType").equals("VARCHAR") && ctc("columnSize").equals("2147483647")) {
            targetDBType match {
              //              case "doris" => ctc + ("columnType" -> "STRING")
              case _ => ctc + ("columnType" -> "TEXT")
            }
          } else ctc
        }
        (cn, rctc)
      }
    }
    logger.debug(s"getCreateSql-fields:\n${fields.mkString("Array(", ", ", ")")}")

    val primaryKeys = {
      val tpks = getSingleTablePrimaryKeys(sconfig, connection)
      if (columnNameCase.nonEmpty) {
        columnNameCase match {
          case "lower" => tpks.map(_.toLowerCase)
          case "upper" => tpks.map(_.toUpperCase)
        }
      } else tpks
    }
    logger.debug(s"getCreateSql-primaryKeys: ${primaryKeys.mkString("Array(", ", ", ")")}")

    val cols = {
      if (sourceDBType.equals(targetDBType)) {
        fields.map { case (cn, ctConfig) =>
          val ct = getRealColumnType(targetDBType, ctConfig)
          cn -> ct
        }
      } else {
        val relations = CommonUtil.getRelations("db", sourceDBType, targetDBType, dtMappingFileRootPath)
        fields.map { case (cn, ctConfig) =>
          logger.debug(s"getCreateSql-ctConfig: $ctConfig")
          val ct = ctConfig("columnType").split("\\(").head
          val option = relations.find(_.sourceTypes.contains(ct))
          val rct = option match {
            case Some(x) =>
              val target = x.targetTypes
              logger.debug(s"getCreateSql-target: ${target.mkString("Array(", ", ", ")")}")
              val head = target.head
              logger.debug(s"getCreateSql-head: $head")
              head
            case None => throw new Exception(
              s"""
                 |当前不存在【数据库类型为$sourceDBType，字段类型为$ct => 数据库类型为$targetDBType】的映射关系。
                 |请在【${DAPH_HOME.default}/conf/mapping/db-${sourceDBType}_$targetDBType-mapping.json】文件中自行添加映射关系。
                 |""".stripMargin
            )
          }

          val rrct = getRealColumnType(targetDBType, ctConfig + ("columnType" -> rct))
          cn -> rrct
        }
      }
    }

    val rstn = TableUtil.getCaseName(tableNameCase, sourceTableName)
    val tableName = targetDBType match {
      case "postgresql" | "sqlserver" =>
        val sn = if (targetSNName == null) "public" else targetSNName
        s"$targetDBName.$sn.$prefix$rstn$suffix"
      case "oracle" =>
        val sn = if (targetSNName == null) "public" else targetSNName
        s""""$sn"."$prefix$rstn$suffix""""
      case _ => s"$targetDBName.$prefix$rstn$suffix"
    }

    val config = if (primaryKeys.nonEmpty) {
      val primaryKeyPart = s"PRIMARY KEY(${primaryKeys.reduce((a, b) => s"$a,$b")})"
      targetDBConfig + ("primaryKeyPart" -> primaryKeyPart)
    } else targetDBConfig

    createSqlString(tableName, cols, config)
  }

  def getCreateSqls(tableNames: Array[String], dtMappingFileRootPath: String,
                    sourceDBConfig: Map[String, String], targetDBConfig: Map[String, String],
                    connection: Connection): Array[String] = {
    tableNames.map(tn => getCreateSql(
      tn, dtMappingFileRootPath,
      sourceDBConfig, targetDBConfig,
      connection)
    )
  }

  def usingConnection[T](connection: Connection)(f: Connection => T): T = {
    try {
      f(connection)
    } finally {
      if (connection != null) connection.close()
    }
  }

  private def jdbcConnection(config: Map[String, String]): Connection = {
    val url = config("daph.url")
    val username = config("daph.username")
    val password = config("daph.password")
    logger.debug(s"jdbcConnection-config:\n$config")

    DriverManager.getConnection(url, username, password)
  }

  private def loadDriver(dn: String, url: String = ""): String = {
    val cls = {
      if (StringUtils.isEmpty(dn)) url match {
        case url if url.startsWith("jdbc:mysql") => Class.forName("com.mysql.cj.jdbc.Driver")
        case url if url.startsWith("jdbc:oracle") => Class.forName("oracle.jdbc.driver.OracleDriver")
        case url if url.startsWith("jdbc:postgresql") => Class.forName("org.postgresql.Driver")
        case url if url.startsWith("jdbc:sqlserver") => Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
        case url if url.startsWith("jdbc:hive") => Class.forName("org.apache.hive.jdbc.HiveDriver")
        case url if url.startsWith("jdbc:doris") => Class.forName("org.apache.doris.jdbc.Driver")
        case _ => throw new IllegalArgumentException("Unknown or unsupported jdbc driver!")
      } else Class.forName(dn)
    }

    cls.getName
  }

  def getRealColumnType(dbType: String, ctConfig: Map[String, String]): String = {
    logger.debug(s"getRealColumnType-ctConfig: $ctConfig")
    val vct = ctConfig("columnType").split("\\(")
    logger.debug(s"getRealColumnType-vct: ${vct.mkString("Array(", ", ", ")")}")
    val ct = vct.head
    val vv = vct.last.replace(")", "").split(",")
    val vcs = vv.head
    logger.debug(s"getRealColumnType-vcs: $vcs")
    val vdd = vv.last
    logger.debug(s"getRealColumnType-vdd: $vdd")
    val cs = ctConfig("columnSize")
    val dd = ctConfig.getOrElse("dd", "0")

    ct match {
      case "VARCHAR" | "CHAR" | "VARCHAR2" => {
        val rvcs = if (vcs.matches("\\d+")) vcs else "4000"

        if (cs.toInt == 0 || cs.toInt > rvcs.toInt) s"$ct($rvcs)"
        else s"$ct($cs)"
      }
      case "DECIMAL" | "DECIMALV2" | "DECIMALV3" | "NUMERIC" => {
        val rvcs = if (vcs.matches("\\d+")) vcs else "27"
        val rvdd = if (vdd.matches("\\d+")) vdd else "9"

        val (d1, d2) = {
          if (cs.toInt == 0 || cs.toInt > rvcs.toInt) {
            (rvcs.toInt, rvdd.toInt)
          } else {
            (cs.toInt, if (dd != null) dd.toInt else 0)
          }
        }

        if (d2 > 0) s"$ct($d1,$d2)"
        else s"$ct($d1)"
      }
      case _ => ct
    }
  }
}

case class EDBConfig(sourceDBConfig: Map[String, String], targetDBConfig: Map[String, String])
