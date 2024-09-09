package com.dasea.daph.node.spark3.dataframe.general.transformer.union

import com.dasea.daph.spark3.api.node.dataframe.transformer.DataFrameMultipleTransformer
import org.apache.spark.sql.DataFrame

class Union extends DataFrameMultipleTransformer {
  override protected def transform(lineToDS: Map[String, DataFrame]): DataFrame = {
    val config = nodeConfig.asInstanceOf[UnionConfig]
    var result: DataFrame = null
    lineToDS.foreach(kV => {
      var tableUnionType = config.unionType
      var tableAllowMissingColumns = config.allowMissingColumns
      if (config.tableUnionConfig.nonEmpty && config.tableUnionConfig.contains(kV._1)) {
        tableUnionType = config.tableUnionConfig.get(kV._1).get.unionType
        tableAllowMissingColumns = config.tableUnionConfig.get(kV._1).get.allowMissingColumns
      }

      if (UnionType.withName(tableUnionType).eq(UnionType.POSITION)) {
        result = if (result==null)kV._2 else  result.unionAll(kV._2)
      } else {
        result = if (result==null)kV._2 else result.unionByName(kV._2, tableAllowMissingColumns)
      }
    })
    result
  }

  override def getNodeConfigClass: Class[UnionConfig] = classOf[UnionConfig]


}
