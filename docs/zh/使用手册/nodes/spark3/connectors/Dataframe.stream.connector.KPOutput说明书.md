<!-- TOC -->
  * [简介](#简介)
  * [配置项](#配置项)
  * [使用案例](#使用案例)
    * [DAG图](#dag图)
    * [job.json](#jobjson)
<!-- TOC -->
## 简介

- **节点标识**：Spark3.dataframe.stream.connector.KPOutput
- **节点类型**：输出节点
- **节点功能**：接收一个DataFrame，将DataFrame承载的数据写出到一个kafka/pulsar主题
- **流批类型**：流

## 配置项

| 配置名称                 | 配置类型               | 是否必填项 | 默认值            | 描述                                                                                                                                                                                                                       |
|----------------------|--------------------|-------|----------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| format               | String             | 是     | -              | kafka/pulsar                                                                                                                                                                                                             |
| cfg                  | Map[String,String] | 是     | -              | 请参考<br/>[structured-streaming-kafka-integration](https://spark.apache.org/docs/2.4.8/structured-streaming-kafka-integration.html)<br/>[pulsar-spark](https://github.com/streamnative/pulsar-spark/blob/master/README.md) |
| outputMode           | String             | 否     | append         | 对应DataStreamWriter的outputMode方法参数                                                                                                                                                                                        |
| triggerType          | String             | 否     | ProcessingTime | 对应DataStreamWriter的trigger方法参数                                                                                                                                                                                           |
| triggerTime          | String             | 否     | 0 seconds      | 对应DataStreamWriter的trigger方法参数                                                                                                                                                                                           |
| partitionColumnNames | Array[String]      | 否     | -              | 对应DataStreamWriter的partitionBy方法参数                                                                                                                                                                                       |
| queryName            | String             | 否     | -              | 对应DataStreamWriter的queryName方法参数                                                                                                                                                                                         |
| keyType              | String             | 否     | STRING         | string or binary，不区分大小写                                                                                                                                                                                                  |
| valueType            | String             | 否     | STRING         | string or binary，不区分大小写                                                                                                                                                                                                  |

## 使用案例

### DAG图

```mermaid
graph LR
    a[socket-in] --> aa[kafka-out];
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
      "flag": "Spark3.dataframe.stream.connector.KPOutput",
      "config": {
        "format": "kafka",
        "cfg": {
          "kafka.bootstrap.server": "host1:port1,host2:port2",
          "topic": "daph"
        }
      },
      "inLines": [
        "in-line"
      ]
    }
  ]
}
```
