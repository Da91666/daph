package com.dasea.daph.api.node.base.connector.output

abstract class MultipleOutput[IN] extends Output {
  final override def run(): Unit = {
    val res = before(lineToDS)
    out(res)
    after()
  }

  protected def before(lineToDS: Map[String, IN]): Map[String, IN] = {
    round("daph.before.node.")
    lineToDS
  }

  protected def out(lineToDS: Map[String, IN]): Unit

  protected def after(): Unit = {
    round("daph.after.node.")
  }

  private def lineToDS = {
    getInLineToDS.map { case (line, ds) => line -> ds.asInstanceOf[IN] }
  }
}
