/*
 * Created on 17/12/2004
 */
package appman;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author lucasa
 */
public interface AppManConsoleRemote extends Remote
{
	public void runApplicationManagerRemote(String filepath) throws RemoteException;
}
