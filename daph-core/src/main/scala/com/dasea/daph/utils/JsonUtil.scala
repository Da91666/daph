package com.dasea.daph.utils

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.json4s.jackson.JsonMethods
import org.json4s.jackson.JsonMethods._

import java.io.File
import java.nio.file.Files
import scala.reflect.{ClassTag, classTag}

object JsonUtil {
  private lazy val defaultMapper: ObjectMapper = {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper.registerModule(DefaultScalaModule)
    mapper
  }

  def read[T](value: String, classType: Class[T]): T = {
    defaultMapper.readValue(value, classType)
  }

  def convert[T](value: Any, classType: Class[T]): T = {
    JsonMethods.mapper.convertValue(value, classType)
  }

  def loadFile[T: ClassTag](path: String): T = {
    val json = new String(Files.readAllBytes(new File(path).toPath))
    val res = JsonUtil.read(json, classTag[T].runtimeClass)
    res.asInstanceOf[T]
  }
}
