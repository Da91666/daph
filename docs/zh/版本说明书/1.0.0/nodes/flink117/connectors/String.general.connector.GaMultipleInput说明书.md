<!-- TOC -->
  * [简介](#简介)
  * [配置项](#配置项)
    * [一级配置项](#一级配置项)
    * [catalogExtraConfig包含的配置项](#catalogextraconfig包含的配置项)
    * [createConfig包含的配置项](#createconfig包含的配置项)
  * [使用案例](#使用案例)
    * [作业描述](#作业描述)
    * [DAG图](#dag图)
    * [job.json](#jobjson)
<!-- TOC -->

## 简介

- **节点标识**：Flink117.sql.general.connector.GaMultipleInput
- **节点类型**：输入节点
- **节点功能**：可将多个数据表或文件读取成多个flink表，注册到TableEnv，并流转到多个下游节点
- **流批类型**：流批

## 配置项

### 一级配置项

| 配置名称               | 配置类型                      | 是否必填项 | 默认值              | 描述                                                                                                                                                                                                                                                                  |
|--------------------|---------------------------|-------|------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| catalogName        | String                    | 否     | default_catalog  | catalog名称。<br/>若未配置createConfigs，且未配置createSqls，则必填。                                                                                                                                                                                                                |
| catalogConfig      | Map[String,String]        | 否     | -                | catalog配置。<br/>若catalog名称不存在，则必填。<br/>请参考<br/>[flink-docs-release-1.17/docs/connectors/table](https://nightlies.apache.org/flink/flink-docs-release-1.17/docs/connectors/table/overview/)<br/>[iceberg flink](https://iceberg.apache.org/docs/nightly/flink/)       |
| databaseName       | String                    | 否     | default_database | catalog数据库名称                                                                                                                                                                                                                                                        |
| sqlDialect         | String                    | 否     | default          | default/hive                                                                                                                                                                                                                                                        |
| enableCatalogEDB   | Boolean                   | 否     | false            | catalog整库同步开关                                                                                                                                                                                                                                                       |
| enableCatalogCDC   | Boolean                   | 否     | false            | catalog cdc开关                                                                                                                                                                                                                                                       |
| catalogExtraConfig | Map[String,String]        | 否     | -                | catalog额外配置。<br/>用于catalog整库同步与catalog cdc。                                                                                                                                                                                                                         |
| createSqls         | Array[String]             | 否     | -                | flink表创建语句。<br/>若未配置catalog，且未配置createConfigs，则必填。<br/>若未指定catalog或database，则createSql中的表名格式必须是catalog名.数据库名.表名。<br/>请参考[flink-docs-release-1.17/docs/connectors/table](https://nightlies.apache.org/flink/flink-docs-release-1.17/docs/connectors/table/overview/) |
| createConfigs      | Array[Map[String,String]] | 否     | -                | 用于自动创建flink表创建语句的配置项。<br/>若未配置catalog，且未配置createSqls，则必填。                                                                                                                                                                                                           |
| queryConfigs       | Array[QueryConfig]        | 否     | -                | 用于对输入的表进行sql处理。<br/>若数组内只有一个元素，则QueryConfig不必指定line，results会被流转到其所有直接下游节点。                                                                                                                                                                                          |

```scala
// line是输出线
// results是该输出线上承载的结果表名，及获取结果表名需填写的sql语句
case class QueryConfig(line: String, results: Array[ResultTableToSql])

// 结果表名。表名格式必须是catalog名.数据库名.表名。
// 获取结果表名的flink sql查询语句。sql中的表名格式必须是catalog名.数据库名.表名。
case class ResultTableToSql(resultTable: String, sql: String)
```

### catalogExtraConfig包含的配置项

| 配置名称                           | 是否必填项 | 默认值   | 描述                                                                                                 |
|--------------------------------|-------|-------|----------------------------------------------------------------------------------------------------|
| daph.dbType                    | 否     | -     | 数据库类型。<br/>可不填写，程序内部会根据节点catalog名称自动推断。<br/>目前可选项有mysql/postgresql/hive/iceberg。                   |
| daph.jdbcDriver                | 否     | -     | jdbc驱动类名。程序内部能够通过url自动推断，或存在driver配置项，就不用填写                                                        |
| daph.tableNames                | 否     | *     | 表名表达式。<br/>默认为*，表示输入catalog的database下的所有表。<br/>若包含逗号，则要将期望输入的表名以逗号为分隔符，罗列出来。<br/>除了以上两种情况，就是正则表达式。 |
| daph.flink.edbNumber           | 否     | ""    | 整库序号。以便整库输出端找到整库输入端                                                                                |
| daph.flink.onlyPrimaryTables   | 否     | false | 是否仅输入catalog的database下具有主键的表                                                                       |
| daph.flink.onlyUnPrimaryTables | 否     | false | 是否仅输入catalog的database下不具有主键的表                                                                      |
| 不以daph.开头的配置名称                 | 否     | -     | 用于cdc整库同步时，所有cdc表都会用到的flink-cdc配置项，比如scan.startup.mode                                             |

### createConfig包含的配置项

| 配置名称                           | 是否必填项 | 默认值   | 描述                                                                                           |
|--------------------------------|-------|-------|----------------------------------------------------------------------------------------------|
| daph.dbType                    | 否     | -     | 数据库类型。只要connector不是jdbc，就不用填写                                                                |
| daph.jdbcDriver                | 否     | -     | jdbc驱动类名。程序内部能够通过url自动推断，或存在driver配置项，就不用填写                                                  |
| daph.url                       | 否     | -     | 用于建立jdbc连接。<br/>若createConfig存在名称为url的配置项，则不用配置。                                             |
| daph.databaseName              | 是     | -     | 数据库名称。只要存在database-name或database配置项，就不用填写                                                    |
| daph.schemaName                | 否     | -     | schema名称。只要存在schema-name配置项，就不用填写                                                            |
| daph.tableNames                | 是     | -     | 表名表达式。<br/>默认为*，表示输入数据库或schema下的所有表。<br/>若包含逗号，则要将期望输入的表名以逗号为分隔符，罗列出来。<br/>除了以上两种情况，就是正则表达式。 |
| daph.flink.enableEDB           | 否     | false | 整库开关                                                                                         |
| daph.flink.edbNumber           | 否     | ""    | 整库序号。以便整库输出端找到整库输入端。                                                                         |
| daph.flink.onlyPrimaryTables   | 否     | false | 是否仅输入database或schema下具有主键的表                                                                  |
| daph.flink.onlyUnPrimaryTables | 否     | false | 是否仅输入database或schema下不具有主键的表                                                                 |
| 不以daph.开头的配置名称                 | 是     | -     | flink sql with中的配置项，比如connector                                                              |

## 使用案例

### 作业描述

- 输入：mysql整库
- 输出：hive

### DAG图

```mermaid
graph LR
    a[GaMultipleInput] --> aa[GaMultipleOutput];
```

### job.json

```json
{
  "nodes": [
    {
      "flag": "Flink117.sql.general.connector.GaMultipleInput",
      "id": "in1",
      "config": {
        "createConfigs": [
          {
            "daph.dbType": "mysql",
            "daph.flink.enableEDB": "true",
            "connector": "jdbc",
            "url": "jdbc:mysql://192.168.3.202:3306/daph",
            "username": "root",
            "password": "root"
          }
        ]
      },
      "outLines": [
        "in-line"
      ]
    },
    {
      "flag": "Flink117.sql.general.connector.GaMultipleOutput",
      "config": {
        "catalogName": "hive_catalog",
        "databaseName": "flink_db",
        "enableCatalogEDB": true,
        "catalogExtraConfig": {
          "daph.tableNamePrefix": "z_",
          "daph.flink.edbInNodeId": "in1",
          "daph.flink.saveMode": "append",
          "daph.hive.flinkDialect": "hive",
          "daph.hive.format": "TextFile",
          "bucketing_version": "2"
        }
      },
      "inLines": [
        "in-line"
      ]
    }
  ]
}
```
