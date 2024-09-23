## 目录

- [简介](#简介)
- [配置项](#配置项)
    - [节点配置项](#节点配置项)
    - [支持的数据源类型及详细配置项](#支持的数据源类型及详细配置项)
- [使用案例](#使用案例)
    - [DAG图](#DAG图)
    - [job.json](#jobjson)

## 简介

- **节点标识**：Spark3.dataframe.stream.connector.CommonInput
- **节点类型**：输入节点
- **节点功能**：可将一个流读取成一个DataFrame，并流转到多个下游节点
- **流批类型**：流
- **支持的数据源类型**：见[支持的数据源类型及详细配置项](#支持的数据源类型及详细配置项)

## 配置项

### 节点配置项

| 配置名称   | 配置类型               | 是否必填项 | 默认值 | 描述                                                                                                                                   |
|--------|--------------------|-------|-----|--------------------------------------------------------------------------------------------------------------------------------------|
| format | String             | 是     | -   | 详见[支持的数据源类型及详细配置项](#支持的数据源类型及详细配置项)                                                                                                  |
| cfg    | Map[String,String] | 是     | -   | 详见[支持的数据源类型及详细配置项](#支持的数据源类型及详细配置项)                                                                                                  |
| path   | String             | 否     | -   | 对应load方法参数，请参考[sql-data-sources-load-save-functions](https://spark.apache.org/docs/latest/sql-data-sources-load-save-functions.html) |

### 支持的数据源类型及详细配置项

| 数据源类型     | format                     | cfg                                                                                                                                                               | 说明  |
|-----------|----------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----|
| doris     | doris                      | 请参考[spark-doris-connector](https://doris.apache.org/zh-CN/docs/dev/ecosystem/spark-doris-connector/)                                                              |     |
| starrocks | starrocks                  | 请参考[Spark-connector-starrocks](https://docs.starrocks.io/zh/docs/loading/Spark-connector-starrocks/)                                                              |     |
| hudi      | hudi                       | 请参考<br>[hudi spark doc1](https://hudi.apache.org/cn/docs/quick-start-guide)<br>[hudi spark doc2](https://hudi.apache.org/cn/docs/configurations#SPARK_DATASOURCE) |     |
| iceberg   | iceberg                    | 请参考[iceberg spark doc1](https://iceberg.apache.org/docs/1.4.0/spark-getting-started/)                                                                             |     |
| deltalake | delta                      | 请参考[delta spark](https://docs.delta.io/latest/index.html)                                                                                                         |     |
| paimon    | paimon                     | 请参考[paimon spark](https://paimon.apache.org/docs/0.7/engines/spark/)                                                                                              |     |
| kafka     | kafka                      | 请参考[structured-streaming-kafka-integration](https://spark.apache.org/docs/3.5.1/structured-streaming-kafka-integration.html)                                      |     |
| pulsar    | pulsar                     | 请参考[pulsar-spark](https://github.com/streamnative/pulsar-spark/blob/master/README.md)                                                                             |     |
| neo4j     | org.neo4j.spark.DataSource | 请参考[Spark neo4j](https://neo4j.com/docs/spark/4.2/overview/)                                                                                                      |     |
| redis     | org.apache.spark.sql.redis | 请参考[spark-redis](https://github.com/RedisLabs/spark-redis/blob/master/doc)                                                                                        |     |
| socket    | socket                     | 请参考[Spark sql-data-sources](https://spark.apache.org/docs/3.5.1/sql-data-sources.html)                                                                            |     |
| text      | text                       | 同上                                                                                                                                                                |     |
| csv       | csv                        | 同上                                                                                                                                                                |     |
| json      | json                       | 同上                                                                                                                                                                |     |
| orc       | orc                        | 同上                                                                                                                                                                |     |
| parquet   | parquet                    | 同上                                                                                                                                                                |     |
| avro      | avro                       | 同上                                                                                                                                                                |     |

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
