package com.dasea.daph.node.spark3.dataframe.batch.connector.kp

import com.dasea.daph.api.config.NodeConfig

case class KPOutputConfig(
  format: String,
  cfg: Map[String, String],
  saveMode: String = "default",
  partitionColumnNames: Array[String] = Array.empty,
  sortColumnNames: Array[String] = Array.empty,
  bucketColumnNames: Array[String] = Array.empty,
  numBuckets: Int,
  keyType: String,
  valueType: String
) extends NodeConfig
