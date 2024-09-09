package com.dasea.daph.tools

import com.dasea.daph.api.executor.Executor
import com.dasea.daph.core.execution.{BaseFutureExecutor, BasePoolExecutor, ExecutorConfig}

object DefaultExecutorFactory {
  def getExecutor(config: ExecutorConfig): Executor = {
    if (config == null) new BasePoolExecutor(1)
    else config.exeType match {
      case "base-pool" => new BasePoolExecutor(config.options("parallelism").toInt)
      case "base-future" => new BaseFutureExecutor(config.options("parallelism").toInt)
      case _ => new BasePoolExecutor(config.options("parallelism").toInt)
    }
  }
}
