if [ $# = "0" ]
then
	tasks=10
else
	tasks=$1
fi


echo 'graph independent' > '/home/SO/dalto/eclipse/workspace/appman19.12.05/exemplos/f.dag'
echo 'foreach i in 1..'${tasks}' {' >> '/home/SO/dalto/eclipse/workspace/appman19.12.05/exemplos/f.dag'
echo '  task ${i} -e "chmod a+x factorial_3s; ./factorial_3s > t.out."${i} -i http://www.cos.ufrj.br/~kayser/tmp/factorial_3s -o "t.out."${i}'  >> '/home/SO/dalto/eclipse/workspace/appman19.12.05/exemplos/f.dag'
echo '}'  >> '/home/SO/dalto/eclipse/workspace/appman19.12.05/exemplos/f.dag'



./monitor_load.sh ${tasks} &
./exehda/bin/isam-run appman-console.isam -- /home/SO/dalto/eclipse/workspace/appman19.12.05/exemplos/f.dag
