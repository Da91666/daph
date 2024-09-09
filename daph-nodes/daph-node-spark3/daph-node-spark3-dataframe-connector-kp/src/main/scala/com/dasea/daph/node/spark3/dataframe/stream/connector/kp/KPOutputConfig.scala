package com.dasea.daph.node.spark3.dataframe.stream.connector.kp

import com.dasea.daph.api.config.NodeConfig

case class KPOutputConfig(
  format: String,
  cfg: Map[String, String],
  outputMode: String = "append",
  triggerType: String = "ProcessingTime",
  triggerTime: String = "5 seconds",
  partitionColumnNames: Array[String] = Array.empty[String],
  queryName: String,
  keyType: String,
  valueType: String
) extends NodeConfig