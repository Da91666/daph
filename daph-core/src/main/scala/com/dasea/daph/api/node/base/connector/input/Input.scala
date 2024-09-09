package com.dasea.daph.api.node.base.connector.input

import com.dasea.daph.api.GlobalContext
import com.dasea.daph.api.node.{Node, NodeDescription, NodeInitErrorResult}

abstract class Input extends Node {
  override def init(nodeDescription: NodeDescription,
                    gc: GlobalContext): java.util.List[NodeInitErrorResult] = {
    val res = super.init(nodeDescription, gc)

    if (nodeDescription.outLines.isEmpty) {
      res.add(NodeInitErrorResult(
        this.getClass.getSimpleName,
        "outLines",
        "应当有输出线",
        s"${nodeDescription.outLines.mkString("Array(", ", ", ")")}"
      ))
    }

    res
  }
}
