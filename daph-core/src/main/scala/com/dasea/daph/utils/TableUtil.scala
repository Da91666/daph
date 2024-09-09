package com.dasea.daph.utils

object TableUtil {
  def getCaseName(nameCase: String, name: String): String = {
    if (nameCase.nonEmpty) {
      nameCase match {
        case "lower" => name.toLowerCase
        case "upper" => name.toUpperCase
      }
    } else name
  }

}
