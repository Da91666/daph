package com.dasea.daph.api.node.base.connector.input

abstract class MultipleInput[OUT] extends Input {
  final override def run(): Unit = {
    before()
    val ds = in()
    val res = after(ds)

    res.foreach { case (line, ds) =>
      setDSToLine(ds, line)
    }
  }

  protected def before(): Unit = {
    round("daph.before.node.")
  }

  protected def in(): Map[String, OUT]

  protected def after(lineToDS: Map[String, OUT]): Map[String, OUT] = {
    round("daph.after.node.")
    lineToDS
  }
}
