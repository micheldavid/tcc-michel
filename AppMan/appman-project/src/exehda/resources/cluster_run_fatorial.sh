#!/bin/bash

TASKS=${1:-10}

EXE_FILE="http://www.cos.ufrj.br/~kayser/tmp/factorial_3s"
OUT_DIR="resultados/fatorial/${TASKS}/$(date +%Y%m%d%H%M)/"
TMP_DIR=${HOME}
DAG_FILE="f-input.dag"


declare -a EXEHDA_NODOS
EXEHDA_PROFILE="vecpar-gradep"
EXEHDA_BASE="localhost"
EXEHDA_NODOS=("c0-1" "c0-2" "c0-3") 

function init() 
{
  mkdir -p ${OUT_DIR}

  DAG_FILE=$(readlink -f ${OUT_DIR})/${DAG_FILE}

   echo "* Diretório de saida configurado para $OUT_DIR"
   echo "* Gerando dag para $TASKS tarefas..."

  cat > $DAG_FILE <<EOF
  graph independent
  foreach i in 1..${TASKS} {
    task \${i} -e "chmod a+x factorial_3s; ./factorial_3s > t.out."\${i} -i ${EXE_FILE} -o "t.out."\${i}
  }	
EOF
}

function check_bootstrap_ok() 
{
    local nodo=$1

    grep "EXEHDA is ready" exehda/var/log/exehda.log.$nodo || (echo "falhou!" && exit -1)
}

function bootstrap_exehda() 
{
   echo "* Limpando logs..."
   rm -f exehda/var/log/*

   echo "* Disparando a base, aguarde..."   
   ./exehda/bin/cluster-fork-exehda -p ${EXEHDA_PROFILE} ${EXEHDA_BASE}; sleep 5;
   check_bootstrap_ok ${EXEHDA_BASE}


   echo "* Disparando nodos, aguarde..."
   ./exehda/bin/cluster-fork-exehda -p ${EXEHDA_PROFILE} ${EXEHDA_NODOS[@]}; sleep 5;
}

function shutdown_exehda()
{
   echo "* Finalizando execução do middleware..."
   ./exehda/bin/cluster-kill-exehda ${EXEHDA_NODOS[@]} ${EXEHDA_BASE}
}

function find_output_file()
{
  file=$1

  ( test -f $file && echo $file ) || echo ${TMP_DIR}/$file  
}

function obtem_resultados() 
{
  declare -a output_files=( clusters.xml
			    edges.xml
			    manifest.xml 
           		    tasks.xml 
        		    tasks-execution-appman.trace 
        	            parseOut.txt
                            hosts.txt 
                            appman.log 
			    appman_contact_adress.txt 
			    graphiz.dot 
			    tempoExecucao.txt )

  for h in ${output_files[*]}; do 
	 mv -f $(find_output_file $h) ${OUT_DIR} ; 
  done

  # keep logs for debuging
  cp exehda/var/log/exehda.log.* ${OUT_DIR};

  echo "Contents of ${OUT_DIR}:" && ls -l ${OUT_DIR}
}

function monitor_load()
{
   while [ 1 ]; do
     cat /proc/loadavg >> $OUT_DIR/mon_loadavg;
     sleep 1s;

# linha abaixo funcionou mas nao eh suficiente pq faz trabalho apos terminar as tarefas
#    if [ -f "exehda/bin/tasks-execution-appman.trace" ]       # Check if file exists.
     local parseOut=$(find_output_file "parseOut.txt")
     if [ -f "$parseOut" ]; then
       nlines=$(wc -l < $parseOut)
       if [ $nlines -ge 4 ]; then
          echo parseOut com 4 linhas...
          break;
       fi
     fi
   done
}

#
# MAIN
#

echo "======================================================================"
run_pid=$$

init

bootstrap_exehda

# read -p "Tecle enter para continuar..."

( monitor_load; sleep 4; shutdown_exehda; obtem_resultados; sleep 2; kill $run_pid ) &
mon_pid=$!

echo "* monitor_load pid is $mon_pid" 

./exehda/bin/isam-run appman-console.isam -- ${DAG_FILE} || kill -9 $mon_pid

echo "* Execution in course..."

wait $mon_pid

ls -l $OUT_DIR

echo "======================================================================"




