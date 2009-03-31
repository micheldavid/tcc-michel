package appman;

import java.io.Serializable;
import java.util.Vector;

import appman.parser.ApplicationDescription;
import appman.parser.SimpleParser;

public class Graph implements Serializable
{
	private String graphId;
	private String mysubmanId; // default submission manager

    private int state;
    
	public static final int GRAPH_READY = 0;
	public static final int GRAPH_EXECUTING= 1;
	public static final int GRAPH_END= 2;

		
	private edu.berkeley.guir.prefuse.graph.Graph graph;
	
	private Vector taskList; // vector of the tasks that are managed by the default subman
	//private Vector alltaskList; // vector of all tasks of the graph
	private Vector datafileList;
	
	// info from the parser module
	private ApplicationDescription appDescription;
	
	
	
	public Graph(String graphid, String subid, ApplicationDescription appdesc)
	{
		graphId = new String(graphid);
		mysubmanId = new String(subid);
		datafileList = new Vector();
		taskList = new Vector();
		GraphGenerator generator = new GraphGenerator(subid);
		graph = generator.convertApplicationDescriptionToGraph(appdesc);
		datafileList = generator.getDataFileList();
		taskList = buildTaskList();
		
		state = GRAPH_READY;
	
		updateGraphNodesInternalData();

		Debug.debug("Graph new graph ["+ taskList.size() +"]nodes created: " + graphId + " - SubMan: " + mysubmanId);
	}
	
	public Graph(String graphid, String subid)
	{
		graphId = new String(graphid);
		mysubmanId = new String(subid);
		//graph = new edu.berkeley.guir.prefuse.graph.DefaultGraph(true);		
		datafileList = new Vector();
		taskList = new Vector();		
		
		createDefaultGraph(2, 2, (float) 0.9);
		
		updateGraphNodesInternalData();
		
		state = GRAPH_READY;
		
		Debug.debug("Graph new graph ["+ taskList.size() +"]nodes created: " + graphId + " - SubMan: " + mysubmanId);
	}
	public Graph(String graphid, String subid, String subidother)
	{
		graphId = new String(graphid);
		mysubmanId = new String(subid);
		//graph = new edu.berkeley.guir.prefuse.graph.DefaultGraph(true);		
		datafileList = new Vector();
		taskList = new Vector();		
	
		createDefaultGraph2(2, 2, (float) 0.9, subid, subidother);
	
		updateGraphNodesInternalData();
	
		state = GRAPH_READY;
	
		Debug.debug("Graph new graph ["+ taskList.size() +"]nodes created: " + graphId + " - SubMan: " + mysubmanId);
	}
	public Graph(String graphid, String subid, boolean empty)
	{
		graphId = new String(graphid);
		mysubmanId = new String(subid);
		graph = null;		
		datafileList = new Vector();
		taskList = new Vector();
		
		if(empty == false)
		{
			createDefaultGraph(2, 2, (float) 0.9);
			updateGraphNodesInternalData();
			state = GRAPH_READY;
			Debug.debug("Graph new graph ["+ taskList.size() +"]nodes created: " + graphId + " - SubMan: " + mysubmanId);
		}
		else
		{
			state = GRAPH_READY;
			Debug.debug("Graph new empty graph created");
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
		Debug.debug(datafileList);
		
		taskList = buildTaskList();
		Debug.debug(taskList);
		
		Debug.debug("Graph default graph structure created");
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
			Debug.debug(datafileList);
		
			taskList = buildTaskList();
			Debug.debug(taskList);
		
			Debug.debug("Graph default graph structure created");
		}
		
	/*
	* A partir dos dados da lista de tarefas, atualize o conteudo dos nodos do grafo
	* Esta fun??o deve ser chamada quando o conte?do da lista de tarefas e de datafiles forem alterados
	*/	

	public int StateToIntColor( String state ){
		
		int color=-1;
		
		if( state.compareTo("TASK_DEPENDENT") == 0 ){
			color = 3;//red
		}
		else if( state.compareTo("TASK_READY") == 0 ){
			color = 1;//yellow
		}
		else if( state.compareTo("TASK_EXECUTING") == 0 ){
			color = 0;//green
		}
		else if( state.compareTo("TASK_FINAL") == 0 ){
			color = 2;//blue
		}
		
		return color;
			 
	}
		
	

		
	int indexDSC;
	public void updateGraphNodesInternalData()
	{
		edu.berkeley.guir.prefuse.graph.Graph g = this.getGraph();
		GraphNode[] n = GraphGenerator.convertGraphNodesIteratorToNodesArray(g.getNodes());		
		// para cada n? do grafo
		for(int i =0; i < n.length; i++)
		{
			Task x = (Task)n[i].getNodeData(); // retorna a tarefa do n? [i]
			Task xlist = this.getTask(x.getTaskId());
			if( xlist != null)
			{
				n[i].setNodeData(xlist); // atualiza a tarefa do node [i] do grafo, usando os dados da lista de tarefas			
				n[i].setAttribute("status", xlist.getTaskStateString()); // atualiza os atributos do n?, o status
				n[i].setAttribute("label", "Name: " + xlist.getTaskId() + ", SubMan: " +xlist.getMySubManagerId()+" , INput: " + xlist.getInputFiles().length + ", OUTput: " + xlist.getOutputFiles().length + ", STATUS: " + xlist.getTaskStateString());
				Debug.log(this+"\tName: "+xlist.getTaskId()+"  Status:"+xlist.getTaskStateString()+" DAG_DSC "+ appDescription); 
				//inserir aqui chamadas para atualizacao dos nodos. "xlist.getTaskId()" eh o nome da tarefa 


				appDescription = appman.parser.SimpleParser.appDescription;
				//quando a tarefa nao tem nome nao consigo achar na minha estrutura e retorno -1
				//no caso do xlist.getTaskId() do Lucas, retorna vazio e nao consigo achar o indice na minha estrutura
				indexDSC = appDescription.applicationDAG.getIndexByName(xlist.getTaskId());
				System.out.println("\n\nGET_TASK_ID: "+xlist.getTaskId()+"  GET_NAME: "+xlist.getName()+ "indexDSC: "+indexDSC+"\n\n");
				//indexDSC = appDescription.applicationDAG.getIndexByName(xlist.getName());
				//System.out.println("STATE VDN:"+xlist.getTaskStateString());
				appDescription.applicationDAG.changeColor(indexDSC, StateToIntColor(xlist.getTaskStateString()));
				//System.out.println("[GRAPH UPDATE] "+"indexDSC:"+indexDSC);
				
				/*/
				if(  ((String)xlist.getTaskStateString()).compareTo("TASK_FINAL") == 0 ){ 
					appDescription.incrementFinishedTasks();
					
				}
				*/

			}
		}
	}
	
		/*
		* A partir do conteudo dos nodos do grafo, atualize os dados da lista de tarefas		
		*/	
/*		public void updateInternalDataFromGraphNodes()
		{
			//edu.berkeley.guir.prefuse.graph.Graph g = this.getGraph();
			//GraphNode[] n = GraphGenerator.convertGraphNodesIteratorToNodesArray(g.getNodes());
			
			Vector tasks = getAllTaskList();
			synchronized(taskList)
			{			
				taskList.removeAllElements();
				// para cada n? do grafo								
				for(int i =0; i < tasks.size(); i++)
				{
						Task xall = (Task)tasks.get(i);				
						if(xall.getMySubManagerId().equals(this.getSubmissionManagerId()))
						{
							taskList.add(xall);
						}			
				}
			}
		}
	*/
	public Vector getTaskList()
	{
		return taskList;
	}
	
	public void setTaskList(Vector list)
	{
		taskList = list;
	}
		
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
				if( isAllTaskInputsAvailable(t.getTaskId()) && t.getTaskState()==Task.TASK_DEPENDENT )
				{
					t.setTaskState(Task.TASK_READY);
					readyTasks.add(t);
				}
			}
			//Debug.debug("Task READY: " + readyTasks);	
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
			int numberOfInsertedTasks=0;
			for(int i=0;i<taskList.size();i++)
			{
				Task t = (Task)taskList.elementAt(i); 
				if( isAllTaskInputsAvailable(t.getTaskId()) && t.getTaskState()==Task.TASK_DEPENDENT )
				{
					t.setTaskState(Task.TASK_READY);
					readyTasks.add(t);
					numberOfInsertedTasks++;
				}
				// check if reached limit of tasks to be included
				if (maxNumberOfTasks == numberOfInsertedTasks) { 
					break;
				}
			}
			//Debug.debug("Task READY: " + readyTasks);	
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
				if( ! isAllTaskInputsAvailable(t.getTaskId()) && t.getTaskState()==Task.TASK_DEPENDENT)
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
					DataFile d = (DataFile)datafileList.elementAt(j);
					
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
									if(d.getFromTask().getTaskState() == Task.TASK_FOREIGN_FINAL)
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
		DataFile[] inputs = task.getInputFiles();
		
