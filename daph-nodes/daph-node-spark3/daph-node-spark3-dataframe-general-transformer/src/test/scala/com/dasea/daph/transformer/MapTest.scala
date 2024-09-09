package com.dasea.daph.transformer

import com.dasea.daph.api.config.NodeConfig
import com.dasea.daph.api.node.{Node, SimpleNodeDescription}
import com.dasea.daph.node.spark3.dataframe.general.transformer.map.{Map, MapConfig}
import com.dasea.daph.utils.JsonUtil
import org.apache.spark.sql.DataFrame
import org.json4s.jackson.JsonMethods
import org.junit.Test

import java.io.File
import java.nio.file.Files

class MapTest {
  val rootPath = s"${System.getProperty("user.dir")}${File.separator}src${File.separator}test${File.separator}resources${File.separator}testdata${File.separator}"
  @Test
  def UnionTest(): Unit = {
    val map =new Map()
    val dataFrame = TestUtils.getTestDataframe()
    val filePath = new File(s"${rootPath}transformer${File.separator}union${File.separator}mapConfig.json").toPath
    val params=dataFrame
    val _nodeDescription = classOf[Node].getDeclaredField("_nodeDescription")
    _nodeDescription.setAccessible(true)
    val res = new String(Files.readAllBytes(filePath))
    val simpleNodeDescription = JsonUtil.read(res, classOf[SimpleNodeDescription])
    val flatMapConfig= JsonMethods.mapper.convertValue(
      simpleNodeDescription.config,
      classOf[MapConfig]
    ).asInstanceOf[NodeConfig]

    val _config = classOf[Node].getDeclaredField("_config")
    _config.setAccessible(true)
    _config.set(map, flatMapConfig)
    val transform=classOf[Map].getMethod("transform",classOf[DataFrame])
    transform.setAccessible(true)
    val result=transform.invoke(map,params).asInstanceOf[DataFrame]
    result.show()

  }


}
