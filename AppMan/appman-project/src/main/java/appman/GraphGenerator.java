/*
 * Created on Jun 18, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package appman;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import appman.clustering.DAG_DSC;
import appman.log.Debug;
import appman.parser.ApplicationDescription;
import appman.parser.TaskDescription;
import appman.task.MyTask;
import appman.task.Task;
import appman.task.TaskType;
import edu.berkeley.guir.prefuse.graph.DefaultEdge;
import edu.berkeley.guir.prefuse.graph.DefaultGraph;
import edu.berkeley.guir.prefuse.graph.DefaultTreeNode;
import edu.berkeley.guir.prefuse.graph.Edge;
import edu.berkeley.guir.prefuse.graph.Graph;
import edu.berkeley.guir.prefuse.graph.GraphLib;
import edu.berkeley.guir.prefuse.graph.Node;

/**
 * @author lucasa
 *
 */
public class GraphGenerator extends GraphLib
{
		private int taskid = 0; // generate tasks ids
		private int fileid = 0; // generate datafile ids
		private Vector datafiles;
		private String submanid;
		private String[] listsubmanid;
				
		public GraphGenerator(String subid)
		{
			//Debug.debug("GraphGenerator loading submisson manager Id:");
			submanid = new String(subid);
			listsubmanid = new String[]{new String(submanid)};
		}
		
		public GraphGenerator(String[] subid)
		{
			listsubmanid = subid;
			submanid = listsubmanid[0];
			/*
			Debug.debug("GraphGenerator loading submisson managers Ids:");
			for(int i=0; i < listsubmanid.length; i++)
			{
				Debug.debug(listsubmanid[i]);
			}
			*/
		}
	
		public Graph getRandomDirected(int n, int l)
		{
			Graph g = new DefaultGraph(true);
						
			for ( int i=0; i < n; i++ )
			{
				Node nn = new DefaultTreeNode();
				nn.setAttribute("label",String.valueOf(i));
				g.addNode(nn);
			}
			
			Node[] nodes = convertGraphNodesIteratorToNodesArray(g.getNodes());
			Random rand = new Random();
			for ( int j=0; j < l; j++ )
			{
				int from = rand.nextInt(nodes.length);
				int to = rand.nextInt(nodes.length);				
				Edge e = new DefaultEdge(nodes[from], nodes[to], true);
				g.addEdge(e);
			}
			return g;
		} //
		
	public static GraphNode[] convertGraphNodesIteratorToNodesArray(Iterator iter)
	{
			Vector nodes = new Vector();		
			while ( iter.hasNext() )
			{
						GraphNode n = (GraphNode)iter.next();
						nodes.addElement(n);
			}
					return (GraphNode[])nodes.toArray(new GraphNode[nodes.size()]);
	}
	
		private void insertRandomDirectedSubTree(Graph g, GraphNode node, int n, int depth, float depend)
		{
			int d = depth - 1; // decrementa profundidade da subarvore
			if(d < 0) return; // quando atingir profundidade maxima, return
			
			Random rand = new Random();
			int r = Math.abs(rand.nextInt()%listsubmanid.length);
			String random_submanid = new String(listsubmanid[r]);
			Debug.debug("GraphGenerator creating task with submisson managers Id: " + random_submanid);
			//Node child = new DefaultTreeNode(); // create root
			GraphNode child = new GraphNode(createDefaultTask(String.valueOf(taskid),random_submanid,"/bin/echo ["+ String.valueOf(taskid)+"]  > ./file["+String.valueOf(taskid)+"].txt"));			
			child.setAttribute("label",((Task)child.getNodeData()).getTaskId());
			child.setAttribute("depth",String.valueOf(d));
			child.setAttribute("status","DEPENDENT");
			g.addNode(child);
			taskid+= 1;
						
			// create the file dependencies between the child and the father
			Task from = (Task)(child).getNodeData();		
			DataFile datafile = new DataFile( "./file["+from.getTaskId()+"].txt", String.valueOf(fileid), from);
			datafiles.addElement(datafile); // add this new datafile to the list
			from.getFiles().addDataFileToOutputList(datafile);
			Task to = (Task)(node).getNodeData();						
			to.getFiles().addDataFileToInputList(datafile);
			fileid+= 1;
			
			// create a inverse edge, to the files go from the child to the center of the graph
			Edge e = new DefaultEdge(child, node, true);
			g.addEdge(e);
			
			// create some random dependencies from another node
				int k = rand.nextInt(g.getNodeCount());
				GraphNode[] all = convertGraphNodesIteratorToNodesArray(g.getNodes());
				int ki = new Integer(all[k].getAttribute("depth")).intValue();
				if(ki < d) // to avoid loops in the graph 
				{
					if(rand.nextFloat() < depend)
					{
						from = (Task)(all[k]).getNodeData();						
						datafile = new DataFile( "./file["+from.getTaskId()+"].txt", String.valueOf(fileid), from);
						datafiles.addElement(datafile); // add this new datafile to the list
						from.getFiles().addDataFileToOutputList(datafile);
						
						to = (Task)(child).getNodeData();						
						to.getFiles().addDataFileToInputList(datafile);
						g.addEdge(new DefaultEdge(all[k], child, true));
						fileid+= 1; // increment to create new datafile id
					}
				}			
			
			// create the subtree recursively
			for ( int i=0; i < n; i++ )
			{
				insertRandomDirectedSubTree(g, child, n, d, depend);
			}
		}
		
