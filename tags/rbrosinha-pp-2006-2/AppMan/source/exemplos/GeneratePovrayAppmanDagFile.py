#Execute com o comando: "python GeneratePovrayAppmanDagFile > file.dag"

def generatePovrayAppmanDagFile(povray_path, package, num_workers, url_initial_package, width,height):
        print "graph low-coupled"
        for i in range(num_workers):
                print "POVTASK"+str(i)+"=\"mkdir -p povtask"+str(i)+" && cp ./"+package+"-set.tar.gz povtask"+str(i)+"/ && cd povtask"+str(i)+" && /bin/tar -vzxf ./"+package+"-set.tar.gz && cd "+package+" && nice -19 "+povray_path+" Display=false "+package+"_"+str(i)+".ini && mv "+package+"_"+str(i)+".ppm* ../../"+package+"_"+str(i)+".ppm\""

        print "POV-INITIAL=\"/bin/tar -vzxf ./"+package+".tar.gz && cd "+package+"/ && ./povray-makeset "+package+" "+str(num_workers)+" "+str(width)+" "+str(height)+" && cd .. && /bin/tar -vzcf "+package+"-set.tar.gz "+package+"/\""
        print "POV-COLLECT=\"/bin/tar -vzxf ./"+package+".tar.gz && cp *.ppm "+package+"/ && cd "+package+"/ && ./povray-combineppm "+package+" "+str(num_workers)+" > "+package+".ppm && cp "+package+".ppm ../.\""
        print "task pov-initial ${POV-INITIAL} -i "+url_initial_package+" -o "+package+"-set.tar.gz"
        
        pov_names = ""
        for i in range(num_workers):
                print "task pov"+str(i)+" ${POVTASK"+str(i)+"} -i "+package+"-set.tar.gz -o "+package+"_"+str(i)+".ppm"
                if i < num_workers-1:
                        pov_names+= package+"_"+str(i)+".ppm;"
                else:
                        pov_names+= package+"_"+str(i)+".ppm"

        print "task pov-collect ${POV-COLLECT} -i "+url_initial_package+";"+pov_names+" -o "+package+".ppm"
        
#generatePovrayAppmanDagFile("/usr/bin/povray", "star", 20, "http://www.inf.ufrgs.br/~lassantos/appman/star.tar.gz", 1024, 780)
generatePovrayAppmanDagFile("/usr/bin/povray", "urbatree", 10, "http://www.inf.ufrgs.br/~lassantos/appman/urbatree.tar.gz", 80, 60)
     
