#!/bin/bash

script_dir=$(dirname "$0")
root_dir=$(pwd)
SHELL_PATH=$root_dir/$0
BIN_DIR=$(dirname "$SHELL_PATH")
DAPH_ROOT_DIR=$(dirname "$BIN_DIR")
echo $DAPH_ROOT_DIR
DAPH_DIR=$DAPH_ROOT_DIR
NODES_FILE=$DAPH_DIR/conf/deploy-nodes
FLINK_NODES_FILE=$DAPH_DIR/conf/flink-client-nodes
SPARK_NODES_FILE=$DAPH_DIR/conf/spark-client-nodes
DAPH_ROOT_DIR=$(dirname $DAPH_DIR)
cat "$NODES_FILE" | while read -r line; do
  echo "start scp -v $DAPH_ROOT_DIR/daph to node "$line $DAPH_ROOT_DIR
  # 对变量进行操作或处理
  scp -v -r $DAPH_ROOT_DIR/daph root@$line:$DAPH_ROOT_DIR/
done

echo "copy daph jars to spark jars start!"
while read LINE
do
    echo $LINE
    ssh "root@$LINE" "$DAPH_DIR/bin/copy_spark_work_jar.sh" 
done < $SPARK_NODES_FILE
echo  "copy daph jars to spark jars end!"


echo "copy daph jars to flink jars start!"
while read LINE
do   
    ssh "root@$LINE" "$DAPH_DIR/bin/copy_flink_work_jar.sh" 
done < $FLINK_NODES_FILE
echo  "copy daph jars to flink jars end!"