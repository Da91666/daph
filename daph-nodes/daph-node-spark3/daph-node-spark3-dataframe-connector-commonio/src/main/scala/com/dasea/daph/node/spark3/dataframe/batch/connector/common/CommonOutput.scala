package com.dasea.daph.node.spark3.dataframe.batch.connector.common

import com.dasea.daph.spark3.api.node.dataframe.connector.output.DataFrameSingleOutput
import org.apache.spark.sql.DataFrame

class CommonOutput extends DataFrameSingleOutput {
  override def out(df: DataFrame): Unit = {
    val config = nodeConfig.asInstanceOf[CommonOutputConfig]

    outDF(df, config)
  }


  override def getNodeConfigClass = classOf[CommonOutputConfig]
}
