package appman.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import appman.AppManConsole;

public class MemoryLifeCycleListener implements AppManConsoleLifeCycleListener {

	private static final Log log = LogFactory.getLog(MemoryLifeCycleListener.class);

	public void applicationEnd(AppManConsole console) {
		logMemoryUsage();
	}

	public void applicationStart(AppManConsole console) {
		logMemoryUsage();
	}

	private void logMemoryUsage() {
		long max = Runtime.getRuntime().maxMemory();
		long avail = Runtime.getRuntime().totalMemory();
		log.info("Memória: " + bytesToMb(max - avail) + "/" + bytesToMb(max));
	}

	private double bytesToMb(long b) {
		return (((long) (b / 1024d / 1024d * 100)) / 100d);
	}
}
