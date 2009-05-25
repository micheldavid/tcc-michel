package appman.event;

import org.apache.commons.logging.LogFactory;

import appman.AppManConsole;

public class LogFactoryLifeCycleListener implements AppManConsoleLifeCycleListener {

	public void applicationStart(AppManConsole console) {}

	public void applicationEnd(AppManConsole console) {
		LogFactory.releaseAll();
	}

}
