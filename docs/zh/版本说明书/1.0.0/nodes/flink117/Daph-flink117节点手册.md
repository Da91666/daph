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

- 每个输入节点都可以流转多个表到多个下游节点
- 完美支持Flink catalog
- 仅2种连接器节点，但已支持flink1.17.2与flink-cdc所支持的所有数据源类型，已支持多种链路类型的全量与增量整库同步

### 支持的数据源

支持的数据源类型，请参考：

- [flink-docs-release-1.17/docs/connectors/table](https://nightlies.apache.org/flink/flink-docs-release-1.17/docs/connectors/table/overview/)
- [flink-cdc-docs-release-3.0](https://nightlies.apache.org/flink/flink-cdc-docs-release-3.0/)

### 节点列表

| 支持的数据源类型 | 节点标识                                            | 节点类型 | 节点功能                                         | 流批类型 | 节点说明书                                                               |
|----------|-------------------------------------------------|------|----------------------------------------------|------|---------------------------------------------------------------------|
| 多种数据源    | Flink117.sql.general.connector.GaMultipleInput  | 输入节点 | 可将多个数据表或文件读取成多个flink表，注册到TableEnv，并流转到多个下游节点 | 流批   | [节点说明书](connectors/String.general.connector.GaMultipleInput说明书.md)  |
| 多种数据源    | Flink117.sql.general.connector.GaMultipleOutput | 输出节点 | 将TableEnv中的多个表，写出到多个外部数据表或文件                 | 流批   | [节点说明书](connectors/String.general.connector.GaMultipleOutput说明书.md) |

## 转换器节点

### 概况

- 一个通用多表Sql节点

### 节点列表

| 输入输出数据结构              | 节点标识                                     | 节点类型   | 节点功能                                                    | 流批类型 | 节点说明书                                                          |
|-----------------------|------------------------------------------|--------|---------------------------------------------------------|------|----------------------------------------------------------------|
| 输入多个String，输出多个String | Flink117.sql.general.transformer.GaMMSQL | 综合处理节点 | 基于截止上游节点时的TableEnv，执行sql，更新TableEnv，以供下游节点使用最新的TableEnv | 流批   | [节点说明书](transformers/String.general.transformer.GaMMSQL说明书.md) |
