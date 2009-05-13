/*
 * Created on 31/08/2004
 *
 */
package appman;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author lucasa
 *
 */
public class GridRemoteHostsFileProperties extends Properties implements Serializable
{
	private static final long serialVersionUID = -4809139501645717731L;
	private static final Log log = LogFactory.getLog(GridRemoteHostsFileProperties.class);
	private String propertiesFileName;
	private String propertiesFileSection;
	
	public GridRemoteHostsFileProperties(String filename, String filesection) {
		super();
		try {
			propertiesFileName = filename;
			propertiesFileSection = filesection;
			// log.debug("Load Properties File: " + propertiesFileName);
			InputStream fis = getClass().getClassLoader().getResourceAsStream(propertiesFileName);
			// log.debug(fis);
			load(fis);
			fis.close();
		} catch (IOException ex) {
			log.warn(ex, ex);
			System.exit(0);
		}
	}
	
	public ArrayList getTargetHosts()
	{
		ArrayList targetHosts = new ArrayList();
		String hosts = this.getProperty(propertiesFileSection);
		log.debug("GridRemoteHostsFileProperties hosts loaded from file: " + propertiesFileName + " - section: " + propertiesFileSection + " - ["+hosts+"]");
		StringTokenizer st = new StringTokenizer(hosts, ";");
		while (st.hasMoreTokens())
		{
			String str = st.nextToken();
			if(!str.equals(";"))
			{
				targetHosts.add(str);
				log.debug(str);
			}
		}
		
		return targetHosts;
	}

}
