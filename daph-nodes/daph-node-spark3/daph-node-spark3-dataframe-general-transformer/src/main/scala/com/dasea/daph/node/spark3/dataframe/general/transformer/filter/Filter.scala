package com.dasea.daph.node.spark3.dataframe.general.transformer.filter

import com.dasea.daph.spark3.api.node.dataframe.transformer.DataFrameSingleTransformer
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{expr, not}

class Filter extends DataFrameSingleTransformer {
  override def transform(df: DataFrame): DataFrame = {
    val config = nodeConfig.asInstanceOf[FilterConfig]
    val conditions = config.conditions

    val condition = conditions.map { c =>
      val expression = expr(c.expression)
      val isNot = c.isNot
      if (isNot) not(expression) else expression
    }.reduce(_ and _)

    val res = df.filter(condition)
    res
  }

  override def getNodeConfigClass = classOf[FilterConfig]
}
