package com.dasea.daph.node.jvm.httpresult.simple.connector

import com.dasea.daph.jvm.api.node.httpresult.HttpResultSingleInput
import com.dasea.daph.node.jvm.httpresult.{HttpConfig, http}
import com.dasea.daph.utils.HttpResult

class HttpInput extends HttpResultSingleInput {
  override protected def in(): HttpResult = {
    val config = nodeConfig.asInstanceOf[HttpConfig]

    http(config)
  }

  override def getNodeConfigClass = classOf[HttpConfig]
}
