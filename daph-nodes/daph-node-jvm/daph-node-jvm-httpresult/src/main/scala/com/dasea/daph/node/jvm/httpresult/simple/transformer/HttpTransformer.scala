package com.dasea.daph.node.jvm.httpresult.simple.transformer

import com.dasea.daph.jvm.api.node.httpresult.HttpResultMultipleTransformer
import com.dasea.daph.node.jvm.httpresult.{HttpConfig, http}
import com.dasea.daph.utils.HttpResult

class HttpTransformer extends HttpResultMultipleTransformer {
  override protected def transform(lineToDS: Map[String, HttpResult]): HttpResult = {
    val config = nodeConfig.asInstanceOf[HttpConfig]

    http(config)
  }

  override def getNodeConfigClass = classOf[HttpConfig]
}
