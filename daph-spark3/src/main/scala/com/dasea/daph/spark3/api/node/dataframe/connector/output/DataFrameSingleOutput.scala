package com.dasea.daph.spark3.api.node.dataframe.connector.output

import com.dasea.daph.api.node.base.connector.output.SingleOutput
import com.dasea.daph.spark3.api.node.dataframe.DataFrameNode
import org.apache.spark.sql.DataFrame

abstract class DataFrameSingleOutput
	extends SingleOutput[DataFrame]
		with DataFrameNode {
	override protected def before(df: DataFrame): DataFrame = {
		roundDF("daph.spark.before.node.dataframe.", super.before(df))
	}
}
