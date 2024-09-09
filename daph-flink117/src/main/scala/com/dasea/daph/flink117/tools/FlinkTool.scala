package com.dasea.daph.flink117.tools

import com.dasea.daph.flink117.computer.FlinkComputerConfig
import com.dasea.daph.flink117.utils.FlinkUtil
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.table.api.TableEnvironment

object FlinkTool {
  def createStreamExecutionEnvironment(config: FlinkComputerConfig): StreamExecutionEnvironment = {
    FlinkUtil.createStreamExecutionEnvironment(config.envConfig)
  }

  def createTableEnvironment(config: FlinkComputerConfig): TableEnvironment = {
    FlinkUtil.createTableEnvironment(config.envConfig)
  }

}
