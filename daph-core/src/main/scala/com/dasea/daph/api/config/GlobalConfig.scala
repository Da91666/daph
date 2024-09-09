package com.dasea.daph.api.config

import com.dasea.daph.api.config.GlobalConfig.{parse, parseSimple}
import com.dasea.daph.api.node.NodeDescription
import com.dasea.daph.tools.ConfigTool
import com.dasea.daph.utils.JsonUtil
import org.apache.logging.log4j.scala.Logging

import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConverters.{asScalaBufferConverter, mapAsScalaConcurrentMapConverter, mapAsScalaMapConverter}
import scala.collection.mutable

class GlobalConfig extends Cloneable with Logging with Serializable {
  private val _options = new ConcurrentHashMap[String, String].asScala
  private val _nodeDescriptions = mutable.HashSet[NodeDescription]()
  private var _json = ""

  def this(json: String, simple: Boolean = true) = {
    this()
    _json = json
    val config = if (simple) parseSimple(json) else parse(json)
    setAll(config._1, config._2)
  }

  def this(options: Map[String, String]) = {
    this()
    options.foreach { case (k, v) => _options.put(k, v) }
  }

  def this(options: Map[String, String], nodeDescriptions: Set[NodeDescription]) = {
    this()
    setAll(options, nodeDescriptions)
  }

  def jobJson: String = _json

  private def setAll(options: Map[String, String],
                     nodeDescriptions: Set[NodeDescription]): Unit = {
    options.foreach { case (k, v) => _options.put(k, v) }
    _nodeDescriptions ++= nodeDescriptions
  }

  def set(key: String, value: String): GlobalConfig = {
    if (key == null) throw new NullPointerException("null key")
    if (value == null) throw new NullPointerException("null value for " + key)

    _options.put(key, value)
    this
  }

  def get(key: String): String = {
    getOption(key).getOrElse(throw new NoSuchElementException(key))
  }

  def getOption(key: String): Option[String] = _options.get(key)

  def getOrElse(key: String, value: String): String = _options.getOrElse(key, value)

  def setOptions(options: Map[String, String]): GlobalConfig = {
    options.foreach { case (k, v) => set(k, v) }
    this
  }

  def getOptions: Map[String, String] = _options.toMap

  def setNodeDescriptions(nodeDescriptions: Array[NodeDescription]): GlobalConfig = {
    _nodeDescriptions ++= nodeDescriptions.toSet
    this
  }

  def getNodeDescriptions: Set[NodeDescription] = _nodeDescriptions.toSet

  def remove(key: String): GlobalConfig = {
    _options.remove(key)
    this
  }

  private def clear(): Unit = {
    _options.clear()
    _nodeDescriptions.clear()
    logger.info("DaphConfig已清除")
  }

  override def clone: GlobalConfig = {
    val cloned = new GlobalConfig
    _options.foreach { case (k, v) => cloned.set(k, v) }
    cloned._nodeDescriptions ++= _nodeDescriptions
    cloned
  }
}

object GlobalConfig {
  def parse(json: String): (Map[String, String], Set[NodeDescription]) = {
    val jConfig = JsonUtil.read(json, classOf[JGlobalConfig])
    val options = jConfig.getOptions.asScala.toMap
    val nodes = jConfig.getNodes.asScala.toSet
    (options, nodes)
  }

  def parseSimple(json: String): (Map[String, String], Set[NodeDescription]) = {
    val sjConfig = JsonUtil.read(json, classOf[SimpleJGlobalConfig])
    val jConfig = ConfigTool.simpleConfigToConfig(sjConfig)
    val options = jConfig.getOptions.asScala.toMap
    val nodes = jConfig.getNodes.asScala.toSet
    (options, nodes)
  }
}
