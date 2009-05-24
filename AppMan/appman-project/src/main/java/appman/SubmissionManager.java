package appman;

import java.rmi.RemoteException;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import appman.task.Task;
import appman.task.TaskManager;
import appman.task.TaskState;

public class SubmissionManager implements SubmissionManagerRemote, Runnable
{
	private static final Log log = LogFactory.getLog(SubmissionManager.class);
    private String submissionmanagerId;

    
    private Vector tasksmanagerList; // Vector<TaskManager>
        /** para gerar novos numeros de ids de TaskManagers */
    private int taskmanId = 0;  
    private Vector graphs; // Vector<Graph>

    private ApplicationManagerRemote appmanager = null;

    private boolean end = false; // flag que sinaliza o fim da execucao // VDN 2006/01/31 - tiramos o volatile
    volatile private static int static_die = 0;
    
    String my_contact_address = null; // endereço de contato deste objeto

    /** PKVM (2005/08/30) : to limit number of tasks to each SM */
    private static final int MAX_NUMBER_OF_TASKS_TO_SM = 100;//20
    /** Lucas (2005/08/31) : to limit number of tasks to each subgraph */
    private static final int MAX_NUMBER_OF_TASKS_TO_SG = 100;//10
    
    private static final int MAX_NUMBER_OF_RETRIES = 5;//10
    
    public static ImproveDownload ID = new ImproveDownload(MAX_NUMBER_OF_TASKS_TO_SM); 
    private long downloadTimeOfTasksManagers = 0;
    
    public SubmissionManager(String id, String contact_address)
        {
            setSubmissionManagerId(id);
            graphs = new Vector();
            tasksmanagerList = new Vector();
            //ID  = new ImproveDownload();
            appmanager = (ApplicationManagerRemote)GeneralObjectActivator.getRemoteObjectReference(contact_address, ApplicationManagerRemote.class);
            System.out.println("\n\n\n[CONTACT]: "+contact_address+"\n\n\n");
            log.debug("SubmissionManager created.");	
            static_die++;
            
        }

    public float getGraphStatePercentCompletedRemote(String graphId) throws RemoteException
        {
            return getGraphStatePercentCompleted(graphId);
        }
    public float getGraphStatePercentCompleted(String graphId)
        {
            return getGraph(graphId).getStatePercentCompleted();
        }
    public float getTaskStateRemote(String taskId, String graphId) throws RemoteException
        {
            return getTaskState(taskId, graphId);
        }
    public float getTaskState(String taskId, String graphId)
        {
            return getGraph(graphId).getTask(taskId).getState().getCode();
        }
    public  void addGraphRemote(Graph g) throws RemoteException
        {
            addGraph(g);
        }
    public void addGraph(Graph g)
        {	
            synchronized(graphs)
            {
            	if (graphs == null) System.out.println("ERRO  addGraph == graphs Null :-P");//PKVM
            	if (g == null) System.out.println("ERRO addGraph == g Null :-P");//PKVM

                g.setState(Graph.GRAPH_READY);		
                graphs.addElement(g);
                log.debug("SubmissionManager ["+this.getSubmissionManagerId()+"] add a graph: " + g.PrintInfo() + " in the new list: " + graphs.size());
            }
        }
    public Graph getGraphRemote(String graphId) throws RemoteException
        {
            return getGraph(graphId);
        }
    public Graph getGraph(String graphId)
        {
            for(int i=0;i<graphs.size();i++)
            {
                if( ((Graph)graphs.elementAt(i)).getGraphId().compareTo(graphId) == 0  )
                {
                    return (Graph)graphs.elementAt(i);
                }		
            }	
		 
            return null;
        }

    public String getMyObjectRemoteContactAddress() throws java.rmi.RemoteException
    {
    		return my_contact_address;    
    }
    
