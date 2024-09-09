package com.dasea.daph.node.spark3.dataframe.batch.connector.es

import com.dasea.daph.api.config.NodeConfig

case class ESOutputConfig(
  resource: String,
  cfg: Map[String, String]
) extends NodeConfig