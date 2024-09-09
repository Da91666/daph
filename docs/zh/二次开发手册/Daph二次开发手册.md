<!-- TOC -->
  * [概述](#概述)
  * [开发环境准备](#开发环境准备)
  * [计算器开发](#计算器开发)
    * [概况](#概况)
    * [详情](#详情)
      * [1、实现Computer抽象类](#1实现computer抽象类)
      * [2、继承与定义Node抽象类](#2继承与定义node抽象类)
  * [节点开发](#节点开发)
  * [调试](#调试)
<!-- TOC -->

## 概述

Daph二次开发包含两个方面的开发，分别是计算器开发与节点开发。

## 开发环境准备

| 元素    | 版本       |
|-------|----------|
| JDK   | 1.8      |
| IDEA  | 2021及以上  |
| Maven | 3.8.1及以上 |

## 计算器开发

### 概况

计算器开发包含3个方面，分别是实现Computer抽象类、继承与定义Node抽象类、实现特定功能接口。<br>
其中，实现Computer抽象类、继承与定义Node抽象类是必选项，实现特定功能接口是可选项。

### 详情

#### 1、实现Computer抽象类

实现entryPoint即可。<br>
可参考JVMComputer/SparkComputer/FlinkComputer。

#### 2、继承与定义Node抽象类

按需实现Node即可。<br>
请参考daph-jvm/daph-flink117/daph-spark3工程。

## 节点开发

请参考daph-nodes中的节点工程。

## 调试

请参考daph-jvm/daph-flink117/daph-spark3工程中的test源码。