    public byte[] downloadFileFromGridTask(String taskId, String filepath) throws java.rmi.RemoteException
    {
    	for(int i=0;i < graphs.size(); i++)
        {
            Graph g = (Graph)graphs.elementAt(i);
            Task t = g.getTask(taskId);
            if(t != null)
            {
                if(t.getState().getCode() == TaskState.TASK_FINAL)
                {                    
                    GridFileServiceRemote rfs = t.getRemoteGridTaskFileService();
                    log.debug("SubmissionManager ["+this.getSubmissionManagerId()+"] downloading a file ["+filepath+"] from GridTask: " + taskId);
                    return rfs.downloadFile(filepath);
                }
            }
        }	
            return null;
    }
    /**
     *  Calculate how many tasks are managed by this Submission Manager.
     * 
     * @return n Tasks.
     */
    private int computeSubmissonManagerTasksLoad()
    {
    	int num_tasks_running = 0;
    	int num_tasks_ready = 0; // VDN 2006/02/10 - included to count ready tasks
    	for(int i=0; i<tasksmanagerList.size(); i++)
    	{
    		TaskManager tm = (TaskManager)tasksmanagerList.elementAt(i);
    		// soma o numero de tarefas que estao executando
    		num_tasks_running += tm.getTaskCount(TaskState.TASK_EXECUTING);
    		// soma o numero de tarefas prontas
    		num_tasks_ready += tm.getTaskCount(TaskState.TASK_READY);
    		if (num_tasks_ready>0) log.debug("SM getTaskCount READY - not counted ..."+num_tasks_ready);
    	}
    	return num_tasks_running+num_tasks_ready;
    }
    private void executeGraph(String graphId)
        {		
            // retorna o grafo do vetor de grafos
            Graph graph = getGraph(graphId);	
            if(graph.getState() == Graph.GRAPH_READY)
            {		
                graph.setState(Graph.GRAPH_EXECUTING);		
            }
	
                //Debug.debug("SubmissionManager ["+this.getSubmissionManagerId()+"] get ready tasks List from Graph.", true);
            //  original do lucas => agora chamado la embaixo, com nro limitado de tarefas
            // Vector allreadytasks = graph.getReadyTaskList();
            Vector newreadytasks = new Vector();
            
            int num_tasks_running = computeSubmissonManagerTasksLoad();
            // se o numero de tarefas executando é menor que o máximo
            // então adiciona mais tarefas aos Task Managers
            log.debug("\t ****NUMERO DE TAREFAS NO SM:"+num_tasks_running+"\n");
            System.out.print("\t ****NUMERO DE TAREFAS NO SM:"+num_tasks_running+"\n");
            if(num_tasks_running < SubmissionManager.MAX_NUMBER_OF_TASKS_TO_SM)
            {            	
            	int n = SubmissionManager.MAX_NUMBER_OF_TASKS_TO_SM - num_tasks_running; // numero de novas tarefas a serem executadas
            	//(limite de tarefas para cada grafo, evita que o sub-grafo ocupe todo o SM, deixando os outros de lado)
            	if(n > SubmissionManager.MAX_NUMBER_OF_TASKS_TO_SG)
            		n = SubmissionManager.MAX_NUMBER_OF_TASKS_TO_SG;
            	log.debug("SubmissionManager ["+this.getSubmissionManagerId()+"] executing graph [" + graphId + "] can manage "+ n +" more tasks, the limit is "+ MAX_NUMBER_OF_TASKS_TO_SM);
            	// se há mais tarefas para executar que o valor limite
            	// então adiciona apenas algumas
            	
            	//PKVM 2006/01/06 => this call replaces the original method call without parameter
            	// This was include to solve a bug detected by PKVM and VDN: AppMan did not worked
            	// for clusters with size greater than MAX_NUMBER_OF_TASKS_TO_SG.
            	// The problem happen when just a part of the ready tasks was included
            	// (reached the limit) and the other tasks was never put in execution
            	// (the rest of the list was ignored and the other tasks, since was already set to READY
            	// would never be included again in the allreadytasks list)
                Vector allreadytasks = graph.getReadyTaskList(n);

                // adiciona o numero maximo de novas tarefas
                n = ( n > allreadytasks.size() ) ? allreadytasks.size() : n;
            	for(int i=0; i < n; i++)
            	{
            		newreadytasks.addElement(allreadytasks.elementAt(i));
            	}
            	
            }
            else
            {
            	log.debug("SubmissionManager ["+this.getSubmissionManagerId()+"] executing graph [" + graphId + "] CANNOT MANAGE more tasks, there is "+num_tasks_running+", the limit ");
            }
	
                // insere a lista de tarefas no TaskManager
            if(newreadytasks.size() > 0)
            {
                log.debug("SubmissionManager ["+this.getSubmissionManagerId()+"] executing graph: " + graphId);
		 
                TaskManager taskman = null;
                if(taskmanId < 1) // limita o numero de task managers      
                {                	
                     //	cria um novo TaskManager para executar o grado dado
                	taskman = new TaskManager("taskman"+taskmanId);
                    taskmanId++; // novo id
                    tasksmanagerList.addElement(taskman);
                }
		 
                if(taskman != null)
                {                	
                	log.debug("SubmissionManager ["+this.getSubmissionManagerId()+"] add tasks " + newreadytasks.toString() + " to TaskManager.");
                	taskman.addTaskToList(newreadytasks);
                } 
            }
	
                //	Através da lista de tarefas do grafo que não estão prontas para serem executadas,
                // procura informações sobre aquelas que possuem arquivos de entrada provenientes
                // de outro SubmissionManager					
            updateForeignTasksStatus(graph);
        }

