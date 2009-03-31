#!/bin/sh
task=$1
while [ 1 ]
do
#    echo -n "#" ;
    cat /proc/loadavg >> resultadosFatorial/load$task;
    sleep 1s;
#    X=`./testFimAppMan.sh`;
#    if [ $X -ge 1 ]; then
# linha abaixo funcionou mas nao eh suficiente pq faz trabalho apos terminar as tarefas
#    if [ -f "exehda/bin/tasks-execution-appman.trace" ]       # Check if file exists.
    if [ -f "exehda/bin/parseOut.txt" ]
    then
       nlines=` wc -l ./exehda/bin/parseOut.txt | gawk '{print $1}'`
    else
       nlines=0
    fi
    if [ $nlines -ge 4 ]
    then 
       echo parseOut com 4 linhas...
       break;
    fi
done


./obtem_resultados.sh ${task};
