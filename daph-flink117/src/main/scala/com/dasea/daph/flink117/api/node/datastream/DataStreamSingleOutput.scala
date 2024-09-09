package com.dasea.daph.flink117.api.node.datastream

import com.dasea.daph.api.node.base.connector.output.SingleOutput
import org.apache.flink.streaming.api.datastream.DataStream

abstract class DataStreamSingleOutput[IN]
  extends SingleOutput[DataStream[IN]]
    with DataStreamNode
