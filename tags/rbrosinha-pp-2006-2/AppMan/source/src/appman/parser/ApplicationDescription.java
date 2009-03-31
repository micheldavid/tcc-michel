/*
 * GRAND - kayser@cos.ufrj.br (c) 2004
 * ApplicationDescription.java 
 * 2004/04/30
 */

package appman.parser;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import appman.Debug;
import appman.clustering.*;

  /**
   * stores the DAG application parsed
   * @author kayser@cos.ufrj.br
   * @since 2004
   */
  public class  ApplicationDescription implements Serializable
  {
    private static int INDEP = 0; // graph=independent
    private static int LOW   = 1; // graph=loosely-coupled
    private static int HIGH  = 2; // graph=tightly-coupled
    private static int DEFAULT_GRAPH  = LOW; 
    private int graphType;
    
    private static int DEFAULT_COMPUTATIONAL_COST=1;
    private static int DEFAULT_COMMUNICATION_COST=1;
    
    public DAG_DSC applicationDAG = null;
    private Vector listOfTasks;
    private int numberOfTasks=0;
    private int numberOfFinishedTasks=0;
    
    //public transient FileWriter grappaOut;
        
    public ApplicationDescription () {
       this.graphType = DEFAULT_GRAPH;
       //Inicia com 10 tarefas, mas pode ir aumentando
       this.listOfTasks = new Vector(10,10);
       /*VDN: 4/1/6
       try{
       		//grappaOut = new FileWriter("/home/SO/dalto/eclipse/workspace/appman-mgc/grappaOut.txt");
       		grappaOut = new FileWriter("/tmp/grappaOut.txt");
    
       }catch( IOException e){
       		System.out.println("[APP.DESC] :"+e);
       }
       */
       Debug.log(this + "\t[GRAND]\tApplicationDescription created");
    }
    
    /**
     *
     */
    public void putGraphType(int graphType) {
        this.graphType = graphType;
    }

    public int getGraphType() {
        return this.graphType;
    }
    
    public DAG_DSC getDAG()
    {
    	return applicationDAG;
    }
    
    public int getNumberOfTasks()
    {
    	return numberOfTasks;
    }
    
	public Vector getListOfTasks()
	{
		return listOfTasks;
	}
    
	
	public int getNumerOfFinishedTasks(){
		return numberOfFinishedTasks;
	}
    
	public int incrementFinishedTasks(){
		
		numberOfFinishedTasks++;
		return numberOfFinishedTasks;
	}
	
	
    /**
     *
     */
    public void addTask(String taskName, String executable,
           Vector inputFiles, Vector outputFiles, int computationalCost){
       numberOfTasks++;
       
       
       if( taskName.compareTo("") == 0 ){
			taskName = "task"+(numberOfTasks-1);
			//System.out.println("VAZIOOOOOOOOOOO!!!!");
       }
       
       TaskDescription t = new TaskDescription(taskName, executable);
       t.putInputFiles(inputFiles);
       t.putOutputFiles(outputFiles);
       if (computationalCost<0){
          t.putComputationalCost(DEFAULT_COMPUTATIONAL_COST);
       }else{
          t.putComputationalCost(computationalCost);     
       }
       listOfTasks.addElement(t);
    }
    /**
     * Convert graph from GRID-ADL to DAG_DSC
     */
    public DAG_DSC inferDAG() {
       applicationDAG = new DAG_DSC(numberOfTasks);
       
       Debug.log(this + "\t[GRAND]\tStarting to build DAG...");

       Enumeration e = listOfTasks.elements();
       while (e.hasMoreElements()) {
          TaskDescription t = (TaskDescription)e.nextElement();
          //applicationDAG.putNodeName(t.getTaskName()); -->comentado pr vindn
          //vdn 2005/03/18: changed first line, included second line
          DAGNode node = applicationDAG.putNodeName(t.getTaskName());
          node.setExecutable(t.getExecutable());
          
          // outline of the algorithm:
          //    for each task "t"
          //       check each input file "i"
          //           search for each task "ti" until find first output equals to "i"
          //           include an incoming edge in the task "t" ("ti" -> "t")
          
          Enumeration inputList = (t.getInputFiles()).elements();
          while (inputList.hasMoreElements()) {
             String outfile = (String) inputList.nextElement();
             
             Enumeration aux = listOfTasks.elements();
             boolean found = false;
             while ((!found) && (aux.hasMoreElements())) {
                TaskDescription t_aux = (TaskDescription)aux.nextElement();
                if (!(t_aux.equals(t))){
                   if (t_aux.hasOutputFile(outfile)) {
                  	 Debug.log(this + "\t[GRAND]\tedge "+t_aux.getTaskName()+"->"+t.getTaskName());
                      applicationDAG.insertEdges(t.getTaskName(), t_aux.getTaskName());
                      found = true;
                   }
                }
             }

          }
          
          
       }

       
       // http://www.informatics.susx.ac.uk/courses/dats/notes/html/node133.html#7452
       // LinkedList -- ArrayList edges = new ArrayList(); -- TreeSet 
       
       Debug.log(this + "\t[GRAND]\tDAG done ("+applicationDAG.getNumberOfNodes()+" nodes).");
       
       //VDN: 15/07/05//////////////// estava no SimpleParser.java
  		//applicationDAG.dump();
        applicationDAG.clustering();
  		applicationDAG.dumpGraphiz();
  		//applicationDAG.createServerRMI();
      ///////////////////////////////
       
       return applicationDAG;
    }
    
    public TaskDescription getTaskDescription(String name)
    {
		TaskDescription[] tasks = (TaskDescription[])listOfTasks.toArray(new TaskDescription[0]);
		
    	for(int i=0; i < numberOfTasks; i++)
    	{    		
    		if(tasks[i].getTaskName().equals(name))
    			return tasks[i];
    	}
    	return null;
    }
    
  }
