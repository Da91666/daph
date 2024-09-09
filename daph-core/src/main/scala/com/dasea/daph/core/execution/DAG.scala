package com.dasea.daph.core.execution

import com.dasea.daph.core.execution.task.Task
import com.dasea.daph.utils.DAGUtil.findChildren
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.scala.Logging

class DAG(tasks: Set[Task]) extends Logging {
  private val taskToUpTasks: Map[Task, Set[Task]] = tasks.map { task =>
    val inputLines = task.getNodeDescription.inLines.filter(StringUtils.isNotBlank).toSet
    val upTasks = tasks.filter { task =>
      val outputLines = task.getNodeDescription.outLines.filter(StringUtils.isNotBlank).toSet
      (inputLines & outputLines).nonEmpty
    }
    task -> upTasks
  }.toMap
  private val taskToDownTasks = tasks.map { task =>
    val outputLines = task.getNodeDescription.outLines.filter(StringUtils.isNotBlank).toSet
    val downTasks = tasks.filter { task =>
      val inputLines = task.getNodeDescription.inLines.filter(StringUtils.isNotBlank).toSet
      (inputLines & outputLines).nonEmpty
    }
    task -> downTasks
  }.toMap

  def getTasks: Set[Task] = tasks

  def getAllUpTasks(task: Task): Set[Task] = findChildren(task, taskToUpTasks)

  def getAllDownTasks(task: Task): Set[Task] = findChildren(task, taskToDownTasks)
}
