package com.dasea.daph.node.spark3.dataframe.batch.connector

import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.jdbc.JdbcDialect
import org.apache.spark.sql.{Column, DataFrame}

package object common {
  def outDF(df: DataFrame, config: CommonOutputConfig): Unit = {
    val format = config.format
    val cfg = config.cfg
    val saveMode = config.saveMode
    val partitionColumnNames = config.partitionColumnNames
    val sortColumnNames = config.sortColumnNames
    val bucketColumnNames = config.bucketColumnNames
    val numBuckets = config.numBuckets
    val method = config.method
    val name = config.name
    val v2 = config.v2
    val v2Cfg = config.v2Config

    if (v2) {
      val tableName = v2Cfg.tableName
      val method = v2Cfg.method
      val options = v2Cfg.options
      val provider = v2Cfg.provider
      val partitionColumns = v2Cfg.partitionColumns
      val properties = v2Cfg.properties
      val column = v2Cfg.column

      val writer = df.writeTo(tableName).options(options)
      var creator = if (!StringUtils.isEmpty(provider)) writer.using(provider) else writer
      creator = if (partitionColumns.nonEmpty) {
        val columns = partitionColumns.map(column => new Column(column))
        creator.partitionedBy(columns.head, columns.tail: _*)
      } else creator
      creator = if (properties.nonEmpty) {
        properties.foreach { case (k, v) => creator.tableProperty(k, v) }
        creator
      } else creator

      method match {
        case "create" => creator.create()
        case "replace" => creator.replace()
        case "createOrReplace" => creator.createOrReplace()
        case "append" => writer.append()
        case "overwrite" => writer.overwrite(new Column(column))
        case "overwritePartitions" => writer.overwritePartitions()
      }
    } else {
      var res = {
        if (format.contains("jdbc2")) df.write.format(format).options(cfg)
        else df.write.format(format).mode(saveMode).options(cfg)
      }
      if (partitionColumnNames.nonEmpty) res = res.partitionBy(partitionColumnNames: _*)
      if (sortColumnNames.nonEmpty) res = res.sortBy(sortColumnNames.head, sortColumnNames.tail: _*)
      if (bucketColumnNames.nonEmpty) res = res.bucketBy(numBuckets, bucketColumnNames.head, bucketColumnNames: _*)

      method match {
        case "save" => if (StringUtils.isEmpty(name)) res.save() else res.save(name)
        case "saveAsTable" => res.saveAsTable(name)
        case "insertInto" => res.insertInto(name)
        case _ => if (StringUtils.isEmpty(name)) res.save() else res.save(name)
      }
    }
  }
}

class HiveDialect extends JdbcDialect {
  override def canHandle(url: String): Boolean = {
    url.startsWith("jdbc:hive2")
  }

  override def quoteIdentifier(colName: String): String = {
    if (colName.contains(".")) {
      val colName1 = colName.substring(colName.indexOf(".") + 1)
      s"`$colName1`"
    } else {
      s"`$colName`"
    }
  }
}