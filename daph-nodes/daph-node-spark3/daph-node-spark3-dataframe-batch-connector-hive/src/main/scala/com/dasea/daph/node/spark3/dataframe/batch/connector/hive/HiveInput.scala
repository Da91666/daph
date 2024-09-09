package com.dasea.daph.node.spark3.dataframe.batch.connector.hive

import com.dasea.daph.spark3.api.node.dataframe.connector.input.DataFrameSingleInput
import org.apache.spark.sql.DataFrame

class HiveInput extends DataFrameSingleInput {
  override protected def in(): DataFrame = {
    val config = nodeConfig.asInstanceOf[HiveInputConfig]
    val sql = config.sql

    spark.sql(sql)
  }

  override def getNodeConfigClass = classOf[HiveInputConfig]
}