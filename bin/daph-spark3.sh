#!/bin/bash
# Usage
echo "
Usage:
sh ${DAPH_HOME}/bin/daph-spark3.sh --job job.json --computer computer.json
sh ${DAPH_HOME}/bin/daph-spark3.sh -j job.json -c computer.json
"

SHORT_OPTS="s:,g:,j:,c:,e:,d:"
LONG_OPTS="st:,sc:,job:,computer:,executor:,dudps:"
ARGS=$(getopt --options $SHORT_OPTS \
  --longoptions $LONG_OPTS -- "$@" )
eval set -- "$ARGS"
while true;
do
    case $1 in
        -s|--st)
           ST=$2
           ;;
        -g|--sc)
           SC=$2
           ;;
        -j|--job)
           JOB=$2
           ;;
        -c|--computer)
           COMPUTER=$2
           ;;
        -e|--executor)
           EXECUTOR=$2
           ;;
        -d|--dudps)
           DUDPS=$2
           ;;
        --)
           break
           ;;
    esac
    shift
done

cmd="
spark-submit \
--class com.dasea.daph.starter.spark3.DaphSpark3Main \
${DAPH_HOME}/jars/starters/daph-starter-spark3.jar \

"
if [ -n "$ST" ]; then
    cmd="$cmd -st $ST"
fi
if [ -n "$SC" ]; then
    cmd="$cmd -sc $SC"
fi
if [ -n "$JOB" ]; then
    cmd="$cmd -job $JOB"
fi
if [ -n "$COMPUTER" ]; then
    cmd="$cmd -computer $COMPUTER"
fi
if [ -n "$EXECUTOR" ]; then
    cmd="$cmd -executor $EXECUTOR"
else
    cmd="$cmd -executor ${DAPH_HOME}/conf/executor.json"
fi
if [ -n "$DUDPS" ]; then
    cmd="$cmd -dudps $DUDPS"
fi

echo "The final command is: $cmd"
$cmd