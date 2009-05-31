#!/bin/sh
# final number of local IP 10.0.0.x
#for n in "14" "6" "8" "11" "13" "3"
for host in $@
do
#       get_pid_exehda
#       X=`ps -ef | grep exehda | gawk 'BEGIN{tmp=" "}{tmp= tmp+" "+ $2}END{print tmp}'`
#       ssh -f dalto@10.0.0.$n 'X=`/home/SO/dalto/eclipse/workspace/appman19.12.05/get_pid_exehda.sh` ; kill $X'
	ssh -f kayser@$host 'X=`/home/SO/kayser/APPMAN/svn/appman-dev/pid-exehda.sh` ; kill $X'
done

