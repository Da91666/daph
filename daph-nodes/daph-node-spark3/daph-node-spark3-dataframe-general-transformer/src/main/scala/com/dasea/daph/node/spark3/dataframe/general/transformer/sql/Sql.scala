package com.dasea.daph.node.spark3.dataframe.general.transformer.sql

import com.dasea.daph.spark3.api.node.dataframe.transformer.DataFrameSingleTransformer
import com.dasea.daph.utils.SQLUtil
import org.apache.spark.sql.DataFrame

class Sql extends DataFrameSingleTransformer {
  override def transform(df: DataFrame): DataFrame = {
    val config = nodeConfig.asInstanceOf[SqlConfig]
    val sql = config.sql

    val tn = SQLUtil.getTableNames(sql).head
    df.createOrReplaceTempView(tn)

    val res = spark.sql(sql)
    res
  }

  override def getNodeConfigClass = classOf[SqlConfig]
}
