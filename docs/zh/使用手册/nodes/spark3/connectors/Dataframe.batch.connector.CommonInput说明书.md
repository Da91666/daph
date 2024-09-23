## 目录

- [简介](#简介)
- [配置项](#配置项)
    - [节点配置项](#节点配置项)
    - [支持的数据源类型及详细配置项](#支持的数据源类型及详细配置项)
- [使用案例](#使用案例)
    - [DAG图](#DAG图)
    - [job.json](#jobjson)

## 简介

- **节点标识**：Spark3.dataframe.batch.connector.CommonInput
- **节点类型**：输入节点
- **节点功能**：可将一个数据表或文件读取成一个DataFrame，并流转到多个下游节点
- **流批类型**：批
- **支持的数据源类型**：见[支持的数据源类型及详细配置项](#支持的数据源类型及详细配置项)

## 配置项

### 节点配置项

| 配置名称           | 配置类型               | 是否必填项 | 默认值 | 描述                                                                                                                                   |
|----------------|--------------------|-------|-----|--------------------------------------------------------------------------------------------------------------------------------------|
| format         | String             | 否     | -   | 若未配置tableName或sql，则必填。详见[支持的数据源类型及详细配置项](#支持的数据源类型及详细配置项)                                                                            |
| cfg            | Map[String,String] | 否     | -   | 若未配置tableName或sql，则必填。详见[支持的数据源类型及详细配置项](#支持的数据源类型及详细配置项)                                                                            |
| paths          | Array[String]      | 否     | -   | 对应load方法参数，请参考[sql-data-sources-load-save-functions](https://spark.apache.org/docs/latest/sql-data-sources-load-save-functions.html) |
| isCatalogTable | Boolean            | 否     | -   | 是否是catalog表                                                                                                                          |
| tableName      | String             | 否     | -   | 若未配置format或sql，则必填。表名                                                                                                                |
| sql            | String             | 否     | -   | 若未配置format或tableName，则必填。spark sql语句                                                                                                 |

### 支持的数据源类型及详细配置项

| 数据源类型         | format                                      | cfg                                                                                                                                                               | 说明  |
|---------------|---------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----|
| mysql         | jdbc                                        | 请参考[Spark sql-data-sources](https://spark.apache.org/docs/3.5.1/sql-data-sources.html)                                                                            |     |
| oracle        | jdbc                                        | 同上                                                                                                                                                                |     |
| postgresql    | jdbc                                        | 同上                                                                                                                                                                |     |
| sqlserver     | jdbc                                        | 同上                                                                                                                                                                |     |
| db2           | jdbc                                        | 同上                                                                                                                                                                |     |
| doris         | doris                                       | 请参考[spark-doris-connector](https://doris.apache.org/zh-CN/docs/dev/ecosystem/spark-doris-connector/)                                                              |     |
| starrocks     | starrocks                                   | 请参考[Spark-connector-starrocks](https://docs.starrocks.io/zh/docs/loading/Spark-connector-starrocks/)                                                              |     |
| tidb          | jdbc                                        | 请参考[Spark sql-data-sources](https://spark.apache.org/docs/3.5.1/sql-data-sources.html)                                                                            |     |
| kudu          | kudu                                        | 请参考[kudu spark](https://kudu.apache.org/docs/developing.html)                                                                                                     |     |
| oceanbase     | jdbc                                        | 请参考[Spark sql-data-sources](https://spark.apache.org/docs/3.5.1/sql-data-sources.html)                                                                            |     |
| snowflake     | net.snowflake.spark.snowflake               | 请参考[snowflake spark](https://docs.snowflake.com/en/user-guide/spark-connector)                                                                                    |     |
| vertica       | com.vertica.spark.datasource.DefaultSource  | 请参考[vertica spark](https://github.com/vertica/spark-connector)                                                                                                    |     |
| dm            | jdbc                                        | 请参考[Spark sql-data-sources](https://spark.apache.org/docs/3.5.1/sql-data-sources.html)                                                                            |     |
| gbase         | jdbc                                        | 同上                                                                                                                                                                |     |
| cassandra     | org.apache.spark.sql.cassandra              | 请参考[spark-cassandra-connector](https://github.com/datastax/spark-cassandra-connector/blob/master/doc/14_data_frames.md)                                           |     |
| hudi          | hudi                                        | 请参考<br>[hudi spark doc1](https://hudi.apache.org/cn/docs/quick-start-guide)<br>[hudi spark doc2](https://hudi.apache.org/cn/docs/configurations#SPARK_DATASOURCE) |     |
| iceberg       | iceberg                                     | 请参考[iceberg spark doc1](https://iceberg.apache.org/docs/1.4.0/spark-getting-started/)                                                                             |     |
| deltalake     | delta                                       | 请参考[delta spark](https://docs.delta.io/latest/index.html)                                                                                                         |     |
| paimon        | paimon                                      | 请参考[paimon spark](https://paimon.apache.org/docs/0.7/engines/spark/)                                                                                              |     |
| kafka         | kafka                                       | 请参考[structured-streaming-kafka-integration](https://spark.apache.org/docs/3.5.1/structured-streaming-kafka-integration.html)                                      |     |
| pulsar        | pulsar                                      | 请参考[pulsar-spark](https://github.com/streamnative/pulsar-spark/blob/master/README.md)                                                                             |     |
| mongodb       | mongo 或 com.mongodb.spark.sql.DefaultSource | 请参考[mongodb spark-connector](https://www.mongodb.com/docs/spark-connector/v2.4/)                                                                                  |     |
| elasticsearch | es                                          | 请参考[elasticsearch spark](https://www.elastic.co/guide/en/elasticsearch/hadoop/current/spark.html)                                                                 |     |
| counchbase    | couchbase.query<br/>couchbase.analytics     | 请参考[Couchbase spark-connector](https://docs.couchbase.com/spark-connector/current/spark-sql.html)                                                                 |     |
| clickhouse    | jdbc                                        | 请参考[Spark sql-data-sources](https://spark.apache.org/docs/3.5.1/sql-data-sources.html)                                                                            |     |
| tdengine      | jdbc                                        | 同上                                                                                                                                                                |     |
| iotdb         | org.apache.iotdb.spark.db                   | 请参考[Spark-IoTDB](https://iotdb.apache.org/zh/UserGuide/latest/Ecosystem-Integration/Spark-IoTDB.html)                                                             |     |
| neo4j         | org.neo4j.spark.DataSource                  | 请参考[Spark neo4j](https://neo4j.com/docs/spark/4.2/overview/)                                                                                                      |     |
| phoenix       | phoenix                                     | 请参考[phoenix_spark](https://phoenix.apache.org/phoenix_spark.html)                                                                                                 |     |
| socket        | socket                                      | 请参考[Spark sql-data-sources](https://spark.apache.org/docs/3.5.1/sql-data-sources.html)                                                                            |     |
| text          | text                                        | 同上                                                                                                                                                                |     |
| csv           | csv                                         | 同上                                                                                                                                                                |     |
| json          | json                                        | 同上                                                                                                                                                                |     |
| orc           | orc                                         | 同上                                                                                                                                                                |     |
| parquet       | parquet                                     | 同上                                                                                                                                                                |     |
| avro          | avro                                        | 同上                                                                                                                                                                |     |
| singlestore   | singlestore                                 | 请参考[singlestore-spark-connector](https://github.com/memsql/singlestore-spark-connector)                                                                           |     |
| trino         | jdbc                                        | 请参考[Spark sql-data-sources](https://spark.apache.org/docs/3.5.1/sql-data-sources.html)                                                                            |     |
| redis         | org.apache.spark.sql.redis                  | 请参考[spark-redis](https://github.com/RedisLabs/spark-redis/blob/master/doc)                                                                                        |     |
| hbase         | org.apache.hadoop.hbase.spark               | 请参考[hbase-spark](https://hbase.org.cn/docs/137.html)                                                                                                              |     |

## 使用案例

### DAG图

```mermaid
graph LR
    a[mysql-in] --> aa[postgres-out];
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
        "in-line"
      ]
    },
    {
      "flag": "Spark3.dataframe.batch.connector.CommonOutput",
      "config": {
        "format": "jdbc",
        "cfg": {
          "url": "jdbc:postgresql://127.0.0.1:5432/postgres",
          "dbtable": "public.out_t",
          "user": "postgres",
          "password": "postgres",
          "driver": "org.postgresql.Driver"
        }
      },
      "inLines": [
        "in-line"
      ]
    }
  ]
}
```
