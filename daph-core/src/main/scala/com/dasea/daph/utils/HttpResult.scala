package com.dasea.daph.utils

case class HttpResult(
  code: Int = 500,
  message: String = "Internal Server Error"
)
