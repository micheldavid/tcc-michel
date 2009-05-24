/*
 * Created on 17/01/2006
 * @author VDN 
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package appman;

import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author dalto
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ImproveDownload {
	
	private static final Log log = LogFactory.getLog(ImproveDownload.class);
	
	private Hashtable hashPaths;
	/**
	 * 
	 */
	public ImproveDownload(int numberMaxOfTasks ) {
		// TODO Auto-generated constructor stub
		hashPaths = new Hashtable(numberMaxOfTasks);
		
	}
	
	/**
	 * Adciona o ultimo arquivo baixado, especificando a url de onde foi baixado e o diretorio para onde ele foi copiado.
	 * @param URLpath
	 * @param localPath
	 */
	public synchronized void setLastURLFilePath(String URLpath, String localPath){

		log.debug("ImproveDownload: add file info ("+URLpath+", "+localPath+").");
        hashPaths.put(URLpath, localPath);
	}
	
	
	/**
	 * Verifica se o arquivo ja foi baixado
	 * @param url
	 * @return
	 */
	public boolean URLFileExists(String url){
		
		log.debug("ImproveDownload: URLpaths.size() = "+hashPaths.size());		
		String urlPath = (String)hashPaths.get(url);
		log.debug("ImproveDownload: URLpaths = "+urlPath);
		return urlPath != null; 
		
	}
	
	//TODO: Preciso Otimizar isso, Muito Ruim, varro 2 vezes a lista!!!
	/**
	 * Dado a url retorno o path para onde o arquivo foi copiado
	 */
	public synchronized String getLocalPathFromURL( String url ){
		
		return (String)hashPaths.get(url);
	}
}
