APPMAN_HOME=/home/michel/share/appman
EXEHDA_HOME=$APPMAN_HOME/exehda

COMMAND="$EXEHDA_HOME/bin/isam-run $APPMAN_HOME/bin/exehda-test.isam"
echo $COMMAND
$COMMAND
