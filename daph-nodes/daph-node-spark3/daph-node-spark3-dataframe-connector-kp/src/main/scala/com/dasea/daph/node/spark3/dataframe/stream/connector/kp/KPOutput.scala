package com.dasea.daph.node.spark3.dataframe.stream.connector.kp

import com.dasea.daph.spark3.api.node.dataframe.connector.output.DataFrameSingleOutput
import org.apache.spark.sql.DataFrame

class KPOutput extends DataFrameSingleOutput {
  override def out(df: DataFrame): Unit = {
    val config = nodeConfig.asInstanceOf[KPOutputConfig]
    val format = config.format
    val cfg = config.cfg
    val outputMode = config.outputMode
    val triggerType = config.triggerType
    val triggerTime = config.triggerTime
    val partitionColumnNames = config.partitionColumnNames
    val queryName = config.queryName
    val keyType = config.keyType
    val valueType = config.valueType

    var res = df.selectExpr(s"CAST(key AS $keyType)", s"CAST(value AS $valueType)")
      .writeStream
      .format(format)
      .outputMode(outputMode)
      .trigger(getTrigger(triggerType, triggerTime))
      .options(cfg)
    if (partitionColumnNames.nonEmpty) res = res.partitionBy(partitionColumnNames: _*)
    if (queryName.nonEmpty) res = res.queryName(queryName)

    res.start()
      .awaitTermination()
  }

  override def getNodeConfigClass = classOf[KPOutputConfig]
}
