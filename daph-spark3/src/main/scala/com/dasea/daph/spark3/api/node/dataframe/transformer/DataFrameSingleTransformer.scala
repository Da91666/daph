package com.dasea.daph.spark3.api.node.dataframe.transformer

import com.dasea.daph.api.node.base.transformer.SingleTransformer
import com.dasea.daph.spark3.api.node.dataframe.DataFrameNode
import org.apache.spark.sql.DataFrame

abstract class DataFrameSingleTransformer extends SingleTransformer[DataFrame, DataFrame]
    with DataFrameNode {
  override protected def before(df: DataFrame): DataFrame = {
    roundDF("daph.spark.before.node.dataframe.", super.before(df))
  }

  override protected def after(df: DataFrame): DataFrame = {
    roundDF("daph.spark.after.node.dataframe.", super.after(df))
  }
}
