package com.dasea.daph.node.spark3.dataframe.stream.connector.common

import com.dasea.daph.api.config.NodeConfig

case class CommonInputConfig(
  override val format: String,
  override val cfg: Map[String, String],
  path: String
) extends CommonConfig(format, cfg)

case class CommonOutputConfig(
  override val format: String,
  override val cfg: Map[String, String] = Map.empty,
  outputMode: String,
  triggerType: String,
  triggerTime: String,
  partitionColumnNames: Array[String] = Array.empty[String],
  queryName: String,
  method: String = "start",
  name: String,
  batch: Boolean = false,
  batchConfig: com.dasea.daph.node.spark3.dataframe.batch.connector.common.CommonOutputConfig
) extends CommonConfig(format, cfg)

class CommonConfig(
  val format: String,
  val cfg: Map[String, String],
) extends NodeConfig