package com.dasea.daph.flink117.utils

import com.dasea.daph.flink117.constants.FlinkSqlConstants.{DIALECT_DEFAULT, DIALECT_HIVE}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.table.api.{EnvironmentSettings, SqlDialect, TableEnvironment}
import org.apache.logging.log4j.scala.Logging

import scala.collection.JavaConverters.mapAsJavaMapConverter

object FlinkUtil extends Logging {
  def createStreamExecutionEnvironment(flinkEnvConfig: Map[String, String]): StreamExecutionEnvironment = {
    if (flinkEnvConfig != null && flinkEnvConfig.nonEmpty) {
      val realFlinkConfig = Configuration.fromMap(flinkEnvConfig.asJava)
      StreamExecutionEnvironment.getExecutionEnvironment(realFlinkConfig)
    } else {
      StreamExecutionEnvironment.getExecutionEnvironment
    }
  }

  def createTableEnvironment(flinkEnvConfig: Map[String, String]): TableEnvironment = {
    if (flinkEnvConfig != null && flinkEnvConfig.nonEmpty) {
      val realFlinkConfig = Configuration.fromMap(flinkEnvConfig.asJava)
      TableEnvironment.create(realFlinkConfig)
    } else {
      val settings = EnvironmentSettings.newInstance()
        .inBatchMode()
        .build()
      TableEnvironment.create(settings)
    }
  }

  def setSqlDialect(tableEnv: TableEnvironment, sqlDialect: String): Unit = {
    sqlDialect match {
      case DIALECT_HIVE =>
        tableEnv.getConfig.setSqlDialect(SqlDialect.HIVE)
      case DIALECT_DEFAULT =>
        tableEnv.getConfig.setSqlDialect(SqlDialect.DEFAULT)
    }
  }
}
