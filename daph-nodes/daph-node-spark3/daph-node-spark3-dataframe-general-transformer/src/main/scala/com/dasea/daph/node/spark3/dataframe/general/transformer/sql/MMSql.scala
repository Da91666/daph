package com.dasea.daph.node.spark3.dataframe.general.transformer.sql

import com.dasea.daph.spark3.api.node.dataframe.transformer.DataFrameMMTransformer
import org.apache.spark.sql.DataFrame

class MMSql extends DataFrameMMTransformer {
  override def transform(lineToDF: Map[String, DataFrame]): Map[String, DataFrame] = {
    val config = nodeConfig.asInstanceOf[MMSqlConfig]
    val lineIdToTableName = config.inLineIdToTableName
    val dataIdToSql = config.dataIdToSql
    val dataIdToOutLineId = config.dataIdToOutLineId

    lineIdToTableName.foreach { case (line, tn) =>
      lineToDF(line).createOrReplaceTempView(tn)
    }

    val res = dataIdToSql.map { case (dataId, sql) =>
      val df = spark.sql(sql)
      dataIdToOutLineId(dataId) -> df
    }

    res
  }

  override def getNodeConfigClass = classOf[MMSqlConfig]
}
