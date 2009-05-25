package appman.event;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import appman.AppManConsole;
import appman.db.AppDataSource;

public class DataSourceLifeCycleListener implements AppManConsoleLifeCycleListener {

	private static final Log log = LogFactory.getLog(DataSourceLifeCycleListener.class);

	public void applicationEnd(AppManConsole console) {
		try {
			AppDataSource.close();
		} catch (SQLException e) {
			log.error("closing datasource", e);
		}
	}

	public void applicationStart(AppManConsole console) {
		AppDataSource.initialize();
	}

}
