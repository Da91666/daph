package com.dasea.daph.utils

object CommonUtil {
  def getRelations(relationType: String, sourceType: String, targetType: String, mappingFileRootPath: String): Array[Relation] = {
    val mappingFilePath = s"$mappingFileRootPath/$relationType-${sourceType}_$targetType-mapping.json"
    JsonUtil.loadFile[Array[Relation]](mappingFilePath)
  }
}

case class Relation(sourceTypes: Array[String], targetTypes: Array[String])