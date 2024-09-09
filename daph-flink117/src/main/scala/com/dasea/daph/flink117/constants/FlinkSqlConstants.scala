package com.dasea.daph.flink117.constants

object FlinkSqlConstants {
  val DIALECT_DEFAULT = "default"
  val DIALECT_HIVE = "hive"

  val NAME_DEFAULT_CATALOG = "default_catalog"
  val NAME_DEFAULT_DATABASE = "default_database"

  /**
   * oracle exists
   */
  val ORACLE_TABLE_EXISTS = "SELECT 1 FROM all_tables WHERE table_name = ? AND OWNER = ?"

  val SQLSERVER_TABLE_EXISTS = "SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ? AND TABLE_SCHEMA = ?"

  /**
   * DAPH dbtype
   */
  val DAPH_DB_TYPE_KEY = "daph.dbType"

  /**
   * job json schema
   */
  val DAPH_SCHEMA_NAME_KEY = "daph.schemaName"

  /**
   * job json tablename
   */
  val DAPH_TABLE_NAME_KEY = "daph.tableName"

}
