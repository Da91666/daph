package com.dasea.daph.node.flink117.sql.general

import com.dasea.daph.flink117.api.node.datastream.DataStreamNode
import com.dasea.daph.flink117.constants.FlinkSqlConstants._
import com.dasea.daph.flink117.utils.FlinkUtil.setSqlDialect
import org.apache.commons.lang3.StringUtils

trait GeneralNode extends DataStreamNode {
  def su(config: GeneralConfig): Unit = {
    val catalogName = config.catalogName
    val catalogConfig = config.catalogConfig
    val databaseName = config.databaseName
    val sqlDialect = config.sqlDialect

    setDialect(sqlDialect)
    useCatalog(catalogName, catalogConfig)
    if (!StringUtils.isEmpty(databaseName)) useDatabase(databaseName)
  }

  def suDefault(): Unit = {
    setDialect(DIALECT_DEFAULT)
    tableEnv.useCatalog(NAME_DEFAULT_CATALOG)
    useDatabase(NAME_DEFAULT_DATABASE)
  }

  def createTemporaryView(tableName: String, sqlStatement: String): Unit = {
    setDialect(DIALECT_DEFAULT)
    tableEnv.useCatalog(NAME_DEFAULT_CATALOG)
    tableEnv.useDatabase(NAME_DEFAULT_DATABASE)
    val table = tableEnv.sqlQuery(sqlStatement)
    tableEnv.createTemporaryView(tableName, table)
  }

  def createTable(createSql: String): Unit = {
    tableEnv.executeSql(createSql)
  }

  def setDialect(sqlDialect: String): Unit = setSqlDialect(tableEnv, sqlDialect)

  private def useCatalog(catalogName: String, catalogConfig: Map[String, String] = Map.empty): Unit = {
    createCatalog(catalogName, catalogConfig)

    tableEnv.useCatalog(catalogName)
  }

  private def useDatabase(databaseName: String): Unit = {
    tableEnv.useDatabase(databaseName)
  }
}