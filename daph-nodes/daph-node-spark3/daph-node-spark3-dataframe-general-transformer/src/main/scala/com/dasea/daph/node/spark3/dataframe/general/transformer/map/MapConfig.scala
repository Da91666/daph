package com.dasea.daph.node.spark3.dataframe.general.transformer.map

import com.dasea.daph.api.config.NodeConfig

case class MapConfig(
  expressions: Array[String]
) extends NodeConfig