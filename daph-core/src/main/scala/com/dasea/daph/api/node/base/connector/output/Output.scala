package com.dasea.daph.api.node.base.connector.output

import com.dasea.daph.api.GlobalContext
import com.dasea.daph.api.node.{Node, NodeDescription, NodeInitErrorResult}

abstract class Output extends Node {
  override def init(nodeDescription: NodeDescription,
                    gc: GlobalContext): java.util.List[NodeInitErrorResult] = {
    val res = super.init(nodeDescription, gc)

    if (nodeDescription.inLines.isEmpty) {
      res.add(NodeInitErrorResult(
        this.getClass.getSimpleName,
        "inLines",
        "应当有输入线",
        s"${nodeDescription.inLines.mkString("Array(", ", ", ")")}"
      ))
    }

    res
  }
}
