package com.dasea.daph.node.spark3.dataframe.general.transformer.flatMap

import com.dasea.daph.api.config.NodeConfig

case class FlatMapConfig(
  resultColumns:Array[String]=Array.empty,
  expoldeConfig: Array[ExpoldeConfig]
) extends NodeConfig