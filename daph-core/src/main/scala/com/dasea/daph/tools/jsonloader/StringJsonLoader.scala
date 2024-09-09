package com.dasea.daph.tools.jsonloader


class StringJsonLoader extends JsonLoader {
  override def load(path: String): String = {
    path
  }

  override def loadAll(dJson: DJson): DJson = {
    dJson
  }
}