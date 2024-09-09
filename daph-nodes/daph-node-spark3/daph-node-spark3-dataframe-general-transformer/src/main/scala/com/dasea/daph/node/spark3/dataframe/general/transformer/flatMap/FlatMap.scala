package com.dasea.daph.node.spark3.dataframe.general.transformer.flatMap

import com.dasea.daph.spark3.api.node.dataframe.transformer.DataFrameSingleTransformer
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.explode

/**
 * 对指定列进行拆分，一行变多行
 * id items
 * a a,b,c
 *
 * ===>
 *
 * id items
 * a a
 * a b
 * a c
 */
class FlatMap extends DataFrameSingleTransformer {
  override protected def transform(ds: DataFrame): DataFrame = {
    val config = nodeConfig.asInstanceOf[FlatMapConfig]
    val showColumnsArray = config.resultColumns.map(column => {
      ds.col(column)
    }) ++ config.expoldeConfig.map(expoldConfig => {
      explode(org.apache.spark.sql.functions.split(ds.col(expoldConfig.colName), expoldConfig.splitKey)).as(expoldConfig.newName)
    })
    // 推断出新的schema,然后进行分割
    ds.select(showColumnsArray.toList: _*)
  }
  override def getNodeConfigClass: Class[FlatMapConfig] = classOf[FlatMapConfig]
}
