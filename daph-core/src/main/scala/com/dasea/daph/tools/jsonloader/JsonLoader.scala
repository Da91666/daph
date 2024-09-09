package com.dasea.daph.tools.jsonloader

trait JsonLoader {
  def load(path: String): String

  def loadAll(jsonPath: DJson): DJson

  def reload(path: String): String = load(path)
}

case class DJson(
  job: String,
  computer: String = "",
  storage: String = "",
  executor: String = ""
)
