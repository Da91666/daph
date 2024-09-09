package com.dasea.daph.node.spark3.dataframe.batch.connector.es

import com.dasea.daph.spark3.api.node.dataframe.connector.output.DataFrameSingleOutput
import org.apache.spark.sql.DataFrame
import org.elasticsearch.spark.sql.sparkDatasetFunctions

class ESOutput extends DataFrameSingleOutput {
  override def out(df: DataFrame): Unit = {
    val config = nodeConfig.asInstanceOf[ESOutputConfig]
    val resource = config.resource
    val cfg = config.cfg

    df.saveToEs(resource, cfg)
  }

  override def getNodeConfigClass = classOf[ESOutputConfig]
}
