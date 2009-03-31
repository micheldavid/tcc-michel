/*
 * Created on 13/05/2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package appman;
import java.util.Vector;
import java.rmi.RemoteException;
/**
 * @author lucasa
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface TaskManagerRemote extends java.rmi.Remote
{
	public void addTaskToListRemote(Task task) throws RemoteException;
	public void addTaskToListRemote(Vector tasks) throws RemoteException;
	public long getDownloadTimeOfTasks( ) throws RemoteException;
	public long calculateDownloadTimeNow() throws RemoteException;
}
