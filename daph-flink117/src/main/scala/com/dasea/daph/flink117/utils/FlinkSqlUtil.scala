package com.dasea.daph.flink117.utils

import com.dasea.daph.api.config.option.GlobalConfigOptions.{DAPH_DT_MAPPING_PATH, DAPH_HOME}
import com.dasea.daph.flink117.constants.FlinkSqlConstants
import com.dasea.daph.utils.{CommonUtil, JdbcUtil, TableUtil}
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.scala.Logging

import java.sql.Connection
import java.util.regex.Pattern
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object FlinkSqlUtil extends Logging {
  def specialKeys(connector: String): Map[String, String] = connector match {
    case "mongodb" => Map(
      "tableKey" -> "products"
    )
    case "starrocks" => Map(
      "urlKey" -> "jdbc-url",
      "databaseKey" -> "database-name",
      "schemaKey" -> "schema-name",
      "tableKey" -> "table-name"
    )
    case "doris" => Map(
      "urlKey" -> "jdbc-url",
      "tableKey" -> "table.identifier"
    )
    case "kafka" | "upsert-kafka" => Map(
      "tableKey" -> "topic"
    )
    case _ => Map(
      "databaseKey" -> "database-name",
      "schemaKey" -> "schema-name",
      "tableKey" -> "table-name"
    )
  }

  def withPart(config: Map[String, String]): String = {
    val connector = config("connector")
    val tableKey = specialKeys(connector)("tableKey")
    val tn = {
      if (connector.equals("doris")) config("table.identifier")
      else config("daph.tableNames")
    }
    val sn = if (config.contains("daph.schemaName")) config("daph.schemaName") else null

    val rtn = if (sn == null || config.contains("schema-name")) tn else s"$sn.$tn"
    val c = config.filterNot(_._1.startsWith("daph.")) - "dtMappingFileRootPath"
    val rc = c + (tableKey -> rtn)

    val res = rc.map { case (k, v) => s"'$k'='$v'" }.reduce((a, b) => s"$a,\n$b")
    s"(\n$res\n)"
  }

  def createSqlString(tn: String, fp: String, wp: String, partitions: String = ""): String = {
    var newTableName = tn
    // 如果tablename没有带反引号，这些都加上，如果有反引号，反引号位置默认都是OK的
    if (!tn.contains("`")) {
      if (tn.contains(".")) {
        // 此时已经是三段式的表，需要为最后一部分加上单引号
        val lastDotIndex = tn.lastIndexOf(".")
        val prefix = tn.substring(0, lastDotIndex + 1)
        val tableName = tn.substring(lastDotIndex + 1)
        val quotedTableName = s"`$tableName`"
        newTableName = s"$prefix$quotedTableName"
      } else {
        // 非三段式的表，直接加反引号
        newTableName = s"`$tn`"
      }
    }
    logger.info(s"The new tableName is s$newTableName")

    if (partitions.isEmpty)
      s"""CREATE TABLE if not exists $newTableName
         |$fp
         |WITH $wp""".stripMargin
    else
      s"""CREATE TABLE if not exists $newTableName
         |$fp
         |PARTITIONED BY ($partitions)
         |WITH $wp""".stripMargin
  }

  def getNeedTableNames(allTableNames: Array[String], tableExpr: String): Array[String] = {
    if (tableExpr.equals("*")) {
      allTableNames
    }
    else if (tableExpr.contains(",")) {
      tableExpr.split(",")
    }
    else {
      val pattern = Pattern.compile(tableExpr)
      allTableNames.filter(tn => pattern.matcher(tn).find())
    }
  }

  def getCC(cc: Map[String, String]): Map[String, String] = {
    val buffer = mutable.Map[String, String]()
    buffer ++= cc

    if (!cc.contains("daph.dbType") && cc.contains("connector") && !cc("connector").equals("jdbc")) {
      val connector = cc("connector")
      if (connector.contains("-")) {
        val sp = connector.split("-")
        val v = if (sp.head.equals("postgres")) "postgresql" else sp.head
        buffer += "daph.dbType" -> v
      } else {
        buffer += "daph.dbType" -> connector
      }
    }
    if (!cc.contains("daph.databaseName")) {
      if (cc.contains("database-name")) buffer += "daph.databaseName" -> cc("database-name")
      if (cc.contains("database")) buffer += "daph.databaseName" -> cc("database")
    }
    if (!cc.contains("daph.schemaName")) {
      if (cc.contains("schema-name")) buffer += "daph.schemaName" -> cc("schema-name")
    }

    buffer.toMap
  }
}

