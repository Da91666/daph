package com.dasea.daph.flink117.api.node.datastream

import com.dasea.daph.api.node.base.transformer.SingleTransformer
import org.apache.flink.streaming.api.datastream.DataStream

abstract class DataStreamSingleTransformer[IN, OUT]
  extends SingleTransformer[DataStream[IN], DataStream[OUT]]
    with DataStreamNode
