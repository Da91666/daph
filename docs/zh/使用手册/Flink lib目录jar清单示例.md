<!-- TOC -->
  * [概述](#概述)
  * [jar清单示例](#jar清单示例)
    * [Flink1.17.2安装包lib目录已存在的jars](#flink1172安装包lib目录已存在的jars)
    * [Daph-core jars](#daph-core-jars)
    * [Daph-flink117 jar](#daph-flink117-jar)
    * [Daph-flink117连接器所需jars](#daph-flink117连接器所需jars)
<!-- TOC -->

## 概述

Daph-flink117的安装部署分为2个部分：

- 将Daph安装包安装部署到集群每个节点，设置DAPH_HOME
- 将DAPH_HOME/jars目录中core目录及子目录的jars、computers/flink117目录及子目录的jars有选择地放入FLINK_HOME/lib目录中

FLINK_HOME/lib目录的最终的jars清单分为四个部分，分别是

- Flink1.17.2安装包lib目录已存在的jars【必须有】
- Daph-core jars【必须有】
- Daph-flink117 jar【必须有】
- Daph-flink117连接器所需jars【这部分jar包，可根据需求，随意增减】

## jar清单示例

### Flink1.17.2安装包lib目录已存在的jars

```text
flink-cep-1.17.2.jar
flink-connector-files-1.17.2.jar
flink-csv-1.17.2.jar
flink-dist-1.17.2.jar
flink-json-1.17.2.jar
flink-scala_2.12-1.17.2.jar
flink-table-api-java-uber-1.17.2.jar
flink-table-planner_2.12-1.17.2.jar
flink-table-runtime-1.17.2.jar
log4j-1.2-api-2.17.1.jar
log4j-api-2.17.1.jar
log4j-core-2.17.1.jar
log4j-slf4j-impl-2.17.1.jar
```

### Daph-core jars

```text
cli-parser-1.1.6.jar
commons-codec-1.11.jar
commons-lang3-3.12.0.jar
commons-logging-1.2.jar
daph-core-1.0.0-SNAPSHOT.jar
jackson-annotations-2.15.2.jar
jackson-core-2.15.2.jar
jackson-databind-2.15.2.jar
jackson-module-scala_2.12-2.15.2.jar
json4s-ast_2.12-3.5.3.jar
json4s-core_2.12-3.5.3.jar
json4s-jackson_2.12-3.5.3.jar
jsqlparser-4.0.jar
log4j-api-scala_2.12-12.0.jar
scala-collection-compat_2.12-2.9.0.jar
guava-12.0.1.jar
httpclient-4.5.13.jar
httpcore-4.4.13.jar
jsr305-1.3.9.jar
kafka-clients-3.4.0.jar
lz4-java-1.8.0.jar
protobuf-java-3.11.4.jar
snappy-java-1.1.8.4.jar
zstd-jni-1.5.2-1.jar
```

### Daph-flink117 jar

```text
daph-flink117-1.0.0-SNAPSHOT.jar
```

### Daph-flink117连接器所需jars

```text
flink-cdc-dist-3.0.1.jar
flink-connector-hive_2.12-1.17.2.jar
flink-connector-jdbc-3.1.1-1.17.jar
flink-sql-avro-1.17.2.jar
flink-sql-connector-hbase-2.2-1.17.2.jar
flink-sql-connector-hive-3.1.3_2.12-1.17.2.jar
flink-sql-connector-kafka-3.0.0-1.17.jar
flink-sql-connector-mysql-cdc-3.0.1.jar
flink-sql-connector-oracle-cdc-3.0.1.jar
flink-sql-connector-postgres-cdc-3.0.1.jar
flink-sql-connector-sqlserver-cdc-3.0.1.jar
hudi-common-0.14.1.jar
hudi-flink-0.14.1.jar
mssql-jdbc-9.2.0.jre8.jar
mysql-connector-j-8.0.33.jar
ojdbc8-12.2.0.1.jar
paranamer-2.8.jar
postgresql-42.7.0.jar
```
