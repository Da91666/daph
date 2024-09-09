package com.dasea.daph.api.executor

import com.google.common.util.concurrent.ThreadFactoryBuilder

import java.util.concurrent.{LinkedBlockingQueue, ThreadPoolExecutor, TimeUnit}

abstract class AbstractPoolExecutor(parallelism: Int) extends Executor {
  protected final val pool = new ThreadPoolExecutor(
    parallelism, parallelism,
    60L, TimeUnit.SECONDS,
    new LinkedBlockingQueue[Runnable](),
    new ThreadFactoryBuilder().setNameFormat(nameFormat).build()
  )

  protected def nameFormat: String = "AbstractPoolExecutor-%d"
}
