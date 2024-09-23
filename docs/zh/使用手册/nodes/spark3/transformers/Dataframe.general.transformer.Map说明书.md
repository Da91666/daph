## 简介

- **节点标识**：Spark3.dataframe.general.transformer.Map
- **节点类型**：转换节点
- **节点功能**：接收一个dataframe，以条件过滤后得到一个dataframe，并流转到多个下游节点
- **流批类型**：流批

## 输入输出数据结构

| 输入数据结构      | 输出数据结构      |
|-------------|-------------|
| 一个DataFrame | 一个DataFrame |

## 配置项

| 配置名称        | 配置类型          | 是否必填项 | 默认值 | 描述                       |
|-------------|---------------|-------|-----|--------------------------|
| expressions | Array[String] | 是     | -   | 对应DataFrame的selectExpr方法 |

## 使用案例

### DAG图

```mermaid
graph LR
    a[es-in] --> aa[map-tr] --> aaa[es-out];
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
      "flag": "Spark3.dataframe.general.transformer.Map",
      "config": {
        "expressions": [
          "f1 as id",
          "abs(f2) as value"
        ]
      },
      "inLines": [
        "in-line"
      ],
      "outLines": [
        "tr-line"
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
        "tr-line"
      ]
    }
  ]
}
```
