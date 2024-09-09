package com.dasea.daph.spark3

import com.dasea.daph.api.GlobalContext
import com.dasea.daph.core.JVMStorage
import com.dasea.daph.core.execution.ExecutorConfig
import com.dasea.daph.spark3.computer.SparkComputer
import com.dasea.daph.tools.DefaultExecutorFactory
import com.dasea.daph.tools.jsonloader.{DJson, DefaultJsonLoaderFactory}
import com.dasea.daph.utils.JsonUtil

import scala.language.postfixOps

trait BaseTest {
  final val rootPath: String = System.getProperty("user.dir") + "/src/test/resources/"

  final def executeJob(jobJsonPath: String,
                       computerJsonPath: String = rootPath + "/base/computer.json",
                       storageJsonPath: String = "",
                       executorJsonPath: String = rootPath + "/base/executor.json"): Unit = {
    val jsonPath = DJson(
      jobJsonPath,
      computerJsonPath,
      storageJsonPath,
      executorJsonPath
    )
    val jsonResult = DefaultJsonLoaderFactory.getJsonLoader("local-fs").loadAll(jsonPath)
    val jobJson = jsonResult.job
    val computerJson = jsonResult.computer
    val storageJson = jsonResult.storage
    val executorJson = jsonResult.executor

    val computer = new SparkComputer(computerJson)

    val storage = new JVMStorage

    val exeConfig = JsonUtil.read(executorJson, classOf[ExecutorConfig])
    val executor = DefaultExecutorFactory.getExecutor(exeConfig)

    val gc = new GlobalContext(jobJson, computer, storage, executor)
    gc.execute()
    gc.stop()
  }
}