object FlinkJdbcUtil extends Logging {
  private def getRealFlinkType(flinkType: String, dbType: String, ctConfig: Map[String, String]): String = {
    val vct = flinkType.split("\\(")
    val ct = vct.head
    val vv = vct.last.replace(")", "").split(",")
    val vcs = vv.head
    val vdd = vv.last

    val actualColumnSize = ctConfig("columnSize")
    val cs = ct match {
      case "DECIMAL" => {
        val rvcs = if (vcs.matches("\\d+")) vcs else "27"
        val rvdd = if (vdd.matches("\\d+")) vdd else "9"

        val size = ctConfig.getOrElse("columnSize", rvcs)
        val cs = if (size != null && size.matches("\\d+") && size.toInt > 0) size.toInt else rvcs
        val d = ctConfig.getOrElse("dd", rvdd)
        val dd = if (d != null && d.matches("\\d+")) d.toInt else rvdd
        s"$cs,$dd"
      }
      case "VARCHAR" => {
        val rvcs = if (vcs.matches("\\d+")) vcs else "4000"
        if (actualColumnSize.toInt == 0 || actualColumnSize.toInt > rvcs.toInt) rvcs
        else actualColumnSize
      }
      case "CHAR" => {
        val rvcs = if (vcs.matches("\\d+")) vcs else "255"
        if (actualColumnSize.toInt == 0 || actualColumnSize.toInt > rvcs.toInt) rvcs
        else actualColumnSize
      }
      case _ => ""
    }
    logger.debug(s"getRealFlinkType-cs: $cs")

    if (cs.isEmpty) ct
    else s"$ct($cs)"
  }

  def fieldPart(config: Map[String, String], connection: Connection): (Array[(String, String)], Array[String]) = {
    val dbType = config("daph.dbType")
    val dc = daphConfig(config)
    val fields = JdbcUtil.getSingleTableFields(dc, connection)
    logger.debug(s"FlinkJdbcUtil-fieldPart-fields:\n${fields.mkString("Array(", ", ", ")")}")

    if (fields.isEmpty) throw new Exception("No fields found!")
    val primaryKeys = JdbcUtil.getSingleTablePrimaryKeys(dc, connection)
    logger.debug(s"FlinkJdbcUtil-fieldPart-primaryKeys:\n${primaryKeys.mkString("Array(", ", ", ")")}")

    val dtMappingFileRootPath = config.getOrElse("dtMappingFileRootPath", DAPH_DT_MAPPING_PATH.default)

    val relations = CommonUtil.getRelations("flink-sql", dbType, "flink", dtMappingFileRootPath)
    val fs = fields.map { case (name, ctConfig) =>
      val ct = ctConfig("columnType").split("\\(").head
      logger.debug(s"FlinkJdbcUtil-fieldPart-ct: $ct")

      val option = relations.find(_.sourceTypes.contains(ct))
      val rct = option match {
        case Some(x) => x.targetTypes.head
        case None => throw new Exception(
          s"""
             |当前不存在【数据库类型为$dbType，字段类型为$ct => Flink字段类型】的映射关系。
             |请在【${DAPH_HOME.default}/conf/mapping/flink-sql-${dbType}_flink-mapping.json】文件中自行添加映射关系。
             |""".stripMargin
        )
      }

      s"`$name`" -> getRealFlinkType(rct, dbType, ctConfig)
    }
    logger.debug(s"FlinkJdbcUtil-fieldPart-fs: ${fs.mkString("Array(", ", ", ")")}")

    (fs, primaryKeys)
  }

  def fieldPartString(config: Map[String, String], connection: Connection): String = {
    val (fs, priKeys) = fieldPart(config, connection)
    val fp = fs.map { case (cn, ct) => s"$cn $ct" }.reduce((a, b) => s"$a,\n$b")
    val fPriKeys = config.getOrElse("daph.flink.primaryKeys", "")

    val res = if (priKeys.isEmpty && fPriKeys.isEmpty) {
      fp
    } else {
      val pp =
        if (fPriKeys.nonEmpty) fPriKeys
        else priKeys.reduce((a, b) => s"$a,$b")
      fp + s", PRIMARY KEY ($pp) NOT ENFORCED"
    }

    s"(\n$res\n)"
  }