    private void updateForeignTasksStatus(Graph g)
        {
            Vector alltasks = g.getAllTaskList();	
	
                // procura tarefas que não pertencem a este SubmissionManager
            for(int i=0;i<alltasks.size();i++)
            {
                Task t = (Task)alltasks.elementAt(i);
				
                if(g.getTask(t.getTaskId()) == null)
                    if( ! isTaskFromHere(t.getTaskId())  ) // the task is from another submissiom manager
                    {
                            //Debug.debug("SubmissionManager graph ["+g.getGraphId()+"] ["+this.getSubmissionManagerId()+"] updating not ready task["+t.getTaskId()+"] " + t.getTaskStateString());			
                        try
                        {				
                            if(t.getState().getCode() == TaskState.TASK_DEPENDENT)
                            {
                                t.setState(TaskState.getInstance(TaskState.TASK_FOREIGN));
								log.debug("Task setting state: " + t.getState().getName());
                            }
                            if(t.getState().getCode() == TaskState.TASK_FOREIGN)
                            {
                                log.debug("SubmissionManager graph ["+g.getGraphId()+"]  ["+this.getSubmissionManagerId()+"]  looking for task["+t.getTaskId()+"] outputs files from another SubmissionManager.");


                                for ( int needRetry = MAX_NUMBER_OF_RETRIES; needRetry>0; needRetry-- ) {
	                                try{
		                                if(appmanager.isTaskOutputsRemoteAvailable(t.getTaskId()))
		                                {
		                                    log.debug("SubmissionManager graph ["+g.getGraphId()+"]  ["+this.getSubmissionManagerId()+"]  found foreign task["+t.getTaskId()+"] FINAL state from another SubmissionManager.");
		                                    String gfsr = appmanager.getTaskGridFileServiceContactAddressRemote(t.getTaskId()); // atualiza a referencia ao serviço de arquivo das task remota
		                                    t.setSubmissionManagerContactAddress(gfsr);
		                                    t.setState(TaskState.getInstance(TaskState.TASK_FOREIGN_FINAL));
											log.debug("Task setting state: " + t.getState().getName());
		                                }
                                            //
                                            // XXX: abort! need not to retry since there were
                                            // no network errors, the files are just not
                                            // yet there.
                                            //
                                        needRetry = 0;
	                                }catch(java.rmi.ConnectException e){
	                                	System.out.println("Trying isTaskOutputsRemoteAvailable - connect");
	                                	e.printStackTrace();
	                                }catch(java.net.SocketException e2) {
	                                	System.out.println("Trying isTaskOutputsRemoteAvailable - socket");
	                                	e2.printStackTrace();	                                	
	                                }
                                    finally {
                                            // avoid to overload the remote node with too many requests
                                        try { Thread.sleep(500); }
                                        catch (InterruptedException ie) {}
                                    }
                                }
                            }
								
                        } catch (RemoteException e)
                        {
                            AppManUtil.exitApplication("Tolerancia a Falhas - ApplicationManager DEAD, Submissonmanager SUICIDE" + e, e);
                        }

                    }
                    else // Else the Task is from a graph managed by this submission manager
                    {
                            //Debug.debug("SubmissionManager looking for tasks from another graph within this SubmissionManager.");
                            // look for the graph that has the task
                        for(int j=0;j<graphs.size();j++)
                        {				
                            Graph gf = (Graph)graphs.elementAt(j);
                            Task tf = gf.getTask(t.getTaskId());
                            if(tf != null)
                            {
                                try
                                {
                                    if(t.getState().getCode() == TaskState.TASK_DEPENDENT)
                                    {									
                                        t.setState(TaskState.getInstance(TaskState.TASK_FOREIGN));
										log.debug("Task setting state: " + t.getState().getName());									
                                            //Debug.debug("SubmissionManager graph ["+g.getGraphId()+"]  ["+this.getSubmissionManagerId()+"]  updating foreign task["+t.getTaskId()+"] status from another graph within this SubmissionManager.");
                                    }
                                    if(t.getState().getCode() == TaskState.TASK_FOREIGN)
                                    {
                                            //Debug.debug("SubmissionManager graph ["+g.getGraphId()+"]  ["+this.getSubmissionManagerId()+"]  looking for task["+t.getTaskId()+"] outputs files from another SubmissionManager.");
                                            //appmanager.PrintInfoRemote();
                                        int outPutRetry = MAX_NUMBER_OF_RETRIES; 
                                        boolean sucess = false;
                                        while(!sucess && outPutRetry > 0){ //VDN
        	                                try{         	
		                                        if(appmanager.isTaskOutputsRemoteAvailable(t.getTaskId()))
		                                        {
		                                            log.debug("SubmissionManager graph ["+g.getGraphId()+"]  ["+this.getSubmissionManagerId()+"]  found foreign task["+t.getTaskId()+"] FINAL state from another SubmissionManager.");
		                                            //String gfsr = this.getTaskGridFileServiceContactAddressRemote(t.getTaskId());
		                                            String gfsr = this.getMyObjectRemoteContactAddress();
		                                            t.setSubmissionManagerContactAddress(gfsr);
		                                            t.setState(TaskState.getInstance(TaskState.TASK_FOREIGN_FINAL));
													log.debug("Task setting state: " + t.getState().getName());
		                                            sucess = true;
		                                        }
        	                                }catch(java.rmi.ConnectException e){
        	                                	outPutRetry--;
        	                                	System.out.println("Trying isTaskOutputsRemoteAvailable - connect");
        	                                	e.printStackTrace();
        	                                }catch(java.net.SocketException e2) {
        	                                	System.out.println("Trying isTaskOutputsRemoteAvailable - socket");
        	                                	e2.printStackTrace();	                                	
        	                                }
        	                                	
        	                                
            	                                        
                                        }
                                    }
                                }
							    catch (RemoteException e)
								{
                                    AppManUtil.exitApplication("Tolerância a Falhas - ApplicationManager DEAD, Submissonmanager SUICIDE" + e, e);
// 									Debug.debug("Tolerância a Falhas - ApplicationManager DEAD, Submissonmanager SUICIDE" + e);				
// 									e.printStackTrace();
//                                         //System.exit(0);
// 									((Executor) Exehda.getService(Executor.SERVICE_NAME)).exitApplication();
								} 
//							    catch (java.net.SocketException e2) {
//									Debug.debug("INTRODUZIMOS UM ERRO AQUI? :-)");				
// 									e2.printStackTrace();
//								}
                            }
                        }	
                    }
            }

        }

