/*
 * Created on 13/05/2004
 */
package appman.task;
import java.util.Vector;
import java.rmi.RemoteException;
/**
 * @author lucasa
 */
public interface TaskManagerRemote extends java.rmi.Remote
{
	public void addTaskToListRemote(Task task) throws RemoteException;
	public void addTaskToListRemote(Vector tasks) throws RemoteException;
	public long getDownloadTimeOfTasks( ) throws RemoteException;
}