		for(int i=0;i < inputs.length;i++)
		{			
				if( ! isDataFileAvailable(inputs[i].getDataFileId()) )
				{
					//Debug.debug("Graph isDisponibleTaskInputsTask ["+task.getTaskId()+"] FALSE: " + inputs[i].getDataFileId());
					return false;
				}
		}
		
		return true;
	}
	
	public Vector buildTaskList()
	{
		// get the tasks vector from the graph
		Vector tasks = GraphGenerator.getTaskList(graph);
		Debug.debug("build task: " + tasks);		
		// add mytasks to the task list
		Vector mytasks = new Vector();
		// create a vector with the tasks from the my submanager
		Debug.debug("Graph ["+ getGraphId() +"] buildtaskList SubManId: " + getSubmissionManagerId());
		
		for(int i=0;i<tasks.size();i++)
		{
			if( ((Task)tasks.elementAt(i)).getMySubManagerId().compareTo(mysubmanId) == 0)
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
			if( ((Task)tasks.elementAt(i)).getMySubManagerId().compareTo(mysubmanId) == 0)
			{
				((Task)tasks.elementAt(i)).setTaskSubmissionManagerId(subid);
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
			if( ((Task)taskList.elementAt(i)).getTaskState() == Task.TASK_FINAL )
			{
				n+= 1;
			}		
		}
		if(taskList.size() > 0)
			return (float)(n / taskList.size());
		else
			return 1;	
	}
	
	public Vector getDataFileList()
	{
			return datafileList;
	}
	
	public void setDataFileList(Vector list)
	{
			datafileList = list;
	}
	
	/**
	 * @return string
	 */
	public String getGraphId()
	{
		return graphId;
	}
	
	public String getSubmissionManagerId()
	{
		return mysubmanId;
	}
	
	

	/**
	 * @param string
	 */
	public void setGraphId(String string) {
		graphId = string;
	}

	/**
	 * @return
	 */
	public synchronized int getState()
	{
		return state;
	}

	/**
	 * @param f
	 */
	public synchronized void setState(int s)
	{
		state = s;
	}

	public edu.berkeley.guir.prefuse.graph.Graph getGraph()
	{
		return graph;
	}

	public void setGraph(edu.berkeley.guir.prefuse.graph.Graph graph)
	{
		this.graph = (edu.berkeley.guir.prefuse.graph.Graph)graph;
	}

}
