package com.dasea.daph.node.spark3.dataframe.batch.connector.common

import com.dasea.daph.api.config.NodeConfig

case class CommonInputConfig(
  override val format: String,
  override val cfg: Map[String, String],
  paths: Array[String] = Array.empty,
  isCatalogTable: Boolean = false,
  tableName: String,
  sql: String
) extends CommonConfig(format, cfg)

case class CommonOutputConfig(
  override val format: String,
  override val cfg: Map[String, String] = Map.empty,
  saveMode: String = "default",
  partitionColumnNames: Array[String] = Array.empty,
  sortColumnNames: Array[String] = Array.empty,
  bucketColumnNames: Array[String] = Array.empty,
  numBuckets: Int,
  method: String = "save",
  name: String,
  v2: Boolean = false,
  v2Config: V2Config
) extends CommonConfig(format, cfg)

case class V2Config(
  tableName: String,
  method: String = "append",
  options: Map[String, String] = Map.empty,
  provider: String,
  partitionColumns: Array[String] = Array.empty,
  properties: Map[String, String] = Map.empty,
  column: String
)

class CommonConfig(
  val format: String,
  val cfg: Map[String, String]
) extends NodeConfig