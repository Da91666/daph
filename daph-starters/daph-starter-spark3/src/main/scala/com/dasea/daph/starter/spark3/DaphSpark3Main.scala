package com.dasea.daph.starter.spark3

import com.dasea.daph.api.GlobalContext
import com.dasea.daph.api.config.option.GlobalConfigOptions.DAPH_HOME
import com.dasea.daph.core.JVMStorage
import com.dasea.daph.core.execution.ExecutorConfig
import com.dasea.daph.spark3.computer.SparkComputer
import com.dasea.daph.tools.jsonloader.{DJson, DefaultJsonLoaderFactory}
import com.dasea.daph.tools.{CliTool, DefaultExecutorFactory}
import com.dasea.daph.utils.{JsonUtil, LogUtil}
import org.apache.commons.lang3.StringUtils

import scala.language.postfixOps

object DaphSpark3Main {
  def main(args: Array[String]): Unit = {
    val cli = CliTool.extractInput(args)

    val logXml = cli.daphUserDefinedParameters.getOrElse("logXml", DAPH_HOME.default + "/conf/log/log4j2-spark3.xml")
    LogUtil.setLogXml(logXml)

    val jsonPath = DJson(
      cli.daphJobJsonPath,
      cli.daphComputerJsonPath,
      cli.daphStorageJsonPath,
      cli.daphExecutorJsonPath
    )
    val jsonResult = if (StringUtils.isEmpty(cli.daphJsonStorageType)) {
      DefaultJsonLoaderFactory.getJsonLoader().loadAll(jsonPath)
    } else {
      DefaultJsonLoaderFactory.getJsonLoader(cli.daphJsonStorageType, cli.daphJsonStorageConfig)
        .loadAll(jsonPath)
    }
    val jobJson = jsonResult.job
    val computerJson = jsonResult.computer
    val storageJson = jsonResult.storage
    val executorJson = jsonResult.executor

    // 创建Computer
    val computer = new SparkComputer(computerJson)

    // 创建Storage
    val storage = new JVMStorage

    // 创建Executor
    val executor = if (!StringUtils.isEmpty(executorJson)) {
      val exeConfig = JsonUtil.read(executorJson, classOf[ExecutorConfig])
      DefaultExecutorFactory.getExecutor(exeConfig)
    } else null

    val gc =
      if (executor != null) new GlobalContext(jobJson, computer, storage, executor)
      else new GlobalContext(jobJson, computer, storage)
    gc.execute()
    gc.stop()
  }
}
