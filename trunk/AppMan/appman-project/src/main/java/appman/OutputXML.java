/**
 * created 05/27/055
 * @author vindn
 * Gera saída xml da descrição das tarefas, arestas e clusters
 */
package appman;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import appman.clustering.DAGEdge;
import appman.clustering.DAGNode;
import appman.clustering.DAG_DSC;
import appman.parser.ApplicationDescription;
import appman.parser.SimpleParser;
import appman.parser.TaskDescription;

public class OutputXML{
	
	private static final Log log = LogFactory.getLog(OutputXML.class);
	protected transient BufferedWriter manifest;
	protected transient BufferedWriter clusters;
	protected transient BufferedWriter edges;
	protected transient BufferedWriter tasks;
	protected String filenameManifest;
	protected String filenameTasks;
	protected String filenameEdges;
	protected String filenameClusters;
	
	
	public OutputXML(){
		filenameManifest = "manifest.xml";
		filenameTasks = "tasks.xml";
		filenameEdges = "edges.xml";
		filenameClusters = "clusters.xml";
	}
	
	public void createManifest(){
				
		try {
			manifest = new BufferedWriter(new FileWriter(filenameManifest));
		} catch (IOException e) {
			log.error("[GRAND]\tError creating file "+filenameManifest, e);
			log.error("[GRAND]\tExecution graph cannot be created - aborting execution...", e);
			System.exit(0);
		}
		
	}
	
	public void createTasks(){
			
		try {
			tasks = new BufferedWriter(new FileWriter(filenameTasks));
		} catch (IOException e) {
			log.error("[GRAND]\tError creating file "+filenameTasks, e);
			log.error("[GRAND]\tExecution graph cannot be created - aborting execution...", e);
			System.exit(0);
		}
		
	}
	
	public void createEdges(){
		
		try {
			edges = new BufferedWriter(new FileWriter(filenameEdges));
		} catch (IOException e) {
			log.error("[GRAND]\tError creating file "+filenameEdges, e);
			log.error("[GRAND]\tExecution graph cannot be created - aborting execution...", e);
			System.exit(0);
		}
		
	}
	
	public void createClusters(){
		
		try {
			clusters = new BufferedWriter(new FileWriter(filenameClusters));
		} catch (IOException e) {
			log.error("[GRAND]\tError creating file "+filenameClusters, e);
			log.error("[GRAND]\tExecution graph cannot be created - aborting execution...", e);
			System.exit(0);
		}
		
	}

	
	
	public void writeManifest(){
		
		try {
			manifest.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			manifest.write("<m:Manifest xmlns:m=\"http://www.cos.ufrj.br/~grand/2005/06/manifest\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.cos.ufrj.br/grand/2005/07/manifest http://www.cos.ufrj.br/grand/2005/06/manifest.xsd\">\n");
			manifest.write("\t<m:FileEntry type=\"tasks\" path=\"tasks.xml\"/>\n");
			manifest.write("\t<m:FileEntry type=\"edges\" path=\"edges.xml\"/>\n");
			manifest.write("\t<m:FileEntry type=\"clusters\" path=\"clusters.xml\"/>\n");
			manifest.write("</m:Manifest>\n");
			
		} catch (IOException e) { 
			log.error("[GRAND]\tError in first writing to file "+filenameManifest, e);
		}
		
	}
	
