#!/bin/bash

TASKS=${1:-10}

POV_EXE="http://www.inf.ufrgs.br/~lucc/grand/povray"
POV_INC="http://www.inf.ufrgs.br/~lucc/grand/povray-include.tar.gz"
POV_PKG="star"
POV_WIDTH="800"
POV_HEIGHT="600"
POV_DATA="http://www.inf.ufrgs.br/~lassantos/appman/${POV_PKG}.tar.gz"
POV_ARGS="${POV_PKG} ${TASKS} ${POV_WIDTH} ${POV_HEIGHT}"

OUT_DIR="resultados/povray/${TASKS}/$(date +%Y%m%d%H%M)/"
TMP_DIR=${HOME}
DAG_FILE="povray-input.dag"


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

  let WORKERS=$TASKS-1;

  local PPMS=$(for i in $(seq 0 $i $WORKERS); do echo -n ";${POV_PKG}_$i.ppm"; done);

 cat > $DAG_FILE <<EOF
 graph low-coupled

 task pov-init -e "tar -zxvf ${POV_PKG}.tar.gz && cd ${POV_PKG} && ./povray-makeset ${POV_ARGS} && cd .. && tar -zcvf pov-set.tar.gz ${POV_PKG}" -i ${POV_DATA} -o pov-set.tar.gz

 foreach i in 0..${WORKERS} {
   task \${i} -e "chmod u+x povray && tar -zxvf povray-include.tar.gz && tar -zxvf pov-set.tar.gz && cd ${POV_PKG} && ../povray +L../include Display=false ./${POV_PKG}_"\${i}".ini && cp ${POV_PKG}_"\${i}".ppm* ../${POV_PKG}_"\${i}".ppm" -i "${POV_EXE}";${POV_INC};"pov-set.tar.gz" -o "${POV_PKG}_"\${i}".ppm"
 }

 task pov-collect -e "tar -zxvf ${POV_PKG}.tar.gz && cp *.ppm ${POV_PKG}/ && cd ${POV_PKG}/ && ./povray-combineppm ${POV_PKG} ${TASKS} > ${POV_PKG}.ppm && cp ${POV_PKG}.ppm ../" -i ${POV_DATA}${PPMS} -o ${POV_PKG}.ppm
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




