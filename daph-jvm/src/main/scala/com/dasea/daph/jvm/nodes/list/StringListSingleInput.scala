package com.dasea.daph.jvm.nodes.list

import com.dasea.daph.jvm.api.node.list.ListSingleInput

class StringListSingleInput extends ListSingleInput[String] {
  override protected def in(): List[String] = {
    List("daph-jvm-list-string-data")
  }
}
