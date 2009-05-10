/*
 * Created on 17/12/2004
 */
package appman;

import java.io.FileWriter;
import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.isam.exehda.ApplicationId;
import org.isam.exehda.HostId;
import org.isam.exehda.ObjectId;

import appman.db.DBHelper;
import appman.log.Debug;

/**
 * @author lucasa
 */
public class AppManConsole implements AppManConsoleRemote {

	private static final Log log = LogFactory.getLog(AppManConsole.class);

	private ApplicationManagerRemote appman = null;
	private ApplicationId aid = null;

	/**
	 * Este método obtém e guarda localmente o identificador EXEHDA da aplicação. Esse identificador é usado
	 * posteriormente com a chamada Executor.runAction(...), para garantir que a ação a ser executada o seja dentro do
	 * contexto de execução correto (basicamente, classloader correto, permissões).
	 */
	public AppManConsole() {
		aid = AppManUtil.getExecutor().currentApplication();
		Debug.debug("AppManConsole Application: " + aid);
	}

	public void runApplicationManagerRemote(String filepath) throws RemoteException {
		try {
			appman = this.createApplicationManager("appman");

			GridFileService fileservice = new GridFileService("AppManConsole");
			appman.addApplicationDescriptionRemote(fileservice.fileToByteArray(filepath));

			appman.startApplicationManager();
		} catch (Exception ex) {
			log.error(ex, ex);
		}
	}

	private ApplicationManagerRemote createApplicationManager(String appmanId) throws RemoteException {

		GeneralObjectActivator activator = new GeneralObjectActivator("ApplicationManager",
			new Class[] { ApplicationManagerRemote.class }, new String[] { "ApplicationManagerRemote" }, true);

		ObjectId h = AppManUtil.getExecutor().createObject(ApplicationManager.class, new Object[] { appmanId },
			activator, HostId.getLocalHost());

		ApplicationManagerRemote stub = (ApplicationManagerRemote) GeneralObjectActivator.getRemoteObjectReference(h,
			ApplicationManagerRemote.class, "ApplicationManagerRemote");

		String contact = activator.getContactAddress(0);
		stub.setMyObjectContactAddressRemote(contact);
		return stub;
	}

	public static void main(String[] args) throws Exception {
		Integer jobId = null;
		if (args.length >= 2) jobId = Integer.valueOf(args[1]);

		FileWriter out = new FileWriter("tempoExecucao.txt");
		out.write("vindn " + System.currentTimeMillis() + "\n");
		out.close();

		AppManConsole console = new AppManConsole();

		if (jobId != null) {
			DBHelper.registerAppId(console.aid.toResourceName().getSimpleName(), jobId);
		}

		boolean success = true;
		try {
			console.runApplicationManagerRemote(args[0]);
		} catch (Exception ex) {
			success = false;
			log.error("erro fatal", ex);
		}

		if (jobId != null) {
			DBHelper.registerAppEnd(jobId, success);
		}
		System.out.println("\t ******************************************");
		System.out.println("\t *** EXECUÇÃO TERMINADA COM SUCESSO!!!! ***");
		System.out.println("\t ******************************************");

		out = new FileWriter("tempoExecucao.txt");
		out.write("vindn " + System.currentTimeMillis() + "\n");
		out.close();
	}
}
