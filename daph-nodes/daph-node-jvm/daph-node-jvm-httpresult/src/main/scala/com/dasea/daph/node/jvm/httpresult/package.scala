package com.dasea.daph.node.jvm

import com.dasea.daph.utils.{HttpResult, HttpUtil}

import scala.collection.JavaConverters._

package object httpresult {
  def http(config: HttpConfig): HttpResult = {
    val url = config.url
    val method = config.method
    val headers = config.headers.asJava
    val params = config.params.asJava

    HttpMethodType.withName(method) match {
      case HttpMethodType.GET => HttpUtil.doGet(url, headers, params)
      case HttpMethodType.POST => HttpUtil.doPost(url, headers, params)
      case HttpMethodType.PUT => HttpUtil.doPut(url, params)
      case HttpMethodType.DELETE => HttpUtil.doDelete(url, params)
    }
  }
}
