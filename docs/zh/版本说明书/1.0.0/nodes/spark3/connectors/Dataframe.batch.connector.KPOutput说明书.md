## 简介

- **节点标识**：Spark3.dataframe.batch.connector.KPOutput
- **节点类型**：输出节点
- **节点功能**：接收一个DataFrame，将DataFrame承载的数据写出到一个kafka/pulsar主题
- **流批类型**：批

## 配置项

| 配置名称                 | 配置类型                 | 是否必填项 | 默认值     | 描述                                                                                                                                                                                                                       |
|----------------------|----------------------|-------|---------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| format               | String               | 是     | -       | kafka/pulsar                                                                                                                                                                                                             |
| cfg                  | Map[String,String]   | 是     | -       | 请参考<br/>[structured-streaming-kafka-integration](https://spark.apache.org/docs/2.4.8/structured-streaming-kafka-integration.html)<br/>[pulsar-spark](https://github.com/streamnative/pulsar-spark/blob/master/README.md) |
| saveMode             | String               | 否     | default | spark saveMode                                                                                                                                                                                                           |
| partitionColumnNames | Array[String,String] | 否     | -       | 对应DataFrameWriter的partitionBy方法参数                                                                                                                                                                                        |
| sortColumnNames      | Array[String,String] | 否     | -       | 对应DataFrameWriter的sortBy方法参数                                                                                                                                                                                             |
| bucketColumnNames    | Array[String,String] | 否     | -       | 对应DataFrameWriter的bucketBy方法参数                                                                                                                                                                                           |
| numBuckets           | Int                  | 否     | -       | 对应DataFrameWriter的bucketBy方法参数                                                                                                                                                                                           |
| keyType              | String               | 否     | STRING  | string or binary，不区分大小写                                                                                                                                                                                                  |
| valueType            | String               | 否     | STRING  | string or binary，不区分大小写                                                                                                                                                                                                  |

## 使用案例

### DAG图

```mermaid
graph LR
    a[es-in] --> aa[kafka-out];
```

### job.json

```json
{
  "nodes": [
    {
      "flag": "Spark3.dataframe.batch.connector.CommonInput",
      "config": {
        "format": "es",
        "cfg": {
          "es.nodes": "127.0.0.1:9200,127.0.0.2:9200"
        }
      },
      "outLines": [
        "in-line"
      ]
    },
    {
      "flag": "Spark3.dataframe.batch.connector.KPOutput",
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
