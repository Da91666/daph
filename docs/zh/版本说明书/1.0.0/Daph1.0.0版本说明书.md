<!-- TOC -->
  * [功能清单](#功能清单)
    * [Daph-core功能清单](#daph-core功能清单)
    * [Daph-flink117功能清单](#daph-flink117功能清单)
    * [Daph-spark3功能清单](#daph-spark3功能清单)
  * [节点矩阵](#节点矩阵)
<!-- TOC -->

## 功能清单

### Daph-core功能清单

- 通用DAG模型流水线能力
- 任意jvm数据结构的接入能力
- 任意符合Flink/Spark编程范式的计算引擎的接入能力
- 所有节点都支持节点前与节点后，执行jdbc所支持的数据库的所有类型的sql语句
- 所有节点都支持节点前与节点后，根据源端库，在目标端库中进行批量整库建表

### Daph-flink117功能清单

- 支持DAG模型的数据输入处理输出模型
- 支持所有Flink Catalog
- 支持所有flink1.17.2版本所支持的数据源类型的数据输入与输出
- 支持所有flink-cdc3.0.1版本所支持的数据源类型的CDC数据输入
- 支持通用多表sql数据处理
- 支持多种链路类型的全量增量整库同步
    - 理论上，目前源端与目标端均支持mysql/postgresql/oracle/sqlserver/doris/starrocks/hive/iceberg/kafka
    - 实际上，目前支持的源端与目标端数据源类型，依赖于$DAPH_HOME/conf/mapping目录下具有的json文件
        - 若希望支持更多数据源类型，请参照mapping目录下的json文件，自行编写新的json文件，以支持您期望的整库同步链路类型
        - 期望大家，共建Daph
    - 已测试过的链路类型是：源端mysql/postgresql/oracle/sqlserver/doris/starrocks，目标端mysql/postgresql/oracle/sqlserver/doris/starrocks/hive/iceberg/kafka
    - 支持整库自动建表
    - 支持整库表名大写或小写
    - 支持整库字段名大写或小写

### Daph-spark3功能清单

- 支持DAG模型的数据输入处理输出模型
- 支持多达40多种数据源类型的数据输入与输出
- 支持通用多表sql数据处理

## 节点矩阵

- **daph-jvm**：见[Daph-jvm节点手册](nodes/jvm/Daph-jvm节点手册.md)
- **daph-flink117**：见[Daph-flink117节点手册](nodes/flink117/Daph-flink117节点手册.md)
- **daph-spark3**：见[Daph-spark3节点手册](nodes/spark3/Daph-spark3节点手册.md)
