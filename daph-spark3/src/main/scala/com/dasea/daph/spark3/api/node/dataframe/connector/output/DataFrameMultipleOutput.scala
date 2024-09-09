package com.dasea.daph.spark3.api.node.dataframe.connector.output

import com.dasea.daph.api.node.base.connector.output.MultipleOutput
import com.dasea.daph.spark3.api.node.dataframe.DataFrameNode
import org.apache.spark.sql.DataFrame

abstract class DataFrameMultipleOutput
  extends MultipleOutput[DataFrame]
    with DataFrameNode
