/*
 * Created on 01/06/2004
 *
 */
package appman;

import java.io.InputStream;

import appman.log.Debug;
import appman.task.Task;

/**
 * @author lucasa@gmail.com
 * 
 */
public class GridTask extends GridFileService implements Runnable, GridTaskRemote
{
	private static final long serialVersionUID = 94618337786246610L;
	private String command;
	private Task mytask;


	private boolean run = false;
	private boolean die = false;	
	private boolean end = false;
	private boolean sucess = true;
	
	private StringBuffer errorbuffer = null;

	
	public GridTask(Task task, String cmd, String filepath_seed)
	{	
		super(filepath_seed);
		mytask = task;
		command = cmd;
		
		Debug.debug("\tGRIDTASK ["+mytask.getTaskId()+"] cmd: "+ cmd, true);
		
		errorbuffer = new StringBuffer();
	}
	
	public synchronized void setRun(boolean b) 
	{		
		Debug.debug("GridTask ["+mytask.getTaskId()+"] GOING TO RUN ", true);
		run = b;
        notifyAll();
	}


	public void setDie()
	{		
		try
		{						
			cleanSandBoxDirectory();
			Debug.debug("GridTask ["+mytask.getTaskId()+"] RETRY ["+mytask.getRetryTimes()+"] DIED", true);

            synchronized (this) {
                die = true;
                setEnd(true);
            }
		} catch (Exception e)
		{
			e.printStackTrace();
			errorbuffer.append(e.getMessage());
		}		
	}
    
        /**
         * returns the status of the execution, if is running(false) or if is finished(true).
         *
         * <p>Will block up to <code>timeoutSeconds</code> seconds waiting for task completion.
         *
         * @param timeoutSeconds an <code>int</code> value
         * @return a <code>boolean</code> value
         */
    public synchronized boolean getEnd(int timeoutSeconds)
    {
        try {
            if ( !end ) wait(timeoutSeconds*1000);
        }
        catch (InterruptedException ie) { /* empty */}
        return end;
    }

    
        /**
         * returns the state of the job, if is ok or error
         * @return a <code>boolean</code> value
         */
    public synchronized boolean getSuccess()
	{
		return sucess;
	}
	
	public synchronized  String getErrorMessage()
	{
		return errorbuffer.toString();
	}
	

    
	public void run()
	{
		try
		{			
			// enquanto n?o for o momento de executar espera
            synchronized (this) {
                while (!run) wait();
            }			
		} catch (Exception e)
		{
			Debug.debug("[AppMan]\tError in run of GridTask thread, while waiting to run task"); //VDN 2006/01/13
			System.out.println("[AppMan]\tError in run of GridTask thread, while waiting to run task"); //VDN 2006/01/13
			e.printStackTrace();
			errorbuffer.append(e.getMessage());			
			sucess = false;
			setDie();			
			return;
		}

		try {
			//VDN:25/08/05
			//mytask.setTaskState(Task.TASK_EXECUTING);
			// execute o comando
			
			int v = execute();
			if( v  == 0 )
			{
				sucess = true;
				Debug.debug("GridTask ["+mytask.getTaskId()+"]  RETRY ["+mytask.getRetryTimes()+"] Sucess OK ", true);
				//mytask.setTaskState(Task.TASK_FINAL);//VDN:25/08/05
			}
			else
			{
				sucess = false;				
				Debug.debug("GridTask ["+mytask.getTaskId()+"]  RETRY ["+mytask.getRetryTimes()+"] Error number return: " + v, true);
			}
			
			setEnd(true);
			return;			
		} catch (Exception e)
		{
			Debug.debug("[AppMan]\tError in run of GridTask thread, while executing task."); //VDN 2006/01/13
			e.printStackTrace();
			errorbuffer.append(e.getMessage());			
			sucess = false;
			setDie();			
			return;
		}
	}
	
	@Override
	public void finalize()
	{
		try
		{
			Debug.debug("GridTask ["+mytask.getTaskId()+"]  RETRY ["+mytask.getRetryTimes()+"]  - Objeto sendo recolhido pelo garbage collection", true);
			cleanSandBoxDirectory();
		}catch (Exception e)
		{
			e.printStackTrace();			
		}
	}


        //////////////////////////////////////////////////////////////
        // PRIVATE METHODS
        //////////////////////////////////////////////////////////////

        /**
         * limpa a sujeira ;-)
         *
         * @exception Exception if an error occurs
         */
    private void cleanSandBoxDirectory()throws Exception 
	{
		String dir = GridFileService.getTaskSandBoxPath(mytask.getName());
		Debug.debug("GridTask from Task ["+mytask.getTaskId()+"]  RETRY ["+mytask.getRetryTimes()+"]  cleaning application sandbox directory: " + dir, true);
		GridFileService.removeDir(dir);		
	}

    private int execute() throws Exception
	{   
        Debug.debug("GridTask ["+mytask.getTaskId()+"] RUNNING", true);

        checkDie();
        
        String dir = GridFileService.getTaskSandBoxPath(mytask.getName());
			//Comentado VDN
			//String[] cmd = {"/bin/bash", "--login", "-c", "mkdir -p " + dir + " && cd " + dir + " && " + command.substring(command.indexOf('\"')+1,command.lastIndexOf('\"'))/* + " &> /tmp/"+dir+".log"*/};
			//novo comando: agora parser guarda sem aspas
        String[] cmd = {"/bin/bash",
                        "--login",
                        "-c",
                        "mkdir -p " + dir + " && cd " + dir + " && " + command};			

			//String[] cmd = {"/bin/bash", "-i", "-c", "export > log; read"};


        Debug.debug("GridTask from Task ["+mytask.getTaskId()+"]  RETRY ["+mytask.getRetryTimes()+"]  executing comand line application: " + cmd[3], true);

        checkDie();
			
        Process proc = Runtime.getRuntime().exec(cmd);
        StringBuffer inBuffer = new StringBuffer();
        InputStream inStream = proc.getInputStream();
        new InputStreamHandler( inBuffer, inStream );

        errorbuffer = new StringBuffer();
        InputStream errStream = proc.getErrorStream();
        new InputStreamHandler( errorbuffer , errStream );
			
        checkDie();
        
        proc.waitFor();
			// loop until the proc finish or DIE signal
			/*
              boolean end = false;			
              while(end == false)
              {
              if(die == true) throw new Exception("GridTask ["+mytask.getTaskId()+"] is time to DIE");
              try
              {				
              proc.exitValue();
              Thread.sleep(500);
              end = true;
              }catch (IllegalThreadStateException e)
              {
              end = false;
              }
              }			
			*/
        return proc.exitValue();
	}

    private final synchronized void setEnd(boolean isEnd)
    {
        this.end = isEnd;
        notifyAll();
    }

    private final synchronized void checkDie() throws Exception
    {
        if (die) throw new Exception("GridTask going to DIE");
    }
}
