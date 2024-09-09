package com.dasea.daph.starter.flink117

import com.dasea.daph.api.GlobalContext
import com.dasea.daph.api.config.option.GlobalConfigOptions.DAPH_HOME
import com.dasea.daph.core.JVMStorage
import com.dasea.daph.core.execution.ExecutorConfig
import com.dasea.daph.flink117.computer.FlinkStreamComputer
import com.dasea.daph.tools.jsonloader.{DJson, DefaultJsonLoaderFactory}
import com.dasea.daph.tools.{CliTool, DefaultExecutorFactory}
import com.dasea.daph.utils.{JsonUtil, LogUtil}
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.scala.Logging

import java.io.File

object DaphFlink117Main extends Logging {
  def main(args: Array[String]): Unit = {
    val cli = CliTool.extractInput(args)
    val home = cli.daphUserDefinedParameters.getOrElse("home", DAPH_HOME.default)

    val logXml = cli.daphUserDefinedParameters.getOrElse("logXml", home + "/conf/log/log4j2-flink117.xml")
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
    val executorJson = jsonResult.executor

    // 创建Computer
    val computer = new FlinkStreamComputer(computerJson)

    // 创建Storage
    val storage = new JVMStorage

    // 创建Executor
    val executor = if (!StringUtils.isEmpty(executorJson)) {
      val exeConfig = JsonUtil.read(executorJson, classOf[ExecutorConfig])
      DefaultExecutorFactory.getExecutor(exeConfig)
    } else {
      val exeJsonPath = home + "/conf/executor.json"
      val exists = new File(exeJsonPath).exists()
      val executorConfig = {
        if (exists) JsonUtil.loadFile[ExecutorConfig](exeJsonPath)
        else null
      }
      DefaultExecutorFactory.getExecutor(executorConfig)
    }

    val gc = new GlobalContext(jobJson, computer, storage, executor)
    gc.execute()
    gc.stop()
  }
}
