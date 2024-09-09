package com.dasea.daph.tools.jsonloader

import com.dasea.daph.enums.JsonStorageType

object DefaultJsonLoaderFactory {
  def getJsonLoader(jsType: String = "local-fs", config: Map[String,String] = Map.empty): JsonLoader = {
    JsonStorageType.withName(jsType) match {
      case JsonStorageType.STRING => new StringJsonLoader
      case JsonStorageType.LOCAL_FS => new LocalFSJsonLoader
      case JsonStorageType.MYSQL => new MySQLJsonLoader(config)
      case JsonStorageType.MINIO => new MinIOJsonLoader
    }
  }
}
