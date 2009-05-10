#Execute com o comando: "python GenerateDagFile > file.dag"

def generatePovrayAppmanDagFile(num_workers):
        print "graph low-coupled"
        for i in range(num_workers):
                print "TASK"+str(i)+"=\"/bin/ls > ls" +str(i)+ ".out\""

        out_names = ""

        for i in range(num_workers):
                print "task task"+str(i)+" -e ${TASK"+str(i)+"} -i http://www.cos.ufrj.br/~kayser/index.html -o ls"+str(i)+".out"
                if i < num_workers-1:
                        out_names+= "ls"+str(i)+".out;"
                else:
                        out_names+= "ls"+str(i)+".out"

        print "task task_final -e /bin/echo nada > /tmp/nada -i "+out_names+" -o /tmp/nada"
        
generatePovrayAppmanDagFile(10)
     
