package com.dasea.daph.flink117.utils

import org.apache.flink.table.api.TableEnvironment
import org.apache.logging.log4j.scala.Logging

object CatalogUtil extends Logging {
  def createCatalog(tableEnv: TableEnvironment, config: Map[String, String]): String = {
    val catalogName = config("name")
    val withPart = (config - "name")
      .map { case (k, v) => s"'$k'='$v'" }
      .reduce((a, b) => s"$a,$b")
    val catalogSql = s"CREATE CATALOG $catalogName WITH ($withPart)"

    tableEnv.executeSql(catalogSql)
    catalogName
  }

  /**
   * 打印Catalog详细信息
   *
   * @param tableEnv Table环境对象
   */
  def logAndProduceCatalogDetails(tableEnv: TableEnvironment): Unit = {
    val cc = tableEnv.getCurrentCatalog
    val cd = tableEnv.getCurrentDatabase
    val cs = tableEnv.listCatalogs()
    val dbs = tableEnv.listDatabases()
    val ts = tableEnv.listTables()
    val tts = tableEnv.listTemporaryTables()
    val vs = tableEnv.listViews()
    val tvs = tableEnv.listTemporaryViews()

    logger.info(
      s"""
         |CurrentCatalog: $cc
         |CurrentDatabase: $cd
         |Catalogs: ${cs.mkString("Array(", ", ", ")")}
         |Databases: ${dbs.mkString("Array(", ", ", ")")}
         |Tables: ${ts.mkString("Array(", ", ", ")")}
         |TemporaryTables: ${tts.mkString("Array(", ", ", ")")}
         |Views: ${vs.mkString("Array(", ", ", ")")}
         |TemporaryViews: ${tvs.mkString("Array(", ", ", ")")}
         |""".stripMargin
    )
  }

}
