/*
 * Created on 15/12/2006
 */
package appman;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobInfo;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

import appman.rmswrapper.pbs.drmaa.JobTemplateImpl;

/**
 * @author rbrosinha@inf.ufrgs.br
 */
public class GridTaskDrmaa extends GridFileService implements Runnable, GridTaskRemote {

	private static final long serialVersionUID = -5834167770555224385L;

	private String command;

	private Task mytask;

	private boolean run = false;

	// private boolean die = false;

	private boolean end = false;

	private boolean sucess = true;

	private StringBuffer errorbuffer = null;

	public GridTaskDrmaa(Task task, String cmd, String filepath_seed) {
		super(filepath_seed);
		mytask = task;
		command = cmd;
		Debug.debug("GridTaskDrmaa [" + mytask.getTaskId() + "] cmd: " + cmd, true);
		errorbuffer = new StringBuffer("");
	}

	public synchronized void setRun(boolean b) {
		Debug.debug("GridTaskDrmaa [" + mytask.getTaskId() + "] GOING TO RUN ", true);
		run = b;
		notifyAll();
	}

	public void setDie() {
		try {
			cleanSandBoxDirectory();
			Debug.debug("GridTaskDrmaa [" + mytask.getTaskId() + "] RETRY [" + mytask.getRetryTimes() + "] DIED", true);
			synchronized (this) {
				// die = true;
				setEnd(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
			errorbuffer.append(e.getMessage());
		}
	}

	/**
	 * returns the status of the execution, if is running(false) or if is
	 * finished(true).
	 * 
	 * <p>
	 * Will block up to <code>timeoutSeconds</code> seconds waiting for task
	 * completion.
	 * 
	 * @param timeoutSeconds
	 *          an <code>int</code> value
	 * @return a <code>boolean</code> value
	 */
	public synchronized boolean getEnd(int timeoutSeconds) {
		try {
			if (!end)
				wait(timeoutSeconds * 1000);
		} catch (InterruptedException ie) { /* empty */
		}
		return end;
	}

	/**
	 * returns the state of the job, if is ok or error
	 * 
	 * @return a <code>boolean</code> value
	 */
	public synchronized boolean getSuccess() {
		return sucess;
	}

	public synchronized String getErrorMessage() {
		return errorbuffer.toString();
	}

	public void run() {
		try {
			synchronized (this) {
				while (!run)
					wait();
			}
		} catch (Exception e) {
			e.printStackTrace();
			errorbuffer.append(e.getMessage());
			sucess = false;
			setDie();
			return;
		}

		try {
			int v = execute();
			if (v == 0) {
				sucess = true;
				Debug.debug("GridTaskDrmaa [" + mytask.getTaskId() + "]  RETRY [" + mytask.getRetryTimes() + "] Sucess OK ", true);
			} else {
				sucess = false;
				Debug.debug("GridTaskDrmaa [" + mytask.getTaskId() + "]  RETRY [" + mytask.getRetryTimes() + "] Error number return: " + v, true);
			}
			setEnd(true);
			return;
		} catch (Exception e) {
			Debug.debug("[AppMan]\tError in run of GridTask thread, while executing task."); // VDN
			// 2006/01/13
			e.printStackTrace();
			errorbuffer.append(e.getMessage());
			sucess = false;
			setDie();
			return;
		}
	}

	public void finalize() {
		try {
			Debug.debug("GridTaskDrmaa [" + mytask.getTaskId() + "]  RETRY [" + mytask.getRetryTimes() + "]  - Objeto sendo recolhido pelo garbage collection", true);
			cleanSandBoxDirectory();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void cleanSandBoxDirectory() throws Exception {
		String dir = GridFileService.getTaskSandBoxPath(mytask.getName());
		Debug.debug("GridTaskDrmaa [" + mytask.getTaskId() + "]  RETRY [" + mytask.getRetryTimes() + "]  cleaning application sandbox directory: " + dir, true);
		GridFileService.removeDir(dir);
	}

	private int execute() throws Exception {
		int jobExitStatus = -1;

		// sets the provider for SessionFactory
		// TODO: make it externally configurable
		System.setProperty("org.ggf.drmaa.SessionFactory", "appman.rmswrapper.pbs.drmaa.SessionFactoryImpl");
		// to solve an issue with SessionFactory not finding provider class
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
		
		// get a session to call a rms
		SessionFactory factory = SessionFactory.getFactory();
		Session session = factory.getSession();

		// initialize the session
		session.init(null);

		// create a job template
		JobTemplate jobTemplate = session.createJobTemplate();

		// copy all attributes from this class to the newly created job template
		copyAttributesTo(jobTemplate);

		// submit the job
		String jobId = session.runJob(jobTemplate);

		// delete job template to free resource
		session.deleteJobTemplate(jobTemplate);

		// wait for job completion
		JobInfo jobInfo = session.wait(jobId, Session.TIMEOUT_WAIT_FOREVER);

		// get job exit status
		jobExitStatus = jobInfo.getExitStatus();

		// exit the session
		session.exit();

		return jobExitStatus;
	}

	private final synchronized void setEnd(boolean isEnd) {
		this.end = isEnd;
		notifyAll();
	}

	// private final synchronized void checkDie() throws Exception {
	// if (die)
	// throw new Exception("GridTaskDrmaa going to DIE");
	// }

	private String taskIdPerPBS;

	public void setTaskIdPerPBS(String taskIdPerPBS) {
		this.taskIdPerPBS = taskIdPerPBS;
	}

	public String getTaskDir() {
		return GridFileService.getTaskSandBoxPath(mytask.getName());
	}

	public String getTaskCommand() {
		return command;
	}

	public String getTaskId() {
		return mytask.getTaskId();
	}

	public String getTaskName() {
		return mytask.getName();
	}

	public String getTaskIdPerPBS() {
		return taskIdPerPBS;
	}

	private void copyAttributesTo(JobTemplate jt) throws DrmaaException {
		jt.setWorkingDirectory(GridFileService.getTaskSandBoxPath(mytask.getName()));
		jt.setRemoteCommand(command);
		if (jt instanceof JobTemplateImpl) {
			((JobTemplateImpl)jt).setAppManJobName(this.mytask.getName());
		}
		
	}
}
