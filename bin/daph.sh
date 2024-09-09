#!/bin/bash
# Usage
echo "
Usage:
sh ${DAPH_HOME}/bin/daph.sh -a jvm --job job.json -- -Xms2g -Xmx2g
sh ${DAPH_HOME}/bin/daph.sh -a jvm -j job.json
sh ${DAPH_HOME}/bin/daph.sh -a jvm-spark3 -j job.json -c computer.json -- -Xms2g -Xmx2g
sh ${DAPH_HOME}/bin/daph.sh -a jvm-flink117 -j job.json -c computer.json -- -Xms2g -Xmx2g
sh ${DAPH_HOME}/bin/daph.sh -a spark3 -j job.json -c computer.json
sh ${DAPH_HOME}/bin/daph.sh -a flink117 -b run -t local -j job.json -c computer.json
"

SHORT_OPTS="a:,b:,t:,s:,j:,c:,e:,d:"
LONG_OPTS="st:,job:,computer:,executor:,dudps:"
ARGS=$(getopt --options $SHORT_OPTS \
  --longoptions $LONG_OPTS -- "$@" )
eval set -- "$ARGS"
while true;
do
    case $1 in
        -a)
           A=$2
           ;;
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

case $A in
    jvm)
      cmd="sh ${DAPH_HOME}/bin/daph-jvm.sh"
      ;;
    jvm-spark3)
      cmd="sh ${DAPH_HOME}/bin/daph-jvm-spark3.sh"
      ;;
    jvm-flink117)
      cmd="sh ${DAPH_HOME}/bin/daph-jvm-flink117.sh"
      ;;
    spark3)
      cmd="sh ${DAPH_HOME}/bin/daph-spark3.sh"
      ;;
    flink117)
      cmd="sh ${DAPH_HOME}/bin/daph-flink117.sh -b $B -t $T"
      ;;
esac

if [ -n "$ST" ]; then
    cmd="$cmd --st $ST"
fi
if [ -n "$SC" ]; then
    cmd="$cmd --sc $SC"
fi
if [ -n "$JOB" ]; then
    cmd="$cmd --job $JOB"
fi
if [ -n "$COMPUTER" ]; then
    cmd="$cmd --computer $COMPUTER"
fi
if [ -n "$EXECUTOR" ]; then
    cmd="$cmd --executor $EXECUTOR"
fi
if [ -n "$DUDPS" ]; then
    cmd="$cmd --dudps $DUDPS"
fi

T=$@
if [[ -n "$T" && "$T" != "--" ]]; then
    cmd="$cmd $@"
fi

echo "The initial command is:"
echo "$cmd"
$cmd