  def daphConfig(config: Map[String, String]): Map[String, String] = {
    config ++ Map(
      "daph.url" -> config.getOrElse("jdbc-url", config.getOrElse("url", config("daph.url"))),
      "daph.username" -> config.getOrElse("daph.username", config("username")),
      "daph.password" -> config.getOrElse("daph.password", config("password")),
      "daph.tableName" -> config.getOrElse("daph.tableNames", null),
      "daph.tableNamePrefix" -> config.getOrElse("daph.tableNamePrefix", ""),
      "daph.tableNameSuffix" -> config.getOrElse("daph.tableNameSuffix", "")
    )
  }

  def cggToJCgg(dbType: String, catalogConfig: Map[String, String],
                databaseName: String, schemaName: String = null,
                enableCatalogCDC: Boolean = false): Map[String, String] = {
    val baseUrl = catalogConfig("base-url")
    val bu = baseUrl.replace("/", "").split(":")
    val hostname = bu(2)
    val port = bu(3)
    val username = catalogConfig("username")
    val password = catalogConfig("password")

    if (enableCatalogCDC) {
      dbType match {
        case "mysql" => Map(
          "daph.dbType" -> dbType,
          "daph.url" -> s"$baseUrl/$databaseName",
          "daph.databaseName" -> databaseName,
          "daph.schemaName" -> schemaName,
          "connector" -> (
            if (dbType.equals("postgresql")) "postgres-cdc"
            else s"$dbType-cdc"),
          "hostname" -> hostname,
          "port" -> port,
          "username" -> username,
          "password" -> password,
          "database-name" -> databaseName
        )
        case _ => Map(
          "daph.dbType" -> dbType,
          "daph.url" -> s"$baseUrl/$databaseName",
          "daph.databaseName" -> databaseName,
          "daph.schemaName" -> schemaName,
          "connector" -> (
            if (dbType.equals("postgresql")) "postgres-cdc"
            else s"$dbType-cdc"),
          "hostname" -> hostname,
          "port" -> port,
          "username" -> username,
          "password" -> password,
          "database-name" -> databaseName,
          "schema-name" -> schemaName
        )
      }
    }
    else {
      Map(
        "daph.dbType" -> dbType,
        "daph.url" -> s"$baseUrl/$databaseName",
        "daph.databaseName" -> databaseName,
        "daph.schemaName" -> schemaName,
        "connector" -> "jdbc",
        "url" -> s"$baseUrl/$databaseName",
        "username" -> username,
        "password" -> password
      )
    }
  }

  def getTableNames(config: Map[String, String], connection: Connection): Array[String] = {
    val dc = daphConfig(config)
    JdbcUtil.getTableNames(dc, connection)
  }

