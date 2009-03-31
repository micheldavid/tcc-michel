/*
 * Created on 17/12/2004
 */
package appman;

import java.rmi.RemoteException;

import org.isam.exehda.HostId;
import org.isam.exehda.ObjectId;

/**
 * @author lucasa
 * @author rbrosinha (200611)
 */
public class AppManConsole implements AppManConsoleRemote {
	ApplicationManagerRemote appman = null;

	public AppManConsole() {
		Debug.log("AppManConsole created.");
	}

	public void runApplicationManagerRemote(String filepath) throws RemoteException {
		try {
			appman = this.createApplicationManager("appman");
			GridFileService fileservice = new GridFileService("AppManConsole");
			appman.addApplicationDescriptionRemote(fileservice.fileToByteArray(filepath));
			appman.startApplicationManagerRemote();
			while (appman.getApplicationStatePercentCompletedRemote() < 1) {
				Debug.log(appman.getInfoRemote());
				try {
					Thread.sleep(5000);
				} catch (Exception e) {
					Debug.debug(e);
				}
			}
		} catch (RemoteException e1) {
			Debug.log("RemoteException", e1);
		} catch (Exception e2) {
			Debug.log("Exception", e2);
		}
	}

	private ApplicationManagerRemote createApplicationManager(String appmanId) {
		try {
			GeneralObjectActivator activator = new GeneralObjectActivator("ApplicationManager", new Class[] { ApplicationManagerRemote.class }, new String[] { "ApplicationManagerRemote" }, true);
			ObjectId h = AppManUtil.getExecutor().createObject(ApplicationManager.class, new Object[] { appmanId }, activator, HostId.getLocalHost());
			if (h == null) {
				throw new RemoteException("Host falhou");
			}
			ApplicationManagerRemote stub = (ApplicationManagerRemote) GeneralObjectActivator.getRemoteObjectReference(h, ApplicationManagerRemote.class, "ApplicationManagerRemote");
			stub.setMyObjectContactAddressRemote(activator.getContactAddress(0));
			return stub;
		} catch (Exception e) {
			Debug.debug(e);
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		Debug.init("appman.log");
		Debug.log("ApplicationManager started.");
		AppManConsole console = new AppManConsole();
		console.runApplicationManagerRemote(args[0]);
		while (console.appman.getApplicationStateRemote() != ApplicationManager.ApplicationManager_FINAL) {
			Thread.sleep(10000);
		}
		Debug.log("ApplicationManager ended.");
		Debug.close();
	}
}
