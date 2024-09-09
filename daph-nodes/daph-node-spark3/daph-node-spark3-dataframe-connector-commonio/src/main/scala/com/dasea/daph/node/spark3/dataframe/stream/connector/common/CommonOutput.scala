package com.dasea.daph.node.spark3.dataframe.stream.connector.common

import com.dasea.daph.node.spark3.dataframe.batch.connector.common.outDF
import com.dasea.daph.spark3.api.node.dataframe.connector.output.DataFrameSingleOutput
import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.DataFrame

class CommonOutput extends DataFrameSingleOutput {
  override def out(df: DataFrame): Unit = {
    val config = nodeConfig.asInstanceOf[CommonOutputConfig]
    val format = config.format
    val cfg = config.cfg
    val outputMode = config.outputMode
    val triggerType = config.triggerType
    val triggerTime = config.triggerTime
    val partitionColumnNames = config.partitionColumnNames
    val queryName = config.queryName
    val method = config.method
    val name = config.name
    val batch = config.batch
    val batchConfig = config.batchConfig

    logger.info(s"CommonOutputConfig: $config")

    var res = df.writeStream
    if (!StringUtils.isEmpty(format)) res = res.format(format)
    if (!StringUtils.isEmpty(outputMode)) res = res.outputMode(outputMode)
    if (!StringUtils.isEmpty(triggerType)) res = res.trigger(getTrigger(triggerType, triggerTime))
    if (cfg.nonEmpty) res = res.options(cfg)
    if (partitionColumnNames.nonEmpty) res = res.partitionBy(partitionColumnNames: _*)
    if (!StringUtils.isEmpty(queryName)) res = res.queryName(queryName)
    if (batch) res = res.foreachBatch { (batchDF: DataFrame, batchId: Long) =>
      outDF(batchDF, batchConfig)
    }

    method match {
      case "start" =>
        if (StringUtils.isEmpty(name)) res.start().awaitTermination()
        else res.start(name).awaitTermination()
      case "toTable" => res.toTable(name).awaitTermination()
    }
  }

  override def getNodeConfigClass = classOf[CommonOutputConfig]
}
