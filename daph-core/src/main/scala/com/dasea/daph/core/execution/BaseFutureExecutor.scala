package com.dasea.daph.core.execution

import com.dasea.daph.api.executor.AbstractPoolExecutor

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class BaseFutureExecutor(parallelism: Int) extends AbstractPoolExecutor(parallelism) {
  override protected def nameFormat: String = "BaseFutureExecutor-pool-%d"

  implicit val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(pool)

  override def execute(): Unit = {
    using(_dag)(task => Future {
      task.run()
    })
  }

  override def stop(): Unit = {
    pool.shutdownNow()
  }
}
