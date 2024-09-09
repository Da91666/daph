## 简介

- **节点标识**：Spark3.dataframe.batch.connector.HiveInput
- **节点类型**：输入节点
- **节点功能**：可将一个hive表读取成一个DataFrame，并流转到多个下游节点
- **流批类型**：批

## 配置项

| 配置名称       | 配置类型   | 是否必填项 | 默认值 | 描述    |
|------------|--------|-------|-----|-------|
| sql        | String | 是     | -   | sql语句 |

## 使用案例

### DAG图

```mermaid
graph LR
    a[hive-in] --> aa[es-out];
```

### job.json

```json
{
  "nodes": [
    {
      "flag": "Spark3.dataframe.batch.connector.HiveInput",
      "config": {
        "sql": "select * from hive.t"
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
