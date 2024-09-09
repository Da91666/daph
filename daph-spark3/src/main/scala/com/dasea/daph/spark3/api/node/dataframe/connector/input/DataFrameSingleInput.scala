package com.dasea.daph.spark3.api.node.dataframe.connector.input

import com.dasea.daph.api.node.base.connector.input.SingleInput
import com.dasea.daph.spark3.api.node.dataframe.DataFrameNode
import org.apache.spark.sql.DataFrame

abstract class DataFrameSingleInput
  extends SingleInput[DataFrame]
    with DataFrameNode {
  override protected def after(df: DataFrame): DataFrame = {
    roundDF("daph.spark.after.node.dataframe.", super.after(df))
  }
}
