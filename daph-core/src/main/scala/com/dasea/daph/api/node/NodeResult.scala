package com.dasea.daph.api.node

case class NodeInitErrorResult(
  nodeName: String,
  entryName: String = "",
  entryValue: String = "",
  message: String
) extends Serializable

case class NodeViewResult(
  jobId: String,
  nodeId: String,
  lineId: String,
  header: String,
  body: Array[String]
)

case class NodeMetricResult(
  jobId: String,
  nodeId: String,
  header: String,
  body: Array[String]
)