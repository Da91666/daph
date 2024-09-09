package com.dasea.daph.api.node

case class NodeDescription(
  id: String,
  name: String = "",
  itoType: String = "",
  className: String,
  config: Any = None,
  inLines: Array[String] = Array.empty,
  outLines: Array[String] = Array.empty,
  extraOptions: Map[String, Any] = Map.empty) {
  override def equals(obj: Any): Boolean = {
    obj match {
      case x: NodeDescription =>
        id.equals(x.id) &
          name.equals(x.name) &
          className.equals(x.className) &
          config.equals(x.config) &
          inLines.sameElements(x.inLines) &
          //          outLines.sameElements(x.outLines) &
          extraOptions.equals(x.extraOptions)
      case _ => false
    }
  }

  def toPrettyString: String =
    s"""
			 |id: $id,
			 |name: $name,
			 |flag: $className,
			 |config: $config,
			 |inLines: ${inLines.mkString("Array(", ", ", ")")},
			 |outLines: ${outLines.mkString("Array(", ", ", ")")},
			 |extraOptions: $extraOptions
			 |""".stripMargin
}
