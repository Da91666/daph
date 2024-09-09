package com.dasea.daph.node.spark3.dataframe.general.transformer.filter

import com.dasea.daph.api.config.NodeConfig

case class FilterConfig(
  conditions: Array[Condition]
) extends NodeConfig

case class Condition(expression: String, isNot: Boolean = false)

