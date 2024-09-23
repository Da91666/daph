<!-- TOC -->

* [简介](#简介)
* [配置项](#配置项)
    * [一级配置项](#一级配置项)
    * [catalogExtraConfig包含的配置项](#catalogextraconfig包含的配置项)
    * [createConfig包含的配置项](#createconfig包含的配置项)
        * [通用配置项](#通用配置项)
        * [Doris特有配置项](#doris特有配置项)
        * [Starrocks特有配置项](#starrocks特有配置项)
* [使用案例](#使用案例)
    * [作业描述](#作业描述)
    * [DAG图](#dag图)
    * [job.json](#jobjson)

<!-- TOC -->

## 简介

- **节点标识**：Flink117.sql.general.connector.GaMultipleOutput
- **节点类型**：输出节点
- **节点功能**：将TableEnv中的多个表，写出到多个外部数据表或文件
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
| catalogExtraConfig | Map[String,String]        | 否     | -                | catalog额外配置。<br/>用于catalog整库同步与catalog cdc。                                                                                                                                                                                                                         |
| createSqls         | Array[String]             | 否     | -                | flink表创建语句。<br/>若未配置catalog，且未配置createConfigs，则必填。<br/>若未指定catalog或database，则createSql中的表名格式必须是catalog名.数据库名.表名。<br/>请参考[flink-docs-release-1.17/docs/connectors/table](https://nightlies.apache.org/flink/flink-docs-release-1.17/docs/connectors/table/overview/) |
| createConfigs      | Array[Map[String,String]] | 否     | -                | 用于自动创建flink表创建语句的配置项。<br/>若未配置catalog，且未配置createSqls，则必填。                                                                                                                                                                                                           |
| insertConfigs      | Array[InsertConfig]       | 否     | -                | 用于配置flink表插入语句                                                                                                                                                                                                                                                      |

```scala
// sql是必填项
// primaryKeys用于自动建立目标表时，创建主键。可配置多个主键，以逗号分隔
// order用于在配置多个insertConfig时，规定sql语句的执行顺序。-1代表不参与排序，有效排序数字是大于等于0的整型数字
case class InsertConfig(sql: String, primaryKeys: String = "", order: Int = -1)
```

### catalogExtraConfig包含的配置项

| 配置名称                   | 是否必填项 | 默认值      | 描述                                                                                                 |
|------------------------|-------|----------|----------------------------------------------------------------------------------------------------|
| daph.dbType            | 否     | -        | 数据库类型。<br/>可不填写，程序内部会根据节点catalog名称自动推断。<br/>目前可选项有mysql/postgresql/hive/iceberg。                   |
| daph.jdbcDriver        | 否     | -        | jdbc驱动类名。程序内部能够通过url自动推断，或存在driver配置项，就不用填写                                                        |
| daph.schemaName        | 否     | -        | schema名称。只要存在schema-name配置项，就不用填写                                                                  |
| daph.tableNames        | 否     | *        | 表名表达式。<br/>默认为*，表示输入catalog的database下的所有表。<br/>若包含逗号，则要将期望输入的表名以逗号为分隔符，罗列出来。<br/>除了以上两种情况，就是正则表达式。 |
| daph.tableNamePrefix   | 否     | 空字符串     | 表名前缀。用于整库同步时，给结果表名加前缀                                                                              |
| daph.tableNameSuffix   | 否     | 空字符串     | 表名后缀。用于整库同步时，给结果表名加后缀                                                                              |
| daph.tableNameCase     | 否     | 空字符串     | 表名大小写转换。默认不转换，lower会将输出表名转换为小写，upper会将输出表名转换为大写                                                    |
| daph.columnNameCase    | 否     | 空字符串     | 列名大小写转换。默认不转换，lower会将输出列名转换为小写，upper会将输出列名转换为大写                                                    |
| daph.flink.edbInNodeId | 否     | -        | 整库输入节点ID。以便整库输出端找到整库输入端。<br/>整库同步时，是必填项。                                                           |
| daph.flink.edbNumber   | 否     | 空字符串     | 整库序号。以便整库输出端找到整库输入端                                                                                |
| daph.flink.saveMode    | 否     | append   | insert模式。目前可选项有append/overwrite，后续会有upsert                                                         |
| daph.hive.format       | 否     | TextFile | 用于指定创建hive表时，使用的存储格式。catalog类型为hive时，必填项。                                                          |
| daph.hive.flinkDialect | 否     | hive     | 用于指定创建hive表时，使用的flink sql方言                                                                        |
| 不以daph.开头的配置名称         | 否     | -        | 用于添加flink sql with或TBLPROPERTIES配置项，比如sink.max-retries                                             |

### createConfig包含的配置项

#### 通用配置项

| 配置名称                           | 是否必填项 | 默认值    | 描述                                                                                           |
|--------------------------------|-------|--------|----------------------------------------------------------------------------------------------|
| daph.dbType                    | 否     | -      | 数据库类型。只要connector不是jdbc，就不用填写                                                                |
| daph.jdbcDriver                | 否     | -      | jdbc驱动类名。程序内部能够通过url自动推断，或存在driver配置项，就不用填写                                                  |
| daph.url                       | 否     | -      | 用于建立jdbc连接。<br/>若createConfig存在名称为url的配置项，则不用配置。                                             |
| daph.databaseName              | 是     | -      | 数据库名称。只要存在database-name或database配置项，就不用填写                                                    |
| daph.schemaName                | 否     | -      | schema名称。只要存在schema-name配置项，就不用填写                                                            |
| daph.tableNames                | 是     | -      | 表名表达式。<br/>默认为*，表示输入数据库或schema下的所有表。<br/>若包含逗号，则要将期望输入的表名以逗号为分隔符，罗列出来。<br/>除了以上两种情况，就是正则表达式。 |
| daph.tableNamePrefix           | 否     | ""     | 表名前缀。用于整库同步时，给结果表名加前缀                                                                        |
| daph.tableNameSuffix           | 否     | ""     | 表名后缀。用于整库同步时，给结果表名加后缀                                                                        |
| daph.tableNameCase             | 否     | ""     | 表名大小写转换。用于整库同步时，默认不转换，lower会将输出表名转换为小写，upper会将输出表名转换为大写                                      |
| daph.columnNameCase            | 否     | ""     | 列名大小写转换。用于整库同步时，默认不转换，lower会将输出列名转换为小写，upper会将输出列名转换为大写                                      |
| daph.flink.enableEDB           | 否     | false  | 整库开关                                                                                         |
| daph.flink.edbInNodeId         | 否     | -      | 整库输入节点ID。以便整库输出端找到整库输入端。<br/>整库同步时，是必填项。                                                     |
| daph.flink.edbNumber           | 否     | ""     | 整库序号。以便整库输出端找到整库输入端。                                                                         |
| daph.flink.saveMode            | 否     | append | insert模式。目前可选项有append/overwrite，后续会有upsert                                                   |
| daph.flink.autoSingleInsertSql | 否     | false  | 是否自动产生单表插入语句。仅用于单表输出时，自动产生单表插入语句                                                             |
| 不以daph.开头的配置名称                 | 是     | -      | flink sql with中的配置项，比如connector                                                              |

#### Doris特有配置项

| 配置名称                      | 是否必填项 | 默认值          | 描述                                                                                                                                                           |
|---------------------------|-------|--------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| daph.doris.labelType      | 否     | uuid         | label生成方式。<br/>uuid表示daph程序内部为每个表都生成一个uuid label，可以确保下次启动，不会报label已存在的错误，用于整库批处理场景。<br/>tn表示daph程序内部为每个表都生成一个与表名相同名称的label，确保一个任务中的不同表名的label不会冲突，用于整库流处理场景。 |
| daph.doris.engineType     | 否     | olap         | 对应doris的引擎类型，目前支持olap与mysql                                                                                                                                  |
| daph.doris.keyType        | 否     | DUPLICATE    | 对应doris的keys_type                                                                                                                                            |
| daph.doris.keys           | 否     | ""           | 对应doris的keys_type中的keys                                                                                                                                      |
| daph.doris.castType       | 否     | VARCHAR(255) | 当源表中的某一个或几个字段，将作为doris表的前几位字段，却不符合doris要求时，强制转换为指定类型                                                                                                         |
| daph.doris.commentPart    | 否     | ""           | 对应doris的table_comment                                                                                                                                        |
| daph.doris.partitionPart  | 否     | ""           | 对应doris的partition_info                                                                                                                                       |
| daph.doris.distPart       | 否     | ""           | 对应doris的distribution_desc                                                                                                                                    |
| daph.doris.bucketPart     | 否     | ""           | 对应doris的distribution_desc中的buckets部分                                                                                                                         |
| daph.doris.rollupPart     | 否     | ""           | 对应doris的rollup_list                                                                                                                                          |
| 以daph.doris.props.开头的配置名称 | 否     | -            | 会被设置到doris建表语句的PROPERTIES中                                                                                                                                   |

#### Starrocks特有配置项

| 配置名称                          | 是否必填项 | 默认值          | 描述                                                                                                                                                           |
|-------------------------------|-------|--------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| daph.starrocks.labelType      | 否     | uuid         | label生成方式。<br/>uuid表示daph程序内部为每个表都生成一个uuid label，可以确保下次启动，不会报label已存在的错误，用于整库批处理场景。<br/>tn表示daph程序内部为每个表都生成一个与表名相同名称的label，确保一个任务中的不同表名的label不会冲突，用于整库流处理场景。 |
| daph.starrocks.engineType     | 否     | olap         | 对应starrocks的引擎类型，目前支持olap与mysql                                                                                                                              |
| daph.starrocks.keyType        | 否     | DUPLICATE    | 对应starrocks的keys_type                                                                                                                                        |
| daph.starrocks.keys           | 否     | ""           | 对应starrocks的keys_type中的keys                                                                                                                                  |
| daph.starrocks.castType       | 否     | VARCHAR(255) | 当源表中的某一个或几个字段，将作为starrocks表的前几位字段，却不符合starrocks要求时，强制转换为指定类型                                                                                                 |
| daph.starrocks.commentPart    | 否     | ""           | 对应starrocks的COMMENT                                                                                                                                          |
| daph.starrocks.partitionPart  | 否     | ""           | 对应starrocks的partition_desc                                                                                                                                   |
| daph.starrocks.distPart       | 否     | ""           | 对应starrocks的distribution_desc                                                                                                                                |
| daph.starrocks.bucketPart     | 否     | ""           | 对应starrocks的distribution_desc中的buckets部分                                                                                                                     |
| daph.starrocks.rollupPart     | 否     | ""           | 对应starrocks的rollup_list                                                                                                                                      |
| daph.starrocks.orderPart      | 否     | ""           | 对应starrocks的ORDER BY                                                                                                                                         |
| 以daph.starrocks.props.开头的配置名称 | 否     | -            | 会被设置到starrocks建表语句的PROPERTIES中                                                                                                                               |

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
