package com.dasea.daph.enums

object JsonStorageType extends Enumeration {
  val STRING = Value(name = "string")
  val LOCAL_FS = Value(name = "local-fs")
  val MYSQL = Value(name = "mysql")
  val MINIO = Value(name = "minio")
}
