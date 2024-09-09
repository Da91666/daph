package com.dasea.daph.utils

import org.apache.commons.lang3.StringUtils
import org.apache.kafka.clients.admin.{AdminClient, NewTopic}
import org.apache.kafka.common.KafkaException
import org.apache.logging.log4j.scala.Logging

import java.util.Properties
import scala.jdk.CollectionConverters.{asScalaSetConverter, seqAsJavaListConverter}


object KafkaUtil extends Logging {
  def createTopics(topicConfigs: List[TopicConfig], clientConfig: Map[String, String]): Unit = {
    val props = new Properties
    clientConfig.foreach { case (key, value) => props.put(key, value) }
    try {
      val adminClient = AdminClient.create(props)
      try {
        val existingTopicNames = adminClient.listTopics().names().get().asScala
        val newTopicNames = topicConfigs.map(_.topicName).toSet
        val needCreateTopicNames = newTopicNames &~ existingTopicNames
        if (needCreateTopicNames.nonEmpty) {
          val topics = topicConfigs.filter(tc => needCreateTopicNames.contains(tc.topicName))
            .map(c => new NewTopic(c.topicName, c.partitions, c.replicationFactor))

          adminClient.createTopics(topics.asJava)
          logger.info(s"Topics[$topicConfigs] created successfully.")
        }
      } catch {
        case e: KafkaException =>
          logger.error(s"Error creating topics[$topicConfigs]", e)
      } finally {
        if (adminClient != null) adminClient.close()
      }
    }
  }

  /*def registerSchemas(schemaConfigs: List[SchemaConfig], clientConfig: Map[String, String]) = {
    // 创建一个缓存客户端实例
    val capacity = schemaConfigs.size
    val client = new CachedSchemaRegistryClient("http://localhost:8081", capacity)

    val schemaString =
      """
        |{
        |        "type" : "record",
        |        "namespace" : "Example",
        |        "name" : "Employee",
        |        "fields" : [
        |                { "name" : "Name" , "type" : "string" },
        |                { "name" : "Age" , "type" : "int" }
        |        ]
        |}
        |""".stripMargin
    //    val schema = new AvroSchema(Schema.create(Schema.Type.INT), 1)

    schemaConfigs.foreach { sc =>
      val schema = new AvroSchema(sc.schemaString)
      client.register(sc.subjectName, schema, sc.version, sc.id)
    }
  }*/

  def main(args: Array[String]): Unit = {
    //    val arr = Array("FLOAT", "DOUBLE", "STRING", "ARRAY", "STRUCT", "MAP", "MAP")
    //    val arr2 = Array("FLOAT", "STRING")
    //    val len = arr.length
    //    val arr3 = arr.diff(arr2)
    //    println(len)
    //    println(arr.take(3).toList)
    //    println(arr.takeRight(len - 3).toList)
    //    println(arr3.toList)

    //    val str = "DECIMAL"
    //    val vct = str.split("\\(")
    //    val ct = vct.head
    //    val vv = vct.last.replace(")", "").split(",")
    //    val vcs = vv.head
    //    val vdd = vv.last
    //    println(ct)
    //    println(vcs)
    //    println(vdd)

    val aa = "aa,  `PK_2271` PRIMARY KEY".split("PRIMARY KEY").head
    println(aa.split("CONSTRAINT `").head)

    val arr = Array("").filterNot(StringUtils.isEmpty)
    println(arr.mkString("Array(", ", ", ")"))
    println(arr.isEmpty)
    //    val num = "aa TIMESTAMP(6) bb"
    //    println(num.matches("^TIMESTAMP\\(\\d+\\)$"))
    //    println(num.replaceAll("TIMESTAMP\\(\\d+\\)", ""))
    //    println(num.replaceAll("""TIMESTAMP\(\d+\)""", ""))
    //    val list = List("1").reduce((a, b) => s""""$a","$b"""")
    //    println(list)
  }
}

case class TopicConfig(topicName: String, partitions: Int = 1, replicationFactor: Short = 1)

case class SchemaConfig(subjectName: String, schemaString: String, version: Int = 1, id: Int)
