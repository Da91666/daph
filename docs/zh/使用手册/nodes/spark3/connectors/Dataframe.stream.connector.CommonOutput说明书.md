b## 目录

- [简介](#简介)
- [配置项](#配置项)
    - [节点配置项](#节点配置项)
    - [支持的数据源类型及详细配置项](#支持的数据源类型及详细配置项)
- [使用案例](#使用案例)
    - [DAG图](#DAG图)
    - [job.json](#jobjson)

## 简介

- **节点标识**：Spark3.dataframe.stream.connector.CommonOutput
- **节点类型**：输出节点
- **节点功能**：接收一个DataFrame，将DataFrame承载的数据写出到一个数据源
- **流批类型**：流
- **支持的数据源类型**：见[支持的数据源类型及详细配置项](#支持的数据源类型及详细配置项)

## 配置项

### 节点配置项

| 配置名称                 | 配置类型               | 是否必填项 | 默认值   | 描述                                                                                                           |
|----------------------|--------------------|-------|-------|--------------------------------------------------------------------------------------------------------------|
| format               | String             | 否     | -     | 若未开启batch，则必填。详见[支持的数据源类型及详细配置项](#支持的数据源类型及详细配置项)                                                            |
| cfg                  | Map[String,String] | 否     | -     | 若未开启batch，则必填。详见[支持的数据源类型及详细配置项](#支持的数据源类型及详细配置项)                                                            |
| outputMode           | String             | 否     | -     | 若未开启batch，则必填。对应DataStreamWriter的outputMode方法参数                                                              |
| triggerType          | String             | 否     | -     | 对应DataStreamWriter的trigger方法参数                                                                               |
| triggerTime          | String             | 否     | -     | 对应DataStreamWriter的trigger方法参数                                                                               |
| partitionColumnNames | Array[String]      | 否     | -     | 对应DataStreamWriter的partitionBy方法参数                                                                           |
| queryName            | String             | 否     | -     | 对应DataStreamWriter的queryName方法参数                                                                             |
| method               | String             | 否     | start | 可选项有start/toTable                                                                                            |
| name                 | String             | 否     | -     | 对应DataStreamWriter的start/toTable方法参数                                                                         |
| batch                | Boolean            | 否     | false | 是否使用foreach方法                                                                                                |
| batchConfig          | CommonOutputConfig | 否     | -     | 若开启batch，则必填。对应[Dataframe.batch.connector.CommonOutput说明书中的节点配置项](Dataframe.batch.connector.CommonOutput说明书) |

```scala
case class CommonOutputConfig(
  format: String,
  cfg: Map[String, String] = Map.empty,
  saveMode: String = "default",
  partitionColumnNames: Array[String] = Array.empty,
  sortColumnNames: Array[String] = Array.empty,
  bucketColumnNames: Array[String] = Array.empty,
  numBuckets: Int,
  method: String = "save",
  name: String,
  v2: Boolean = false,
  v2Config: V2Config
)
```

### 支持的数据源类型及详细配置项

| 数据源类型         | format                         | cfg                                                                                                                                                               | 说明  |
|---------------|--------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----|
| doris         | doris                          | 请参考[spark-doris-connector](https://doris.apache.org/zh-CN/docs/dev/ecosystem/spark-doris-connector/)                                                              |     |
| starrocks     | starrocks                      | 请参考[Spark-connector-starrocks](https://docs.starrocks.io/zh/docs/loading/Spark-connector-starrocks/)                                                              |     |
| cassandra     | org.apache.spark.sql.cassandra | 请参考[spark-cassandra-connector](https://github.com/datastax/spark-cassandra-connector/blob/master/doc/14_data_frames.md)                                           |     |
| hudi          | hudi                           | 请参考<br>[hudi spark doc1](https://hudi.apache.org/cn/docs/quick-start-guide)<br>[hudi spark doc2](https://hudi.apache.org/cn/docs/configurations#SPARK_DATASOURCE) |     |
| iceberg       | iceberg                        | 请参考[iceberg spark doc1](https://iceberg.apache.org/docs/1.4.0/spark-getting-started/)                                                                             |     |
| deltalake     | delta                          | 请参考[delta spark](https://docs.delta.io/latest/index.html)                                                                                                         |     |
| paimon        | paimon                         | 请参考[paimon spark](https://paimon.apache.org/docs/0.7/engines/spark/)                                                                                              |     |
| elasticsearch | es                             | 请参考[elasticsearch spark](https://www.elastic.co/guide/en/elasticsearch/hadoop/current/spark.html)                                                                 |     |
| neo4j         | org.neo4j.spark.DataSource     | 请参考[Spark neo4j](https://neo4j.com/docs/spark/4.2/overview/)                                                                                                      |     |
| redis         | org.apache.spark.sql.redis     | 请参考[spark-redis](https://github.com/RedisLabs/spark-redis/blob/master/doc)                                                                                        |     |
| text          | text                           | 请参考[Spark sql-data-sources](https://spark.apache.org/docs/3.5.1/sql-data-sources.html)                                                                            |     |
| csv           | csv                            | 同上                                                                                                                                                                |     |
| json          | json                           | 同上                                                                                                                                                                |     |
| orc           | orc                            | 同上                                                                                                                                                                |     |
| parquet       | parquet                        | 同上                                                                                                                                                                |     |
| avro          | avro                           | 同上                                                                                                                                                                |     |

## 使用案例

### DAG图

```mermaid
graph LR
    a[socket-in] --> aa[es-out];
```

### job.json

```json
{
  "nodes": [
    {
      "flag": "Spark3.dataframe.stream.connector.CommonInput",
      "config": {
        "format": "socket",
        "cfg": {
          "host": "127.0.0.1",
          "port": "9000",
          "includeTimestamp": "true"
        }
      },
      "outLines": [
        "in-line"
      ]
    },
    {
      "flag": "Spark3.dataframe.stream.connector.ESOutput",
      "config": {
        "format": "es",
        "cfg": {
          "checkpointLocation": "/save/location",
          "es.nodes": "127.0.0.1:9200,127.0.0.2:9200",
          "es.mapping.id": "id",
          "es.nodes.wan.only": "true",
          "es.index.auto.create": "true"
        },
        "path": "spark/people"
      },
      "inLines": [
        "in-line"
      ]
    }
  ]
}
```
