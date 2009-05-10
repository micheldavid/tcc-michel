package appman.task;

import java.io.Serializable;
import java.util.Date;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import appman.AppManUtil;
import appman.GridFileServiceRemote;

/**
 * Task.java - Copyright lucasa
 * 
 * This file was generated on Ter Mai 4 2004 at 15:32:53
 * 
 * This is an abstract class that must have the method execute implemented. It
 * implements the user task/job. Task is started as a thread by TaskManager (run
 * method invocates execute)
 */
public abstract class Task implements Runnable, Serializable {

	private static final long serialVersionUID = -5101143107805590612L;
	private static final Log log = LogFactory.getLog(Task.class);

	public static final int MAX_RETRY_TIMES = 5;

	String taskId;

	private String description;

	private String name;

	private int type;

	private String commandLine;

	private TaskState state;

	protected TaskTimer timeInfo;

	private TaskFiles files;

	private String submissionManagerId;

	transient private GridFileServiceRemote remoteFileService = null;

	String submissionManagerContactAddress = null;

	int retryTimes = 0; // retry scheduling times

	public Task(String subid, String id, String desc, String strname, String cmd) {
		initialize(subid, id, desc, strname, null, null, cmd);

	}

	public Task(String subid, String id, String desc, String strname,
			Vector input, Vector output, String cmd) {
		initialize(subid, id, desc, strname, input, output, cmd);
	}

	private void analyzeFaultTolerance(Exception e) {
		// Tolerancia a Falhas - Task
		log.warn("Tolerancia a Falhas na execução de tarefa [" + taskId + "]", e);
		log.warn("Task [" + taskId + "]   Error! Trying to put the task as dependent state again RETRY ["
			+ retryTimes + "] TIMES", e);
		retryTimes++;
		state = TaskState.getInstance(TaskState.TASK_DEPENDENT);
		log.debug("Task setting state: " + state.getName());
		this.remoteFileService = null;
		if (retryTimes > Task.MAX_RETRY_TIMES) {
			AppManUtil.exitApplication("Fatal Error: Task [" + taskId
					+ "] Max " + retryTimes + " Retry Times", e);
		}
	}

	abstract public void execute() throws Exception;

	public synchronized String getCommandLine() {
		return commandLine;
	}

	public String getDescription() {
		return description;
	}

	public String getSubmissionManagerId() {
		return submissionManagerId;
	}

	public synchronized String getSubmissionManagerContactAddress() {
		return submissionManagerContactAddress;
	}

	public String getName() {
		return name;
	}

	public synchronized GridFileServiceRemote getRemoteGridTaskFileService() {
		return remoteFileService;
	}

	public synchronized int getRetryTimes() {
		return retryTimes;
	}

	public TaskState getState() {
		return state;
	}

	public String getTaskId() {
		return taskId;
	}

	public synchronized int getTaskType() {
		return type;
	}

	public TaskTimer getTimeInfo() {
		return timeInfo;
	}

	private void initialize(String subid, String id, String desc,
			String strname, Vector input, Vector output, String cmd) {
		taskId = id;
		description = desc;
		name = strname;

		timeInfo = new TaskTimer();		
		submissionManagerId = subid;
		commandLine = cmd;

		files = new TaskFiles(input, output);
		state = TaskState.getInstance(TaskState.TASK_DEPENDENT);
		
		log.debug("Task setting state: " + state.getName());
		type = TaskType.TASK_TYPE_INTERMEDIATE;
		timeInfo.setTimeTaskCreated(new Date());// VDN:26/08

		log.debug("Task [" + taskId + "] created.");
	}

	public void run() {
		state = TaskState.getInstance(TaskState.TASK_EXECUTING);
		log.debug("Task setting state: " + state.getName());
		timeInfo.setTimeSubmited(new Date());

		log.debug("Task [" + taskId + "] retry[" + retryTimes + "] executing");
		try {
			execute();
			timeInfo.setTimeEnd(new Date());
			timeInfo.setTimeTaskEnd(new Date());
		} catch (Exception e) {
			analyzeFaultTolerance(e);
		}

		files.updateOutputFilesState();

		log.debug("Task FINAL retry[" + retryTimes + "]: " + taskId);
		state = TaskState.getInstance(TaskState.TASK_FINAL);
		log.debug("Task setting state: " + state.getName());
	}

	public synchronized void setCommandLine(String string) {
		commandLine = string;
	}


	public synchronized void setSubmissionManagerContactAddress(
			String contact_adress) {
		submissionManagerContactAddress = contact_adress;
	}

	public void setName(String str) {
		name = str;
	}

	public synchronized void setRemoteGridTaskFileService(
			GridFileServiceRemote gfs) {
		remoteFileService = gfs;
	}

	public void setState(TaskState state) {
		this.state = state;
	}



	public synchronized void setSubmissionManagerId(String subid) {
		submissionManagerId = subid;
	}

	public synchronized void setTaskType(int t) {
		type = t;
	}


	abstract public void setToDie();

	@Override
	public String toString() {
		return name;
	}

	public TaskFiles getFiles() {
		return files;
	}

}
