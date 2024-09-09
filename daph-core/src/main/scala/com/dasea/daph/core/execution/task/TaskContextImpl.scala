package com.dasea.daph.core.execution.task

import com.dasea.daph.api.exception.TaskKilledException
import com.dasea.daph.api.node.NodeDescription

import javax.annotation.concurrent.GuardedBy

class TaskContextImpl(override val nodeDescription: NodeDescription) extends TaskContext {
	private var _submitted: Boolean = false
	private var _succeed: Boolean = false
	private var _failed: Boolean = false
	private var _failure: Throwable = _
	private var _completed: Boolean = false
	@volatile private var _reasonIfKilled: Option[String] = None

	override def isSubmitted: Boolean = synchronized(_succeed)
	
	override def isSucceed: Boolean = synchronized(_succeed)
	
	override def isFailed: Boolean = synchronized(_failed)
	
	override def isCompleted: Boolean = synchronized(_completed)
	
	override def isInterrupted: Boolean = _reasonIfKilled.isDefined
	
	@GuardedBy("this")
	override def markTaskSubmitted(): Unit = synchronized {
		_submitted = true
	}
	
	@GuardedBy("this")
	override def markTaskSucceed(): Unit = synchronized {
		_succeed = true
	}
	
	@GuardedBy("this")
	override def markTaskFailed(error: Throwable): Unit = synchronized {
		_failed = true
		_failure = error
	}
	
	@GuardedBy("this")
	override def markTaskCompleted(): Unit = synchronized {
		_completed = true
	}
	
	override def markInterrupted(reason: String): Unit = {
		_reasonIfKilled = Some(reason)
	}

	private[daph] def killTaskIfInterrupted(): Unit = {
		val reason = _reasonIfKilled
		if (reason.isDefined) {
			throw new TaskKilledException(reason.get)
		}
	}

	override def getKillReason: Option[String] = _reasonIfKilled
	
	override def getException: Option[Throwable] = Option(_failure)
}
