package com.dasea.daph.api.node.base.connector.output

import com.dasea.daph.api.GlobalContext
import com.dasea.daph.api.node.{NodeDescription, NodeInitErrorResult}

abstract class SingleOutput[IN] extends Output {
  override def init(stageDescription: NodeDescription,
                    gc: GlobalContext): java.util.List[NodeInitErrorResult] = {
    val res = super.init(stageDescription, gc)
    if (stageDescription.inLines.length > 1) {
      res.add(NodeInitErrorResult(
        this.getClass.getSimpleName,
        "inLines",
        s"${stageDescription.inLines.mkString("Array(", ", ", ")")}",
        "不应有多条输入线"
      ))
    }
    res
  }

  final override def run(): Unit = {
    val res = before(ds)
    out(res)
    after()
  }

  protected def before(ds: IN): IN = {
    round("daph.before.node.")
    ds
  }

  protected def out(ds: IN): Unit

  protected def after(): Unit = {
    round("daph.after.node.")
  }

  private def ds = {
    val lines = nodeDescription.inLines

    if (lines.nonEmpty) getDSByLine(lines.head).asInstanceOf[IN]
    else null.asInstanceOf[IN]
  }
}
