/*
 * Created on 11/06/2004
 *
 */
package appman;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author lucasa
 *
 */
public interface GridFileServiceRemote extends Remote
{
	public void uploadFile(byte[] filedata, String filepath) throws RemoteException;
	public byte[] downloadFile(String filepath) throws RemoteException;
	public void installURLFile(String url, String localFile, boolean chmod) throws RemoteException;
	public String getDefaultDir() throws RemoteException;
}
