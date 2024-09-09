package com.dasea.daph.api.node.base.transformer

abstract class MMTransformer[IN, OUT] extends Transformer {
	final override def run(): Unit = {
		val be = before(lineToDS)
		val tr = transform(be)
		val af = after(tr)
		af.foreach { case (line, ds) =>
			setDSToLine(ds, line)
		}
	}

	protected def before(lineToDS: Map[String, IN]): Map[String, IN] = {
		round("daph.before.node.")
		lineToDS
	}
	protected def transform(lineToDS: Map[String, IN]): Map[String, OUT]
	protected def after(lineToDS: Map[String, OUT]): Map[String, OUT] = {
		round("daph.after.node.")
		lineToDS
	}

	private def lineToDS = getInLineToDS.map { case (line, ds) =>
		line -> ds.asInstanceOf[IN]
	}
}
