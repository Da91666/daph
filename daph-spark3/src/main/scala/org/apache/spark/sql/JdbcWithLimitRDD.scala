package org.apache.spark.sql

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.execution.datasources.jdbc.{JDBCOptions, JDBCPartition, JdbcUtils}
import org.apache.spark.sql.jdbc.JdbcDialects
import org.apache.spark.sql.sources.Filter
import org.apache.spark.sql.types.StructType
import org.apache.spark.util.CompletionIterator
import org.apache.spark.{InterruptibleIterator, Partition, SparkContext, TaskContext}

import java.sql.{Connection, PreparedStatement, ResultSet}
import scala.util.control.NonFatal

class JdbcWithLimitRDD(
	sc: SparkContext,
	getConnection: Int => Connection,
	schema: StructType,
	columns: Array[String],
	filters: Array[Filter],
	partitions: Array[Partition],
	url: String,
	options: JDBCOptions,
	limit: Int)
	extends RDD[InternalRow](sc, Nil) {
	
	/**
	 * Retrieve the list of partitions corresponding to this RDD.
	 */
	override def getPartitions: Array[Partition] = partitions
	
	/**
	 * `columns`, but as a String suitable for injection into a SQL query.
	 */
	private val columnList: String = {
		val sb = new StringBuilder()
		columns.foreach(x => sb.append(",").append(x))
		if (sb.isEmpty) "1" else sb.substring(1)
	}

	/**
	 * Runs the SQL query against the JDBC driver.
	 *
	 */
	override def compute(thePart: Partition, context: TaskContext): Iterator[InternalRow] = {
		var closed = false
		var rs: ResultSet = null
		var stmt: PreparedStatement = null
		var conn: Connection = null
		
		def close() {
			if (closed) return
			try {
				if (null != rs) {
					rs.close()
				}
			} catch {
				case e: Exception => logWarning("Exception closing resultset", e)
			}
			try {
				if (null != stmt) {
					stmt.close()
				}
			} catch {
				case e: Exception => logWarning("Exception closing statement", e)
			}
			try {
				if (null != conn) {
					if (!conn.isClosed && !conn.getAutoCommit) {
						try {
							conn.commit()
						} catch {
							case NonFatal(e) => logWarning("Exception committing transaction", e)
						}
					}
					conn.close()
				}
				logInfo("closed connection")
			} catch {
				case e: Exception => logWarning("Exception closing connection", e)
			}
			closed = true
		}
		
		context.addTaskCompletionListener[Unit] { _ => close() }
		
		val inputMetrics = context.taskMetrics().inputMetrics
		val part = thePart.asInstanceOf[JDBCPartition]
		conn = getConnection(1)
		val dialect = JdbcDialects.get(url)
		import scala.collection.JavaConverters._
		dialect.beforeFetch(conn, options.asProperties.asScala.toMap)
		
		// This executes a generic SQL statement (or PL/SQL block) before reading
		// the string/query via JDBC. Use this feature to initialize the database
		// session environment, e.g. for optimizations and/or troubleshooting.
		options.sessionInitStatement match {
			case Some(sql) =>
				val statement = conn.prepareStatement(sql)
				logInfo(s"Executing sessionInitStatement: $sql")
				try {
					statement.setQueryTimeout(options.queryTimeout)
					statement.execute()
				} finally {
					statement.close()
				}
			case None =>
		}
		
		// H2's JDBC driver does not support the setSchema() method.  We pass a
		// fully-qualified string name in the SELECT statement.  I don't know how to
		// talk about a string in a completely portable way.
		

		val limitClause = JdbcWithLimitRDD.getLimit(limit, url)
		val sqlText = s"SELECT $columnList FROM ${options.tableOrQuery} $limitClause"
		logInfo(s"[${url}]使用JDBCLimit进行数据预览:${sqlText}")
		stmt = conn.prepareStatement(sqlText,
			ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
		stmt.setFetchSize(options.fetchSize)
		stmt.setQueryTimeout(options.queryTimeout)
		rs = stmt.executeQuery()
		val rowsIterator = JdbcUtils.resultSetToSparkInternalRows(rs, dialect, schema, inputMetrics)
		
		CompletionIterator[InternalRow, Iterator[InternalRow]](
			new InterruptibleIterator(context, rowsIterator), close())
	}
}

object JdbcWithLimitRDD {
	/**
	 * 支持的Limit语法的Jdbc数据库，会匹配{{{jdbc:<format>:}}}开头的数据库
	 */
	private val limitSupport = List(
		"mysql",
		"postgresql",
		"clickhouse"
	)
	
	private def defaultLimit(limit: Int) = {
		s"LIMIT $limit"
	}
	
	def getLimit(limit: Int, url: String): String = {
		if (limit > 0) {
			val p = limitSupport.find { protocol =>
				url.startsWith(s"jdbc:$protocol:")
			}
			if (p.isDefined) {
				defaultLimit(limit)
			} else {
				""
			}
		} else {
			""
		}
	}
}