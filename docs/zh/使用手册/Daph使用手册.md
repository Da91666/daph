<!-- TOC -->
  * [Daph使用手册](#daph使用手册)
  * [Daph计算器【必看】](#daph计算器必看)
    * [daph-jvm计算器](#daph-jvm计算器)
    * [daph-jvm-flink117计算器](#daph-jvm-flink117计算器)
    * [daph-jvm-spark3计算器](#daph-jvm-spark3计算器)
    * [daph-flink117计算器](#daph-flink117计算器)
    * [daph-spark3计算器](#daph-spark3计算器)
  * [Daph文件【必看】](#daph文件必看)
    * [Daph安装包文件【daph-版本号-pkg-后缀.zip】](#daph安装包文件daph-版本号-pkg-后缀zip)
    * [Daph job启动脚本文件【daph.sh】](#daph-job启动脚本文件daphsh)
    * [Daph job配置文件【job.json】](#daph-job配置文件jobjson)
      * [概述](#概述)
      * [配置项](#配置项)
        * [1）options配置项](#1options配置项)
        * [2）node配置项](#2node配置项)
      * [示例](#示例)
        * [文件内容](#文件内容)
        * [对应的DAG模型](#对应的dag模型)
    * [Daph计算器配置文件【computer.json】](#daph计算器配置文件computerjson)
      * [概述](#概述-1)
      * [详述](#详述)
        * [daph-flink117 computer.json内容示例](#daph-flink117-computerjson内容示例)
        * [daph-spark3 computer.json内容示例](#daph-spark3-computerjson内容示例)
<!-- TOC -->

## Daph使用手册

- daph-jvm见[Daph-jvm使用手册](Daph-jvm使用手册.md)
- daph-jvm-flink117见[Daph-jvm-flink117使用手册](Daph-jvm-flink117使用手册.md)
- daph-jvm-spark3见[Daph-jvm-spark3使用手册](Daph-jvm-spark3使用手册.md)
- daph-flink117见[Daph-flink117使用手册](Daph-flink117使用手册.md)
- daph-spark3见[Daph-spark3使用手册](Daph-spark3使用手册.md)

## Daph计算器【必看】

Daph目前总共有5种计算器，分别是daph-jvm、daph-jvm-flink117、daph-jvm-spark3、daph-flink117、daph-spark3。

### daph-jvm计算器

- 目前具有的功能是：以DAG顺序控制多个REST api的执行顺序，可开发插件，实现更多功能
- 该计算器的使用只依赖jdk环境
- 对应的代码工程是daph-jvm

### daph-jvm-flink117计算器

- 具有的功能是：全量增量整库整表数据集成，流批一体海量复杂数据处理
- 该计算器的使用只依赖jdk环境，具有的功能与daph-flink117完全一致，相当于daph-flink117计算器的单机版本
- 对应的代码工程是daph-flink117

### daph-jvm-spark3计算器

- 具有的功能是：全量增量整表数据集成，流批一体海量复杂数据处理
- 该计算器的使用只依赖jdk环境，具有的功能与daph-spark3完全一致，相当于daph-spark3计算器的单机版本
- 对应的代码工程是daph-spark3

### daph-flink117计算器

- 具有的功能是：全量增量整库整表数据集成，流批一体海量复杂数据处理
- 该计算器的使用依赖jdk环境与flink1.17.2集群环境
- 对应的代码工程是daph-flink117

### daph-spark3计算器

- 具有的功能是：全量增量整表数据集成，流批一体海量复杂数据处理
- 该计算器的使用依赖jdk环境与spark3.5.1集群环境
- 对应的代码工程是daph-spark3

## Daph文件【必看】

### Daph安装包文件【daph-版本号-pkg-后缀.zip】

- 可根据期望使用的计算器，选择下载相应的安装包
- 安装包位于百度云盘，下载地址是[Daph](https://pan.baidu.com/s/1r495e7YtTfK24iPXg6dBZg?pwd=p5s7)
- 后缀为 all 的安装包，可用于安装部署目前的所有计算器
- 后缀为 only_flink117 的安装包，可用于安装部署daph-jvm-flink117与daph-flink117计算器

### Daph job启动脚本文件【daph.sh】

daph.sh是Daph启动总入口，可用于启动所有Daph计算器类型的作业。

```shell
sh DAPH_HOME路径/bin/daph.sh \
# 必填项。指定要启动的计算器，可选项有jvm/jvm-spark3/jvm-flink117/spark3/flink117
-a jvm-flink117 \
# daph-flink117特有的必填项。可选项有run/run-application
-b run \
# daph-flink117特有的必填项。可选项有local/yarn-application/k8s-application
-t local \
# 必填项。同--job。指定job.json
-j DAPH_HOME路径/examples/flink117/job.json \
# daph-jvm没有此项，其他计算器必填项。同--computer。指定computer.json
-c DAPH_HOME路径/examples/flink117/computer.json \
# 可选项。同--dudps。指定日志文件，可编写与指定自己的日志配置文件
-d logXml=DAPH_HOME路径/conf/log4j2-flink117.xml \
# daph-jvm/jvm-flink117/jvm-spark3特有的可选项。破折号后的参数是java参数，会被设置到java与-cp之间
-- -Xmx2g -Xms2g
```

### Daph job配置文件【job.json】

#### 概述

job.json文件是关于daph job的配置文件。<br>
job.json文件是必须的配置文件。

#### 配置项

##### 1）options配置项

options中的配置项的数据类型均是字符串类型。

| 配置名称                           | 是否必填项 | 默认值                                   | 描述                                                              |
|--------------------------------|-------|---------------------------------------|-----------------------------------------------------------------|
| daph.home                      | 否     | DAPH_HOME路径                           | Daph安装目录，为应对yarn或k8s模式，container读取不到DAPH_HOME                   |
| daph.id                        | 否     | Default                               | 作业id                                                            |
| daph.name                      | 否     | Default                               | 作业名称                                                            |
| daph.description               | 否     | Default                               | 作业描述                                                            |
| daph.user                      | 否     | Default                               | 作业用户                                                            |
| daph.log.level                 | 否     | info                                  | 作业日志等级                                                          |
| daph.dag.model                 | 否     | dag                                   | dag：DAG模型<br/>two-point-line：两点一线模型<br/>three-point-line：三点一线模型 |
| daph.computer.type             | 否     | -                                     | 计算器类型。目前有jvm/flink117/spark3。用于非dag模型，简化job配置。                  |
| daph.node.dictionary-file.path | 否     | DAPH_HOME路径/conf/node-dictionary.json | 节点字典文件路径。若是yarn或k8s模式，且未配置daph.home，则必填；否则，不用填。                 |
| daph.node.jar-file.root-path   | 否     | DAPH_HOME路径/jars/nodes/               | 节点jar文件所在目录路径。若是yarn或k8s模式，且未配置daph.home，则必填；否则，不用填。            |
| daph.dt.mapping-file.root-path | 否     | DAPH_HOME路径/conf/mapping/             | 数据类型映射文件所在目录路径。若是yarn或k8s模式，且未配置daph.home，则必填；否则，不用填。           |

##### 2）node配置项

| 配置名称         | 配置类型             | 是否必填项 | 默认值           | 描述                                           |
|--------------|------------------|-------|---------------|----------------------------------------------|
| flag         | String           | 是     | -             | 节点标识。详见DAPH_HOME路径/conf/node-dictionary.json |
| id           | String           | 否     | $flag-从1开始的整数 | 节点ID。若未填写，则程序内部会自动生成                         |
| name         | String           | 否     | $flag-从1开始的整数 | 节点名称。若未填写，则程序内部会自动生成                         |
| config       | Any              | 是     | -             | 节点配置。不同的节点有不同的配置。详见各个计算器的节点手册                |
| inLines      | Array[String]    | 否     | -             | 节点输入线                                        |
| outLines     | Array[String]    | 否     | -             | 节点输出线                                        |
| extraOptions | Map[String, Any] | 否     | -             | 额外配置项                                        |

目前适用于所有计算器节点的extraOptions有：

| 配置名称                            | 配置类型                                                                                       | 是否必填项 | 默认值 | 描述                                  |
|---------------------------------|--------------------------------------------------------------------------------------------|-------|-----|-------------------------------------|
| daph.before.node.createEDBs     | Array[EDBConfig(sourceDBConfig: Map[String, String], targetDBConfig: Map[String, String])] | 否     | -   | 节点执行前，根据源端数据库配置，在目标端数据库创建源端整库包含的所有表 |
| daph.after.node.createEDBs      | Array[EDBConfig(sourceDBConfig: Map[String, String], targetDBConfig: Map[String, String])] | 否     | -   | 节点执行后，根据源端数据库配置，在目标端数据库创建源端整库包含的所有表 |
| daph.before.node.executeMDBSQLs | Array[Map[String, String]]                                                                 | 否     | -   | 节点执行前，根据数据库配置，在数据库中执行sql语句          |
| daph.after.node.executeMDBSQLs  | Array[Map[String, String]]                                                                 | 否     | -   | 节点执行后，根据数据库配置，在数据库中执行sql语句          |

目前daph-spark3特有的额外配置项有：

| 配置名称                                      | 配置类型   | 是否必填项 | 默认值 | 描述                |
|-------------------------------------------|--------|-------|-----|-------------------|
| daph.spark.before.node.dataframe.querySQL | String | 否     | -   | 节点执行前，执行spark sql |
| daph.spark.after.node.dataframe.querySQL  | String | 否     | -   | 节点执行后，执行spark sql |

#### 示例

##### 文件内容

```json
{
  "options": {
    "daph.home": "DAPH_HOME路径"
  },
  "nodes": [
    {
      "flag": "计算器名称...Input",
      "id": "计算器名称...Input-1",
      "name": "计算器名称...Input-1",
      "config": {},
      "outLines": [
        "in-line"
      ]
    },
    {
      "flag": "计算器名称...Transformer",
      "config": {},
      "inLines": [
        "in-line"
      ],
      "outLines": [
        "tr-line"
      ]
    },
    {
      "flag": "计算器名称...Output",
      "config": {},
      "inLines": [
        "in-line"
      ],
      "extraOptions": {
        "daph.before.node.createEDBs": [
          {
            "sourceDBConfig": {
              "daph.dbType": "mysql",
              "daph.url": "jdbc:mysql://localhost:3306/daph",
              "daph.username": "root",
              "daph.password": "fffff",
              "daph.databaseName": "daph"
            },
            "targetDBConfig": {
              "daph.dbType": "mysql",
              "daph.url": "jdbc:mysql://localhost:3306/daph",
              "daph.username": "root",
              "daph.password": "fffff",
              "daph.databaseName": "daph2",
              "daph.tableNamePrefix": "test_",
              "daph.tableNameSuffix": "_auto_generated"
            }
          }
        ],
        "daph.before.node.executeMDBSQLs": [
          {
            "daph.dbType": "mysql",
            "daph.url": "jdbc:mysql://localhost:3306/daph",
            "daph.username": "root",
            "daph.password": "fffff",
            "daph.sqls": "CREATE TABLE if not exists daph2.out_t6 (id INT PRIMARY KEY, name VARCHAR(50), age INT);CREATE TABLE if not exists daph2.out_t7 (id INT PRIMARY KEY, name VARCHAR(50), age INT)"
          }
        ]
      }
    }
  ]
}
```

##### 对应的DAG模型

```mermaid
graph LR
    a1[Input] --> aa2[Transformer] --> aaa1[Output];
```

### Daph计算器配置文件【computer.json】

#### 概述

computer.json文件是关于daph计算器的配置文件，其中的配置项可参考各个计算器源码工程中的计算器配置类。<br>
除daph-jvm计算器目前没有规划computer.json文件外，computer.json文件是必须的配置文件。

#### 详述

##### daph-flink117 computer.json内容示例

```json
{
  // Flink配置项，均会设置到StreamExecutionEnvironment中
  "envConfig": {
    "table.sql-dialect": "default",
    "execution.runtime-mode": "STREAMING",
    "execution.checkpointing.mode": "EXACTLY_ONCE",
    "execution.checkpointing.interval": "10 s",
    "table.exec.source.cdc-events-duplicate": "false",
    "table.exec.sink.not-null-enforcer": "DROP"
  },
  // 用于创建全局catalog
  "catalogConfigs": [
    {
      "enabled": "true",
      "type": "jdbc",
      "name": "mysql_catalog",
      "default-database": "daph",
      "username": "root",
      "password": "root",
      "base-url": "jdbc:mysql://192.168.6.66:3306"
    },
    {
      "enabled": "true",
      "type": "jdbc",
      "name": "pg_catalog",
      "default-database": "bigdata",
      "username": "bigdata",
      "password": "root",
      "base-url": "jdbc:postgresql://192.168.6.66:5432"
    },
    {
      "enabled": "true",
      "type": "hive",
      "name": "hive_catalog",
      "default-database": "flink_db",
      "hive-conf-dir": "/opt/datasophon/hive-3.1.0/conf"
    },
    {
      "enabled": "true",
      "type": "iceberg",
      "name": "iceberg_catalog",
      "catalog-type": "hive",
      "uri": "thrift://192.168.6.66:9083",
      "clients": "5",
      "property-version": "1",
      "warehouse": "hdfs://192.168.6.66:8020/user/hive/warehouse",
      "cache-enabled": "true",
      "cache.expiration-interval-ms": "5000"
    }
  ]
}
```

##### daph-spark3 computer.json内容示例

```json
{
  // Spark配置项。此处的配置项均会设置到SparkConf
  "envConfig": {
    "spark.master": "local[*]",
    "spark.app.name": "Spark Example",
    "spark.sql.warehouse.dir": "warehouse",
    "spark.ui.enabled": "false"
  }
}
```
