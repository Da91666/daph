package com.dasea.daph.transformer

import org.apache.spark.sql
import org.apache.spark.sql.types.DataTypes
import org.apache.spark.sql.{Row, SparkSession}

import java.util
import scala.collection.mutable.ArrayBuffer

object TestUtils {
  val sc = SparkSession.builder().master("local[1]").getOrCreate()
  def getTestDataframe(sc: SparkSession=TestUtils.sc): sql.DataFrame = {
    val array = new ArrayBuffer[Row]()
    array.append(Row(1, "张三丰", "真武七截阵,绕指剑法,太极拳,太极剑"))
    array.append(Row(2, "张翠山", "双手互搏,过沟"))
    array.append(Row(3, "张无忌", "九阳神功,乾坤大挪移,太极剑"))
    val structType = DataTypes.createStructType(
      util.Arrays.asList(
        DataTypes.createStructField("id", DataTypes.IntegerType, true),
        DataTypes.createStructField("name", DataTypes.StringType, true),
        DataTypes.createStructField("ability", DataTypes.StringType, true)
      )
    )
    //创建DataFrame 数组
    import scala.collection.JavaConverters._
    val dataframe = sc.createDataFrame(array.toList.asJava, structType)
    dataframe
  }

  def getOrderDataframe(sc: SparkSession = TestUtils.sc): sql.DataFrame = {
    val array = new ArrayBuffer[Row]()
    array.append(Row(1, "1", 20.0))
    array.append(Row(2, "1", 30.36))
    array.append(Row(3, "2", 7.0))
    array.append(Row(4, "4", 5.0))
    val structType = DataTypes.createStructType(
      util.Arrays.asList(
        DataTypes.createStructField("id", DataTypes.IntegerType, true),
        DataTypes.createStructField("name", DataTypes.StringType, true),
        DataTypes.createStructField("total_price", DataTypes.DoubleType, true)
      )
    )
    //创建DataFrame 数组
    import scala.collection.JavaConverters._

    val dataframe = sc.createDataFrame(array.toList.asJava, structType)
    dataframe
  }
}
