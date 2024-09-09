package com.dasea.daph.flink117.api.node.strings

import com.dasea.daph.api.node.base.connector.output.MultipleOutput
import com.dasea.daph.flink117.api.node.datastream.DataStreamNode

abstract class StringsMultipleOutput extends MultipleOutput[Array[String]]
  with DataStreamNode