	public void writeTasks(){
		
		ApplicationDescription appDesc = SimpleParser.appDescription;
//		DAG_DSC dag = appDesc.getDAG();
		Vector listOfTasks = appDesc.getListOfTasks();
		 String jobName = "";
		 String jobID = ""; 
		 String jobWeight = "";
		 String appName = "";
		 String executable = "";
		 String argument = "";
		 Vector input = new Vector();
		 Vector output = new Vector();

		try {
			tasks.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			tasks.write("<jsdl:JobDefinition xmlns:jsdl-posix=\"http://schemas.ggf.org/jsdl/2005/04/jsdl-posix\" xmlns:jsdl=\"http://schemas.ggf.org/jsdl/2005/04/jsdl\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:jsdl-grand=\"http://www.cos.ufrj.br/~grand/2005/06/jsdl-grand\" xsi:schemaLocation=\"http://schemas.ggf.org/jsdl/2005/04/jsdl http://schemas.ggf.org/jsdl/2005/04/jsdl/jsdl.xsd http://schemas.ggf.org/jsdl/2005/04/jsdl-posix http://schemas.ggf.org/jsdl/2005/04/jsdl-posix/jsdl-posix.xsd http://www.cos.ufrj.br/~grand/2005/06/jsdl-grand http://www.cos.ufrj.br/~grand/2005/06/jsdl-grand/jsdl-grand.xsd\">\n");
		} catch (IOException e) { 
			log.error("[GRAND]\tError in first writing to file "+filenameTasks, e);
		}

		for (int i=0; i< listOfTasks.size();i++) {
			TaskDescription node = (TaskDescription)listOfTasks.get(i);
			jobName =  node.getTaskName();
			jobID = "task"+i; 
			jobWeight = "1.0"; //TODO: deveria pegar o peso da aresta
			
			appName = node.getExecutable(); 

			// BUG: o código abaixo falha para comandos composts ou quando existe 
                        // manipulação de diretórios (ver aplicação povray)

			// pegar o nome sem caminho
			int barra = appName.lastIndexOf('/')+1;
			appName = appName.substring(barra);
			int space = appName.indexOf(" ");
			// se indexOf não encontrar, retorna -1
			appName = (space > 0 ) ? appName.substring(0,space) : appName;

			executable = node.getExecutable();
			argument = ""; // TODO: verificar onde foi armazenado a estrutura do parametro -a
		
			try {
				
				//varredura das tarefas
				tasks.write("\t<jsdl:JobDescription>\n");
				tasks.write("\t\t<jsdl:JobIdentification>\n");
				tasks.write("\t\t\t<jsdl:JobName>"+jobName+"</jsdl:JobName>\n");
				tasks.write("\t\t\t<jsdl-grand:JobUID>"+jobID+"</jsdl-grand:JobUID>\n");
				tasks.write("\t\t\t<jsdl-grand:JobWeight>"+jobWeight+"</jsdl-grand:JobWeight>\n");
				tasks.write("\t\t</jsdl:JobIdentification>\n");
				tasks.write("\t\t<jsdl:Application>\n");
				tasks.write("\t\t\t<jsdl:ApplicationName>"+appName+"</jsdl:ApplicationName>\n");
				tasks.write("\t\t\t<jsdl-posix:POSIXApplication>\n");
				tasks.write("\t\t\t\t<jsdl-posix:Executable>"+executable+"</jsdl-posix:Executable>\n");
				tasks.write("\t\t\t\t<jsdl-posix:Argument>"+argument+"</jsdl-posix:Argument>\n");
			} catch (IOException e) { 
				log.error("[GRAND]\tError in first writing to file "+filenameTasks, e);
			}
			
			//FALTA VARRER O VETOR DE ENTRADAS E SAIDAS!!!!!!!!
			input = node.getInputFiles();
			output = node.getOutputFiles();
			//input =   //Vector inputFiles = node.getInputFiles();
			
			
			
			try {
				for(int j=0; j < input.size(); j++)
					tasks.write("\t\t\t\t<jsdl-posix:Input>"+input.get(j)+"</jsdl-posix:Input>\n");
				for(int j=0; j < output.size(); j++)
					tasks.write("\t\t\t\t<jsdl-posix:Output>"+output.get(j)+"</jsdl-posix:Output>\n");
			} catch (IOException e) { 
				log.error("[GRAND]\tError in first writing to file "+filenameTasks, e);
			}
			
			try {
				tasks.write("\t\t\t</jsdl-posix:POSIXApplication>\n");
				tasks.write("\t\t</jsdl:Application>\n");
				tasks.write("\t</jsdl:JobDescription>\n");
				//fim varredura
			} catch (IOException e) { 
				log.error("[GRAND]\tError in first writing to file "+filenameTasks, e);
			}
			
		}
		
	    try {
			tasks.write("</jsdl:JobDefinition>\n");
		} catch (IOException e) { 
			log.error("[GRAND]\tError in first writing to file "+filenameTasks, e);
		}
		
	}
	

