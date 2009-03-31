for n in "3" "6" "14" "10" 
do
	#ssh -f dalto@10.0.0.$n 'cd /home/SO/dalto/eclipse/workspace/appman19.12.05/exehda/bin; ./exehda --profile nodo-labia -q -l execucao'$n'.log'
	ssh -vv -f dalto@10.0.0.$n 'cd /home/SO/dalto/eclipse/workspace/appman19.12.05/exehda/bin; ./exehda --profile nodo-labia'
done
