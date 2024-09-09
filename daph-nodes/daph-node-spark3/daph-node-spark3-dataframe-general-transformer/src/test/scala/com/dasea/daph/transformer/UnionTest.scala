package com.dasea.daph.transformer

import com.dasea.daph.api.node.{Node, NodeDescription, SimpleNodeDescription}
import com.dasea.daph.node.spark3.dataframe.general.transformer.union.{Union, UnionConfig, UnionType}
import com.dasea.daph.utils.JsonUtil
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.junit.Test

import java.io.File
import java.nio.file.Files

class UnionTest {
  val rootPath = s"${System.getProperty("user.dir")}${File.separator}src${File.separator}test${File.separator}resources${File.separator}testdata${File.separator}"
  val targetPath = s"${System.getProperty("user.dir")}${File.separator}target${File.separator}transformer${File.separator}union"

  @Test
  def UnionTest(): Unit = {
    val union = new Union()
    val sc = SparkSession.builder().master("local[1]").getOrCreate()
    val dataFrame = TestUtils.getTestDataframe(sc)
    val dataFrame1 = dataFrame
    val dataFrame2 = dataFrame
    val dataFrame3 = dataFrame
    val filePath = new File(s"${rootPath}transformer${File.separator}union${File.separator}unionJob.json").toPath
    val params=Map[String, DataFrame]("ds1"->dataFrame1,"ds2"->dataFrame2,"ds3"->dataFrame3)
    val unionConfig = UnionConfig(UnionType.POSITION.toString, false)
   val _config = classOf[Node].getDeclaredField("_config")
    _config.setAccessible(true)
    _config.set(union, unionConfig)
    val _nodeDescription = classOf[Node].getDeclaredField("_nodeDescription")
    _nodeDescription.setAccessible(true)
    val res = new String(Files.readAllBytes(filePath))
    val simpleNodeDescription = JsonUtil.read(res, classOf[SimpleNodeDescription])
    val nodeDescription = NodeDescription(simpleNodeDescription.name, simpleNodeDescription.name, "",classOf[Union].toString, simpleNodeDescription.config, simpleNodeDescription.inLines, simpleNodeDescription.outLines, simpleNodeDescription.extraOptions)
    _nodeDescription.set(union, nodeDescription)
    val transform=classOf[Union].getMethod("transform",classOf[Map[String, DataFrame]])
    transform.setAccessible(true)
    val result=transform.invoke(union,params).asInstanceOf[DataFrame]
    result.show()

  }


}
