package com.dasea.daph.spark3.api.node.dataframe.transformer

import com.dasea.daph.api.node.base.transformer.MultipleTransformer
import com.dasea.daph.spark3.api.node.dataframe.DataFrameNode
import org.apache.spark.sql.DataFrame

abstract class DataFrameMultipleTransformer
	extends MultipleTransformer[DataFrame, DataFrame]
		with DataFrameNode