/*
 * Created on 17/12/2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package appman;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author lucasa
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface AppManConsoleRemote extends Remote
{
	public void runApplicationManagerRemote(String filepath) throws RemoteException;
}
