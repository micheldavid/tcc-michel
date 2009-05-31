#!/bin/sh
times=$1
task=$2
i=0

while [ $times -ge $i ]

APPMAN_BIN=$EXEHDA_HOME/appman-bin
$APPMAN_BIN/run-appman.sh $task
while [ 1 ]
do
    cat /proc/loadavg >> $EXEHDA_HOME/resultados/load$i;
    sleep 1s;
    if [ -f "$EXEHDA_HOME/log/tempoExecucao.txt" ]
    then
        nlines=` wc -l ./exehda/bin/parseOut.txt | gawk '{print $1}'`
    else
        nlines=0
    fi
#    if [ $nlines -ge 4 ]
#    then 
        echo parseOut com 4 linhas...
        break;
#    fi
done

mkdir -p $EXEHDA_HOME/resultados/$i
mv -r $EXEHDA_HOME/log/* $EXEHDA_HOME/resultados/$i

#./obtem_resultados.sh ${task};

$i=$i+1
done

