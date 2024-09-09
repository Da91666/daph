package com.dasea.daph.transformer

import com.dasea.daph.api.node.{Node, SimpleNodeDescription}
import com.dasea.daph.node.spark3.dataframe.general.transformer.groupbykey.{GroupAggConfig, GroupByKey}
import com.dasea.daph.utils.JsonUtil

import org.apache.spark.sql.DataFrame
import org.json4s.jackson.JsonMethods
import org.junit.Test

import java.io.File
import java.nio.file.Files

class GroupByTest {
  val rootPath = s"${System.getProperty("user.dir")}${File.separator}src${File.separator}test${File.separator}resources${File.separator}testdata${File.separator}"
  val targetPath = s"${System.getProperty("user.dir")}${File.separator}target${File.separator}transformer${File.separator}union"

  @Test
  def UnionTest(): Unit = {
    val groupBykey = new GroupByKey()
    val dataFrame = TestUtils.getOrderDataframe()
    val filePath = new File(s"${rootPath}transformer${File.separator}union${File.separator}groupByKeyConfig.json").toPath
    val params=dataFrame
    val _nodeDescription = classOf[Node].getDeclaredField("_nodeDescription")
    _nodeDescription.setAccessible(true)
    val res = new String(Files.readAllBytes(filePath))
    val simpleNodeDescription = JsonUtil.read(res, classOf[SimpleNodeDescription])
    val flatMapConfig= JsonMethods.mapper.convertValue(
      simpleNodeDescription.config,
      classOf[GroupAggConfig]
    )
    val _config = classOf[Node].getDeclaredField("_config")
    _config.setAccessible(true)
    _config.set(groupBykey, flatMapConfig)
//    val nodeDescription = NodeDescription(simpleNodeDescription.name, simpleNodeDescription.name, "", simpleNodeDescription.config, simpleNodeDescription.inLines, simpleNodeDescription.outLines, simpleNodeDescription.extraOptions)
//    _nodeDescription.set(flatMap, nodeDescription)
    val transform=classOf[GroupByKey].getMethod("transform",classOf[DataFrame])
    transform.setAccessible(true)
    val result=transform.invoke(groupBykey,params).asInstanceOf[DataFrame]
    result.show()

  }


}
