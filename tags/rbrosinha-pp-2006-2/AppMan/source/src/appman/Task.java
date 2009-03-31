package appman;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Task.java - Copyright lucasa
 * 
 * This file was generated on Ter Mai 4 2004 at 15:32:53
 * 
 * This is an abstract class that must have the method execute implemented. It
 * implements the user task/job. Task is started as a thread by TaskManager (run
 * method invocates execute)
 */
public abstract class Task /* extends GridFileService */implements Runnable,
		Serializable {
	/* {src_lang=Java} */

	private String taskId;

	private String description;

	private String name;

	private int type;

	public static final int TASK_TYPE_ROOT = 0;
	public static final int TASK_TYPE_FINAL = 1;
	public static final int TASK_TYPE_INTERMEDIATE = 2;

	private String command_line;

	private int state;

	public static final int TASK_DONT_KNOW = -1;
	public static final int TASK_DEPENDENT = 0;
	public static final int TASK_READY = 1;
	public static final int TASK_EXECUTING = 2;
	public static final int TASK_FINAL = 3;
	public static final int TASK_FOREIGN = 4;
	public static final int TASK_FOREIGN_FINAL = 5;

	private java.util.Vector inputFiles;

	private java.util.Vector outputFiles;

	private String submanId; // id do SubmissionManager responsavel por executar
							 // esta tarefa

	transient private GridFileServiceRemote remote_file_service = null;

	private String my_submission_manager_contact_address = null;

	private int retrytimes = 0; // retry scheduling times

	private String time_task_created = "";
	private String time_submited = "";
	private String time_task_start = "";
	private String time_task_end = "";
	private long time_start = 0;
	private long time_end = 0;
	private long downloadTimeFiles = 0;
	
	protected String concreteTaskClassName = "GridTask";

	public static final int MAX_RETRY_TIMES = 5;
	

	public Task(String subid, String id, String desc, String strname,
			Vector input, Vector output, String cmd) {
		//super(strname); // a seed para o servi�o GridFileService � o nome da
		// tarefa

		setTaskId(id);
		setDescription(desc);
		setName(strname);
		submanId = new String(subid);

		command_line = new String(cmd);

		inputFiles = new Vector(input);
		outputFiles = new Vector(output);

        setTaskState(Task.TASK_DEPENDENT);
		type = Task.TASK_TYPE_INTERMEDIATE;
		time_task_created = getTime();//VDN:26/08

		Debug.debug("Task [" + getTaskId() + "] created.", true);
	}

	public Task(String subid, String id, String desc, String strname, String cmd) {
		//super(strname);

		setTaskId(id);
		setDescription(desc);
		setName(strname);
		submanId = new String(subid);

		command_line = new String(cmd);

		inputFiles = new Vector();
		outputFiles = new Vector();

        setTaskState(Task.TASK_DEPENDENT);
		type = Task.TASK_TYPE_INTERMEDIATE;

		time_task_created = getTime();//VDN:26/08

	}

	private String getTime() { //VDN
		Calendar cal = new GregorianCalendar();
		String date = new String();
		date = cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.DAY_OF_MONTH)
				+ "/" + cal.get(Calendar.YEAR);

		String time = new String();
		time = cal.get(Calendar.HOUR) + ":" + cal.get(Calendar.MINUTE) + ":"
				+ cal.get(Calendar.SECOND);

		return date + " " + time;
	}

	public String getTimeTaskCreated() { //VDN:26/08
		return time_task_created;
	}

	public long getTimeStart() {
		return time_start;
	}

	public long getTimeEnd() {
		return time_end;
	}

	public String getTimeSubmit() {
		return time_submited;
	}

	public void setTimeStart(long t) {
		time_start = t;
	}

	public void setTimeTaskStart() {

		time_task_start = getTime();
	}

	public String getTimeTaskStart() {
		return time_task_start;
	}

	public String getTimeTaskEnd() {
		return time_task_end;
	}

	/**
	 * Get the value of name
	 * 
	 * @return the value of name
	 *  
	 */

	public String getName() {
		/*
		 * {return=the value of name }
		 */

		/* {return=the value of name} */
		return name;
	}

	/**
	 * Set the value of name
	 * 
	 *  
	 */

	private void setName(String str) {
		name = str;
	}

	/**
	 * Get the value of taskId
	 * 
	 * @return the value of taskId
	 * 
	 * 
	 * 
	 * 
	 *  
	 */

	public String getTaskId() {
		/*
		 * {return=the value of taskId }
		 */

		/* {return=the value of taskId} */
		return taskId;
	}

	public String getMySubManagerId() {
		return submanId;
	}

	/**
	 * Set the value of taskId
	 * 
	 *  
	 */

	private void setTaskId(String id) {
		taskId = id;
	}

	abstract public void setToDie();

	/**
	 * Get the value of description
	 * 
	 * @return the value of description
	 * 
	 * 
	 * 
	 * 
	 *  
	 */

	public String getDescription() {
		/*
		 * {return=the value of description }
		 */

		/* {return=the value of description} */
		return description;
	}

	/**
	 * Set the value of description
	 * 
	 *  
	 */

	private void setDescription(String desc) {
		description = desc;
	}

	public synchronized void setTaskSubmissionManagerId(String subid) {
		submanId = new String(subid);
	}

	abstract public void execute() throws Exception;

	public String toString() {
		return name;
	}

    public synchronized void setTaskState(int s) {
        state = s;
		Debug.debug("Task setting state: " + getTaskStateString());
	}
    
	public synchronized int getTaskState() {
		return state;
	}

	public String getTaskStateString() {
		String s = new String();

        int st = getTaskState();

        switch (st) {
            case TASK_DEPENDENT    : return "TASK_DEPENDENT";
            case TASK_EXECUTING    : return "TASK_EXECUTING";
            case TASK_FINAL        : return "TASK_FINAL";
            case TASK_READY        : return "TASK_READY";
            case TASK_FOREIGN      : return "TASK_FOREIGN";
            case TASK_FOREIGN_FINAL: return "TASK_FOREIGN_FINAL";
            case TASK_DONT_KNOW:
            default                : return "TASK_DONT_KNOW";
        }
	}



	public synchronized void setTaskType(int t) {
		//Debug.debug("Task setting type: " + s);
		type = t;
	}

	public synchronized int getTaskType() {
		//Debug.debug("Task setting type: " + s);
		return type;
	}

	public void run() {
		setTaskState(TASK_EXECUTING);
		time_submited = getTime();

		Debug.debug("Task [" + this.getTaskId() + "] retry[" + retrytimes
				+ "] executing", true);
		try {
			// DUVIDA: aqui neste execute ele instancia uma tarefa remotamente
			// usando
			// a politica do chooseCreationHost da classe GridSchedule? Isto eh,
			// eh uma
			// escolha totalmente aleatoria sobre todo o conj. de maq. da rede
			// local?
			execute();
			time_end = System.currentTimeMillis();
			time_task_end = getTime();
		} catch (Exception e) {
			//			Tolerancia a Falhas - Task
			Debug.debug("Tolerancia a Falhas na execu��o de tarefa ["
					+ this.getTaskId() + "]" + e, true);
			Debug
					.debug(
							"Task ["
									+ this.getTaskId()
									+ "]   Error! Trying to put the task as dependent state again RETRY ["
									+ retrytimes + "] TIMES", true);
			e.printStackTrace();
			retrytimes++;
			setTaskState(TASK_DEPENDENT);
			this.remote_file_service = null;
			if (retrytimes > Task.MAX_RETRY_TIMES) {
				AppManUtil.exitApplication("Fatal Error: Task ["
						+ this.getTaskId() + "] Max " + retrytimes
						+ " Retry Times", e);
			}
			return;
		}

		// atualiza o status dos arquivos de sa�da
		for (int i = 0; i < outputFiles.size(); i++) {
			Debug.debug("Task FINAL retry[" + retrytimes
					+ "] set output file exists: "
					+ ((DataFile) outputFiles.elementAt(i)).getName(), true);
			((DataFile) outputFiles.elementAt(i)).setDataFileExist(true);
		}

		Debug.debug(
				"Task FINAL retry[" + retrytimes + "]: " + this.getTaskId(),
				true);
		setTaskState(TASK_FINAL);
	}

	public synchronized void setAllOutputFileAsNotExist() {
		//	atualiza o status dos arquivos de sa�da
		for (int i = 0; i < outputFiles.size(); i++) {
			((DataFile) outputFiles.elementAt(i)).setDataFileExist(false);
		}
	}

	public synchronized void addDataFileToInputList(DataFile data) {
		inputFiles.addElement(data);
	}

	public synchronized void addDataFileToOutputList(DataFile data) {
		outputFiles.addElement(data);
	}

	public synchronized void addDataFileToInputList(Vector data) {
		inputFiles.addAll(data);
	}

	public synchronized void addDataFileToOutputList(Vector data) {
		outputFiles.addAll(data);
	}

	/**
	 * @return DataFile
	 */
	public synchronized DataFile[] getInputFiles() {
		return (DataFile[]) inputFiles.toArray(new DataFile[0]);
	}

	/**
	 * @return
	 */
	public synchronized DataFile[] getOutputFiles() {
		return (DataFile[]) outputFiles.toArray(new DataFile[0]);
	}

	/**
	 * @return
	 */
	public synchronized String getCommand_Line() {
		return command_line;
	}

	/**
	 * @param string
	 */
	public synchronized void setCommand_Line(String string) {
		command_line = string;
	}

	/**
	 * @return GridFileServiceRemote
	 */
	public synchronized GridFileServiceRemote getRemoteGridTaskFileService() {
		return remote_file_service;
	}

	public synchronized String getMySubmissionManagerRemoteContactAddress() {
		return my_submission_manager_contact_address;
	}

	public synchronized void setRemoteGridTaskFileService(GridFileServiceRemote gfs) {
		remote_file_service = gfs;
	}

	/**
	 * @param remote
	 */
	public synchronized void setMySubmissionManagerRemoteContactAddress(String contact_adress) {
		//remote_file_service =
		// (GridFileServiceRemote)GeneralObjectActivator.getRemoteObjectReference(contact_adress,
		// GridFileServiceRemote.class);
		my_submission_manager_contact_address = contact_adress;
	}

	public synchronized int getRetryTimes() {
		return retrytimes;
	}


	/**
	 * @return Returns the time_download_files.
	 */
	public synchronized long getDownloadTimeOfFiles() {
		return downloadTimeFiles;
	}
	/**
	 * @param time_download_files The time_download_files to set.
	 */
	public synchronized void setDownloadTimeOfFiles(long time_download_files) {
		Debug.log(this+"\t[TESTE] Download Time: "+time_download_files);
		this.downloadTimeFiles = time_download_files;
	}
	

	public String getConcreteTaskClassName() {
		return concreteTaskClassName;
	}

	public void setConcreteTaskClassName(String concreteTaskClassName) {
		this.concreteTaskClassName = concreteTaskClassName;
	}

}
