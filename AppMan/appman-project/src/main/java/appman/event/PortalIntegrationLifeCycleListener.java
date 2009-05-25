package appman.event;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import appman.AppManConsole;
import appman.db.DBHelper;

public class PortalIntegrationLifeCycleListener implements AppManConsoleLifeCycleListener {

	public static final Log log = LogFactory.getLog(PortalIntegrationLifeCycleListener.class);
	
	private Integer appId;

	public void applicationStart(AppManConsole console) {
		appId = console.getId();
		if (appId != null) {
			try {
				DBHelper.registerAppId(console.getAppId().toResourceName().getSimpleName(), appId);
			} catch (SQLException e) {
				log.error("registrando inicialização da aplicação no BD", e);
			}
		}
	}

	public void applicationEnd(AppManConsole console) {
		if (appId != null) {
			try {
				DBHelper.registerAppEnd(appId, console.isSuccess());
			} catch (SQLException e) {
				log.error("registrando finalização da aplicação no BD", e);
			}
		}
	}

}
