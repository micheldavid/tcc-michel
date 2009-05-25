package appman.event;

import java.util.EventListener;

import appman.AppManConsole;

public interface AppManConsoleLifeCycleListener extends EventListener {

	void applicationStart(AppManConsole console);
	void applicationEnd(AppManConsole console);
}
