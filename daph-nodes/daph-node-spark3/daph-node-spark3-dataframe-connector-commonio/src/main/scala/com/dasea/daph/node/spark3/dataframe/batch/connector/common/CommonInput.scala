package com.dasea.daph.node.spark3.dataframe.batch.connector.common

import com.dasea.daph.node.spark3.dataframe.batch.connector.HiveDialect
import com.dasea.daph.spark3.api.node.dataframe.connector.input.DataFrameSingleInput
import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.jdbc.JdbcDialects

class CommonInput extends DataFrameSingleInput {
  override protected def in(): DataFrame = {
    val config = nodeConfig.asInstanceOf[CommonInputConfig]
    val format = config.format
    val cfg = config.cfg
    val paths = config.paths
    val isCatalogTable = config.isCatalogTable
    val tableName = config.tableName
    val sql = config.sql

    val res = if (isCatalogTable) {
      if (StringUtils.isEmpty(sql)) {
        spark.table(tableName)
      } else {
        spark.sql(sql)
      }
    } else {
      if (cfg.contains("url") && cfg("url").startsWith("jdbc:hive2")) {
        JdbcDialects.registerDialect(new HiveDialect)
      }
      spark.read
        .format(format)
        .options(cfg)
        .load(paths: _*)
    }

    res
  }

  override def getNodeConfigClass = classOf[CommonInputConfig]
}