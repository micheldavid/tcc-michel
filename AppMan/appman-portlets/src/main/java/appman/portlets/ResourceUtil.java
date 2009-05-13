package appman.portlets;

import java.net.URL;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class ResourceUtil {

	public static Configuration getEnvConfig() throws ConfigurationException {
		return getPropertiesConfiguration(ResourceUtil.class.getResource("/env.properties"));
	}

	public static Configuration getPropertiesConfiguration(URL file) throws ConfigurationException {
		return new PropertiesConfiguration(file);
	}
}
