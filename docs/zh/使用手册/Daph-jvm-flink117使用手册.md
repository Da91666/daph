## 准备流程

1. 准备一台具有jdk8环境的服务器
2. 从百度云盘下载daph安装包，只需下载daph-1.0.0-pkg-only_flink.tar.gz即可，下载地址是[Daph](https://pan.baidu.com/s/1r495e7YtTfK24iPXg6dBZg?pwd=p5s7)
3. 将daph安装包上传并解压到服务器的任一目录
4. 配置daph环境变量DAPH_HOME为以上目录路径

## 使用流程

1. 参照DAPH_HOME/examples/flink117/中的json文件，编写job.json与computer.json
2. 运行以下命令，启动daph-jvm-flink117任务：

```shell
sh $DAPH_HOME/bin/daph.sh -a jvm-flink117 \
-j $DAPH_HOME/examples/flink117/job.json \
-c $DAPH_HOME/examples/flink117/computer.json \
-- -Xmx2g -Xms2g
```
