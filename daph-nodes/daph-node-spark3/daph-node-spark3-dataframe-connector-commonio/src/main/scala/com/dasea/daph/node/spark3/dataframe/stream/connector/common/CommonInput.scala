package com.dasea.daph.node.spark3.dataframe.stream.connector.common

import com.dasea.daph.spark3.api.node.dataframe.connector.input.DataFrameSingleInput
import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.DataFrame

class CommonInput extends DataFrameSingleInput {
  override protected def in(): DataFrame = {
    val config = nodeConfig.asInstanceOf[CommonInputConfig]
    val format = config.format
    val cfg = config.cfg
    val path = config.path

    val base = spark.readStream.format(format).options(cfg)
    val res = if (StringUtils.isEmpty(path)) base.load() else base.load(path)
    res
  }

  override def getNodeConfigClass = classOf[CommonInputConfig]
}