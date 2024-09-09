package com.dasea.daph.flink117.api.node.datastream

import com.dasea.daph.flink117.api.node.FlinkNode
import com.dasea.daph.flink117.computer.FlinkStreamComputer
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment

trait DataStreamNode extends FlinkNode {
  final def env: StreamExecutionEnvironment = computer.entrypoint

  final def computer: FlinkStreamComputer = gc.computer.asInstanceOf[FlinkStreamComputer]

  override def tableEnv: StreamTableEnvironment = computer.tableEnv
}