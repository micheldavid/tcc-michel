package appman.portlets;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

public class AppManConfig extends HttpServlet {

	private static final long serialVersionUID = 1536047617106675633L;

	private static Configuration config;

	public static Configuration get() {
		if (config == null) throw new Error(AppManConfig.class + " nao inicializada");
		return config;
	}

	public void init(ServletConfig sConfig) throws ServletException {
		try {
			AppManConfig.config = ResourceUtil.getEnvConfig();
		} catch (ConfigurationException ex) {
			throw new ServletException(ex);
		}
	}

	public void destroy() {
		AppManConfig.config = null;
	}
}
