package com.dasea.daph.transformer

import com.dasea.daph.api.node.{Node, SimpleNodeDescription}
import com.dasea.daph.node.spark3.dataframe.general.transformer.filter.{Filter, FilterConfig}
import com.dasea.daph.utils.JsonUtil
import org.apache.spark.sql.DataFrame
import org.json4s.jackson.JsonMethods
import org.junit.Test

import java.io.File
import java.nio.file.Files

class FilterTest {
  val rootPath = s"${System.getProperty("user.dir")}${File.separator}src${File.separator}test${File.separator}resources${File.separator}testdata${File.separator}"
  val targetPath = s"${System.getProperty("user.dir")}${File.separator}target${File.separator}transformer${File.separator}union"

  @Test
  def UnionTest(): Unit = {
    val filter = new Filter()
    val dataFrame = TestUtils.getTestDataframe()
    val filePath = new File(s"${rootPath}transformer${File.separator}union${File.separator}filterConfig.json").toPath
    val params=dataFrame
    val _nodeDescription = classOf[Node].getDeclaredField("_nodeDescription")
    _nodeDescription.setAccessible(true)
    val res = new String(Files.readAllBytes(filePath))
    val simpleNodeDescription = JsonUtil.read(res, classOf[SimpleNodeDescription])
    val flatMapConfig= JsonMethods.mapper.convertValue(
      simpleNodeDescription.config,
      classOf[FilterConfig]
    )
    val _config = classOf[Node].getDeclaredField("_config")
    _config.setAccessible(true)
    _config.set(filter, flatMapConfig)
    val transform=classOf[Filter].getMethod("transform",classOf[DataFrame])
    transform.setAccessible(true)
    val result=transform.invoke(filter,params).asInstanceOf[DataFrame]
    result.show()

  }


}
