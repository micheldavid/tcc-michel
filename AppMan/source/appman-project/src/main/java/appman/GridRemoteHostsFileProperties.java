/*
 * Created on 31/08/2004
 *
 */
package appman;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * @author lucasa
 *
 */
public class GridRemoteHostsFileProperties extends Properties implements Serializable
{
	private static final long serialVersionUID = -4809139501645717731L;
	private String propertiesFileName;
	private String propertiesFileSection;
	
	public GridRemoteHostsFileProperties(String filename, String filesection)
	{
			super();
			try
			{
					propertiesFileName = filename;
					propertiesFileSection = filesection;
					//Debug.debug("Load Properties File: " + propertiesFileName);
					InputStream fis = this.getClass().getClassLoader().getResourceAsStream(propertiesFileName);
					//Debug.debug(fis);
					this.load(fis);					
			} catch (FileNotFoundException fnfe)
			   {
					Debug.debug(fnfe);
					fnfe.printStackTrace();
					System.exit(0);
				} catch (IOException ioe)
				{
					ioe.printStackTrace();
					System.exit(0);
				}
	}
	
	public ArrayList getTargetHosts()
	{
		ArrayList targetHosts = new ArrayList();
		String hosts = this.getProperty(propertiesFileSection);
		Debug.debug("GridRemoteHostsFileProperties hosts loaded from file: " + propertiesFileName + " - section: " + propertiesFileSection + " - ["+hosts+"]", true);
		StringTokenizer st = new StringTokenizer(hosts, ";", true);
		while (st.hasMoreTokens())
		{
			String str = st.nextToken();
			if(!str.equals(";"))
			{
				targetHosts.add(str);
				Debug.debug(" " + str, false);
			}
		}
		
		return targetHosts;
	}

}
