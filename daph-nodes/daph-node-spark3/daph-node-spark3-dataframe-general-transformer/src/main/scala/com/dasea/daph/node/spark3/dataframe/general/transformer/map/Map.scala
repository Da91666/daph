package com.dasea.daph.node.spark3.dataframe.general.transformer.map

import com.dasea.daph.spark3.api.node.dataframe.transformer.DataFrameSingleTransformer
import org.apache.spark.sql.DataFrame

class Map extends DataFrameSingleTransformer {
  override def transform(df: DataFrame): DataFrame = {
    val config = nodeConfig.asInstanceOf[MapConfig]
    val expressions = config.expressions

    df.selectExpr(expressions: _*)
  }

  override def getNodeConfigClass = classOf[MapConfig]
}
