## 简介

- **节点标识**：Spark3.dataframe.general.transformer.MSql
- **节点类型**：转换节点
- **节点功能**：接收多个dataframe，执行一条sql后得到一个dataframe，并流转到多个下游节点
- **流批类型**：流批

## 输入输出数据结构

| 输入数据结构      | 输出数据结构      |
|-------------|-------------|
| 多个DataFrame | 一个DataFrame |

## 配置项

| 配置名称              | 配置类型               | 是否必填项 | 默认值 | 描述          |
|-------------------|--------------------|-------|-----|-------------|
| sql               | String             | 是     | -   | spark sql语句 |
| lineIdToTableName | Map[String,String] | 是     | -   | 输入线与表名的映射关系 |

## 使用案例

### DAG图

```mermaid
graph LR
    a1[mysql-in] --> aa[msql-tr];
    a2[es-in] --> aa[msql-tr];
    aa[msql-tr] --> aaa[es-out];
```

### job.json

```json
{
  "nodes": [
    {
      "flag": "Spark3.dataframe.batch.connector.CommonInput",
      "config": {
        "format": "jdbc",
        "cfg": {
          "url": "jdbc:mysql://127.0.0.1:3306/daph",
          "dbtable": "t",
          "user": "root",
          "password": "root",
          "driver": "com.mysql.cj.jdbc.Driver"
        }
      },
      "outLines": [
        "in-line1"
      ]
    },
    {
      "flag": "Spark3.dataframe.batch.connector.CommonInput",
      "config": {
        "format": "es",
        "cfg": {
          "es.nodes": "127.0.0.1:9200,127.0.0.2:9200"
        }
      },
      "outLines": [
        "in-line2"
      ]
    },
    {
      "flag": "Spark3.dataframe.general.transformer.MSql",
      "config": {
        "sql": "select id, t1.name, t2.age from t1 join t2 on t1.id = t2.id",
        "lineIdToTableName": {
          "in-line1": "t1",
          "in-line2": "t2"
        }
      },
      "inLines": [
        "in-line1",
        "in-line2"
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
