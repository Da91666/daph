#!/bin/bash
# Usage
echo "
Usage:
sh ${DAPH_HOME}/bin/daph-jvm.sh --job job.json -- -Xms2g -Xmx2g
sh ${DAPH_HOME}/bin/daph-jvm.sh -j job.json
"

SHORT_OPTS="s:,g:,j:,e:,d:"
LONG_OPTS="st:,sc:,job:,executor:,dudps:"
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

CPS="${DAPH_HOME}/jars/core/*:${DAPH_HOME}/jars/core/lib/*:${DAPH_HOME}/jars/computers/jvm/*:${DAPH_HOME}/jars/starters/daph-starter-jvm.jar"

T=$@
if [[ -n "$T" && "$T" != "--" ]]; then
  JAVA_OPTS=$(echo "$@" | sed 's/-- //g')
else
  JAVA_OPTS=""
fi

cmd="
java $JAVA_OPTS -cp ${CPS} \
com.dasea.daph.starter.jvm.DaphJVMMain \

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
if [ -n "$EXECUTOR" ]; then
    cmd="$cmd -executor $EXECUTOR"
fi
if [ -n "$DUDPS" ]; then
    cmd="$cmd -dudps $DUDPS"
fi

echo "The final command is: $cmd"
$cmd