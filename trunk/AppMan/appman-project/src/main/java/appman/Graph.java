package appman;

import java.io.Serializable;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import appman.parser.ApplicationDescription;
import appman.task.Task;
import appman.task.TaskState;

public class Graph implements Serializable {

	private static final long serialVersionUID = -535333494608899387L;
	private static final Log log = LogFactory.getLog(Graph.class);

	private String graphId;
	private String mysubmanId; // default submission manager

    private int state;
    
	public static final int GRAPH_READY = 0;
	public static final int GRAPH_EXECUTING= 1;
	public static final int GRAPH_END= 2;

		
	private edu.berkeley.guir.prefuse.graph.Graph graph;
	
	private Vector taskList; // vector of the tasks that are managed by the default subman
	//private Vector alltaskList; // vector of all tasks of the graph
	private Vector<DataFile> datafileList;
	
	// info from the parser module
	private ApplicationDescription appDescription;

    private int indexDSC;
	
	
	
	public Graph(String graphid, String subid, ApplicationDescription appdesc)
	{
		graphId = graphid;
		mysubmanId = subid;
//		datafileList = new Vector();
		taskList = new Vector();
		GraphGenerator generator = new GraphGenerator(subid);
		graph = generator.convertApplicationDescriptionToGraph(appdesc);
		datafileList = generator.getDataFileList();
		taskList = buildTaskList();
		
		state = GRAPH_READY;
	
		updateGraphNodesInternalData();

		log.debug("Graph new graph ["+ taskList.size() +"]nodes created: " + graphId + " - SubMan: " + mysubmanId);
	}
	
	public Graph(String graphid, String subid)
	{
		graphId = graphid;
		mysubmanId = subid;
		//graph = new edu.berkeley.guir.prefuse.graph.DefaultGraph(true);		
		datafileList = new Vector<DataFile>();
		taskList = new Vector();		
		
		createDefaultGraph(2, 2, (float) 0.9);
		
		updateGraphNodesInternalData();
		
		state = GRAPH_READY;
		
		log.debug("Graph new graph ["+ taskList.size() +"]nodes created: " + graphId + " - SubMan: " + mysubmanId);
	}
	public Graph(String graphid, String subid, String subidother)
	{
		graphId = graphid;
		mysubmanId = subid;
		//graph = new edu.berkeley.guir.prefuse.graph.DefaultGraph(true);		
		datafileList = new Vector<DataFile>();
		taskList = new Vector();
	
		createDefaultGraph2(2, 2, (float) 0.9, subid, subidother);
	
		updateGraphNodesInternalData();
	
		state = GRAPH_READY;
	
		log.debug("Graph new graph ["+ taskList.size() +"]nodes created: " + graphId + " - SubMan: " + mysubmanId);
	}
	public Graph(String graphid, String subid, boolean empty)
	{
		graphId = graphid;
		mysubmanId = subid;
		graph = null;		
		datafileList = new Vector<DataFile>();
		taskList = new Vector();
		
		if(empty == false)
		{
			createDefaultGraph(2, 2, (float) 0.9);
			updateGraphNodesInternalData();
			state = GRAPH_READY;
			log.debug("Graph new graph ["+ taskList.size() +"]nodes created: " + graphId + " - SubMan: " + mysubmanId);
		}
		else
		{
			state = GRAPH_READY;
			log.debug("Graph new empty graph created");
		}
	}
	public String PrintInfo()
	{		
		return "Graph Structure: " + taskList.toString() + " - Num: " + taskList.size() + " - Name: " + getGraphId() + " - SubMan: " + getSubmissionManagerId();
	}
	
	/*
	 Esta fun??o cria um grafo direcionado de forma rand?mica, sem loops de inconsist?ncia
	 todas as tarefas do grafo, s?o de um s? submission manager: mysubmanId
	*/
	public void createDefaultGraph(int a, int b, float c)
	{				
		GraphGenerator generator = new GraphGenerator(mysubmanId);
		graph = generator.getRandomTreeDirected(a,b, c);
		datafileList = generator.getDataFileList();
		log.debug(datafileList);
		
		taskList = buildTaskList();
		log.debug(taskList);
		
		log.debug("Graph default graph structure created");
	}
	
