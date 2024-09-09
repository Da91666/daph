package com.dasea.daph.jvm.nodes.list

import com.dasea.daph.jvm.api.node.list.ListSingleTransformer

class StringListSingleTransformer extends ListSingleTransformer[String,String] {
  override protected def transform(ds: List[String]): List[String] = {
    ds
  }
}
