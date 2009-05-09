package exehda.test;

import java.rmi.RemoteException;

import exehda.GeneralObjectActivator;

public class AsyncExecTest implements AsyncExecTestRemote {

	public void sleep(long id, long millis) throws RemoteException {
		System.out.println(id + ". sleep chegou -> " + millis);
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			System.out.println(id + ". interrompido aguardando " + millis);
			e.printStackTrace(System.out);
		}
		System.out.println(id + ". sleep saiu -> " + millis + " -> finished");
	}

	/**
	 * @param args
	 * @throws RemoteException
	 */
	public static void main(String[] args) throws RemoteException {
		AsyncExecTestRemote remote = GeneralObjectActivator.createRemoteObj(AsyncExecTest.class,
			AsyncExecTestRemote.class);
		long id = System.currentTimeMillis();
		Thread thread1 = new AsyncTest(id, remote);
		Thread thread2 = new AsyncTest(id, remote);
		thread1.start();
		thread2.start();
		try {
			thread1.join();
			thread2.join();
		} catch (InterruptedException e) {
			System.out.println("interrompido aguardando threads");
			e.printStackTrace(System.out);
		}
		System.out.println("terminou execução");
	}

	private static class AsyncTest extends Thread {
		private long id;
		private AsyncExecTestRemote instance;

		public AsyncTest(long id, AsyncExecTestRemote instance) {
			this.id = id;
			this.instance = instance;
			setDaemon(false);
		}

		@Override
		public void run() {
			try {
				System.out.println(id + ". antes de chamar sleep");
				instance.sleep(id, 10000);
				System.out.println(id + ". depois de chamar sleep");
			} catch (RemoteException e) {
				System.out.println(id + ". erro remoto");
				e.printStackTrace(System.out);
			}
		}
	}
}