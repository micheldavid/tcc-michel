EXEHDA_HOME=/home/aluno/Desktop/TCC/AppMan/appman-project/exehda
APPMAN_BIN=$EXEHDA_HOME/appman-bin

DAG_FILE=$APPMAN_BIN/dag/t10.dag
#DAG_FILE=$APPMAN_BIN/dag/timer.dag

COMMAND="$EXEHDA_HOME/bin/isam-run $APPMAN_BIN/appman-console.isam -- $DAG_FILE"
echo $COMMAND
$COMMAND