  def createBySchemaString(dbType: String, dtMappingFileRootPath: String,
                           tableNameToResolvedSchemaString: Map[String, String],
                           config: Map[String, String] = Map.empty,
                           enableFix: Boolean = true,
                           tn2Keys: Map[String, String] = Map.empty): String = {
    val databaseName = config.getOrElse("daph.databaseName", "")
    val relations = CommonUtil.getRelations("flink-sql", "flink", dbType, dtMappingFileRootPath)
    val prefix = if (enableFix) config.getOrElse("daph.tableNamePrefix", "") else ""
    val suffix = if (enableFix) config.getOrElse("daph.tableNameSuffix", "") else ""
    val tableNameCase = config.getOrElse("daph.tableNameCase", "")
    val columnNameCase = config.getOrElse("daph.columnNameCase", "")

    val tnrs = {
      if (tableNameCase.nonEmpty) {
        tableNameCase match {
          case "lower" => tableNameToResolvedSchemaString.map { case (k, v) => k.toLowerCase -> v }
          case "upper" => tableNameToResolvedSchemaString.map { case (k, v) => k.toUpperCase -> v }
        }
      } else tableNameToResolvedSchemaString
    }
    val createSqls = tnrs.map { case (tn, fp) =>
      val st = {
        val t =
          if (tn.contains("`")) tn.replace("`", "").split("\\.").last
          else tn.split("\\.").last
        TableUtil.getCaseName(tableNameCase, t)
      }
      val rtn = {
        val sn = config.getOrElse("daph.schemaName", null)
        if (sn == null) s"$databaseName.$prefix$st$suffix"
        else s"$sn.$prefix$st$suffix"
      }
      val b = fp.replace("\n", "")
      val len = b.length
      val base = b.substring(1, len - 1).trim

      def getFDS(ss: String): Array[(String, String)] = {
        val all = ss.split("`").tail
        val cns = new ArrayBuffer[String]
        val cts = new ArrayBuffer[String]
        for (i <- all.indices) {
          if (i % 2 == 0) cns += all(i)
          else cts += all(i)
        }
        val rcns = {
          if (columnNameCase.nonEmpty) {
            columnNameCase match {
              case "lower" => cns.map(_.toLowerCase)
              case "upper" => cns.map(_.toUpperCase)
            }
          } else cns
        }
        val zip = rcns.zip(cts)

        zip.map { case (cn, tct) =>
          val ct = tct.trim
          val len = ct.length
          val rct = if (ct.contains("STRING")) "VARCHAR(65533)" else ct.substring(0, len - 1)
          cn -> rct
        }
      }.toArray

      val (fds, pp) = {
        if (fp.contains("PRIMARY KEY")) {
          logger.debug(s"createBySchemaString-getFDS-base: $base")
          val b = base.split("PRIMARY KEY")
          val a = b.head.replace("`CONSTRAINT PRIMARY`", "").split("CONSTRAINT `").head.trim
          val fds = getFDS(a)
          val pp = {
            val tpp = b.last.replace("(", "").replace(")", "").replace("NOT ENFORCED", "").trim
            TableUtil.getCaseName(columnNameCase, tpp)
          }

          (fds, s"PRIMARY KEY($pp)")
        }
        else if (tn2Keys.nonEmpty) {
          val keys = tn2Keys(tn)
          if (StringUtils.isEmpty(keys)) (getFDS(s"$base,"), "")
          else (getFDS(s"$base,"), s"PRIMARY KEY(${tn2Keys(tn)})")
        }
        else {
          (getFDS(s"$base,"), "")
        }
      }
      logger.debug(s"FlinkJdbcUtil-createBySchemaString-(fds, pp):\n${fds.mkString("Array(", ", ", ")")}\n$pp")

      val cols = fds.map { case (cn, nt) =>
        logger.debug(s"FlinkJdbcUtil-createBySchemaString-nt: $nt")

        if (nt.contains("(")) {
          val base = nt.split("\\(")
          val ct = base.head
          val csp = base.last.split("\\)").head

          val option = relations.find(_.sourceTypes.contains(ct))
          val rct = option match {
            case Some(x) => x.targetTypes.head
            case None => throw new Exception(
              s"""
                 |当前不存在【Flink字段类型为$ct => ${dbType}数据库字段类型】的映射关系。
                 |请在【${DAPH_HOME.default}/conf/mapping/flink-sql-flink_$dbType-mapping.json文件】中自行添加映射关系。
                 |""".stripMargin
            )
          }

          val ctConfig = {
            if (csp.contains(",")) {
              val arr = csp.split(",")
              Map(
                "columnType" -> rct,
                "columnSize" -> arr.head,
                "dd" -> arr.last
              )
            } else {
              Map(
                "columnType" -> rct,
                "columnSize" -> csp
              )
            }
          }
          val rrct = JdbcUtil.getRealColumnType(dbType, ctConfig)
          cn -> rrct
        } else {
          val ct = nt.split(" ").head

          val option = relations.find(_.sourceTypes.contains(ct))
          val rct = option match {
            case Some(x) => x.targetTypes.head
            case None => throw new Exception(
              s"""
                 |当前不存在【Flink字段类型为$ct => ${dbType}数据库字段类型】的映射关系。
                 |请在【${DAPH_HOME.default}/conf/mapping/flink-sql-flink_$dbType-mapping.json】文件中自行添加映射关系。
                 |""".stripMargin
            )
          }
          cn -> rct
        }
      }

      val resConfig = if (pp.isEmpty) config else config + ("primaryKeyPart" -> pp)

      JdbcUtil.createSqlString(rtn, cols, resConfig)
    }
    logger.debug(s"FlinkJdbcUtil-createBySchemaString-createSqls:\n$createSqls")

    createSqls.reduce((a, b) => s"$a;$b")
  }

  def createByDBConfig(dtMappingFileRootPath: String,
                       sourceDBConfig: Map[String, String],
                       targetDBConfig: Map[String, String],
                       tableNames: Array[String] = Array.empty): String = {
    val sc = daphConfig(sourceDBConfig)
    val tc = daphConfig(targetDBConfig)
    JdbcUtil.using(sc) { conn =>
      JdbcUtil.getCreateSqls(
        if (tableNames.nonEmpty) tableNames else JdbcUtil.getTableNames(sourceDBConfig, conn),
        dtMappingFileRootPath, sc, tc, conn
      )
    }.reduce((a, b) => s"$a;$b")
  }