	/*
		 Esta fun??o cria um grafo direcionado de forma rand?mica, sem loops de inconsist?ncia
		 as tarefas do grafo, s?o divididas de forma aleat?ria entre 2 submission managers: submanId1, submanId2
		*/
		public void createDefaultGraph2(int a, int b, float c, String submanId1, String submanId2)
		{
			String[] listsubmanId = new String[2];
			listsubmanId[0] = submanId1;
			listsubmanId[1] = submanId2;
			
			GraphGenerator generator = new GraphGenerator(listsubmanId);
			graph = generator.getRandomTreeDirected(a, b, c);
			datafileList = generator.getDataFileList();
			log.debug(datafileList);
		
			taskList = buildTaskList();
			log.debug(taskList);
		
			log.debug("Graph default graph structure created");
		}

	public Vector getTaskList()
	{
            // BUG: must return a copy, otherwise external modifications to vector will corrupt the graph
		return (Vector) taskList.clone();
	}

//         /**
//          * @deprecated Replaced by method <code>copy(Graph)</code>
//          */
// 	public void setTaskList(Vector list)
// 	{
// 		taskList = list;
// 	}
		
        /**
         * Describe <code>getTask</code> method here.
         *
         * @param taskId a <code>String</code> value
         * @return a <code>Task</code> value
         */
    public Task getTask(String taskId)
	{
		for(int i=0; i<taskList.size();i++)
		{
			if(((Task)taskList.elementAt(i)).getTaskId().compareTo(taskId) == 0)
			{
				return ((Task) taskList.elementAt(i));
			}
		}
		return null;
	}
	public Task getTaskFromAllTaskList(String taskId)
	{
		for(int i=0; i<getAllTaskList().size();i++)
		{
			if(((Task)getAllTaskList().elementAt(i)).getTaskId().compareTo(taskId) == 0)
			{
				return ((Task) getAllTaskList().elementAt(i));
			}
		}
		return null;
	}
	
	/**
	 *	retorna Lista de tarefas prontas para executar, pois possuem todos os arquivos de entrada dispon?veis
	 */
	public Vector getReadyTaskList()
	{		
			Vector readyTasks = new Vector();	
			for(int i=0;i<taskList.size();i++)
			{
				Task t = (Task)taskList.elementAt(i); 
				if( isAllTaskInputsAvailable(t.getTaskId()) && t.getState().getCode()==TaskState.TASK_DEPENDENT )
				{
					t.setState(TaskState.getInstance(TaskState.TASK_READY));
					log.debug("Task setting state: " + t.getState().getName());
					readyTasks.add(t);
				}
                else if ( t.getState().getCode()==TaskState.TASK_READY ) {
                    readyTasks.add(t);
                }
			}
			//log.debug("Task READY: " + readyTasks);	
			return readyTasks;
	}
	
	/**
	 * Return a list of all tasks ready to be executed. For getting this lists, check for all tasks that
	 * were DEPENDENT if its input file are now available.
	 * This method was created to limit the number of tasks to be set to READY due to the limits of the Submission Manager
	 * (PKVM 06/01/2006)
	 */
	//VDN: Adicionamos synchronized e continuou nao funcionando para 300 tarefas (talvez precise aqui, mas em outros lugares tb)
	public Vector getReadyTaskList(int maxNumberOfTasks)
	{		
			Vector readyTasks = new Vector();
			for(int i=0;i<taskList.size();i++)
			{
				Task t = (Task)taskList.elementAt(i); 
				if( isAllTaskInputsAvailable(t.getTaskId()) && t.getState().getCode()==TaskState.TASK_DEPENDENT )
				{
					t.setState(TaskState.getInstance(TaskState.TASK_READY));
					log.debug("Task setting state: " + t.getState().getName());
					readyTasks.add(t);
				}
                else if ( t.getState().getCode()==TaskState.TASK_READY ) {
                    readyTasks.add(t);
                }
                
				// check if reached limit of tasks to be included
				if (readyTasks.size() >= maxNumberOfTasks ) { 
					break;
				}
			}
			log.debug("Task READY: " + readyTasks);	
			return readyTasks;
	}	
	
