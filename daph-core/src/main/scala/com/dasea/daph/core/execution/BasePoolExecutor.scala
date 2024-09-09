package com.dasea.daph.core.execution

import com.dasea.daph.api.executor.AbstractPoolExecutor

class BasePoolExecutor(parallelism: Int) extends AbstractPoolExecutor(parallelism) {
  override protected def nameFormat: String = "BasePoolExecutor-pool-%d"

  override def execute(): Unit = {
    using(_dag)(task => pool.submit(new Runnable {
      override def run(): Unit = task.run()
    }))
  }

  override def stop(): Unit = {
    pool.shutdownNow()
  }
}
