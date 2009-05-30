/*
 * Created on 01/06/2004
 *
 */
package appman;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import appman.task.Task;
import appman.util.IOHelper;

/**
 * @author lucasa@gmail.com
 * 
 */
public class GridTask extends GridFileService implements Runnable, GridTaskRemote
{
	private static final Log log = LogFactory.getLog(GridTask.class);
	private static final long serialVersionUID = 94618337786246610L;
	private String command;
	private Task mytask;


	private boolean run = false;
	private boolean die = false;	
	private boolean end = false;
	private boolean sucess = true;
	
	private String errorbuffer = null;

	
	public GridTask(Task task, String cmd, String filepath_seed)
	{	
		super(filepath_seed);
		mytask = task;
		command = cmd;
		
		log.debug("\tGRIDTASK ["+mytask.getTaskId()+"] cmd: "+ cmd);
		
		errorbuffer = "";
	}
	
	public synchronized void setRun(boolean b) 
	{		
		log.debug("GridTask ["+mytask.getTaskId()+"] GOING TO RUN ");
		run = b;
        notifyAll();
	}


	public void setDie()
	{		
		try
		{						
			cleanSandBoxDirectory();
			log.debug("GridTask ["+mytask.getTaskId()+"] RETRY ["+mytask.getRetryTimes()+"] DIED");

            synchronized (this) {
                die = true;
                setEnd(true);
            }
		} catch (Exception e)
		{
			log.error(e, e);
			errorbuffer += e.getMessage();
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
        catch (InterruptedException e) {
        	log.error(e, e);
        }
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
			log.error("[AppMan]\tError in run of GridTask thread, while waiting to run task", e); //VDN 2006/01/13
			errorbuffer += e.getMessage();			
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
				log.debug("GridTask ["+mytask.getTaskId()+"]  RETRY ["+mytask.getRetryTimes()+"] Sucess OK ");
				//mytask.setTaskState(Task.TASK_FINAL);//VDN:25/08/05
			}
			else
			{
				sucess = false;				
				log.debug("GridTask ["+mytask.getTaskId()+"]  RETRY ["+mytask.getRetryTimes()+"] Error number return: " + v);
			}
			
			setEnd(true);
			return;			
		} catch (Exception e)
		{
			log.debug("[AppMan]\tError in run of GridTask thread, while executing task.", e); //VDN 2006/01/13
			errorbuffer += e.getMessage();			
			sucess = false;
			setDie();			
			return;
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
		log.debug("GridTask from Task ["+mytask.getTaskId()+"]  RETRY ["+mytask.getRetryTimes()+"]  cleaning application sandbox directory: " + dir);
		GridFileService.removeDir(dir);		
	}

    private int execute() throws Exception
	{   
        log.debug("GridTask ["+mytask.getTaskId()+"] RUNNING");

        //Comentado VDN
		//String[] cmd = {"/bin/bash", "--login", "-c", "mkdir -p " + dir + " && cd " + dir + " && " + command.substring(command.indexOf('\"')+1,command.lastIndexOf('\"'))/* + " &> /tmp/"+dir+".log"*/};
		//novo comando: agora parser guarda sem aspas
		String[] cmd = { "/bin/bash", "--login", "-c", command };			

        log.debug("GridTask from Task ["+mytask.getTaskId()+"]  RETRY ["+mytask.getRetryTimes()+"]  executing comand line application: " + cmd[3]);

        // diretório de execução / agora é controlado pelo java
        File dir = new File(GridFileService.getTaskSandBoxPath(mytask.getName()));
        dir.mkdirs();

        checkDie();
        Process proc = new ProcessBuilder(cmd).directory(dir).start();
        proc.waitFor();

        IOHelper.readCharInputStream(proc.getInputStream());
        errorbuffer += IOHelper.readCharInputStream(proc.getErrorStream());

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
