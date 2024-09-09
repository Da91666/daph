<!-- TOC -->

* [连接器节点](#连接器节点)
    * [概况](#概况)
    * [支持的数据源](#支持的数据源)
    * [节点列表](#节点列表)
* [转换器节点](#转换器节点)
    * [概况](#概况-1)
    * [节点列表](#节点列表-1)

<!-- TOC -->

## 连接器节点

### 概况

- 每个输入节点都可以流转一个DataFrame到多个下游节点
- 每个输出节点均只接收来自一个上游节点的一个DataFrame
- 已支持44种数据源类型，详见[支持的数据源](#支持的数据源)与[节点列表](#节点列表)中的**节点说明书**
- CommonInput与CommonOutput支持任意符合Spark DataFrame read/write或readStream/writeStream规范的数据源
    - 目前已支持42种数据源类型，只需要在$SPARK_HOME/jars下放入更多spark datasources连接器jars，即可引入更多数据源类型
    - 详见[节点列表](#节点列表)中的**节点说明书**

### 支持的数据源

可自行增删改pom.xml中的依赖jar版本，参照[Daph二次开发手册](../../../../../../docs/zh/Daph二次开发手册.md)
编译打包，参照[Daph安装包制作手册](../../../../../../docs/zh/Daph安装包制作手册.md)
与[Daph安装部署手册](../../../../../../docs/zh/Daph安装部署手册.md)安装部署新打包的daph-spark3 jars，以适配自己的平台。

| 数据源类型         | 流批输入输出支持情况 | 依赖jar版本                                                                                                                                                           |
|---------------|------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| mysql         | 批io        | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| oracle        | 批io        | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| postgresql    | 批io        | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| sqlserver     | 批io        | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| db2           | 批io        | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| hive          | 批io        | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| doris         | 流io，批io    | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| starrocks     | 流io，批io    | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| tidb          | 批io        | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| kudu          | 批io        | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| oceanbase     | 批io        | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| snowflake     | 批io        | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| vertica       | 批io        | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| dm            | 批io        | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| gbase         | 批io        | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| cassandra     | 流o，批io     | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| hudi          | 流io，批io    | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| iceberg       | 流io，批io    | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| deltalake     | 流io，批io    | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| paimon        | 流io，批io    | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| kafka         | 流io，批io    | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| pulsar        | 流io，批io    | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| mongodb       | 批io        | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| elasticsearch | 流o，批io     | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| counchbase    | 批io        | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| clickhouse    | 批io        | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| tdengine      | 批io        | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| iotdb         | 批io        | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| neo4j         | 流io，批io    | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| hugegraph     | 批o         | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| phoenix       | 批io        | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| socket        | 流i         | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| text          | 流io，批io    | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| csv           | 流io，批io    | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| json          | 流io，批io    | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| orc           | 流io，批io    | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| parquet       | 流io，批io    | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| avro          | 流io，批io    | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| http          | 批i         | 见[daph-node-spark3-dataframe-batch-connector-http pom.xml](../../../../../../daph-nodes/daph-node-spark3/daph-node-spark3-dataframe-batch-connector-http/pom.xml) |
| hbase         | 批io        | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| redis         | 流io，批io    | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| singlestore   | 批io        | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| weaviate      | 批o         | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |
| trino         | 批io        | 见[daph-spark3 pom.xml](../../../../../../daph-spark3/pom.xml)                                                                                                     |

### 节点列表

| 支持的数据源类型      | 节点标识                                           | 节点类型 | 节点功能                                              | 流批类型 | 节点说明书                                                             |
|---------------|------------------------------------------------|------|---------------------------------------------------|------|-------------------------------------------------------------------|
| 多种数据源         | Spark3.dataframe.batch.connector.CommonInput   | 输入节点 | 可将一个数据表或文件读取成一个DataFrame，并流转到多个下游节点               | 批    | [节点说明书](connectors/Dataframe.batch.connector.CommonInput说明书.md)   |
| 多种数据源         | Spark3.dataframe.batch.connector.CommonOutput  | 输出节点 | 接收一个DataFrame，将DataFrame承载的数据写出到一个数据表或文件          | 批    | [节点说明书](connectors/Dataframe.batch.connector.CommonOutput说明书.md)  |
| 多种数据源         | Spark3.dataframe.stream.connector.CommonInput  | 输入节点 | 可将一个数据表或文件读取成一个DataFrame，并流转到多个下游节点               | 流    | [节点说明书](connectors/Dataframe.stream.connector.CommonInput说明书.md)  |
| 多种数据源         | Spark3.dataframe.stream.connector.CommonOutput | 输出节点 | 接收一个DataFrame，将DataFrame承载的数据写出到一个数据表或文件          | 流    | [节点说明书](connectors/Dataframe.stream.connector.CommonOutput说明书.md) |
| elasticsearch | Spark3.dataframe.batch.connector.ESOutput      | 输出节点 | 接收一个DataFrame，将DataFrame承载的数据写出到一个elasticsearch文档 | 批    | [节点说明书](connectors/Dataframe.batch.connector.ESOutput说明书.md)      |
| hive          | Spark3.dataframe.batch.connector.HiveInput     | 输入节点 | 将一个hive数据表读取成一个DataFrame，并流转到多个下游节点               | 批    | [节点说明书](connectors/Dataframe.batch.connector.HiveInput说明书.md)     |
| http          | Spark3.dataframe.batch.connector.HttpInput     | 输入节点 | 将一个http api数据读取成一个DataFrame，并流转到多个下游节点            | 批    | [节点说明书](connectors/Dataframe.batch.connector.HttpInput说明书.md)     |
| kafka/pulsar  | Spark3.dataframe.batch.connector.KPOutput      | 输出节点 | 接收一个DataFrame，将DataFrame承载的数据写出到一个kafka/pulsar主题  | 批    | [节点说明书](connectors/Dataframe.batch.connector.KPOutput说明书.md)      |
| kafka/pulsar  | Spark3.dataframe.stream.connector.KPOutput     | 输出节点 | 接收一个DataFrame，将DataFrame承载的数据写出到一个kafka/pulsar主题  | 流    | [节点说明书](connectors/Dataframe.stream.connector.KPOutput说明书.md)     |

## 转换器节点

### 概况

- 具有3个不同类型的通用Sql节点
- 具有map/filter/join节点

### 节点列表

| 输入输出数据结构                    | 节点标识                                        | 节点类型   | 节点功能                                           | 流批类型 | 节点说明书                                                            |
|-----------------------------|---------------------------------------------|--------|------------------------------------------------|------|------------------------------------------------------------------|
| 输入一个DataFrame，输出一个DataFrame | Spark3.dataframe.general.transformer.Sql    | 综合处理节点 | 从一个上游节点接收一个DataFrame，转换成一个DataFrame，并流转到多个下游节点 | 流批   | [节点说明书](transformers/Dataframe.general.transformer.Sql说明书.md)    |
| 输入多个DataFrame，输出一个DataFrame | Spark3.dataframe.general.transformer.MSql   | 综合处理节点 | 从多个上游节点接收多个DataFrame，转换成一个DataFrame，并流转到多个下游节点 | 流批   | [节点说明书](transformers/Dataframe.general.transformer.MSql说明书.md)   |
| 输入多个DataFrame，输出多个DataFrame | Spark3.dataframe.general.transformer.MMSql  | 综合处理节点 | 从多个上游节点接收一个DataFrame，转换成多个DataFrame，并流转到多个下游节点 | 流批   | [节点说明书](transformers/Dataframe.general.transformer.MMSql说明书.md)  |
| 输入一个DataFrame，输出一个DataFrame | Spark3.dataframe.general.transformer.Map    | 单表处理节点 | 从一个上游节点接收一个DataFrame，转换成一个DataFrame，并流转到多个下游节点 | 流批   | [节点说明书](transformers/Dataframe.general.transformer.Map说明书.md)    |
| 输入一个DataFrame，输出一个DataFrame | Spark3.dataframe.general.transformer.Filter | 单表处理节点 | 从一个上游节点接收一个DataFrame，转换成一个DataFrame，并流转到多个下游节点 | 流批   | [节点说明书](transformers/Dataframe.general.transformer.Filter说明书.md) |
| 输入两个DataFrame，输出一个DataFrame | Spark3.dataframe.general.transformer.Join   | 多表处理节点 | 从两个上游节点接收两个DataFrame，转换成一个DataFrame，并流转到多个下游节点 | 流批   | [节点说明书](transformers/Dataframe.general.transformer.Join说明书.md)   |
 