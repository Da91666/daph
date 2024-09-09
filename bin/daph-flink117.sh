#!/bin/bash
# Usage
echo "
Usage:
sh ${DAPH_HOME}/bin/daph-flink117.sh -b run -t local --job job.json --computer computer.json
sh ${DAPH_HOME}/bin/daph-flink117.sh -b run -t local -j job.json -c computer.json
"

SHORT_OPTS="b:,t:,s:,g:,j:,c:,e:,d:"
LONG_OPTS="st:,sc:,job:,computer:,executor:,dudps:"
ARGS=$(getopt --options $SHORT_OPTS \
  --longoptions $LONG_OPTS -- "$@" )
eval set -- "$ARGS"
echo "$ARGS"
while true;
do
    case $1 in
        -b)
           B=$2
           ;;
        -t)
           T=$2
           ;;
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
flink $B \
-t $T \
-c com.dasea.daph.starter.flink117.DaphFlink117Main \
${DAPH_HOME}/jars/starters/daph-starter-flink117.jar \

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