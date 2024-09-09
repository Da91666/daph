package com.dasea.daph.flink117.api.node.datastream

import com.dasea.daph.api.node.base.transformer.MultipleTransformer
import org.apache.flink.streaming.api.datastream.DataStream

abstract class DataStreamMultipleTransformer[IN, OUT]
  extends MultipleTransformer[DataStream[IN], DataStream[OUT]]
    with DataStreamNode