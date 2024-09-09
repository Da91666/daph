package com.dasea.daph.flink117.enums

object CatalogDatabaseType extends Enumeration {
  val HIVE = Value(name = "hive")
  val JDBC = Value(name = "jdbc")
  val HUDI = Value(name = "hudi")
  val ICEBERG = Value(name = "iceberg")
}
