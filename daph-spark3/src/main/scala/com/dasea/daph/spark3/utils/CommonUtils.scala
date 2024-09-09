package com.dasea.daph.spark3.utils

import com.dasea.daph.utils.SQLUtil
import org.apache.commons.lang3.StringUtils
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

object CommonUtils {
  def createSpark(conf: SparkConf): SparkSession = {
    SparkSession.builder.config(conf).getOrCreate()
  }

  def createSpark(map: Map[String, String]): SparkSession = {
    val conf = new SparkConf
    map.foreach { case (key, value) => conf.set(key, value) }
    SparkSession.builder.config(conf).getOrCreate()
  }


  def createTestSpark(map: Map[String, String] = Map.empty): SparkSession = {
    val conf = new SparkConf
    Map(
      ("spark.master", "local[*]"),
      ("spark.app.name", "Spark Example"),
      ("spark.sql.warehouse.dir", "/spark-sql-warehouse"),
      ("spark.ui.enabled", "false"),
      ("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    ).foreach { case (key, value) => conf.set(key, value) }
    map.foreach { case (key, value) => conf.set(key, value) }
    SparkSession.builder.config(conf).getOrCreate()
  }

  def executeQuerySQL(sql: String, df: DataFrame, spark: SparkSession): DataFrame = {
    if (StringUtils.isEmpty(sql)) df
    else {
      val tn = SQLUtil.getTableNames(sql).head
      df.createOrReplaceTempView(tn)
      spark.sql(sql)
    }
  }
}