    public void PrintInfoRemote() throws RemoteException
        {
            PrintInfo();
        }
    public void PrintInfo()
        {
            synchronized(graphs)
            {
                log.debug("------------ SubmissionManager ["+getSubmissionManagerId()+"] executing "+graphs.size()+" graphs -------------.");				
                for(int j=0;j< graphs.size(); j++)
                {
                    Graph g = (Graph)graphs.elementAt(j);
                    log.debug("SubmissionManager manage Graph ["+g.getGraphId()+"]: " + g.PrintInfo());				
                }
                log.debug("-------------------------.");
            }
        }

/*
 * retorna true e  a tarefa pertence a este submission manager
 * retorna false se a tarefa pertence a outro submission manager 
 */
    private boolean isTaskFromHere(String taskId)
        {	
            for(int i=0;i<graphs.size();i++)
            {			
                Graph g = (Graph)graphs.elementAt(i);
                if(g.getTask(taskId) != null)
                {
                    return true;
                }
            }	
            return false;
        }

/**
 * @param string
 */
    private void setSubmissionManagerId(String string)
        {
            submissionmanagerId = string;
        }
    public String getSubmissionManagerIdRemote() throws java.rmi.RemoteException
        {
            return getSubmissionManagerId();
        }
    public synchronized String getSubmissionManagerId() //VDN 2005/01/13 sync
        {
    	
    		System.out.println("[VINDN]submissionmanagerId: "+submissionmanagerId );
    	
            return submissionmanagerId;
        }
	public void setMyObjectContactAddress(String contact)
	{
		my_contact_address = contact;
	}
	public void setMyObjectRemoteContactAddress(String contact) throws RemoteException
	{
		setMyObjectContactAddress(contact);
	}
   
	

//	VDN:27/1/2006
	private void setDownloadTimeOfTasksManagers( long downloadTime )
	{
		downloadTimeOfTasksManagers = downloadTime;
	}

//	VDN:27/1/2006
	public long getDownloadTimeOfTasksManagers( ) throws RemoteException
	{
		return downloadTimeOfTasksManagers;	
	}
	
	
/*
 *  Set All TaskManagers Remote Grid Tasks to DIE
 */
    public void setDieRemote() throws RemoteException
        {
	    	System.out.println("[SM] SET ALL SM TO DIE: ");
	    	log.debug("[SM] SET ALL SM TO DIE: ");

    		long plus = 0;
            for(int i=0;i<tasksmanagerList.size();i++)
            {
                TaskManager t = (TaskManager)tasksmanagerList.elementAt(i);
                t.setToDie();                
                log.debug("[SM "+i+"] TIME DOWNLOAD: "+t.getDownloadTimeOfTasks());
                plus += t.getDownloadTimeOfTasks(); //	VDN:27/1/2006
            }	
			  System.out.println("[SM] Download Time: "+plus);
			  log.debug("[SM] Download Time: "+plus);
			  setDownloadTimeOfTasksManagers( plus );//	VDN:27/1/2006
            end = true;
            static_die--;	
            end = true;
//             if(SubmissionManager.static_die <= 0)
//                 ((Executor) Exehda.getService("executor")).exitApplication();
        }

