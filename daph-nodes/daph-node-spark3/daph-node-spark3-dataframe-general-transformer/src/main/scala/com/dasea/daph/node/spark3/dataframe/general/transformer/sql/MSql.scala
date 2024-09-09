package com.dasea.daph.node.spark3.dataframe.general.transformer.sql

import com.dasea.daph.spark3.api.node.dataframe.transformer.DataFrameMultipleTransformer
import org.apache.spark.sql.DataFrame

class MSql extends DataFrameMultipleTransformer {
  override def transform(lineToDF: Map[String, DataFrame]): DataFrame = {
    val config = nodeConfig.asInstanceOf[MSqlConfig]
    val sql = config.sql
    val lineIdToTableName = config.lineIdToTableName

    lineIdToTableName.foreach { case (line, tn) =>
      lineToDF(line).createOrReplaceTempView(tn)
    }

    val res = spark.sql(sql)
    res
  }

  override def getNodeConfigClass = classOf[MSqlConfig]
}
