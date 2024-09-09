#!/bin/bash

bin=$(dirname "$0")
bin=$(cd "$bin"; pwd)
DAPH_DIR=$(dirname $bin)
echo "DAPH HOME DIR: ${DAPH_DIR}"
sh $DAPH_DIR/bin/daph-env.sh
if [ -z $FLINK_HOME ] 
then
    echo "must set FLINK_HOME env."
    exit 1
fi


if [ -z $DAPH_HOME ] 
then
    echo "must set DAPH_HOME env."
    exit 1
fi

FLINK_YARN_JARS_FILE=$DAPH_DIR/conf/flink_yarn_jars_file
DEST_DIR=$FLINK_HOME/jars

echo "Start to copy daph jars from daph dir to flink lib"

mv $FLINK_HOME/jars/flink-table-planner-loader-1.17.2.jar ..

while read -r LINE
do
	echo "the jar is $LINE"
    target_jar=$(find . -name "$LINE" | head -n 1)
    echo "the target_jars is ${target_jar}"
    if [ -n "$target_jar" ]; then
       scp -r "$target_jar" "$DEST_DIR"
       echo "Copied $first_jar to $DEST_DIR"
    else
       echo "No jar $LINE found."
    fi 
done < $JARS_FILE

echo "End to copy daph jars from daph dir to flink jars"
exit 0