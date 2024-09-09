package com.dasea.daph.node.spark3.dataframe.general.transformer.groupbykey

import com.dasea.daph.spark3.api.node.dataframe.transformer.DataFrameSingleTransformer
import org.apache.spark.sql.DataFrame

class GroupByKey extends DataFrameSingleTransformer {
  override protected def transform(ds: DataFrame): DataFrame = {
    val config = nodeConfig.asInstanceOf[GroupAggConfig]
    ds.groupBy(config.keyColumns.map(filed => {
      ds.col(filed)
    }).toList:_*).agg(config.exprs).selectExpr(config.exprsName.toList:_*)
  }

  override def getNodeConfigClass: Class[GroupAggConfig] = classOf[GroupAggConfig]

}