  def createJdbcTable(config: Map[String, String], sqls: String): Boolean = {
    var newSqls = sqls
    var daphConfig = FlinkJdbcUtil.daphConfig(config)
    JdbcUtil.using(daphConfig) { connection =>
      val dbType = daphConfig(FlinkSqlConstants.DAPH_DB_TYPE_KEY)
      if (dbType.equals("oracle") || dbType.equals("sqlserver")) {
        val tableExistsSql = buildTableExistSql(dbType)
        val schemName = daphConfig(FlinkSqlConstants.DAPH_SCHEMA_NAME_KEY)
        val tableName = daphConfig(FlinkSqlConstants.DAPH_TABLE_NAME_KEY)
        // 判断表是否存在
        val result = JdbcUtil.checkTableExists(schemName, tableName, tableExistsSql, connection)

        if (result) {
          logger.info(s"The table ${tableName} has existed, the schemaName is ${schemName}")
          return true
        } else {
          logger.debug(s"The table $tableName does not exist, it will create next")
          newSqls = sqls.replaceAll("(?i) IF NOT EXISTS", "")
        }
      }

      daphConfig =  daphConfig + ("daph.sqls" -> newSqls)
      JdbcUtil.executeSQLs(daphConfig, connection)
    }
  }

  def createCreateSql(config: Map[String, String], connection: Connection): String = {
    val partitions = config.getOrElse("daph.flink.partitions", "")
    val tableName = config.getOrElse("daph.flink.tableName", config("daph.tableNames"))
    val sn = if (config.contains("daph.schemaName")) config("daph.schemaName") else null
    val rtn = if (sn == null) tableName else s"`$sn.$tableName`"
    val fp = fieldPartString(config, connection)
    val wp = FlinkSqlUtil.withPart(config)

    FlinkSqlUtil.createSqlString(rtn, fp, wp, partitions)
  }

  def createInsertSql(targetTable: String, sourceTable: String, mode: String,
                      colPart: String = "*", dbType: String = ""): String = {
    // 处理flink 表名带有关键字的场景
    var newTargetTable = targetTable
    var newSourceTable = sourceTable

    // 如果tablename没有带反引号，这些都加上，如果有反引号，反引号位置默认都是OK的
    if (!targetTable.contains("`")) {
      if (targetTable.contains(".")) {
        // 此时已经是三段式的表，需要为最后一部分加上单引号
        val lastDotIndex = targetTable.lastIndexOf(".")
        val prefix = targetTable.substring(0, lastDotIndex + 1)
        val tableName = targetTable.substring(lastDotIndex + 1)
        val quotedTableName = s"`$tableName`"
        newTargetTable = s"$prefix$quotedTableName"
      } else {
        // 非三段式的表，直接加反引号
        newTargetTable = s"`$targetTable`"
      }
    }

    // 如果tablename没有带反引号，这些都加上，如果有反引号，反引号位置默认都是OK的
    if (!sourceTable.contains("`")) {
      if (sourceTable.contains(".")) {
        // 此时已经是三段式的表，需要为最后一部分加上单引号
        val lastDotIndex = sourceTable.lastIndexOf(".")
        val prefix = sourceTable.substring(0, lastDotIndex + 1)
        val tableName = sourceTable.substring(lastDotIndex + 1)
        val quotedTableName = s"`$tableName`"
        newSourceTable = s"$prefix$quotedTableName"
      } else {
        // 非三段式的表，直接加反引号
        newSourceTable = s"`$sourceTable`"
      }
    }

    dbType match {
      case "postgresql" => mode match {
        case "append" => s"INSERT INTO $newTargetTable SELECT $colPart FROM $newSourceTable"
        case "overwrite" => s"INSERT OVERWRITE $newTargetTable SELECT $colPart FROM $newSourceTable"
      }
      case "oracle" => mode match {
        case "append" => s"INSERT INTO $newTargetTable SELECT $colPart FROM $newSourceTable"
        case "overwrite" => s"INSERT OVERWRITE $newTargetTable SELECT $colPart FROM $newSourceTable"
      }
      case _ => mode match {
        case "upsert" => s"INSERT INTO $newTargetTable SELECT $colPart FROM $newSourceTable"
        case "append" => s"INSERT INTO $newTargetTable SELECT $colPart FROM $newSourceTable"
        case "overwrite" => s"INSERT OVERWRITE $newTargetTable SELECT $colPart FROM $newSourceTable"
      }
    }

  }

  def buildTableExistSql(dbType: String): String = {
    dbType match {
      case "oracle" => FlinkSqlConstants.ORACLE_TABLE_EXISTS
      case "sqlserver" => FlinkSqlConstants.SQLSERVER_TABLE_EXISTS
      case _ => throw new IllegalArgumentException("The dbtype is " + dbType +", it is error")
    }
  }
}