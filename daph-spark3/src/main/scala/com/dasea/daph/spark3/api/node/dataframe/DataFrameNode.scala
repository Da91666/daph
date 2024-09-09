package com.dasea.daph.spark3.api.node.dataframe

import com.dasea.daph.api.node.NodeViewResult
import com.dasea.daph.spark3.api.node.SparkNode
import com.dasea.daph.spark3.utils.CommonUtils.executeQuerySQL
import com.dasea.daph.utils.JsonUtil
import org.apache.spark.sql.{DataFrame, LimitLogicalPlan}

trait DataFrameNode extends SparkNode {
  protected def roundDF(rootDefName: String, df: DataFrame): DataFrame = {
    var res = df

    usingRound(rootDefName) { (defName, config) =>
      val sql = JsonUtil.convert(config, classOf[String])
      res = defName match {
        case "querySQL" => querySQL(sql, df)
      }
    }

    res
  }

  protected def querySQL(sql: String, df: DataFrame): DataFrame = {
    executeQuerySQL(sql, df, spark)
  }

  override protected def getNodeViewResult(lineId: String, any: Any, config: Map[String, String]) = {
    val df = any.asInstanceOf[DataFrame]
    val header = df.schema.fieldNames.reduce((a, b) => s"$a,$b")
    val sql = config.getOrElse("sql", "")
    val limit = config.getOrElse("limit", "200").toInt

    val rdf = if (sql.isEmpty) df else querySQL(sql, df)
    val body = LimitLogicalPlan.limit(rdf, limit).collect().map(row => row.mkString(","))

    NodeViewResult(gc.id, id, lineId, header, body)
  }
}
