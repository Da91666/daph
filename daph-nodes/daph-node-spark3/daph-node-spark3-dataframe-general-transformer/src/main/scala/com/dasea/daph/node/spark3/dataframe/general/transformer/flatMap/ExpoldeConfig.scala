package com.dasea.daph.node.spark3.dataframe.general.transformer.flatMap

case class ExpoldeConfig(
                          colName: String,
                          splitKey: String,
                          newName: String
                        )