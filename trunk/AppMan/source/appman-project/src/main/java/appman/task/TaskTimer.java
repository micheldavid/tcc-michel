package appman.task;

import java.io.Serializable;

import appman.AppManUtil;
import appman.log.Debug;

public class TaskTimer implements Serializable {

	private static final long serialVersionUID = -304386420026374881L;
	private long downloadTimeOfFiles = 0;
	private long timeEnd = 0;
	private long timeStart = 0;
	private String timeSubmited = "";
	private String timeTaskCreated = "";
	private String timeTaskEnd = "";
	private String timeTaskStart = "";
	

	public synchronized long getDownloadTimeOfFiles() {
		return downloadTimeOfFiles;
	}
	
	public long getTimeEnd() {
		return timeEnd;
	}

	public long getTimeStart() {
		return timeStart;
	}

	public String getTimeSubmit() {
		return timeSubmited;
	}

	public String getTimeTaskCreated() { // VDN:26/08
		return timeTaskCreated;
	}

	public String getTimeTaskEnd() {
		return timeTaskEnd;
	}
	
	public String getTimeTaskStart() {
		return timeTaskStart;
	}

	public synchronized void setDownloadTimeOfFiles(long time_download_files) {
		Debug.debug("[TESTE] Download Time: " + time_download_files);
		System.out.println("[TESTE] Download Time: " + time_download_files);
		this.downloadTimeOfFiles = time_download_files;
	}

	public void setTimeEnd(long currentTimeMillis) {
		timeEnd = currentTimeMillis;
		
	}

	public void setTimeStart(long t) {
		timeStart = t;
	}

	public void setTimeSubmited(String time) {
		timeSubmited = time;
	}

	public void setTimeTaskCreate(String time) {
		timeTaskCreated = time;		
	}

	public void setTimeTaskEnd() {
		timeTaskEnd = AppManUtil.getTime();
	}

	public void setTimeTaskStart() {
		timeTaskStart = AppManUtil.getTime();
	}

	public void printTraceInfo(Task task, String file_path) {
		long time = timeEnd - timeStart;
		Debug.debug("ApplicationManager task "+task.taskId+" submit time: " + getTimeSubmit());

		Debug.debugToFile("\n"+task.taskId +
		                  "\t"+task.retryTimes+
		                  "\t"+ (timeTaskCreated) +
		                  "\t"+ (timeSubmited) +
		                  "\t"+ (timeTaskStart)+
		                  "\t"+ (timeTaskEnd) +
		                  "\t"+(float)(time)+
		                  "\t"+(float)(time/1000),
		                  file_path, true);

		Debug.debugToFile(task.submissionManagerContactAddress+"\n",file_path, true);
	}

}