		private static Task createDefaultTask(String myid, String mysubmanid, String cmd)
		{					
					MyTask task = new MyTask(mysubmanid,myid,"tarefa de teste","tarefa"+myid, cmd);					
					return task;
		}

	public Graph getRandomTreeDirected(int n, int depth, float con)
	{
		datafiles = new Vector();
		Graph g = new DefaultGraph(true);
		taskid = 0;
		
		Random rand = new Random();		
		
		for ( int i=0; i < n; i++ )
		{
			// random choose the sumbmanid from the list
			int r = Math.abs(rand.nextInt()%listsubmanid.length); 
			String random_submanid = new String(listsubmanid[r]);
			Debug.debug("GraphGenerator creating task with submisson managers Id: " + random_submanid);
			GraphNode nn = new GraphNode(createDefaultTask(String.valueOf(taskid), random_submanid,"/bin/echo ["+ String.valueOf(taskid)+"]  > ./file["+String.valueOf(taskid)+"].txt"));				
			nn.setAttribute("label",((Task)nn.getNodeData()).getTaskId());
			nn.setAttribute("depth",String.valueOf(depth));
			nn.setAttribute("status","DEPENDENT");
			g.addNode(nn);
			taskid = taskid +1;
		}
			
		GraphNode[] nodes = convertGraphNodesIteratorToNodesArray(g.getNodes());
			
		for ( int j=0; j < n; j++ )
		{
			insertRandomDirectedSubTree(g, nodes[j], n, depth, (float)0.7);				
		}
			
		return g;
	}

			// return the list of datafiles created in the generator
			public Vector getDataFileList()
			{
				return datafiles;
			}
			// return a vector with the objects that are inside the nodes
			public static Vector getTaskList(Graph g)
			{
					Vector list = new Vector();
					GraphNode[] nodes = convertGraphNodesIteratorToNodesArray(g.getNodes());
					for(int i=0;i<nodes.length;i++)
					{
						list.addElement(nodes[i].getNodeData());
					}
					
					return list;
			}
			
