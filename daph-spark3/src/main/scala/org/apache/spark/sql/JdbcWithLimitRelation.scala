package org.apache.spark.sql

import org.apache.spark.Partition
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.sql.jdbc.JdbcDialects
import org.apache.spark.sql.sources.{BaseRelation, Filter, InsertableRelation, PrunedFilteredScan}
import org.apache.spark.sql.types.StructType

case class JdbcWithLimitRelation(
  override val schema: StructType,
  parts: Array[Partition],
  jdbcOptions: JDBCOptions,
  limit: Int = 1000
)(@transient val sparkSession: SparkSession)
  extends BaseRelation
    with PrunedFilteredScan
    with InsertableRelation {

  override def sqlContext: SQLContext = sparkSession.sqlContext

  override val needConversion: Boolean = false

  override def buildScan(requiredColumns: Array[String], filters: Array[Filter]): RDD[Row] = {
    // Rely on a type erasure hack to pass RDD[InternalRow] back as RDD[Row]
    val options = jdbcOptions
    val sc = sparkSession.sparkContext
    val url = options.url
    val dialect = JdbcDialects.get(url)
    val quotedColumns = requiredColumns.map(colName => dialect.quoteIdentifier(colName))
    new JdbcWithLimitRDD(
      sc,
      dialect.createConnectionFactory(options),
      pruneSchema(schema, requiredColumns),
      quotedColumns,
      filters,
      parts,
      url,
      options,
      limit).asInstanceOf[RDD[Row]]
  }

  override def insert(data: DataFrame, overwrite: Boolean): Unit = {
    data.write
      .mode(if (overwrite) SaveMode.Overwrite else SaveMode.Append)
      .jdbc(jdbcOptions.url, jdbcOptions.tableOrQuery, jdbcOptions.asProperties)
  }

  override def toString: String = {
    val partitioningInfo = if (parts.nonEmpty) s" [numPartitions=${parts.length}]" else ""
    // credentials should not be included in the plan output, string information is sufficient.
    s"JdbcWithLimitRelation(${jdbcOptions.tableOrQuery})" + partitioningInfo
  }

  private def pruneSchema(schema: StructType, columns: Array[String]): StructType = {
    val fieldMap = Map(schema.fields.map(x => x.name -> x): _*)
    new StructType(columns.map(name => fieldMap(name)))
  }
}