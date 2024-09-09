package com.dasea.daph.api.node

import com.dasea.daph.api.GlobalContext
import com.dasea.daph.api.config.option.GlobalConfigOptions.DAPH_DT_MAPPING_PATH
import com.dasea.daph.api.config.{NodeConfig, NullNodeConfig}
import com.dasea.daph.api.exception.ViewException
import com.dasea.daph.utils.{EDBConfig, HttpCUtil, JdbcUtil, JsonUtil}
import org.apache.logging.log4j.scala.Logging
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization

abstract class Node extends java.io.Serializable with Logging {
  private var _nodeDescription: NodeDescription = _
  private var _config: NodeConfig = _
  private var _gc: GlobalContext = _
  private lazy implicit val formats: DefaultFormats.type = DefaultFormats

  def init(nodeDescription: NodeDescription, gc: GlobalContext): java.util.List[NodeInitErrorResult] = {
    val res = new java.util.ArrayList[NodeInitErrorResult]()
    _nodeDescription = nodeDescription
    try {
      _config = {
        if (getNodeConfigClass != null) {
          JsonUtil.convert(
            nodeDescription.config,
            getNodeConfigClass
          )
        } else {
          new NullNodeConfig()
        }
      }
    } catch {
      case e: Throwable =>
        res.add(NodeInitErrorResult(
          this.getClass.getSimpleName,
          "NodeConfig",
          s"${getNodeConfigClass.getName}",
          s"NodeConfig实例化失败！${e.getMessage}"
        ))
    }
    _gc = gc

    res
  }

  final def runNode(): Unit = {
    try {
      run()
    } catch {
      case e: Throwable =>
        logger.error(s"Node run failed! ${e.getMessage}")
        throw e
    }
  }

  protected def beforeRun(): Unit = {
    round("daph.before.node.")
  }

  protected def run(): Unit

  protected def afterRun(): Unit = {
    round("daph.after.node.")
  }

  protected final def round(rootDefName: String) = usingRound(rootDefName) { (defName, config) =>
    defName match {
      case "executeSDBSQLs" => executeSDBSQLs(config)
      case "executeMDBSQLs" => executeMDBSQLs(config)
      case "createEDB" => createEDB(config)
      case "createEDBs" => createEDBs(config)
      case "viewNode" => viewNode(config)
      // TODO: sendMetrics
      case "sendMetrics" =>
    }
  }

  protected final def usingRound(rootDefName: String)(f: (String, Any) => Any) = {
    val names = getExtraOptions.filter(_._1.startsWith(rootDefName))
    if (names.nonEmpty) {
      names.foreach { case (k, v) =>
        val defName = k.split("\\.").last

        f(defName, v)
      }
    }
  }

  protected final def executeSDBSQLs(config: Any): Unit = {
    val conf = JsonUtil.convert(config, classOf[Map[String, String]])
    JdbcUtil.using(conf) { conn =>
      JdbcUtil.executeSQLs(conf, conn)
    }
  }

  protected final def executeMDBSQLs(config: Any): Unit = {
    val confs = JsonUtil.convert(config, classOf[Array[Map[String, String]]])
    confs.foreach(executeSDBSQLs)
  }

  protected final def createEDB(config: Any): Unit = {
    val conf = JsonUtil.convert(config, classOf[EDBConfig])
    val dtMappingFileRootPath = gc.globalConfig.getOrElse(DAPH_DT_MAPPING_PATH.key, mappingRootPath)
    val sourceDBConfig = conf.sourceDBConfig
    val targetDBConfig = conf.targetDBConfig
    val sqls = JdbcUtil.using(sourceDBConfig) { conn =>
      val tns = JdbcUtil.getTableNames(sourceDBConfig, conn)
      JdbcUtil.getCreateSqls(tns, dtMappingFileRootPath, sourceDBConfig, targetDBConfig, conn)
    }.reduce((a, b) => s"$a;$b")

    JdbcUtil.using(targetDBConfig) { conn =>
      val rconfig = targetDBConfig + ("daph.sqls" -> sqls)
      JdbcUtil.executeSQLs(rconfig, conn)
    }
  }

  protected final def createEDBs(config: Any): Unit = {
    val conf = JsonUtil.convert(config, classOf[Array[EDBConfig]])
    conf.foreach(createEDB)
  }

  protected final def viewNode(config: Any) = {
    if (getOutLineToDS.nonEmpty) {
      val conf = JsonUtil.convert(config, classOf[Map[String, String]])
      getOutLineToDS.foreach { case (line, ds) =>
        val url = conf("url")
        val json = Serialization.write(getNodeViewResult(line, ds, conf))
        val response = HttpCUtil.INSTANCE.post(url, json)
        if (!response.isSuccess) {
          throw new ViewException(s"Node[$id]: Http post failed with StatusCode[${response.getStatusCode}]!")
        }
        throw new ViewException("v")
      }
    } else {
      throw new ViewException(s"Node[$id]: Datas empty!")
    }
  }

  protected def getNodeViewResult(lineId: String, any: Any, config: Map[String, String]): NodeViewResult = {
    NodeViewResult(gc.id, id, lineId, "", Array.empty)
  }

  def getNodeConfigClass: Class[_ <: NodeConfig] = classOf[NullNodeConfig]

  final def home: String = gc.home
  final def mappingRootPath: String = home + "/conf/mapping"

  final def id: String = nodeDescription.id

  final def nodeDescription: NodeDescription = _nodeDescription

  final def nodeConfig: NodeConfig = _config

  final def gc: GlobalContext = _gc

  final protected def setDSToLine(ds: Any, line: String): Unit = {
    gc.storage.setDSToLine(ds, line)
  }

  final protected def setDSToLines(ds: Any, lines: Array[String]): Unit = {
    gc.storage.setDSToLines(ds, lines)
  }

  final protected def getDSByLine(line: String): Any = {
    gc.storage.getDSByLine(line)
  }

  final protected def getInLineToDS: Map[String, Any] = {
    gc.storage.getLineToDSByLines(nodeDescription.inLines)
  }

  final protected def getOutLineToDS: Map[String, Any] = {
    val outLines = nodeDescription.outLines
    if (outLines.nonEmpty) gc.storage.getLineToDSByLines(outLines)
    else Map.empty
  }

  final protected def getInLines: Array[String] = nodeDescription.inLines

  final protected def getOutLines: Array[String] = nodeDescription.outLines

  final protected def getExtraOptions: Map[String, Any] = nodeDescription.extraOptions
}