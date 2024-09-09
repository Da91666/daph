package com.dasea.daph.node.jvm.httpresult

object HttpMethodType extends Enumeration {
  val GET = Value(name = "get")
  val POST = Value(name = "post")
  val PUT = Value(name = "put")
  val DELETE = Value(name = "delete")
}
