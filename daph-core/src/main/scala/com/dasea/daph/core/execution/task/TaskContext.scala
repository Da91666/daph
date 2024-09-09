package com.dasea.daph.core.execution.task

import com.dasea.daph.api.node.NodeDescription

object TaskContext {
  private val taskContext: ThreadLocal[TaskContext] = new ThreadLocal[TaskContext]

  protected[daph] def setTaskContext(tc: TaskContext): Unit = taskContext.set(tc)

  protected[daph] def get(): TaskContext = taskContext.get

  protected[daph] def unset(): Unit = taskContext.remove()
}

abstract class TaskContext {
  def nodeDescription: NodeDescription

  def isSubmitted: Boolean

  def isSucceed: Boolean

  def isFailed: Boolean

  def isCompleted: Boolean

  def isInterrupted: Boolean

  def markTaskSubmitted(): Unit

  def markTaskSucceed(): Unit

  def markTaskFailed(error: Throwable): Unit

  def markTaskCompleted(): Unit

  def markInterrupted(reason: String): Unit

  private[daph] def killTaskIfInterrupted(): Unit

  def getKillReason: Option[String]

  def getException: Option[Throwable]
}