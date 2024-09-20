
## 准备流程

1. 准备具有jdk8环境与spark3.5.1环境的服务器集群
2. 从百度云盘下载daph安装包，只需下载daph-1.0.0-pkg-only_spark.tar.gz即可，下载地址是[Daph](https://pan.baidu.com/s/1r495e7YtTfK24iPXg6dBZg?pwd=p5s7)
3. 在服务器集群的每台服务器节点上执行以下操作：
    1. 将daph安装包上传并解压到任一目录
    2. 配置daph环境变量DAPH_HOME为以上目录路径
    3. 将$DAPH_HOME/jars/core目录及子目录内的所有jar包，及$DAPH_HOME/jars/computers/spark3目录及子目录内的jar包，拷贝到SPARK_HOME/jars目录

## 使用流程

1. 参照DAPH_HOME/examples/spark3/中的json文件，编写job.json与computer.json
2. 运行以下命令，启动daph-spark3任务：

```shell
sh $DAPH_HOME/bin/daph.sh -a spark3 \
-j $DAPH_HOME/examples/spark3/job.json \
-c $DAPH_HOME/examples/spark3/computer.json
```
