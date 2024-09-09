package com.dasea.daph.flink117

import com.dasea.daph.api.GlobalContext
import com.dasea.daph.flink117.computer.FlinkStreamComputer
import com.dasea.daph.tools.jsonloader.{DJson, DefaultJsonLoaderFactory}
import com.dasea.daph.utils.LogUtil

trait BaseTest {
  final val rootPath: String = System.getProperty("user.dir") + "/src/test/resources/"

  final def executeJob(jobJsonPath: String,
                       isStreaming: Int = 0,
                       computerJsonPath: String = rootPath + "/base/computer.json"): Unit = {
    LogUtil.setLogXml(rootPath + "log4j2-test.xml")
    val rComputerJsonPath: String =
      if (isStreaming > 0) rootPath + "/base/computer-stream.json"
      else computerJsonPath
    val jsonPath = DJson(
      jobJsonPath,
      rComputerJsonPath
    )
    val jsonResult = DefaultJsonLoaderFactory.getJsonLoader("local-fs").loadAll(jsonPath)
    val jobJson = jsonResult.job
    val computerJson = jsonResult.computer

    val computer = new FlinkStreamComputer(computerJson)

    val gc = new GlobalContext(jobJson, computer)
    gc.execute()
    gc.stop()
  }
}
