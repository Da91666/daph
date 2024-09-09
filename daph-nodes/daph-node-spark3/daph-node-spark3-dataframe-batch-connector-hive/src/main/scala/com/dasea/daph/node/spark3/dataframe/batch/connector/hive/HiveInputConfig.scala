package com.dasea.daph.node.spark3.dataframe.batch.connector.hive

import com.dasea.daph.api.config.NodeConfig

case class HiveInputConfig(
  sql: String
) extends NodeConfig
