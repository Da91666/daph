package com.dasea.daph.node.flink117.sql.general.transformer

import com.dasea.daph.flink117.api.node.strings.StringsMMTransformer
import com.dasea.daph.flink117.constants.FlinkSqlConstants.{NAME_DEFAULT_CATALOG, NAME_DEFAULT_DATABASE}
import com.dasea.daph.flink117.utils.CatalogUtil
import com.dasea.daph.node.flink117.sql.general.{GaMMSQLConfig, GeneralNode}
import com.dasea.daph.utils.SQLUtil
import org.apache.commons.lang3.StringUtils

class GaMMSQL extends StringsMMTransformer with GeneralNode {
  override def transform(lineToDS: Map[String, Array[String]]): Map[String, Array[String]] = {
    val config = nodeConfig.asInstanceOf[GaMMSQLConfig]
    val queryConfigs = config.queryConfigs

    suDefault()
    CatalogUtil.logAndProduceCatalogDetails(tableEnv)

    val res = if (queryConfigs.length == 1) {
      val results = queryConfigs.head.results
      val tns = results.map { rt =>
        val sql = rt.sql
        val tn =
          if (StringUtils.isEmpty(rt.resultTable)) SQLUtil.getTableName(sql)
          else rt.resultTable
        createTemporaryView(tn, sql)
        if (tn.contains("\\.")) tn
        else s"$NAME_DEFAULT_CATALOG.$NAME_DEFAULT_DATABASE.$tn"
      }

      getOutLines.map(line => line -> tns)
    } else {
      queryConfigs.map { ls =>
        val outLine = ls.line
        val results = ls.results
        val tns = results.map { rt =>
          val sql = rt.sql
          val tn =
            if (StringUtils.isEmpty(rt.resultTable)) SQLUtil.getTableName(sql)
            else rt.resultTable
          createTemporaryView(tn, sql)
          if (tn.contains("\\.")) tn
          else s"$NAME_DEFAULT_CATALOG.$NAME_DEFAULT_DATABASE.$tn"
        }

        outLine -> tns
      }
    }

    res.toMap
  }

  override def getNodeConfigClass = classOf[GaMMSQLConfig]
}
