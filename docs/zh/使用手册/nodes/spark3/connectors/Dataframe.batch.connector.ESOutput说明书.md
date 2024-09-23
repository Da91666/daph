<!-- TOC -->
  * [简介](#简介)
  * [配置项](#配置项)
  * [使用案例](#使用案例)
    * [DAG图](#dag图)
    * [job.json](#jobjson)
<!-- TOC -->
## 简介

- **节点标识**：Spark3.dataframe.batch.connector.ESOutput
- **节点类型**：输出节点
- **节点功能**：接收一个DataFrame，将DataFrame承载的数据写出到一个elasticsearch文档
- **流批类型**：批

## 配置项

| 配置名称     | 配置类型               | 是否必填项 | 默认值 | 描述                                                                                                |
|----------|--------------------|-------|-----|---------------------------------------------------------------------------------------------------|
| resource | String             | 是     | -   | resource 的格式为 index/type                                                                          |
| cfg      | Map[String,String] | 是     | -   | 请参考[elasticsearch spark](https://www.elastic.co/guide/en/elasticsearch/hadoop/current/spark.html) |

## 使用案例

### DAG图

```mermaid
graph LR
    a[es-in] --> aa[es-out];
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
      "flag": "Spark3.dataframe.batch.connector.ESOutput",
      "config": {
        "resource": "index/docs",
        "cfg": {
          "es.nodes": "127.0.0.1:9200,127.0.0.2:9200",
          "es.mapping.id": "id",
          "es.nodes.wan.only": "true",
          "es.index.auto.create": "true"
        }
      },
      "inLines": [
        "in-line"
      ]
    }
  ]
}
```
