#!/bin/sh
times=$1
task=$2
i=1

APPMAN_BIN=$EXEHDA_HOME/appman-bin

rm -r $EXEHDA_HOME/log/*

while [ $i -le $times ]
do

DTSTART=`date --rfc-3339='ns'`
echo $DTSTART>>$EXEHDA_HOME/log/dtstart.txt
echo "${DTSTART}: executando tarefa nro $i";
$APPMAN_BIN/run-appman.sh $task

#verificando finalizacao da tarefa
while [ 1 ]
do
#    cat /proc/loadavg >> $EXEHDA_HOME/resultados/$i;
    sleep 1s;
    if [ -f "$EXEHDA_HOME/log/tempoExecucao.txt" ]
    then
        nlines=` wc -l $EXEHDA_HOME/log/tempoExecucao.txt | gawk '{print $1}'`
    else
        nlines=0
    fi
    if [ $nlines -ge 3 ]
    then 
        echo tempoExecucao.txt com 3 linhas...
        break;
    fi
done

mkdir -p $EXEHDA_HOME/resultados/$i
mv $EXEHDA_HOME/log/* $EXEHDA_HOME/resultados/$i

i=`expr $i + 1` 
done

echo Executou $times vezes
