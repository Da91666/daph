package com.dasea.daph.api.node.base.transformer

import com.dasea.daph.api.GlobalContext
import com.dasea.daph.api.node.{NodeDescription, NodeInitErrorResult}

abstract class SingleTransformer[IN, OUT] extends Transformer {
  override def init(nodeDescription: NodeDescription,
                    gc: GlobalContext): java.util.List[NodeInitErrorResult] = {
    val res = super.init(nodeDescription, gc)

    if (nodeDescription.inLines.length > 1) {
      res.add(NodeInitErrorResult(
        this.getClass.getSimpleName,
        "inLines",
        s"${nodeDescription.inLines.mkString("Array(", ", ", ")")}",
        "不应有多条输入线"
      ))
    }

    res
  }

  final override def run(): Unit = {
    val be = before(ds)
    val tr = transform(be)
    val af = after(tr)
    setDSToLines(af, nodeDescription.outLines)
  }

  protected def before(in: IN): IN = {
    round("daph.before.node.")
    in
  }
  protected def transform(ds: IN): OUT
  protected def after(out: OUT): OUT = {
    round("daph.after.node.")
    out
  }

  private def ds = {
    val lanes = nodeDescription.inLines

    if (lanes.nonEmpty) getDSByLine(lanes.head).asInstanceOf[IN]
    else null.asInstanceOf[IN]
  }
}
