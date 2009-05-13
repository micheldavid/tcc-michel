package exehda.test;

import java.rmi.RemoteException;

public interface AsyncExecTestRemote {

	public void sleep(long id, long millis) throws RemoteException;
}
