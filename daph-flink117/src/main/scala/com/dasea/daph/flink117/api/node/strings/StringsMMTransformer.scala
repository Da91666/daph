package com.dasea.daph.flink117.api.node.strings

import com.dasea.daph.api.node.base.transformer.MMTransformer
import com.dasea.daph.flink117.api.node.datastream.DataStreamNode

abstract class StringsMMTransformer
  extends MMTransformer[Array[String], Array[String]]
    with DataStreamNode