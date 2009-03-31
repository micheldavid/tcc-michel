/*
 * Created on 17/11/2004
 */
package appman;

import java.rmi.Remote;
import java.rmi.RemoteException;

import appman.parser.ApplicationDescription;
import edu.berkeley.guir.prefuse.Display;

/**
 * @author lucasa
 * @author rbrosinha (200611)
 */
public interface ApplicationManagerRemote extends Remote {
	public SubmissionManagerRemote getSubmissionManagerRemote(String subId) throws RemoteException;

	public String getTaskGridFileServiceContactAddressRemote(String taskId) throws RemoteException;

	public boolean isTaskOutputsRemoteAvailable(String taskId) throws RemoteException, java.net.SocketException;

	public String getInfoRemote() throws RemoteException;

	public void addGraphRemote(Graph g) throws RemoteException;

	public Display startAppGUIRemote(String graphId) throws RemoteException;

	public void addApplicationDescriptionRemote(String graphId, String clusterId, ApplicationDescription appdesc) throws RemoteException;

	public void setMyObjectContactAddressRemote(String contact) throws RemoteException;

	public void startApplicationManagerRemote() throws RemoteException;

	public float getApplicationStatePercentCompletedRemote() throws RemoteException;

	public int getApplicationStateRemote() throws RemoteException;

	public void addApplicationDescriptionRemote(byte[] filedata) throws RemoteException;

	public ApplicationDescription getApplicationDescription() throws RemoteException;
}
