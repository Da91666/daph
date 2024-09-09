package com.dasea.daph.flink117.computer

case class FlinkComputerConfig(
  envConfig: Map[String, String],
  methodConfig: Map[String, String] = Map.empty,
  catalogConfigs: Array[Map[String, String]] = Array.empty
)