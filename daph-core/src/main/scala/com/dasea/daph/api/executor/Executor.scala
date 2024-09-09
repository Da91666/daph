package com.dasea.daph.api.executor

import com.dasea.daph.api.GlobalContext
import com.dasea.daph.api.exception.DaphException
import com.dasea.daph.core.execution.DAG
import com.dasea.daph.core.execution.task.Task
import org.apache.logging.log4j.scala.Logging

import java.util.concurrent.atomic.AtomicBoolean
import scala.collection.mutable

abstract class Executor extends Logging {
  protected var _dag: DAG = _
  protected var _gc: GlobalContext = _
  private val isDone = new AtomicBoolean(false)

  protected final def tasks: Set[Task] = _dag.getTasks

  final def setStatus(b: Boolean): Unit = isDone.set(b)

  final def getStatus = isDone.get

  final def init(dag: DAG, gc: GlobalContext): Unit = {
    _dag = dag
    _gc = gc
  }

  def execute(): Unit
  protected def using(dag: DAG)(f: Task => Any): Unit = {
    logger.info("Executor start")

    val submittedTasks = mutable.HashSet[Task]()
    while (!getStatus) {
      val haveFailed = tasks.exists(_.context.isFailed)
      val allSucceed = tasks.forall(_.context.isSucceed)
      if (haveFailed | allSucceed) {
        setStatus(true)
      } else {
        val unSubmittedTasks = tasks.diff(submittedTasks)
        val succeedTasksInSubmitted = submittedTasks.filter(_.context.isSucceed)
        val unSubmittedTasksAllUpSucceed = unSubmittedTasks.filter { task =>
          dag.getAllUpTasks(task).forall(succeedTasksInSubmitted.contains)
        }
        unSubmittedTasksAllUpSucceed.foreach { task =>
          if (!task.context.isSubmitted) {
            logger.info(s"Submit $task")

            f(task)

            task.context.markTaskSubmitted()
            logger.info(s"$task submitted")

            submittedTasks.add(task)
          }
        }
      }
    }

    val tasksFailed = tasks.filter(_.context.isFailed)
    if (tasksFailed.isEmpty) {
      logger.info("Executor succeed")
    } else {
      val v = tasksFailed.exists(_.context.getException.get.getMessage.equals("v"))
      if (v) logger.info("Executor succeed")
      else {
        logger.error(s"Executor failed")
        throw new DaphException(s"Executor failed!")
      }
    }
  }

  def clear(): Unit = {}

  def stop(): Unit = {}
}