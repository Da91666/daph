package com.dasea.daph.api.node

case class SimpleNodeDescription(
  flag: String,
  id: String = "",
  name: String = "",
  itoType: String = "",
  config: Any = None,
  inLines: Array[String] = Array.empty,
  outLines: Array[String] = Array.empty,
  extraOptions: Map[String, Any] = Map.empty) {
  override def equals(obj: Any): Boolean = {
    obj match {
      case x: SimpleNodeDescription =>
        flag.equals(x.flag) &
          id.equals(x.id) &
          name.equals(x.name) &
          config.equals(x.config) &
          inLines.sameElements(x.inLines) &
          //          outLines.sameElements(x.outLines) &
          extraOptions.equals(x.extraOptions)
      case _ => false
    }
  }

  def toPrettyString: String =
    s"""
       |flag: $flag,
       |id: $id,
       |name: $name,
			 |config: $config,
			 |inLines: ${inLines.mkString("Array(", ", ", ")")},
			 |outLines: ${outLines.mkString("Array(", ", ", ")")},
			 |extraOptions: $extraOptions
			 |""".stripMargin
}
