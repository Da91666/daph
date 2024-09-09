package com.dasea.daph.node.flink117.sql.general

import com.dasea.daph.api.config.NodeConfig

class GeneralConfig(
  val catalogName: String = "default_catalog",
  val catalogConfig: Map[String, String] = Map.empty,
  val databaseName: String = "default_database",
  val sqlDialect: String = "default"
) extends NodeConfig

class GaMultipleInputConfig(
  catalogName: String = "default_catalog",
  catalogConfig: Map[String, String] = Map.empty,
  databaseName: String = "default_database",
  sqlDialect: String = "default",
  val enableCatalogEDB: Boolean = false,
  val enableCatalogCDC: Boolean = false,
  val catalogExtraConfig: Map[String, String] = Map.empty,
  val createSqls: Array[String] = Array.empty,
  val createConfigs: Array[Map[String, String]] = Array.empty,
  val queryConfigs: Array[QueryConfig] = Array.empty
) extends GeneralConfig(catalogName, catalogConfig, databaseName, sqlDialect)

class GaMMSQLConfig(
  val queryConfigs: Array[QueryConfig]
) extends NodeConfig

case class QueryConfig(line: String, results: Array[ResultTableToSql])

case class ResultTableToSql(resultTable: String, sql: String)

class GaMultipleOutputConfig(
  catalogName: String = "default_catalog",
  catalogConfig: Map[String, String] = Map.empty,
  databaseName: String,
  sqlDialect: String = "default",
  val byMultiJobs: Boolean = false,
  val tableStructChangeGet: Boolean = false,
  val autoCreateTable: Boolean = true,
  val autoSingleInsertSql: Boolean = true,
  val enableCatalogEDB: Boolean = false,
  val catalogExtraConfig: Map[String, String] = Map.empty,
  val createSqls: Array[String] = Array.empty,
  val createConfigs: Array[Map[String, String]] = Array.empty,
  val insertConfigs: Array[InsertConfig] = Array.empty
) extends GeneralConfig(catalogName, catalogConfig, databaseName, sqlDialect)

case class InsertConfig(sql: String, primaryKeys: String = "", order: Int = -1)