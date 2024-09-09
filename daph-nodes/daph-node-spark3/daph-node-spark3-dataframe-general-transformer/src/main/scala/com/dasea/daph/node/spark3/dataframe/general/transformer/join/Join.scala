package com.dasea.daph.node.spark3.dataframe.general.transformer.join

import com.dasea.daph.node.spark3.dataframe.general.transformer.join.JoinConfig.LaneSelector
import com.dasea.daph.spark3.api.node.dataframe.transformer.DataFrameMultipleTransformer
import org.apache.spark.sql.DataFrame

class Join extends DataFrameMultipleTransformer {
	override def transform(data: Map[String, DataFrame]): DataFrame = {
		val conf = nodeConfig.asInstanceOf[JoinConfig]
		val left = data(conf.getLeftLine).as("left")
		val right = data(conf.getRightLine).as("right")
		val joinCol = conf.getJoinColumns.iterator.map(x => {
			left.col(x.getLeft).eqNullSafe(right.col(x.getRight))
		}).reduce((a, b) => a and b)
		val selected = conf.getOutputColumns.iterator.map(x => {
			x.getLine match {
				case LaneSelector.left => left(x.getIn).as(x.getName)
				case LaneSelector.right => right(x.getIn).as(x.getName)
			}
		}).toList

		val joined = left.join(right, joinCol, conf.getJoinType)
		val res = joined.select(selected: _*)
		res
	}

	override def getNodeConfigClass = classOf[JoinConfig]
}


