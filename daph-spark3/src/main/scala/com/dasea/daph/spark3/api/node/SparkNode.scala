package com.dasea.daph.spark3.api.node

import com.dasea.daph.api.node.Node
import org.apache.spark.sql.SparkSession

trait SparkNode extends Node {
  final def spark: SparkSession = gc.computer.entrypoint.asInstanceOf[SparkSession]
}