	/**
	 *  retorna Lista de tarefas n?o prontas para executar, pois n?o possuem todos os arquivos de entrada dispon?veis
	 */
	public Vector getNotReadyTaskList()
	{		
			Vector all = taskList;
			Vector notreadyTasks = new Vector();	
			for(int i=0;i<all.size();i++)
			{
				Task t = (Task)all.elementAt(i); 
				if( ! isAllTaskInputsAvailable(t.getTaskId()) && t.getState().getCode()==TaskState.TASK_DEPENDENT)
				{					
					notreadyTasks.add(t);
				}
			}
			return notreadyTasks;
	}
	// verifica se a tarefa possui todos os arquivos de entrada dispon?veis
	private boolean isDataFileAvailable(String datafileId)
	{
		for(int j=0;j < datafileList.size();j++)
		{
			DataFile d = datafileList.elementAt(j);
			
			if( d.getDataFileId().compareTo(datafileId) == 0)
			{ 					
				if(d.dataFileExist())
				{
					return true;
				}
				else
				{
					if(d.getFromTask() != null)
					{
						// se o arquivo est? dispon?vel em uma tarefa de outro grafo
						if(d.getFromTask().getState().getCode() == TaskState.TASK_FOREIGN_FINAL)
							return true;
						else
						return false;
					}
					else
					{
						return false;
					}
				}
			}
		}

		return false;		
	}

	/**
	 * Check if all input files was already generated or downloaded
	 * (was first created with the name "isDisponibleTaskInputs")
	 * @param taskid
	 * @return 
	 */
	private boolean isAllTaskInputsAvailable(String taskid)
	{
		Task task = getTask(taskid);
		DataFile[] inputs = task.getFiles().getInputFiles();
		
		for(int i=0;i < inputs.length;i++)
		{			
				if( ! isDataFileAvailable(inputs[i].getDataFileId()) )
				{
					//log.debug("Graph isDisponibleTaskInputsTask ["+task.getTaskId()+"] FALSE: " + inputs[i].getDataFileId());
					return false;
				}
		}
		
		return true;
	}
	
	public Vector<Object> buildTaskList()
	{
		// get the tasks vector from the graph
		Vector<Object> tasks = GraphGenerator.getTaskList(graph);
		log.debug("build task: " + tasks);		
		// add mytasks to the task list
		Vector mytasks = new Vector();
		// create a vector with the tasks from the my submanager
		log.debug("Graph ["+ getGraphId() +"] buildtaskList SubManId: " + getSubmissionManagerId());
		
		for(int i=0;i<tasks.size();i++)
		{
			if( ((Task)tasks.elementAt(i)).getSubmissionManagerId().compareTo(mysubmanId) == 0)
			{
				mytasks.add(tasks.elementAt(i));
			}
		}
		
		return mytasks;
	}
	/*
	 * Retorna todas as tarefas do grafo
	 */
	public Vector getAllTaskList()
	{
		// get the tasks vector from the graph
		Vector tasks = GraphGenerator.getTaskList(graph);
		// add mytasks to the task list
		Vector alltasks = new Vector();
		// create a vector with the tasks from the my submanager		
	
		for(int i=0;i<tasks.size();i++)
		{			
			 alltasks.add(tasks.elementAt(i));			
		}
	
		return alltasks;
	}
	/*
	 * Seta um identificador de submission manager para todas as tarefas que sejam de um dado submission manager
	 */
	public void setSubmissionManagerId(String subid)
	{		
		Vector tasks = GraphGenerator.getTaskList(graph);
		
		for(int i=0;i<tasks.size();i++)
		{
			if( ((Task)tasks.elementAt(i)).getSubmissionManagerId().compareTo(mysubmanId) == 0)
			{
				((Task)tasks.elementAt(i)).setSubmissionManagerId(subid);
			}			
		}
		
		mysubmanId = subid;
	}
	/*
	 * retorna a percentagem das tarefas do grafo em estado FINAL 
	 */
	public float getStatePercentCompleted()
	{
		float n = 0;
		for(int i=0; i<taskList.size(); i++)
		{
			if( ((Task)taskList.elementAt(i)).getState().getCode() == TaskState.TASK_FINAL )
			{
				n+= 1;
			}		
		}
		if(taskList.size() > 0)
			return (n / taskList.size());
		else
			return 1;	
	}

	public Vector<DataFile> getDataFileList() {
		return (Vector<DataFile>) datafileList.clone();
	}

//         /**
//          * @deprecated Replaced by method <code>copy(Graph)</code>
//          */	
// 	public void setDataFileList(Vector list)
// 	{
// 			datafileList = list;
// 	}

	public String getGraphId() {
		return graphId;
	}
	
	public String getSubmissionManagerId() {
		return mysubmanId;
	}

