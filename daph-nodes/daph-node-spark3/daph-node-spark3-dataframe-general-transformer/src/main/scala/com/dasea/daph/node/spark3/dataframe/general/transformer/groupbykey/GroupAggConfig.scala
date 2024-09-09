package com.dasea.daph.node.spark3.dataframe.general.transformer.groupbykey

import com.dasea.daph.api.config.NodeConfig

case class GroupAggConfig(
                           exprs: Map[String, String] = Map.empty,
                           keyColumns: Array[String] = Array.empty,
                           exprsName:Array[String] = Array.empty
                         ) extends NodeConfig {

}