			// lucas - 20/07/2004
			// this method create a prefuse lib graph from the ApplicationDescription loaded in the parse module
			public Graph convertApplicationDescriptionToGraph(ApplicationDescription appdesc)
			{				
				Graph g = new DefaultGraph(true);
				datafiles = new Vector();				
				//DAG dag = appdesc.getDAG();	-->comentado por vindn
				DAG_DSC dag = appdesc.getDAG();	//-->inserido por vindn
				int n = appdesc.getNumberOfTasks();
				List taskList = appdesc.getListOfTasks();
				TaskDescription[] tasks = (TaskDescription[])taskList.toArray(new TaskDescription[0]);
				
				Debug.debug("GraphGenerator convertApplicationDescriptionToGraph: num tasks - " + n);
				
				Vector gnodes = new Vector();// vetor auxiliar com dados do nodo do grafo
				Vector gnodes_names = new Vector();// vetor auxiliar com o nome da tarefa do nodo
				
				for ( int i=0; i < n; i++ )
				{	
					String id = tasks[i].getTaskName();
					String subid = tasks[i].getClusterId();
					String task_command = tasks[i].getExcutable();					
					//String task_command = "/bin/echo " + tasks[i].getTaskName() + " > "+ tasks[i].getTaskName() +".out";
					
					GraphNode node = new GraphNode(createDefaultTask( id, subid, task_command));				
					node.setAttribute("label",((Task)node.getNodeData()).getTaskId());					
					node.setAttribute("status","DEPENDENT");
					g.addNode(node); // add task in  the graph
					gnodes.addElement(node);
					gnodes_names.addElement(id);
					//Debug.debug("GraphGenerator convertApplicationDescriptionToGraph: add Task command line: " + ((Task)node.getNodeData()).getCommand_Line());VDN:4/1/6					
				}
				
					   Enumeration e = appdesc.getListOfTasks().elements();
					   while (e.hasMoreElements())
					   {
						  TaskDescription t = (TaskDescription)e.nextElement();
						  //dag.putNodeName(t.getTaskName());
          
						  // outline of the algorithm:
						  //    for each task "t"
						  //       check each input file "i"
						  //           search for each task "ti" until find first output equals to "i"
						  //		   include an input datafile in the task "t" ("ti" -> "t")
						  //           include an output datafile in the task "ti" ("ti" -> "t"), if the output datafile was not inserted yet					  
          
						  Enumeration inputList = (t.getInputFiles()).elements();
						  while (inputList.hasMoreElements())
						  {
							 String outfile = (String) inputList.nextElement();
             				
							 Enumeration aux = appdesc.getListOfTasks().elements();
							 boolean found = false;
							 while ((!found) && (aux.hasMoreElements()))
							 {
								TaskDescription t_aux = (TaskDescription)aux.nextElement();
								if (!(t_aux.equals(t)))
								{
								   if (t_aux.hasOutputFile(outfile))
								   {
										String to_name = t.getTaskName();
										String from_name = t_aux.getTaskName();
										GraphNode to = (GraphNode)gnodes.elementAt(gnodes_names.indexOf(to_name));
										GraphNode from = (GraphNode)gnodes.elementAt(gnodes_names.indexOf(from_name));
										
										Edge ed = new DefaultEdge(from, to, true);
										g.addEdge(ed);
										
										String filename = outfile;
										 
										Task tfrom = (Task)from.getNodeData();
										Task tto = (Task)to.getNodeData();
										 DataFile datafile = new DataFile(filename, filename, tfrom);										 
										 datafiles.addElement(datafile); // add this new datafile to the list
										 tto.getFiles().addDataFileToInputList(datafile);
										 //Debug.debug("GraphGenerator convertApplicationDescriptionToGraph add datafile ["+datafile.getDataFileId()+"] input dependency from: " + tfrom.getTaskId() +" -> to: " + tto.getTaskId()); VDN:4/1/6
										 
										 DataFile[] from_out = tfrom.getFiles().getOutputFiles();
										 boolean already_inserted = false;
										 for(int i=0; i < from_out.length; i++)
										 {
										 	if(from_out[i].getName().equals(datafile.getName()))
										 	{
										 		already_inserted = true;
										 	}										 		
										 }
										 if(! already_inserted)
										 {
											tfrom.getFiles().addDataFileToOutputList(datafile);
											//Debug.debug("GraphGenerator convertApplicationDescriptionToGraph add datafile ["+datafile.getDataFileId()+"] output dependency from: " + tfrom.getTaskId() +" -> to: " + tto.getTaskId());VDN: 4/1/6
										 }

									  	found = true;
								   }
								}
							 }
							 
							 if(found == false) // se o arquivo de entrada ? input da aplica??o, ou seja nao possui uma tarefa que gera o arquivo
							 {
								String to_name = t.getTaskName();
								GraphNode to = (GraphNode)gnodes.elementAt(gnodes_names.indexOf(to_name));
								String filename = outfile;
								Task tto = (Task)to.getNodeData();
								DataFile datafile = new DataFile(filename, filename, null);
								datafile.setDataFileExist(true);										 
								datafiles.addElement(datafile); // add this new datafile to the list
								tto.getFiles().addDataFileToInputList(datafile);
								//Debug.debug("GraphGenerator convertApplicationDescriptionToGraph add datafile ["+datafile.getDataFileId()+"] input dependency from Application Inputs to Task ["+tto.getTaskId()+"]", true); VDN: 2006/01/13
							 }

						  }
					   }
					   
					   /////
				e = appdesc.getListOfTasks().elements();
				while (e.hasMoreElements())
				{
				   TaskDescription t = (TaskDescription)e.nextElement();
				   // outline of the algorithm:
				   //    for each task "t"
				   //       check each output file "i"
				   //           search for each task "ti" until find first output equals to "i"
				   //		   include an input datafile in the task "t" ("ti" -> "t")
				   //           include an output datafile in the task "ti" ("ti" -> "t"), if the output datafile was not inserted yet          
				   Enumeration outputList = (t.getOutputFiles()).elements();
				   while (outputList.hasMoreElements())
				   {
					  String outfile = (String) outputList.nextElement();
					  Enumeration aux = appdesc.getListOfTasks().elements();
					  boolean found = false;
					  while ((!found) && (aux.hasMoreElements()))
					  {
						 TaskDescription t_aux = (TaskDescription)aux.nextElement();
						 if (!(t_aux.equals(t)))
						 {
							if (t_aux.hasInputFile(outfile))
							{
								found = true;
							}
						 }
					  }
					
					  if(found == false) // se o arquivo de entrada � output da aplica��o, ou seja nao possui uma tarefa que gera o arquivo
					  {
					  	String filename = outfile;
						String to_name = t.getTaskName();
						GraphNode to = (GraphNode)gnodes.elementAt(gnodes_names.indexOf(to_name));
						Task task = (Task)to.getNodeData();
						task.setTaskType(TaskType.TASK_TYPE_FINAL);
						//Debug.debug("GraphGenerator convertApplicationDescriptionToGraph SET Task["+task.getTaskId()+"] TYPE FINAL", true);  VDN 2006/01/13
						DataFile datafile = new DataFile(filename, filename, task);
						datafile.setDataFileExist(false);
						datafiles.addElement(datafile); // add this new datafile to the list						
						task.getFiles().addDataFileToOutputList(datafile);					 	
					 	//Debug.debug("GraphGenerator convertApplicationDescriptionToGraph add datafile ["+datafile.getDataFileId()+"] output dependency from Application Outputs to Task ["+task.getTaskId()+"]", true); VDN 2006/01/13
					  }

				   }
				}					   
			
				return g;
			}
			
