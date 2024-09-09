package com.dasea.daph.tools

import com.dasea.daph.api.config.option.GlobalConfigOptions._
import com.dasea.daph.api.config.{JGlobalConfig, SimpleJGlobalConfig}
import com.dasea.daph.api.node.NodeDescription
import com.dasea.daph.utils.JsonUtil
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.scala.Logging

import java.io.File
import scala.jdk.CollectionConverters.{collectionAsScalaIterableConverter, seqAsJavaListConverter}

object ConfigTool extends Logging{
  def simpleConfigToConfig(config: SimpleJGlobalConfig): JGlobalConfig = {
    val res = new JGlobalConfig
    val nodes = config.getNodes.asScala
    val options = config.getOptions
    val home = options.getOrDefault(DAPH_HOME.key, DAPH_HOME.default)

    val computerType = options.getOrDefault(DAPH_COMPUTER_TYPE.key, DAPH_COMPUTER_TYPE.default)
    val dictionaryPath = options.getOrDefault(NODE_DICTIONARY_PATH.key, NODE_DICTIONARY_PATH.default)
    val rDictionaryPath = {
      if (!StringUtils.isEmpty(dictionaryPath)) dictionaryPath
      else {
        if (StringUtils.isEmpty(computerType)) home + "/conf/node-dictionary.json"
        else home + s"/conf/dictionary/node-dictionary-$computerType.json"
      }
    }
    val dictionary = JsonUtil.loadFile[Array[NodeInfo]](rDictionaryPath)

    val nodeRootPath = options.getOrDefault(NODE_JAR_ROOT_PATH.key, home + "/jars/nodes")
    val nodePaths = if (nodeRootPath.nonEmpty) {
      val dir = new File(nodeRootPath)
      val fns = dir.listFiles().filter(_.isFile).map(_.getCanonicalPath).toSet
      fns
    } else Set.empty

    res.setOptions(options)

    val dagModel = options.getOrDefault(DAPH_DAG_MODEL.key, DAPH_DAG_MODEL.default)
    var i = 0
    val resNodes = nodes.map { node =>
      val flag = node.flag
      val nodeInfo = {
        if (flag.contains(".")) dictionary.find(_.flag.equals(flag)).get
        else dictionary.find(_.alias.contains(flag)).get
      }

      val extraOptions = {
        if (nodePaths.isEmpty) {
          node.extraOptions +
            ("nodePath" -> s"${node.extraOptions("nodePath")}/${nodeInfo.jarName}") +
            ("nodeClassPrefix" -> (
              if (StringUtils.isEmpty(nodeInfo.classPrefix)) nodeInfo.className.split("\\.").dropRight(1).reduce((a, b) => s"$a.$b")
              else nodeInfo.classPrefix))
        } else {
          node.extraOptions +
            ("nodePath" -> nodePaths.find(_.endsWith(nodeInfo.jarName)).get) +
            ("nodeClassPrefix" -> (
              if (StringUtils.isEmpty(nodeInfo.classPrefix)) nodeInfo.className.split("\\.").dropRight(1).reduce((a, b) => s"$a.$b")
              else nodeInfo.classPrefix))
        }
      }

      val (id, inLines, outLines) = dagModel match {
        case "two-point-line" => {
          val inFlags = Seq("input", "in", "source", "src")
          val (inLines, outLines) = {
            if (inFlags.exists(f => flag.startsWith(f))) (Array.empty[String], Array("line"))
            else (Array("line"), Array.empty[String])
          }
          (flag, inLines, outLines)
        }
        case "three-point-line" => {
          val inFlags = Seq("input", "in", "source", "src")
          val outFlags = Seq("output", "out", "sink", "sk")
          val (inLines, outLines) = {
            if (inFlags.exists(f => flag.startsWith(f))) (Array.empty[String], Array("line1"))
            else if (outFlags.exists(f => flag.startsWith(f))) (Array("line2"), Array.empty[String])
            else (Array("line1"), Array("line2"))
          }
          (flag, inLines, outLines)
        }
        case _ => {
          val id = if (StringUtils.isEmpty(node.id)) {
            i += 1
            s"$flag-$i"
          } else node.id
          (id, node.inLines, node.outLines)
        }
      }

      val desc = NodeDescription(
        id, node.name, "", nodeInfo.className, node.config,
        inLines, outLines,
        extraOptions
      )
      logger.debug(s"NodeDescription is ${desc.toPrettyString}")
      desc
    }.toList.asJava
    res.setNodes(resNodes)

    res
  }

  private case class NodeInfo(flag: String, alias: Array[String], jarName: String, className: String, classPrefix: String)
}
