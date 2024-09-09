package com.dasea.daph.jvm.nodes.list

import com.dasea.daph.jvm.api.node.list.ListSingleOutput

class StringListSingleOutput extends ListSingleOutput[String] {
  override protected def out(ds: List[String]): Unit = {
    println(ds.mkString)
  }
}