			/*
			 * Execute a random clustering algorithm, setting the clusterId field of the tasks
			 */
			public static void clusteringAlgorithm(String[] clusterId, ApplicationDescription appdesc)
			{
				int n = appdesc.getNumberOfTasks();			
				List taskList = appdesc.getListOfTasks();
				TaskDescription[] tasks = (TaskDescription[])taskList.toArray(new TaskDescription[0]);
				Random rand  = new Random();
				for(int i=0; i < n; i++)
				{
					int r = Math.abs(rand.nextInt()%clusterId.length); 
					String id = new String(clusterId[r]);
					tasks[i].setClusterId(id);
				}
			}
			
			/*
			 * VDN:8/09/05
			 * Execute a phase clustering algorithm, setting the clusterId field of the tasks
			 */
			public static void clusteringPhaseAlgorithm(Vector cluster, String[] clusterId, ApplicationDescription appdesc)
			{
				int n = appdesc.getNumberOfTasks();
				List taskList = appdesc.getListOfTasks();
				TaskDescription[] tasks = (TaskDescription[])taskList.toArray(new TaskDescription[0]);
				String taskName = new String();
				
				
				for(int i=0; i < cluster.size(); i++)//i equivale ao nivel
				{
					System.out.print("Level "+i+" ");
					for(int j=0; j < ((Vector)cluster.get(i)).size(); j++)//j equivale as tarefas q estao no nivel i
					{
						taskName = ((String)((Vector)cluster.get(i)).get(j)); //nome da tarefa
						System.out.print(((Vector)(cluster.get(i))).get(j) + " " );
						for(int k=0; k < tasks.length; k++)//tenho q buscar o nome da tarefa. arggg!!
						{
							
							if( taskName.compareTo(tasks[k].getTaskName()) == 0 )
							{
								String id = new String(clusterId[i]);
								tasks[k].setClusterId( id );
								System.out.print("\n 1: "+taskName+" 2: "+tasks[k].getTaskName()+" cluster "+id+"\n");
								break;
							}
						}
					}
					System.out.print("\n");
				}
				System.out.print("SAIU!\n");
				
			}
			
		
}// fim da classe
