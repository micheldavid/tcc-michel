# final number of local IP 10.0.0.x
for n in "14" "6" "8" "11" "3"
do
	#get_pid_exehda
#	X=`ps -ef | grep exehda | gawk 'BEGIN{tmp=" "}{tmp= tmp+" "+ $2}END{print tmp}'`

	ssh -f dalto@10.0.0.$n 'X=`/home/SO/dalto/eclipse/workspace/appman19.12.05/get_pid_exehda.sh` ; kill $X'

done


