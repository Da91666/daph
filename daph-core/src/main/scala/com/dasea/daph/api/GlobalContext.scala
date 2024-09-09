package com.dasea.daph.api

import com.dasea.daph.api.GR._
import com.dasea.daph.api.computer.{Computer, JVMComputer}
import com.dasea.daph.api.config._
import com.dasea.daph.api.config.option.GlobalConfigOptions._
import com.dasea.daph.api.exception.TimeException
import com.dasea.daph.api.executor.Executor
import com.dasea.daph.api.node.NodeDescription
import com.dasea.daph.api.storage.Storage
import com.dasea.daph.core.JVMStorage
import com.dasea.daph.core.execution._
import com.dasea.daph.core.execution.task.TaskFactory.getTasks
import com.dasea.daph.enums.{ComputeType, TaskType}
import com.dasea.daph.tools.DefaultExecutorFactory
import com.dasea.daph.utils.{JsonUtil, LogUtil}
import org.apache.logging.log4j.scala.Logging

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConverters.mapAsScalaConcurrentMapConverter

class GlobalContext(val globalConfig: GlobalConfig,
                    val computer: Computer,
                    val storage: Storage,
                    val executor: Executor) extends Logging {
  //  private val logXml = globalConfig.getOrElse(DAPH_LOG_XML.key, DAPH_LOG_XML.default)
  //  LogUtil.setLogXml(logXml)

  LogUtil.setRootLevel(logLevel)

  val globalMap = new ConcurrentHashMap[String, Any].asScala
  private lazy val _nodeOptions = new ConcurrentHashMap[String, String].asScala
  //  val globalSet = new CopyOnWriteArraySet[String]

  def this(json: String,
           computer: Computer = new JVMComputer,
           storage: Storage = new JVMStorage,
           executor: Executor = {
             val exeJson = DAPH_HOME.default + "/conf/executor.json"
             val exists = new File(exeJson).exists()
             val executorConfig = {
               if (exists) JsonUtil.loadFile[ExecutorConfig](exeJson)
               else null
             }
             DefaultExecutorFactory.getExecutor(executorConfig)
           },
           simple: Boolean = true) = {
    this(new GlobalConfig(json, simple), computer, storage, executor)
  }

  def this(options: Map[String, String],
           computer: Computer,
           storage: Storage,
           executor: Executor) = {
    this(new GlobalConfig(options), computer, storage, executor)
  }

  final def execute(): GlobalResult = {
    val dag = createDAG(globalConfig.getNodeDescriptions)
    executeDAG(dag)
  }

  final def execute(nodeDescriptions: Set[NodeDescription]): GlobalResult = {
    val dag = createDAG(nodeDescriptions)
    executeDAG(dag)
  }

  private def createDAG(nodeDescriptions: Set[NodeDescription]): DAG = {
    var dag: DAG = null
    try {
      logger.info(s"Daph[$name]开始初始化")

      val tasks = getTasks(TaskType.RUN, nodeDescriptions, this)
      dag = new DAG(tasks)

      logger.info("Daph初始化完成")
    } catch {
      case e: Throwable =>
        logger.error("Daph初始化失败", e)
        try {
          logger.info("Daph初始化失败后准备停止")
          stop()
        } catch {
          case e: Throwable =>
            logger.error("Daph初始化失败后停止失败", e)
            throw e
        } finally {
          throw e
        }
    }

    dag
  }

  private def executeDAG(dag: DAG): GlobalResult = {
    try {
      executor.init(dag, this)
      executor.execute()

      ComputeType.withName(computationMode) match {
        case ComputeType.BATCH =>
          logger.info("Daph RunSucceed")
          RunSucceed()
        case ComputeType.STREAM =>
          logger.info("Daph StartSucceed")
          StartSucceed
      }
    } catch {
      case te: TimeException =>
        logger.error("Daph RunTimeout!", te)
        Timeout(te)
      case e: Throwable =>
        ComputeType.withName(computationMode) match {
          case ComputeType.BATCH =>
            logger.error("Daph RunFailed!", e)
            RunFailed(e)
          case ComputeType.STREAM =>
            logger.error("Daph StartFailed!", e)
            StartFailed(e)
        }
    }
  }

  def stop(): Unit = {
    if (executor != null) executor.stop()
    if (storage != null) storage.stop()
    if (computer != null) computer.stop()

    logger.info("Daph已停止")
  }

  def clear(): Unit = {
    executor.clear()
    storage.clear()
    computer.clear()
  }

  final def setGlobalValue(key: String, value: Any): Unit = {
    globalMap.put(key, value)
  }

  final def getGlobalValue(key: String): Any = {
    globalMap(key)
  }

  final def removeGlobalValue(key: String): Any = {
    globalMap -= key
  }

  final def setNodeOption(key: String, value: String): Unit = {
    _nodeOptions.put(key, value)
  }

  final def getNodeOption(key: String): String = {
    _nodeOptions(key)
  }

  def home = globalConfig.getOrElse(DAPH_HOME.key, DAPH_HOME.default)

  def id = globalConfig.getOrElse(DAPH_ID.key, DAPH_ID.default)

  def instanceId = globalConfig.getOrElse(DAPH_INSTANCE_ID.key, DAPH_INSTANCE_ID.default)

  def name: String = globalConfig.getOrElse(DAPH_NAME.key, DAPH_NAME.default)

  def description: String = globalConfig.getOrElse(DAPH_DESC.key, DAPH_DESC.default)

  def user: String = globalConfig.getOrElse(DAPH_USER.key, DAPH_USER.default)

  def logLevel: String = globalConfig.getOrElse(DAPH_LOG_LEVEL.key, DAPH_LOG_LEVEL.default)

  def computationMode: String = globalConfig.getOrElse(DAPH_COMPUTATION_MODE.key, DAPH_COMPUTATION_MODE.default)
}
