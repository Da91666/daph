package com.dasea.daph.node.jvm.httpresult.simple.connector

import com.dasea.daph.jvm.api.node.httpresult.HttpResultSingleOutput
import com.dasea.daph.node.jvm.httpresult.{HttpConfig, http}
import com.dasea.daph.utils.HttpResult

class HttpOutput extends HttpResultSingleOutput {
  override protected def out(ds: HttpResult): Unit = {
    val config = nodeConfig.asInstanceOf[HttpConfig]

    http(config)
  }

  override def getNodeConfigClass = classOf[HttpConfig]
}
