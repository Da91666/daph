package com.dasea.daph.node.jvm.httpresult

import com.dasea.daph.api.config.NodeConfig

case class HttpConfig(
  url: String,
  method: String = "get",
  headers: Map[String, String] = Map.empty,
  params: Map[String, String] = Map.empty
) extends NodeConfig
