package com.dasea.daph.utils

import net.sf.jsqlparser.parser.CCJSqlParserManager
import net.sf.jsqlparser.util.TablesNamesFinder

import java.io.StringReader
import scala.jdk.CollectionConverters.collectionAsScalaIterableConverter

object SQLUtil {
  def getTableNames(sql: String): List[String] = {
    val parserManager = new CCJSqlParserManager()
    val statement = parserManager.parse(new StringReader(sql))
    val tablesNamesFinder = new TablesNamesFinder()
    val tableNames = tablesNamesFinder.getTableList(statement).asScala.toList
    tableNames
  }

  def getTableName(sql: String): String = {
    val parserManager = new CCJSqlParserManager()
    val statement = parserManager.parse(new StringReader(sql))
    val tablesNamesFinder = new TablesNamesFinder()
    val tableNames = tablesNamesFinder.getTableList(statement).asScala.toList
    tableNames.head
  }

  def main(args: Array[String]): Unit = {
    val cc = "CREATE TABLE oracle_out_1 (F1 string, F2 string) WITH"
    val ee = "insert into oracle_out_1 select * from default_catalog.default_database.in_t a join tb b"

    val tt = getTableNames(cc)
    val tt2 = getTableNames(ee)

    println(tt)
    println(tt2)
  }
}