	public void setGraphId(String string) {
		graphId = string;
	}

	public synchronized int getState() {
		return state;
	}

	public synchronized void setState(int s) {
		state = s;
	}

	public edu.berkeley.guir.prefuse.graph.Graph getGraph() {
		return graph;
	}

	public void setGraph(edu.berkeley.guir.prefuse.graph.Graph graph) {
		this.graph = graph;
	}


    ////////////////////////////////////////////////////////
    // refactoring
    ///////////////////////////////////////////////////////

    /**
     * Updates this graph state by copying the given graph, but before that do same
     * sanity checks to see if the copy is allowed (eg. both structures must describe
     * the same graph.
     *
     * <p>Assertions (invariants):
     * <ul>
     *   <li>both graphs must have the very same ids
     *   <li>tasks do not desapear! the number of tasks is fixed upon graph creation.
     *   <li>file dependencies do not desapear! the number of file dependencies is fixed upon graph creation.
     *   <li>execution percent done status increases monotonically
     * </ul>
     *
     * @param g a <code>Graph</code> value
     */
    public synchronized void  copy(Graph g)
    {
            // santity checks for verifing copy is allowed
        if ( g == null )
            throw new IllegalArgumentException("source graph must be non-null");
        if ( ! graphId.equals(g.graphId) )
            throw new IllegalArgumentException("source graph do not describe the same logical graph (id differs)");
        if ( taskList.size() != g.taskList.size() )
            throw new IllegalArgumentException("possibly corrupted data structure (task lists sizes differs)");
        if ( datafileList.size() != datafileList.size() )
            throw new IllegalArgumentException("possibly corrupted data structure (data file lists sizes differs)");
        
            // XXX: final (expensive) sanity check: execution status must increase monotonically
        if ( getStatePercentCompleted() > g.getStatePercentCompleted())
            throw new IllegalArgumentException("possibly corrupted data structure (execution status not increasing monotonically)");

        
            // copy task list state
        this.taskList = (Vector) g.taskList.clone();
        
            // copy data files state
        this.datafileList = (Vector<DataFile>) g.datafileList.clone();

            // update peer prefuse structures
        updateGraphNodesInternalData();
    }

    /**
     * Updates the peer prefuse representation of this graph.
     *
     */
    private final void updateGraphNodesInternalData()
	{
		edu.berkeley.guir.prefuse.graph.Graph g = this.getGraph();
		GraphNode[] n = GraphGenerator.convertGraphNodesIteratorToNodesArray(g.getNodes());
        
            // for each node in the graph
		for(int i =0; i < n.length; i++)
		{
			Task x = (Task)n[i].getNodeData(); // retorna a tarefa do n? [i]
			Task xlist = this.getTask(x.getTaskId());
			if( xlist != null)
			{
                    // atualiza a tarefa do node [i] do grafo, usando os dados da lista de tarefas 
				n[i].setNodeData(xlist);
                    // atualiza os atributos do n?, o status
				n[i].setAttribute("status", xlist.getState().getName()); 
				n[i].setAttribute("label",
                                  "Name: " + xlist.getTaskId()
                                  + ", SubMan: " +xlist.getSubmissionManagerId()
                                  +" , INput: " + xlist.getFiles().getInputFiles().length
                                  + ", OUTput: " + xlist.getFiles().getOutputFiles().length
                                  + ", STATUS: " + xlist.getState().getName());
                
				log.debug("Name: "+xlist.getTaskId()
                                   +"  Status:"+xlist.getState().getName()
                                   +" DAG_DSC "+ appDescription);

                    // inserir aqui chamadas para atualizacao dos
                    // nodos. "xlist.getTaskId()" eh o nome da tarefa
				appDescription = appman.parser.SimpleParser.appDescription;

                    // quando a tarefa nao tem nome nao consigo achar na minha estrutura
                    // e retorno -1;
				    // no caso do xlist.getTaskId() do Lucas, retorna vazio e nao consigo
				    // achar o indice na minha estrutura
				indexDSC = appDescription.applicationDAG.getIndexByName(xlist.getTaskId());
				log.debug("GET_TASK_ID: "+xlist.getTaskId()
                                   +"  GET_NAME: "+xlist.getName()
                                   + "indexDSC: "+indexDSC);
				appDescription.applicationDAG.changeColor(indexDSC,xlist.getState().getColor());
			}
		}
	}
}
