package com.dasea.daph.api.node.base.transformer

abstract class MultipleTransformer[IN, OUT] extends Transformer {
	final override def run(): Unit = {
		val be = before(lineToDS)
		val tr = transform(be)
		val af = after(tr)
		setDSToLines(af, nodeDescription.outLines)
	}

	protected def before(lineToDS: Map[String, IN]): Map[String, IN] = {
		round("daph.before.node.")
		lineToDS
	}
	protected def transform(lineToDS: Map[String, IN]): OUT
	protected def after(out: OUT): OUT = {
		round("daph.after.node.")
		out
	}

	private def lineToDS = getInLineToDS.map { case (lane, ds) =>
		lane -> ds.asInstanceOf[IN]
	}
}
