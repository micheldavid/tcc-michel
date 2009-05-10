package appman.task;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import appman.log.Debug;

public class TaskTimer implements Serializable {

	private static final long serialVersionUID = -304386420026374881L;
	private static final Log log = LogFactory.getLog(TaskTimer.class);
	private long downloadTimeMillisOfFiles = 0;
	private Date timeEnd;
	private Date timeStart;
	private Date timeSubmited;
	private Date timeTaskCreated;
	private Date timeTaskEnd;
	private Date timeTaskStart;

	public synchronized long getDownloadTimeOfFiles() {
		return downloadTimeMillisOfFiles;
	}

	public synchronized void setDownloadTimeOfFiles(long time_download_files) {
		log.debug("[TESTE] Download Time: " + time_download_files);
		this.downloadTimeMillisOfFiles = time_download_files;
	}

	public Date getTimeEnd() {
		return timeEnd;
	}

	public void setTimeEnd(Date timeEnd) {
		this.timeEnd = timeEnd;
	}

	public Date getTimeStart() {
		return timeStart;
	}

	public void setTimeStart(Date timeStart) {
		this.timeStart = timeStart;
	}

	public Date getTimeSubmited() {
		return timeSubmited;
	}

	public void setTimeSubmited(Date timeSubmited) {
		this.timeSubmited = timeSubmited;
	}

	public Date getTimeTaskCreated() {
		return timeTaskCreated;
	}

	public void setTimeTaskCreated(Date timeTaskCreated) {
		this.timeTaskCreated = timeTaskCreated;
	}

	public Date getTimeTaskEnd() {
		return timeTaskEnd;
	}

	public void setTimeTaskEnd(Date timeTaskEnd) {
		this.timeTaskEnd = timeTaskEnd;
	}

	public Date getTimeTaskStart() {
		return timeTaskStart;
	}

	public void setTimeTaskStart(Date timeTaskStart) {
		this.timeTaskStart = timeTaskStart;
	}

	public void printTraceInfo(Task task, String file_path) {
		long totalTime = 0;
		if (timeEnd != null)
			totalTime = timeEnd.getTime() - timeStart.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		log.debug("ApplicationManager task " + task.taskId + " submit time: " + sdf.format(getTimeSubmited()));

		Debug.debugToFile(task.taskId +
		                  "\t"+task.retryTimes+
		                  "\t"+ (timeTaskCreated == null ? "-" : sdf.format(timeTaskCreated)) +
		                  "\t"+ (timeSubmited == null ? "-" : sdf.format(timeSubmited)) +
		                  "\t"+ (timeTaskStart == null ? "-" : sdf.format(timeTaskStart))+
		                  "\t"+ (timeTaskEnd == null ? "-" : sdf.format(timeTaskEnd)) +
		                  "\t"+(totalTime)+
		                  "\t"+(totalTime/1000f),
		                  file_path, true);

		if (task.submissionManagerContactAddress != null) {
			Debug.debugToFile(task.submissionManagerContactAddress,file_path, true);
		}
	}
}
