package org.apache.spark.sql

import org.apache.spark.internal.Logging
import org.apache.spark.sql.catalyst.catalog.HiveTableRelation
import org.apache.spark.sql.catalyst.plans.logical.{LeafNode, LocalRelation, LogicalPlan, SerializeFromObject}
import org.apache.spark.sql.execution.datasources.LogicalRelation
import org.apache.spark.sql.execution.datasources.jdbc.JDBCRelation

object LimitLogicalPlan extends Logging {
  def limit(df: DataFrame, limit: Int): DataFrame = {
    val spark = df.sparkSession
    val logicalPlan = df.logicalPlan
    val limitedPlan = limitPlan(logicalPlan, limit, spark)
    Dataset.ofRows(spark, limitedPlan)
  }

  private def limitPlan(plan: LogicalPlan, limit: Int, sparkSession: SparkSession): LogicalPlan = {
    logInfo(s"优化执行计划，限制读取数据条数为[${limit}]")

    val res = if (plan.isInstanceOf[LocalRelation]) {
      plan
    } else {
      plan transformDown { // 全局优化规则
        case serializable: SerializeFromObject => {
          limitSubLogicPlan(serializable, limit, sparkSession)
        }
        case hiveTableRelation: HiveTableRelation => {
          limitSubLogicPlan(hiveTableRelation, limit, sparkSession)
        }
        case LogicalRelation(relation, output, catalogTable, false) => {
          val newRelation = relation match {
            //jdbc下推limit，在JdbcWithLimitRelation配置支持limit数据库
            case JDBCRelation(schema, parts, options) => JdbcWithLimitRelation(schema, parts, options, limit)(sparkSession)
            case other => other
          }
          val newRel = LogicalRelation(newRelation, output, catalogTable, isStreaming = false)

          limitSubLogicPlan(newRel, limit, sparkSession)
        }
        case leafNode: LeafNode => {
          //若是没有子节点的非实时作业，则认为是输入源，因此也需要优化执行计划
          if (!leafNode.isStreaming) {
            limitSubLogicPlan(leafNode, limit, sparkSession)
          } else {
            logWarning("实时数据流，无法使用优化执行计划功能")
            leafNode
          }
        }
      }
    }

    logInfo(s"优化执行计划，优化后的执行计划为\n${res.treeString}")
    res
  }

  private def limitSubLogicPlan(plan: LogicalPlan, limit: Int, sparkSession: SparkSession): LogicalPlan = {
    if (plan.isInstanceOf[LocalRelation]) {
      plan
    } else { // 原子优化规则
      val data = Dataset.ofRows(sparkSession, plan).take(limit)
      LocalRelation.fromExternalRows(plan.output, data)
    }
  }
}
