package com.dasea.daph.node.spark3.dataframe.general.transformer.sql

import com.dasea.daph.api.config.NodeConfig

case class SqlConfig(
  sql: String
) extends NodeConfig

case class MSqlConfig(
  lineIdToTableName: Map[String, String],
  sql: String
) extends NodeConfig

case class MMSqlConfig(
  inLineIdToTableName: Map[String, String],
  dataIdToSql: Map[String, String],
  dataIdToOutLineId: Map[String, String]
) extends NodeConfig
