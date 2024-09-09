## 目录

- mysql：表示源头是mysql，目标也是mysql
- mysql_doris：表示源头是mysql，目标是doris
  其他同理。

## job.json文件

Daph-flink117计算器支持全量增量整库整表同步：

- cdc：表示开启增量
- edb：表示整库

Daph-flink117计算器对应的job.json文件中的node配置项，提供了三种配置方式，三种配置方式可同时生效，通用性极强：

- catalog方式：即通过配置catalog引入数据表
- createConfig方式：即通过配置数据源信息引入数据表
- createSql方式：即通过配置flink sql语句引入数据表

相对应的示例文件名称规则如下：

- job-cg.json：表示以catalog方式引入源头表，以catalog方式引入目标表，建立任务
- job-cc.json：表示以createConfig方式引入源头表，以createConfig方式引入目标表，建立任务
- job-cs.json：表示以createSql方式引入源头表，以createSql方式引入目标表，建立任务
- job-cg_cc.json：表示以catalog方式引入源头表，以createConfig方式引入目标表，建立任务
- job-cc_cg.json：表示以createConfig方式引入源头表，以catalog方式引入目标表，建立任务
- job-cg-cdc.json：表示以catalog方式引入源头表，以catalog方式引入目标表，建立开启增量的任务
- job-cg-edb.json：表示以catalog方式引入源头表，以catalog方式引入目标表，建立整库任务
- job-cg-edb_cdc.json：表示以catalog方式引入源头表，以catalog方式引入目标表，建立开启增量的整库任务
