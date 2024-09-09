package com.dasea.daph.flink117.api.node.strings

import com.dasea.daph.api.node.base.connector.input.MultipleInput
import com.dasea.daph.flink117.api.node.datastream.DataStreamNode

abstract class StringsMultipleInput extends MultipleInput[Array[String]]
  with DataStreamNode