package com.dasea.daph.core.execution.task

import com.dasea.daph.api.GlobalContext
import com.dasea.daph.api.node.NodeDescription

case class RunTask(nodeDescription: NodeDescription,
                   dc: GlobalContext) extends Task(nodeDescription, dc) {
  override def runTask(context: TaskContext): Unit = {
    node.runNode()
  }
}
