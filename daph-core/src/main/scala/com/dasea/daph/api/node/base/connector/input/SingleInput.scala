package com.dasea.daph.api.node.base.connector.input

abstract class SingleInput[OUT] extends Input {
  final override def run(): Unit = {
    before()
    val ds = in()
    val res = after(ds)

    setDSToLines(res, nodeDescription.outLines)
  }

  protected def before(): Unit = {
    round("daph.before.node.")
  }

  protected def in(): OUT

  protected def after(out: OUT): OUT = {
    round("daph.after.node.")
    out
  }
}
