package appman.portlets;

import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class AppManConfig extends HttpServlet {

	private static final long serialVersionUID = 1536047617106675633L;
	private static final Log log = LogFactory.getLog(AppManConfig.class);

	private static Configuration config;

	public static Configuration get() {
		if (config == null) throw new Error(AppManConfig.class + " nao inicializada");
		return config;
	}

	public void init(ServletConfig sConfig) throws ServletException {

		PropertiesConfiguration initConfig = new PropertiesConfiguration();
		for (Enumeration<?> e = sConfig.getInitParameterNames(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			initConfig.addProperty(key, sConfig.getInitParameter(key));
		}

		try {
			CompositeConfiguration comp = new CompositeConfiguration();
			Configuration envConfig = ResourceUtil.getEnvConfig();
			if (envConfig.getStringArray("appman.home").length == 1) {
				log.warn(envConfig.getString("appman.portlets.config") + " não encontrado: usando configuração padrão");
			}
			comp.addConfiguration(envConfig);
			comp.addConfiguration(initConfig);
			AppManConfig.config = comp;
		} catch (ConfigurationException ex) {
			throw new ServletException(ex);
		}
	}

	public void destroy() {
		AppManConfig.config = null;
	}
}
