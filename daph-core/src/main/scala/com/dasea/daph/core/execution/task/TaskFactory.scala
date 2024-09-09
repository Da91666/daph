package com.dasea.daph.core.execution.task

import com.dasea.daph.api.GlobalContext
import com.dasea.daph.api.exception.DaphException
import com.dasea.daph.api.node.{NodeDescription, NodeInitErrorResult}
import com.dasea.daph.enums.TaskType
import org.apache.logging.log4j.scala.Logging

import scala.jdk.CollectionConverters.collectionAsScalaIterableConverter

object TaskFactory extends Logging {
  def getTasks(taskType: TaskType.Value,
               nodeDescriptions: Set[NodeDescription],
               gc: GlobalContext): Set[Task] = {
    val taskToInitResult = nodeDescriptions.map { desc =>
      getTaskAndInitResult(taskType, desc, gc)
    }

    val initResults = taskToInitResult.map(_._2).filterNot(_.isEmpty)
    if (initResults.nonEmpty) throw new DaphException(s"Node初始化错误")

    taskToInitResult.map(_._1)
  }

  def getTaskAndInitResult(taskType: TaskType.Value,
                           nodeDescription: NodeDescription,
                           gc: GlobalContext): (Task, java.util.List[NodeInitErrorResult]) = {
    val task = taskType match {
      case TaskType.RUN => RunTask(nodeDescription, gc)
    }

    val res = task.init()
    if (!res.isEmpty) {
      res.asScala.foreach { r =>
        logger.error(s"[${r.nodeName}-${r.entryName}]: ${r.message}[${r.entryValue}]")
      }
    }

    (task, res)
  }
}