package com.dasea.daph.spark3.computer

import com.dasea.daph.api.computer.Computer
import com.dasea.daph.spark3.utils.CommonUtils
import com.dasea.daph.utils.JsonUtil
import org.apache.spark.sql.SparkSession

class SparkComputer(config: SparkComputerConfig) extends Computer {
  override val entrypoint: SparkSession = CommonUtils.createSpark(config.envConfig)

  def this(json: String) = this(
    JsonUtil.read(json, classOf[SparkComputerConfig])
  )

  def this(map: Map[String, String]) = this(
    SparkComputerConfig(map, Map.empty)
  )

  override def stop(): Unit = entrypoint.stop()

  override def clear(): Unit = {
    entrypoint.catalog.clearCache()
    entrypoint.sparkContext.getPersistentRDDs.values.foreach(x => {
      x.unpersist(false)
    })
  }


}