	public void writeEdges(){
		
		DAG_DSC dag = SimpleParser.appDescription.getDAG();
		List nodesList = dag.getDAGcopy();
		DAGNode dn;
		DAGEdge de;
		Vector pred;
		
		try {
			edges.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			edges.write("<ge:DAGDefinition xmlns:ge=\"http://www.cos.ufrj.br/~grand/2005/06/grand-edges\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.cos.ufrj.br/~grand/2005/06/grand-edges http://www.cos.ufrj.br/~grand/2005/06/grand-edges/grand-edges.xsd\">\n");
			
			for (int i = 0; i < nodesList.size(); i++) {
				dn = (DAGNode) nodesList.get(i);
				pred = dn.getPRED();
				if (pred != null) {
					
					for(int j=0; j < pred.size(); j++){
						//1.0 é o peso da aresta
						de = (DAGEdge)pred.get(j);
						edges.write("\t<ge:Edge weight=\"1.0\" sourceJobID=\""+de.nodeIndex+"\" targetJobID=\""+i+"\"/>\n");
					}
					
				}

			}

			
			edges.write("</ge:DAGDefinition>");
		} catch (IOException e) { 
			log.error("[GRAND]\tError in first writing to file "+filenameEdges, e);
		}
		
	}

	//separa lista de tarefas por cluster
	public void getTasksPerCluster( Vector listOfTasks, Vector clusters, Vector tasks ){
				
		//cria lista de clusters
		for(int i=0; i < listOfTasks.size(); i++){
			
			TaskDescription node = (TaskDescription)listOfTasks.get(i);
			
			if( (clusters == null) || clusters.isEmpty() ){
				clusters.add( node.getClusterId() );
			}
			else{
				if( !clusters.contains( node.getClusterId() )  ){
					clusters.add( node.getClusterId() );
				}
			}
		}
		
		//separa tarefas por cluster
		for(int i=0; i < clusters.size(); i++){
			for(int j=0; j < listOfTasks.size(); j++){
				TaskDescription node = (TaskDescription)listOfTasks.get(i);
				if( ((String)clusters.get(i)).compareTo(node.getClusterId()) == 0 ){
					tasks.add(listOfTasks.get(j));
				}
			}
		}
		
		
	}
	
	public void writeClusters(){
		String clusterID = "";
		String jobID = "";
		ApplicationDescription appDesc = SimpleParser.appDescription;
		Vector listOfTasks = appDesc.getListOfTasks();
		Vector tasks = new Vector();
		Vector cl = new Vector();
		
		try {
			clusters.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			clusters.write("<gc:ClusterSet xmlns:gc=\"http://www.cos.ufrj.br/~grand/2005/06/grand-clusters\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.cos.ufrj.br/~grand/2005/06/grand-clusters http://www.cos.ufrj.br/~grand/2005/06/grand-clusters/grand-clusters.xsd\">\n");
			
			//VARRER OS CLUSTERS E IMPRIMIR NO ARQUIVO AQUI!!!
			getTasksPerCluster(listOfTasks, cl, tasks);
			for (int i=0; i< cl.size();i++) {
				
				clusters.write("\t<gc:ClusterDefinition clusterID=\""+cl.get(i)+"\">\n");

				for(int j=0; j < tasks.size(); j++){
					TaskDescription node = (TaskDescription)tasks.get(j);
					clusters.write("\t\t<gc:JobID>"+ node.getTaskName() +"</gc:JobID>\n");		
				}

				clusters.write("\t</gc:ClusterDefinition>\n");
			}
			////////////////////////////////
			clusters.write("</gc:ClusterSet>\n");
		} catch (IOException e) { 
			log.error("[GRAND]\tError in first writing to file "+filenameClusters, e);
		}
		
	}

	
	
	
	public void closeManifest(){
		try {
			manifest.close();
			
		} catch (IOException e) { 
			log.error("[GRAND]\tError in closing to file "+filenameManifest, e);
		}
		
	}
	
	public void closeTasks(){
		try {
			tasks.close();
			
		} catch (IOException e) { 
			log.error("[GRAND]\tError in closing to file "+filenameTasks, e);
		}
		
	}
	
	public void closeEdges(){
		try {
			edges.close();
			
		} catch (IOException e) { 
			log.error("[GRAND]\tError in closing to file "+filenameEdges, e);
		}
		
	}
	
	public void closeClusters(){
		try {
			clusters.close();
			
		} catch (IOException e) { 
			log.error("[GRAND]\tError in closing to file "+filenameClusters, e);
		}
		
	}

	
}
