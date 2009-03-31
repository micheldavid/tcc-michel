/*
 * Created on 01/06/2004
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
public interface GridTaskRemote extends Remote
{
	public void setRun(boolean b) throws RemoteException;

    
        /**
         * Wait for task completion up to the specified timeout, returning true if the
         * task has completed, otherwise (timeout has expired prior to completion),
         * returns false. Of course, if course, if the task finished before the timeout
         * expires, the method should return as soon as possible.
         *
         * @param timeoutSeconds an <code>int</code> value
         * @return a <code>boolean</code> value
         * @exception RemoteException if an error occurs
         */
    public boolean getEnd(int timeoutSeconds) throws RemoteException;
	public void setDie() throws RemoteException;
	public boolean getSuccess() throws RemoteException;
	public  String getErrorMessage() throws RemoteException;
}
