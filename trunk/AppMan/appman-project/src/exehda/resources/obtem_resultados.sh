task=$1

mkdir -p resultadosFatorial/${task}
mv exehda/bin/clusters.xml  exehda/bin/edges.xml  exehda/bin/manifest.xml  exehda/bin/tasks.xml resultadosFatorial/${task}/
mv exehda/bin/tasks-execution-appman.trace exehda/bin/parseOut.txt exehda/bin/hosts.txt exehda/bin/appman.log resultadosFatorial/${task}/

