EXEHDA_HOME=/home/michel/share/appman-project/exehda
APPMAN_HOME=$EXEHDA_HOME/appman-dist

DAG_FILE=$APPMAN_HOME/bin/dag/t10.dag
#DAG_FILE=$APPMAN_HOME/bin/dag/timer.dag

COMMAND="$EXEHDA_HOME/bin/isam-run $APPMAN_HOME/bin/appman-console.isam -- $DAG_FILE"
echo $COMMAND
$COMMAND
