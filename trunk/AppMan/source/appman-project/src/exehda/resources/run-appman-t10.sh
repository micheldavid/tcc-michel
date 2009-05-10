APPMAN_HOME=/home/michel/share/appman
EXEHDA_HOME=$APPMAN_HOME/exehda

#removendo logs
rm -r $EXEHDA_HOME/log/*

DAG_FILE=$APPMAN_HOME/bin/exemplos/t10.dag
#DAG_FILE=$APPMAN_HOME/bin/exemplos/timer.dag

COMMAND="$EXEHDA_HOME/bin/isam-run $APPMAN_HOME/bin/appman-console.isam -- $DAG_FILE"
echo $COMMAND
$COMMAND
