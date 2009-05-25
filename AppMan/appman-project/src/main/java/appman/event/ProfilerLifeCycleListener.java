package appman.event;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import appman.AppManConsole;

public class ProfilerLifeCycleListener implements AppManConsoleLifeCycleListener {

	private static final Log log = LogFactory.getLog(ProfilerLifeCycleListener.class);
	private long startupTime;

	public void applicationStart(AppManConsole console) {
		startupTime = System.currentTimeMillis();
		try {
			debugTempoExecucao("Início da execução (" + System.currentTimeMillis() + "): "
				+ new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
		} catch (IOException ex) {
			log.error("gravando tempo de execução", ex);
		}
	}

	public void applicationEnd(AppManConsole console) {
		long endTime = System.currentTimeMillis();
		try {
			debugTempoExecucao("Fim da execução (" + endTime + "): "
				+ new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(endTime)));
			debugTempoExecucao("Tempo de execução: " + (endTime - startupTime) + " - "
				+ formatTimeSpan(endTime - startupTime));
		} catch (IOException ex) {
			log.error("gravando tempo de execução", ex);
		}

	}

	private String formatTimeSpan(long timeMillis) {
		String time = "." + (timeMillis % 1000);
		long secs = (long) Math.floor(timeMillis / 1000);
		long mins = secs / 60;
		secs = secs % 60;
		return mins + ":" + (secs < 10 ? "0" : "") + secs + time;
	}

	private void debugTempoExecucao(String str) throws IOException {
		FileOutputStream fout = new FileOutputStream("tempoExecucao.txt", true);
		PrintStream out = new PrintStream(fout);
		out.println(str);
		out.close();
		fout.close();
		log.info(str);
	}
}
