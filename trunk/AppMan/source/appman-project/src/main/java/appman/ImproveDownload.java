/*
 * Created on 17/01/2006
 */
package appman;

import java.util.Hashtable;

import appman.log.Debug;

/**
 * @author dalto
 */
public class ImproveDownload {

	private Hashtable hashPaths;

	public ImproveDownload(int numberMaxOfTasks ) {
		hashPaths = new Hashtable(numberMaxOfTasks);
	}

	/**
	 * Adiciona o ultimo arquivo baixado, especificando a url de onde foi baixado e o diretorio para onde ele foi
	 * copiado.
	 * 
	 * @param URLpath
	 * @param localPath
	 */
	public synchronized void setLastURLFilePath(String URLpath, String localPath){

		Debug.debug("ImproveDownload: add file info ("+URLpath+", "+localPath+").");
        hashPaths.put(URLpath, localPath);
	}
	
	
	/**
	 * Verifica se o arquivo ja foi baixado
	 * @param url
	 * @return
	 */
	public boolean URLFileExists(String url){
		
		Debug.debug("ImproveDownload: URLpaths.size() = "+hashPaths.size());		
		String urlPath = (String)hashPaths.get(url);
		Debug.debug("ImproveDownload: URLpaths = "+urlPath);
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