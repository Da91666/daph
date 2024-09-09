package com.dasea.daph.core.execution.task

import com.dasea.daph.api.GlobalContext
import com.dasea.daph.api.exception.{TaskRunException, ViewException}
import com.dasea.daph.api.node.{Node, NodeDescription, NodeInitErrorResult}
import com.dasea.daph.core.classloader.{INodeClassLoader, JNodeClassLoaderByPath, JNodeClassLoaderByURL}
import com.dasea.daph.utils.JsonUtil
import org.apache.logging.log4j.scala.Logging

import java.net.URL

abstract class Task(nodeDescription: NodeDescription, gc: GlobalContext) extends Logging {
  private var classLoader: INodeClassLoader = _
  protected var node: Node = _
  @volatile
  @transient var context: TaskContext = _
  @volatile
  @transient private var _taskThread: Thread = _

  def init(): java.util.List[NodeInitErrorResult] = {
    context = new TaskContextImpl(nodeDescription)
    TaskContext.setTaskContext(context)
    val res = new java.util.ArrayList[NodeInitErrorResult]()

    try {
      val eos = JsonUtil.convert(nodeDescription.extraOptions, classOf[Map[String, String]])
      val nodePath = eos.getOrElse("nodePath", "")
      val nodeClassName = nodeDescription.className
      val nodeClassPrefix = eos.getOrElse(
        "nodeClassPrefix",
        nodeClassName.split("\\.").dropRight(1).reduce((a, b) => s"$a.$b")
      )
      logger.info(
        s"""
           |Node metadata[
           |  nodeId: $id
           |  nodeName: ${nodeDescription.name}
           |  nodePath: $nodePath
           |  nodeClassName: $nodeClassName
           |  nodeClassPrefix: $nodeClassPrefix
           |]
           |""".stripMargin
      )


      if (nodePath.nonEmpty && !gc.globalMap.keySet.contains(nodePath)) {
        if (nodePath.endsWith(".jar")) {
          val url = new URL("file:" + nodePath)
          classLoader = new JNodeClassLoaderByURL(url, nodeClassPrefix)

          node = classLoader.loadClass(nodeClassName)
            .asSubclass(classOf[Node])
            .newInstance()

        } else if (nodePath.endsWith(".class")) {
          classLoader = new JNodeClassLoaderByPath(nodePath, nodeClassPrefix)
          node = classLoader.loadClass(nodeClassName)
            .asSubclass(classOf[Node])
            .newInstance()
        } else {
          throw new IllegalArgumentException("NodePath need the suffix[.jar or .class]!")
        }

        gc.globalMap += nodePath -> classLoader
      } else {
        node = gc.globalMap(nodePath).asInstanceOf[ClassLoader].loadClass(nodeClassName)
          .asSubclass(classOf[Node])
          .newInstance()
      }
    } catch {
      case e: Throwable =>
        logger.error("Task init failed!", e)
        res.add(NodeInitErrorResult(
          nodeDescription.name,
          "className",
          nodeDescription.className,
          "className不存在"
        ))
    }

    if (node != null) res.addAll(node.init(nodeDescription, gc))
    res
  }

  final def run(): Unit = {
    try {
      _taskThread = Thread.currentThread()

      logger.info(s"$this start")
      runTask(context)

      logger.info(s"$this succeed")
      context.markTaskSucceed()
    } catch {
      case ve: ViewException =>
        if (ve.getMessage.equals("v")) {
          logger.info(s"Node[$id]: View done")
          context.markTaskFailed(ve)
        } else {
          logger.error(s"$this failed", ve)
          context.markTaskFailed(ve)
        }
      case e: Throwable =>
        val te = new TaskRunException(s"Failed to run $this", e)
        logger.error(s"$this failed", e)
        context.markTaskFailed(te)
    } finally {
      logger.info(s"$this end")
      context.markTaskCompleted()
    }
  }

  protected def runTask(context: TaskContext): Unit

  final def clear(): Unit = {
    node = null
  }

  /**
   * 清除task所有资源
   */
  def stop(reason: String = "No reason"): Unit = {
    if (_taskThread != null) _taskThread.interrupt()
    if (context != null) context.markInterrupted(reason)
  }

  final def getNodeDescription: NodeDescription = nodeDescription

  final def id: String = nodeDescription.id

  override def equals(value: Any): Boolean = value match {
    case t: Task => id.equals(t.id)
    case _ => false
  }

  override def hashCode: Int = id.hashCode

  override def toString: String = s"Task[$id]"
}