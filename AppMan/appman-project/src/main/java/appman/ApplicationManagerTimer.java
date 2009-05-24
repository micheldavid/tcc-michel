package appman;

import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ApplicationManagerTimer {

	private static final Log log = LogFactory.getLog(ApplicationManagerTimer.class);
	long downloadTimeOfSM = 0;
	long time_begin = 0;
	long time_execution = 0;
	long time_schedule_begin = 0;
	long time_schedule_end = 0;
	long time_schedule_total = 0;

	void setDownloadTimeOfSM( long downloadTime )
	{
		this.downloadTimeOfSM = downloadTime;
	}

	void setTimeBegin(long time_begin) {
		this.time_begin = time_begin;
	}

	public long getDownloadTimeOfSM( ) throws RemoteException
	{
		return downloadTimeOfSM;	
	}

	long getTimeBegin() {
		return time_begin;
	}

	long getTimeExecution() {
		return time_execution;
	}

	void setTimeExecution(long time_execution) {
		this.time_execution = time_execution;
	}

	void setTimeScheduleBegin(long time_schedule_begin) {
		this.time_schedule_begin = time_schedule_begin;
	}

	long getTimeScheduleBegin() {
		return this.time_schedule_begin;
	}

	void setTimeScheduleEnd(long time_schedule_end) {
		this.time_schedule_end = time_schedule_end;
	}

	long getTimeScheduleEnd() {
		return this.time_schedule_end;
	}

	void setTimeScheduleTotal(long time_schedule_total) {
		this.time_schedule_total = time_schedule_total;
	}

	long getTimeScheduleTotal() {
		return this.time_schedule_total;
	}

	void printFinishTimeInfo() {
		try{
			    FileWriter parserOut = new FileWriter("parseOut.txt",true);
				parserOut.write(((float) getTimeScheduleTotal()) + "\t");
				parserOut.write(((long) getDownloadTimeOfSM() / 1000) + "\t");
				parserOut.write(((float) getTimeExecution() / 1000) + "\t");
				parserOut.write((float) Runtime.getRuntime().totalMemory()+ "\t");
				parserOut.write("DONE\n");
				parserOut.close();
		     
		}catch( IOException e){
			log.error("[APPMAN-ApplicationManager.java]: "+e, e);
		}
	}	

}
