package com.dasea.daph.spark3.api.node.dataframe.transformer

import com.dasea.daph.api.node.base.transformer.MMTransformer
import com.dasea.daph.spark3.api.node.dataframe.DataFrameNode
import org.apache.spark.sql.DataFrame

abstract class DataFrameMMTransformer
  extends MMTransformer[DataFrame, DataFrame]
    with DataFrameNode