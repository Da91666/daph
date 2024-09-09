<!-- TOC -->
  * [概况](#概况)
  * [节点配置项](#节点配置项)
  * [节点列表](#节点列表)
  * [使用案例](#使用案例)
    * [DAG图](#dag图)
    * [job.json](#jobjson)
<!-- TOC -->
## 概况

- 目前仅有3个节点，功能均是执行http url，配置也完全相同
- 可自行修改节点代码，满足特定需求

## 节点配置项

| 配置名称    | 配置类型               | 是否必填项 | 默认值 | 描述                  |
|---------|--------------------|-------|-----|---------------------|
| url     | String             | 是     | -   | http url            |
| method  | String             | 否     | get | get/post/put/delete |
| headers | Map[String,String] | 否     | -   | http请求头             |
| params  | Map[String,String] | 否     | -   | http请求参数            |

## 节点列表

| 节点标识                                              | 节点类型 | 节点功能                                                               |
|---------------------------------------------------|------|--------------------------------------------------------------------|
| JVM.httpresult.simple.connector.HttpInput         | 输入节点 | 执行一个http url，获取一个HttpResult(code: Int, message: String)，并流转到多个下游节点 |
| JVM.httpresult.simple.transformer.HttpTransformer | 处理节点 | 执行一个http url，获取一个HttpResult(code: Int, message: String)，并流转到多个下游节点 |
| JVM.httpresult.simple.connector.HttpOutput        | 输出节点 | 执行一个http url                                                       |

## 使用案例

### DAG图

```mermaid
graph LR
    a[http-in] --> aa[http-tr] --> aaa[http-out];
```

### job.json

```json
{
  "nodes": [
    {
      "flag": "JVM.httpresult.simple.connector.HttpInput",
      "config": {
        "url": "https://cn.bing.com/search?q=w"
      },
      "outLines": [
        "in-line"
      ]
    },
    {
      "flag": "JVM.httpresult.simple.transformer.HttpTransformer",
      "config": {
        "url": "https://cn.bing.com/search?q=x"
      },
      "inLines": [
        "in-line"
      ],
      "outLines": [
        "tr-line"
      ]
    },
    {
      "flag": "JVM.httpresult.simple.connector.HttpOutput",
      "config": {
        "url": "https://cn.bing.com/search?q=y"
      },
      "inLines": [
        "tr-line"
      ]
    }
  ]
}
```