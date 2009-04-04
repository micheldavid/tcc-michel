/*
 * Created on 24/05/2004
 *
 */
package appman;

import java.rmi.RemoteException;

/**
 * @author lucasa
 */
public interface SubmissionManagerRemote extends java.rmi.Remote
{
	public float getGraphStatePercentCompletedRemote(String graphId) throws java.rmi.RemoteException;
	public Graph getGraphRemote(String graphId) throws java.rmi.RemoteException;
	public void addGraphRemote(Graph g) throws java.rmi.RemoteException;
	public float getTaskStateRemote(String taskId,String graphId) throws java.rmi.RemoteException;
	public String getMyObjectRemoteContactAddress() throws java.rmi.RemoteException;
	public void setMyObjectRemoteContactAddress(String contact) throws java.rmi.RemoteException;
	public byte[] downloadFileFromGridTask(String taskId, String filepath) throws java.rmi.RemoteException;
	public String getSubmissionManagerIdRemote() throws java.rmi.RemoteException;
	public boolean getIsAliveRemote() throws java.rmi.RemoteException;
	public void PrintInfoRemote() throws RemoteException;
	public void setDieRemote() throws RemoteException;
	public long getDownloadTimeOfTasksManagers( ) throws RemoteException;
}
