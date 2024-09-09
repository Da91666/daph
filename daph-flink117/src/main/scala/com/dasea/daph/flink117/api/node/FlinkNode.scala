package com.dasea.daph.flink117.api.node

import com.dasea.daph.api.node.Node
import com.dasea.daph.flink117.computer.FlinkComputer
import com.dasea.daph.flink117.constants.FlinkSqlConstants.NAME_DEFAULT_CATALOG
import com.dasea.daph.flink117.utils.CatalogUtil
import org.apache.flink.table.api.TableEnvironment

trait FlinkNode extends Node {
  def computer: FlinkComputer

  def tableEnv: TableEnvironment = computer.tableEnv

  def isGlobalCatalog(catalogName: String): Boolean = computer.globalCatalogConfigs.map(_("name")).contains(catalogName)

  def isDefaultCatalog(catalogName: String): Boolean = catalogName.equals(NAME_DEFAULT_CATALOG)

  def existsCatalog(catalogName: String): Boolean = computer.existsCatalog(catalogName)

  def createCatalog(catalogName: String, catalogConfig: Map[String, String] = Map.empty): Unit = {
    if (!existsCatalog(catalogName)) {
      val config =
        if (isGlobalCatalog(catalogName)) {
          computer.globalCatalogConfigs.find(_("name").equals(catalogName)).get - "enabled"
        } else catalogConfig
      CatalogUtil.createCatalog(tableEnv, config)
      computer.addCatalog(catalogName, config)
    }

    //    val tables = tableEnv.listTables()
    //    val schema = tableEnv.from("in_t").getResolvedSchema
    //    val columns = schema.getColumns
    //    val cns = schema.getColumnNames
    //    val cts = schema.getColumnDataTypes
    //    val pk = schema.getPrimaryKey
    //    val pki = schema.getPrimaryKeyIndexes
    //    val dType = columns.get(1).getDataType.toString
  }

  def getCatalogConfig(catalogName: String): Map[String, String] = {
    computer.getCatalogConfig(catalogName)
  }
}
