/**
 * 
 * This TaskManager is a little different from modeled in GRAND.
 * It receives a subgraph from a Submission Manager
 * (There is one Submission Manager + one Task Manager per subgraph)
 * Each Task Manager deals with the execution of one subgraph.
 * 
 */

package appman.task;

import java.rmi.RemoteException;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TaskManager implements TaskManagerRemote, Runnable
{
	private static final Log log = LogFactory.getLog(TaskManager.class);

private String taskmanagerId;
private String description;

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
	taskmanagerId = id;
	taskList = new Vector();
	newtaskList = new Vector();
	
	log.debug("Taskmanager created.");
	
	Thread thread = new Thread(this);
	thread.start();
}

public synchronized int getTaskState(String taskId)
{
  for(int i=0; i<taskList.size(); i++)
  {
  	if(((Task)taskList.elementAt(i)).getTaskId().compareTo(taskId) == 0) // se as chaves são iguais
  	{			
			//Debug.debug("TaskManager returning task state.");
  			return ((Task)taskList.elementAt(i)).getState().getCode();
  	}
  }
  // chave taskId não encontrada
  return -1;
}

public synchronized int getTaskCount(TaskState state)
{
   int c = 0; 
  for(int i=0; i<taskList.size(); i++)
  {
  	if(((Task)taskList.elementAt(i)).getState().equals(state)) // se as chaves são iguais
  	{			
			c++;
  	}
  }
  // chave taskId não encontrada
  return c;
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
	
  log.debug("TaskManager going to DIE!");

  for(int i=0; i<taskList.size(); i++)
  {
  		((Task)taskList.elementAt(i)).setToDie();
  		log.debug("[TM "+i+"] TIME DOWNLOAD: "+((Task)taskList.elementAt(i)).getTimeInfo().getDownloadTimeOfFiles());
  		plus += ((Task)taskList.elementAt(i)).getTimeInfo().getDownloadTimeOfFiles();//VDN:27/1/2006
  }
  downloadTimeOfTasks = plus;//VDN:27/1/2006
  synchronized (waitForDie) {
  die = true;
  waitForDie.notify();
  }
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
}
public synchronized void addTaskToList(Vector t)
{
	newtaskList.addAll(t);
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
	log.debug("TaskManager thread run.");	
	while(!die)
	{
		synchronized(newtaskList)
		{
			if(newtaskList.size() > 0) // verifica se a lista de novas tarefas tem algum elemento
			{
					synchronized(taskList)
					{
						taskList.addAll(newtaskList); // adiciona os elementos da lista de novas tarefas à lista de tarefas
					}
					log.debug("TaskManager add new tasks "+newtaskList.toString()+" to List.");
					newtaskList.removeAllElements(); //remove os elementos da lista de novas, apos inserção destes na lista de tarefas
					log.debug("TaskManager clean newtasks List.");
			}
			synchronized(taskList)
			{
					for(int i=0;i<taskList.size();i++)
					{
						
						/// PKVM - VDN - provavelmente eh aqui que tem que mexer pra limitar o 
						// nro de tarefas em uma cpu.
						// Percorre a lista, ate encontrar uma em execucao, ai para...
						
						Task task = (Task)taskList.elementAt(i);			
						if( task.getState().equals(TaskState.TASK_READY))
						{
							task.setState(TaskState.TASK_EXECUTING);
							log.debug("Task setting state: " + task.getState().toString());
							log.debug("TaskManager executing task ["+task.getTaskId()+"] READY.");
							Thread thread = new Thread(task);
							thread.start();
						}
					}
			}
		}
		
		synchronized (waitForDie) {
		try
		{
			waitForDie.wait(500);
		} catch (Exception e) {
			log.error(e, e);			
		}
		}
		
	}
}

private Object waitForDie = new Object();

}