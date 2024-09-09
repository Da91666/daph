package com.dasea.daph.node.spark3.dataframe.general.transformer.union

import com.dasea.daph.api.config.NodeConfig


/**
 * 配置按照位置匹配或者按照名称匹配 POSITION/NAME
 * 是否忽略缺失的列
 */
case class UnionConfig(
                        unionType:String  = UnionType.POSITION.toString,
                        allowMissingColumns: Boolean = false,
                        tableUnionConfig: Map[String, UnionConfig] =Map.empty
                      ) extends NodeConfig