    public void run()
        {
            log.debug("SubmissionManager  ["+this.getSubmissionManagerId()+"] thread run.");

            GridResourceMonitor monitor = null;
            try
            {
                    //monitor = new GridResourceMonitor(this.getSubmissionManagerId());
                    //monitor.startMonitor();
                    //Thread.sleep(10000);
            }
            catch (Exception e)
            {
                log.error("SubmissionManager  ["+this.getSubmissionManagerId()+"] GridResourceMonitor creation FAILED.", e);
                monitor = null;	
            }
	
            float c = 0;
            while(!end)
            {
                if(graphs.size() > 0)
                {
                    end = true;
                    for(int i=0;i<graphs.size();i++)
                    {
						// se o grafo está pronto para ser executado, então execute-o
                        Graph g = (Graph)graphs.elementAt(i);					
                        if(g.getStatePercentCompleted() < 1) // if the gragh is not complete executed
                        {
                            end = false;
                            if( g.getState() == Graph.GRAPH_READY || g.getState() == Graph.GRAPH_EXECUTING)
                            {										
                                // executa o grafo
                            	System.out.println("\t ***[VINDN] executeGraph() GRAPHID: "+g.getGraphId());
                                executeGraph(g.getGraphId());
                            }
                        }
									
                        float pc = g.getStatePercentCompleted(); 
                        if(c < pc)
                        {
                            log.debug("SubmissionManager ["+this.getSubmissionManagerId()+"] executing graph ["+g.getGraphId()+"] complete status: " + g.getStatePercentCompleted());
                            c = pc;
                        }
                    }
                }			
				try
				{
					Thread.sleep(5000);
				} catch (Exception e) {
					log.error(e, e);
				}
            }// fim while
	
            log.info("SubmissionManager  ["+this.getSubmissionManagerId()+"] Stoped!");	
	
            if(monitor != null)
            {
				try
				{
					Thread.sleep(10000);
				} catch (Exception e) {
					log.error(e, e);
				}
                monitor.endMonitor();
                log.debug("SubmissionManager  ["+this.getSubmissionManagerId()+"] GridResourceMonitor History: " + monitor.getHistory());
            }
        }

    public static void
    main( String[] args ) throws Exception
        {
            log.debug("This program is going to test the SubMan class");
        }

    public boolean getIsAliveRemote()
        {
            return true;
        }

}
