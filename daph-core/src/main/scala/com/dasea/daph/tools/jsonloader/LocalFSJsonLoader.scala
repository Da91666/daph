package com.dasea.daph.tools.jsonloader

import org.apache.commons.lang3.StringUtils

import java.io.File
import java.nio.file.Files

class LocalFSJsonLoader extends JsonLoader {
  override def load(path: String): String = getJson(path)

  override def loadAll(jsonPath: DJson): DJson = DJson(
    getJson(jsonPath.job),
    if (!StringUtils.isEmpty(jsonPath.computer)) getJson(jsonPath.computer) else "",
    if (!StringUtils.isEmpty(jsonPath.storage)) getJson(jsonPath.storage) else "",
    if (!StringUtils.isEmpty(jsonPath.executor)) getJson(jsonPath.executor) else ""
  )

  private def getJson(path: String): String = {
    val filePath = new File(path).toPath
    val res = new String(Files.readAllBytes(filePath))
    res
  }
}
