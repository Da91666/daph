package com.dasea.daph.node.spark3.dataframe.batch.connector.kp

import com.dasea.daph.spark3.api.node.dataframe.connector.output.DataFrameSingleOutput
import org.apache.spark.sql.DataFrame

class KPOutput extends DataFrameSingleOutput {
  override def out(df: DataFrame): Unit = {
    val config = nodeConfig.asInstanceOf[KPOutputConfig]
    val format = config.format
    val cfg = config.cfg
    val saveMode = config.saveMode
    val partitionColumnNames = config.partitionColumnNames
    val sortColumnNames = config.sortColumnNames
    val bucketColumnNames = config.bucketColumnNames
    val numBuckets = config.numBuckets
    val keyType = config.keyType
    val valueType = config.valueType

    var res = df.selectExpr(s"CAST(key AS $keyType)", s"CAST(value AS $valueType)")
      .write.format(format).options(cfg).mode(saveMode)
    if (partitionColumnNames.nonEmpty) res = res.partitionBy(partitionColumnNames: _*)
    if (sortColumnNames.nonEmpty) res = res.sortBy(sortColumnNames.head, sortColumnNames.tail: _*)
    if (bucketColumnNames.nonEmpty) res = res.bucketBy(numBuckets, bucketColumnNames.head, bucketColumnNames: _*)
    res.save()
  }

  override def getNodeConfigClass = classOf[KPOutputConfig]
}
