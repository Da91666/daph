package com.dasea.daph.transformer

import com.dasea.daph.api.node.{Node, SimpleNodeDescription}
import com.dasea.daph.node.spark3.dataframe.general.transformer.flatMap.{FlatMap, FlatMapConfig}
import com.dasea.daph.utils.JsonUtil
import org.apache.spark.sql.{DataFrame}
import org.json4s.jackson.JsonMethods
import org.junit.Test

import java.io.File
import java.nio.file.Files

class FlatMapTest {
  val rootPath = s"${System.getProperty("user.dir")}${File.separator}src${File.separator}test${File.separator}resources${File.separator}testdata${File.separator}"
  val targetPath = s"${System.getProperty("user.dir")}${File.separator}target${File.separator}transformer${File.separator}union"

  @Test
  def UnionTest(): Unit = {
    val flatMap = new FlatMap()

    val dataFrame = TestUtils.getTestDataframe()
    val filePath = new File(s"${rootPath}transformer${File.separator}union${File.separator}faltMapConfig.json").toPath
    val params=dataFrame
    val _nodeDescription = classOf[Node].getDeclaredField("_nodeDescription")
    _nodeDescription.setAccessible(true)
    val res = new String(Files.readAllBytes(filePath))
    val simpleNodeDescription = JsonUtil.read(res, classOf[SimpleNodeDescription])
    val flatMapConfig= JsonMethods.mapper.convertValue(
      simpleNodeDescription.config,
      classOf[FlatMapConfig]
    )

    val _config = classOf[FlatMap].getDeclaredField("nodeConfig")
    _config.setAccessible(true)
    _config.set(flatMap, flatMapConfig)
//    val nodeDescription = NodeDescription(simpleNodeDescription.name, simpleNodeDescription.name, "", simpleNodeDescription.config, simpleNodeDescription.inLines, simpleNodeDescription.outLines, simpleNodeDescription.extraOptions)
//    _nodeDescription.set(flatMap, nodeDescription)
    val transform=classOf[FlatMap].getMethod("transform",classOf[DataFrame])
    transform.setAccessible(true)
    val result=transform.invoke(flatMap,params).asInstanceOf[DataFrame]
    result.show()

  }


}
