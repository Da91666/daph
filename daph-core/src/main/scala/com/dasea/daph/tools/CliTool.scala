package com.dasea.daph.tools

import com.sampullara.cli.Args

object CliTool {
  def extractInput(args: Array[String]): CliInfo = {
    Args.parseOrExit(classOf[CliLoader], args)
    val jsonStorageConfig = map(CliLoader.daphJsonStorageConfig)
    val daphUserDefinedParameters = map(CliLoader.daphUserDefinedParameters)

    CliInfo(
      CliLoader.daphJsonStorageType,
      jsonStorageConfig,
      CliLoader.daphJobJsonPath,
      CliLoader.daphComputerJsonPath,
      CliLoader.daphStorageJsonPath,
      CliLoader.daphExecutorJsonPath,
      daphUserDefinedParameters
    )
  }

  def map(arr: Array[String]): Map[String,String] = {
    arr.map(x => x.split("=", 2))
      .filter(_.length == 2)
      .map(x => x(0).trim -> x(1).trim)
      .toMap
  }
}

case class CliInfo(
  daphJsonStorageType: String,
  daphJsonStorageConfig: Map[String,String],
  daphJobJsonPath: String,
  daphComputerJsonPath: String,
  daphStorageJsonPath: String,
  daphExecutorJsonPath: String,
  daphUserDefinedParameters: Map[String,String]
)
