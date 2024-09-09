#!/bin/bash
# Usage
echo "
Usage:
sh ${DAPH_HOME}/bin/daph.sh -a jvm --st local-fs --job job.json -e executor.json
sh ${DAPH_HOME}/bin/daph.sh -a jvm -j job.json
sh ${DAPH_HOME}/bin/daph.sh -a jvm-spark3 -j job.json -c computer.json
sh ${DAPH_HOME}/bin/daph.sh -a jvm-flink117 -j job.json -c computer.json
sh ${DAPH_HOME}/bin/daph.sh -a spark3 -j job.json -c computer.json
sh ${DAPH_HOME}/bin/daph.sh -a flink117 -b run -t local -j job.json -c computer.json
sh $DAPH_HOME/bin/daph-test.sh -a jvm-flink117 --sm sync -r $DAPH_HOME/examples/flink117/ -f mysql_test/job-cc.json -d logXml=$DAPH_HOME/conf/log4j2-shao.xml -- -Xms1g -Xmx1g 		
sh $DAPH_HOME/bin/daph-test.sh -a jvm-flink117 --sm sync -r $DAPH_HOME/examples/flink117/ --dir mysql -d logXml=$DAPH_HOME/conf/log4j2-shao.xml -- -Xms1g -Xmx1g 		
sh $DAPH_HOME/bin/daph-test.sh -a jvm-flink117 --sm sync -r $DAPH_HOME/examples/flink117/ -k *cg* -d logXml=$DAPH_HOME/conf/log4j2-shao.xml -- -Xms1g -Xmx1g
"
allJob=()
getPathFile(){    
    OLD_IFS="$IFS"  
    IFS=',' read -ra parts <<< "$1"
    IFS="$OLD_IFS"   
    # 输出数组中的每个部分
    currentJobs=()
    for part in "${parts[@]}"; do
        # echo "$part"
        # echo "$2"
        if [ -n "$2" ]; then                 
          #result=$(find $r  -type f -name "$part")
            tmpfile=$(mktemp)
            #echo "find $r -type f -name \"$part\""
            find $ROOT_DIR -type f -name "$part" > "$tmpfile"
            while IFS= read -r file; do
                allJob+=("$file")
            done < "$tmpfile"
            rm "$tmpfile"
          #echo $2      
        else
            filePath="$ROOT_DIR$part"
            tmpfile=$(mktemp)
            find $filePath -maxdepth 1 -type f > "$tmpfile"
            while IFS= read -r file; do
                allJob+=("$file")
            done < "$tmpfile"
            rm "$tmpfile"
        fi 
    done
}

SHORT_OPTS="a:,b:,t:,s:,j:,c:,e:,d:,r:,f:,k:"
LONG_OPTS="st:,job:,computer:,executor:,dudps:,sm:,cm:,dir:"
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
        -r)
           ROOT_DIR=$2
           ;;
        -f)
           FILES=$2
           ;;
        --dir)
           SCAN_DIR=$2
           ;;
        -k)
           FILE_REG=$2
           ;;
        --sm)
           SUBMIT_MODEL=$2
           ;;
        --cm)
           COMPUTER_MODEL=$2
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

echo "config root  dir    : "$ROOT_DIR


echo "process submit model: "$SUBMIT_MODEL

if [ -n "$FILES" ];then
   echo "config file path    : "$FILES
   getPathFile "$FILES"
fi

if [ -n "$SCAN_DIR" ];then
   echo "config dir path     : "$SCAN_DIR
   getPathFile "$SCAN_DIR"
fi

if [ -n "$FILE_REG" ];then
   echo "config file name reg: "$FILE_REG
   getPathFile "$SCAN_DIR" "1"
fi



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

if [ -n "$EXECUTOR" ]; then
    cmd="$cmd --executor $EXECUTOR"
fi
if [ -n "$DUDPS" ]; then
    cmd="$cmd --dudps $DUDPS"
fi

if [ -n "$COMPUTER" ]; then
      cmd="$cmd --computer $COMPUTER"
fi

if [ -n "$JOB" ]; then
    
    cmd="$cmd --job $JOB"
    cmd="$cmd $@"
    echo "The initial command is:\n$cmd"
    $cmd
else   
   for file in "${allJob[@]}"; do
      #handle every file
      #echo "$file"      
      current_cmd="$cmd --job $file"
      #echo $current_cmd
      STREAM=1
      if [[ $file == *"cdc"* ]]; then
         STREAM=0
         if [ -z "$COMPUTER" ]; then
               current_cmd="$current_cmd --computer $ROOT_DIR/computer-stream.json"
         fi
      else
         if [ -z "$COMPUTER" ]; then
               current_cmd="$current_cmd --computer $ROOT_DIR/computer.json"
         fi
      fi   
      current_cmd="$current_cmd $@"      
      echo "The initial command is:\n$current_cmd"
      case $SUBMIT_MODEL in
          sync)
            $current_cmd 
          ;;
          async)
            nohup $current_cmd >/dev/null 2>&1 &
          ;;
          --)
             echo "Unrecognized pattern $SUBMIT_MODEL,support is sync or async!"
           ;;
      esac    
      
   done
fi


# $cmd