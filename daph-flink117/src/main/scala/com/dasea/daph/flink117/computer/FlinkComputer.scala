package com.dasea.daph.flink117.computer

import com.dasea.daph.api.computer.Computer
import com.dasea.daph.flink117.constants.FlinkSqlConstants.NAME_DEFAULT_CATALOG
import com.dasea.daph.utils.JsonUtil
import org.apache.flink.table.api.TableEnvironment
import org.apache.logging.log4j.scala.Logging
import org.jboss.netty.util.internal.ConcurrentHashMap

abstract class FlinkComputer(val computerConfig: FlinkComputerConfig) extends Computer with Logging {
  val tableEnv: TableEnvironment
  private val catalogs = new ConcurrentHashMap[String, Map[String, String]]

  def this(json: String) = this(
    JsonUtil.read(json, classOf[FlinkComputerConfig])
  )

  def globalCatalogConfigs = computerConfig.catalogConfigs

  def existsCatalog(catalogName: String): Boolean = catalogs.containsKey(catalogName)

  def addCatalog(catalogName: String, catalogConfig: Map[String, String] = Map(
    "type" -> "default",
    "name" -> "default_catalog",
    "default-database" -> "default_database",
  )): Unit = {
    catalogs.put(catalogName, catalogConfig)
  }

  def getCatalogConfig(catalogName: String): Map[String, String] = {
    catalogs.get(catalogName)
  }

  protected def initGlobalCatalog(): Unit = {
    addCatalog(NAME_DEFAULT_CATALOG)
  }
}
