#!/bin/bash

script_dir=$(dirname "$0")
root_dir=$(pwd)
SHELL_PATH=$root_dir/$0
BIN_DIR=$(dirname "$SHELL_PATH")
DAPH_ROOT_DIR=$(dirname "$BIN_DIR")
echo 'DAPH HOME DIR : '$DAPH_ROOT_DIR
sh $BIN_DIR"/daph-env.sh"
if [ -z $SPARK_HOME ] 
then
    echo "must set SPARK_HOME env."
    exit 1
fi


if [ -z $DAPH_HOME ] 
then
    echo "must set DAPH_HOME env."
    exit 1
fi

echo "start copy daph jars to spark jar"
cp $DAPH_HOME/jars/core/*.jar $SPARK_HOME/jars/
cp $DAPH_HOME/jars/core/lib/*.jar $SPARK_HOME/jars/

cp $DAPH_HOME/jars/computers/spark3/*.jar $SPARK_HOME/jars/
cp $DAPH_HOME/jars/computers/spark3/lib/*.jar $SPARK_HOME/jars/
echo "end copy daph jars to spark jars"
exit 0