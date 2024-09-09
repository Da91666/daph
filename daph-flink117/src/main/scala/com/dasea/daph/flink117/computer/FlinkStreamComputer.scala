package com.dasea.daph.flink117.computer

import com.dasea.daph.flink117.tools.FlinkTool.createStreamExecutionEnvironment
import com.dasea.daph.utils.JsonUtil
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment

class FlinkStreamComputer(config: FlinkComputerConfig) extends FlinkComputer(config) {
  override val entrypoint: StreamExecutionEnvironment = createStreamExecutionEnvironment(config)
  lazy val tableEnv: StreamTableEnvironment = StreamTableEnvironment.create(entrypoint)

  private val methodConfig = computerConfig.methodConfig
  if (methodConfig.nonEmpty) {
    if (methodConfig("enableCheckpointing").equals("true")) {
      val interval = methodConfig("enableCheckpointing.interval").toLong
      val mode = methodConfig("enableCheckpointing.mode") match {
        case "EXACTLY_ONCE" => CheckpointingMode.EXACTLY_ONCE
        case "AT_LEAST_ONCE" => CheckpointingMode.AT_LEAST_ONCE
      }
      entrypoint.enableCheckpointing(interval, mode)
    }
  }

  def this(json: String) = this(JsonUtil.read(json, classOf[FlinkComputerConfig]))

  initGlobalCatalog()

}