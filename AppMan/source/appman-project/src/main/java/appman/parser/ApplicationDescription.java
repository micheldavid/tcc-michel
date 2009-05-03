/*
 * GRAND - kayser@cos.ufrj.br (c) 2004
 * ApplicationDescription.java 
 * 2004/04/30
 */

package appman.parser;

import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;

import appman.Debug;
import appman.clustering.DAGNode;
import appman.clustering.DAG_DSC;

  /**
   * stores the DAG application parsed
   * @author kayser@cos.ufrj.br
   * @since 2004
   */
  public class  ApplicationDescription implements Serializable {

	private static final long serialVersionUID = -3554750829219220099L;
    private static final GraphType DEFAULT_GRAPH  = GraphType.LOW;
    private GraphType graphType;
    
    private static int DEFAULT_COMPUTATIONAL_COST = 1;
    private static int DEFAULT_COMMUNICATION_COST = 1;

    private Vector<TaskDescription> listOfTasks;
    private int numberOfTasks=0;
    private int numberOfFinishedTasks=0;

    public DAG_DSC applicationDAG = null;

    public ApplicationDescription () {
       this.graphType = DEFAULT_GRAPH;
       //Inicia com 10 tarefas, mas pode ir aumentando
       this.listOfTasks = new Vector<TaskDescription>(10,10);
       /*VDN: 4/1/6
       try{
       		//grappaOut = new FileWriter("/home/SO/dalto/eclipse/workspace/appman-mgc/grappaOut.txt");
       		grappaOut = new FileWriter("/tmp/grappaOut.txt");
    
       }catch( IOException e){
       		System.out.println("[APP.DESC] :"+e);
       }
       */
       System.out.println("[GRAND]\tApplicationDescription created");
    }

    public void putGraphType(int graphType) {
        this.graphType = GraphType.fromCode(graphType);
    }

    public void setGraphType(GraphType graphType) {
        this.graphType = graphType;
    }

    public GraphType getGraphType() {
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
    
	public Vector<TaskDescription> getListOfTasks()
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
       if (inputFiles != null) t.putInputFiles(inputFiles);
       if (outputFiles != null) t.putOutputFiles(outputFiles);
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
       
       System.out.println("[GRAND]\tStarting to build DAG...");

       for (TaskDescription task : listOfTasks) {
          //applicationDAG.putNodeName(t.getTaskName()); -->comentado pr vindn
          //vdn 2005/03/18: changed first line, included second line
          DAGNode node = applicationDAG.putNodeName(task.getTaskName());
          node.setExecutable(task.getExecutable());
          
          // outline of the algorithm:
          //    for each task "t"
          //       check each input file "i"
          //           search for each task "ti" until find first output equals to "i"
          //           include an incoming edge in the task "t" ("ti" -> "t")
          
          if (task.getInputFiles() != null) {
        	  for (String outfile : task.getInputFiles()) {

					for (TaskDescription foundTask : listOfTasks) {
						if (foundTask == task) continue;

						if (foundTask.hasOutputFile(outfile)) {
							System.out.println("[GRAND]\tedge " + foundTask.getTaskName() + "->" + task.getTaskName());
							applicationDAG.insertEdges(task.getTaskName(), foundTask.getTaskName());
							break;
						}
					}
				}
          }

       }

       // http://www.informatics.susx.ac.uk/courses/dats/notes/html/node133.html#7452
       // LinkedList -- ArrayList edges = new ArrayList(); -- TreeSet 

       System.out.println("[GRAND]\tDAG done ("+applicationDAG.getNumberOfNodes()+" nodes).");

       //VDN: 15/07/05//////////////// estava no SimpleParser.java
  		//applicationDAG.dump();
        applicationDAG.clustering();
  		try {
			applicationDAG.dumpGraphiz();
		} catch (IOException e) {
			Debug.debug("erro gerando arquivo .dot", e);
		}
  		//applicationDAG.createServerRMI();
      ///////////////////////////////

       return applicationDAG;
    }
    
    public TaskDescription getTaskDescription(String name) {
		TaskDescription[] tasks = (TaskDescription[]) listOfTasks.toArray(new TaskDescription[listOfTasks.size()]);

		for (TaskDescription task : tasks) {
			if (task.getTaskName().equals(name)) return task;
		}
    	return null;
    }
    
  }
