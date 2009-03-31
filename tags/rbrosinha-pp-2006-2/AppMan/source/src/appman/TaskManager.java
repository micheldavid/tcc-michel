/**
 * 
 * This TaskManager is a little different from modeled in GRAND.
 * It receives a subgraph from a Submission Manager
 * (There is one Submission Manager + one Task Manager per subgraph)
 * Each Task Manager deals with the execution of one subgraph.
 * 
 */

package appman;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.Vector;

import org.isam.exehda.HostId;

public class TaskManager implements TaskManagerRemote, Runnable
{

private String taskmanagerId;
private String description;
private String concreteTaskClassName;

private java.util.Vector taskList;
private java.util.Vector newtaskList;

private boolean die = false;

private long downloadTimeOfTasks = 0;

/**
 * Initialize two empty vectors taskList and newTaskList and
 * starts this object as a thread (see run method)
 * @param id unique identifier 
 * ("taskman"+an integer number related to the order of task manager creation)
 */
public TaskManager(String id)
{
	taskmanagerId = new String(id);
	taskList = new Vector();
	newtaskList = new Vector();
	concreteTaskClassName = loadConcreteTaskClassName();
	Debug.debug("Taskmanager created.");
	
	Thread thread = new Thread(this);
	thread.start();
}

public synchronized int getTaskState(String taskId)
{
  for(int i=0; i<taskList.size(); i++)
  {
  	if(((Task)taskList.elementAt(i)).getTaskId().compareTo(taskId) == 0) // se as chaves s�o iguais
  	{			
			//Debug.debug("TaskManager returning task state.");
  			return ((Task)taskList.elementAt(i)).getTaskState();
  	}
  }
  // chave taskId n�o encontrada
  return -1;
}

public synchronized int getTaskCount(int state)
{
   int c = 0; 
  for(int i=0; i<taskList.size(); i++)
  {
  	if(((Task)taskList.elementAt(i)).getTaskState() == state) // se as chaves s�o iguais
  	{			
			c++;
  	}
  }
  // chave taskId n�o encontrada
  return c;
}

//VDN:27/1/2006
public long calculateDownloadTimeNow() throws RemoteException
{
	  long plus = 0;
	  
	  for(int i=0; i<taskList.size(); i++)
	  {
	  		plus += ((Task)taskList.elementAt(i)).getDownloadTimeOfFiles();
	  }
	
	  downloadTimeOfTasks = plus;
	  
	  return plus;
}

//VDN:27/1/2006
private void setDownloadTimeOfTasks( long downloadTime )
{
	downloadTimeOfTasks = downloadTime;
}

//VDN:27/1/2006
public long getDownloadTimeOfTasks( ) throws RemoteException
{
	return downloadTimeOfTasks;	
}


public void setToDie()
{
  long plus = 0;	
	
  Debug.debug("TaskManager going to DIE!", true);
//	System.out.println("[TM] SET ALL TM TO DIE: ");
//	Debug.debug("[TM] SET ALL TM TO DIE: ", true);

  for(int i=0; i<taskList.size(); i++)
  {
  		((Task)taskList.elementAt(i)).setToDie();
  		Debug.debug("[TM "+i+"] TIME DOWNLOAD: "+((Task)taskList.elementAt(i)).getDownloadTimeOfFiles(), true);
  		plus += ((Task)taskList.elementAt(i)).getDownloadTimeOfFiles();//VDN:27/1/2006
  }
  //System.out.println("[TM] Download Time: "+plus);
  //Debug.debug("[TM] Download Time: "+plus, true);
  setDownloadTimeOfTasks(plus);//VDN:27/1/2006
  die = true;
}


/**
 * @param task vector of tasks ready to be executed 
 */
public void addTaskToListRemote(Task task) throws RemoteException
{
	addTaskToList(task);
}
public void addTaskToListRemote(Vector task) throws RemoteException
{
	addTaskToList(task);
}
public synchronized void addTaskToList(Task task)
{	
	newtaskList.addElement(task);	
	Task t = (Task)newtaskList.elementAt(0);
	//Debug.debug("Add task to List: "+ t.getName());
}
public synchronized void addTaskToList(Vector t)
{
	//Debug.debug("---------"+t.elementAt(0));
	newtaskList.addAll(t);
	//Debug.debug("---------"+newtaskList.elementAt(0));
	//Debug.debug("Add task Vector to List");
}
private boolean downloadInputTaskFiles(String taskId)
{
	return true;
}
public String pathToTaskOutputFile(String taskId, String datafileId)
{
	return "";
}
/**
 * @return
 */
public String getDescription() {
	return description;
}

/**
 * @param string
 */
public void setDescription(String string) {
	description = string;
}

public void run()
{	
	Debug.debug("TaskManager thread run.");	
	while(!die)
	{
		synchronized(newtaskList)
		{
			if(newtaskList.size() > 0) // verifica se a lista de novas tarefas tem algum elemento
			{
					synchronized(taskList)
					{
						taskList.addAll(newtaskList); // adiciona os elementos da lista de novas tarefas � lista de tarefas
					}
					Debug.debug("TaskManager add new tasks "+newtaskList.toString()+" to List.", true);
					newtaskList.removeAllElements(); //remove os elementos da lista de novas, apos inser��o destes na lista de tarefas
					Debug.debug("TaskManager clean newtasks List.");
			}
			synchronized(taskList)
			{
					for(int i=0;i<taskList.size();i++)
					{
						
						/// PKVM - VDN - provavelmente eh aqui que tem que mexer pra limitar o 
						// nro de tarefas em uma cpu.
						// Percorre a lista, ate encontrar uma em execucao, ai para...
						
						Task task = (Task)taskList.elementAt(i);			
						if( task.getTaskState() == Task.TASK_READY )
						{	
							task.setConcreteTaskClassName(concreteTaskClassName);
							task.setTaskState(Task.TASK_EXECUTING);					
							Debug.debug("TaskManager executing task ["+task.getTaskId()+"] READY.", true);							
							Thread thread = new Thread(task);
							thread.start();
						}
					}
			}
		}
		
		try
		{		
			Thread.sleep(500);
		} catch (Exception e) {
			Debug.debug(e);			
		}
		
	}// fim while		
}


private String loadConcreteTaskClassName() {
	String s = "GridTask"; // default
	
	try {
		Properties props = new Properties();
		props.load(this.getClass().getClassLoader().getResourceAsStream("gridnodes.properties"));
		s = props.getProperty("grid.targetHosts.submissionmanagers.hosts["+HostId.getLocalHost().toExternalForm().substring(7)+"].concreteTaskClassName" );
	} catch (IOException e) {
		Debug.debug("Unable to load concrete task class name from gridnodes.properties.");
		Debug.debug(e);
	}
	
	return s;
}
}