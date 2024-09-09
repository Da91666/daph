package com.dasea.daph.flink117.api.node.datastream

import com.dasea.daph.api.node.base.connector.input.SingleInput
import org.apache.flink.streaming.api.datastream.DataStream

abstract class DataStreamSingleInput[OUT]
  extends SingleInput[DataStream[OUT]]
    with DataStreamNode