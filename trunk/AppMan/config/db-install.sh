#!/bin/sh
## exemplo: jdbc:hsqldb:hsql://192.168.42.142:8887
URL=$1
USER=$2
PASSWD=$3

java -cp ~/.m2/repository/hsqldb/hsqldb/1.8.0.7/hsqldb-1.8.0.7.jar:$APPMAN_PORTLETS/target/classes/ \
appman.InstallDB "$APPMAN_HOME/config/db-init.sql" "$URL" "$USER" "$PASSWD" 
