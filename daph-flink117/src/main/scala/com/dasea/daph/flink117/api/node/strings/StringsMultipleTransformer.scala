package com.dasea.daph.flink117.api.node.strings

import com.dasea.daph.api.node.base.transformer.MultipleTransformer
import com.dasea.daph.flink117.api.node.datastream.DataStreamNode

abstract class StringsMultipleTransformer
  extends MultipleTransformer[Array[String], Array[String]]
    with DataStreamNode