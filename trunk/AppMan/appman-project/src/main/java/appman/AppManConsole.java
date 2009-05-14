/*
 * Created on 17/12/2004
 */
package appman;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.isam.exehda.ApplicationId;
import org.isam.exehda.HostId;
import org.isam.exehda.ObjectId;

import appman.db.DBHelper;

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
		log.debug("AppManConsole Application: " + aid);
	}

	public void runApplicationManagerRemote(String filepath) throws RemoteException {
		try {
			appman = this.createApplicationManager("appman");

			GridFileService fileservice = new GridFileService("AppManConsole");
			appman.addApplicationDescriptionRemote(fileservice.fileToByteArray(filepath));

			appman.startApplicationManager();
			while (appman.getApplicationStatePercentCompleted() < 1) {
				log.debug(appman.getInfoRemote());
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					log.warn(e, e);
				}
			}
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
		debugTempoExecucao("vindn " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));

		Integer jobId = null;
		if (args.length >= 2) jobId = Integer.valueOf(args[1]);

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
		// MICHEL: blocking call
		try {
			while (!ApplicationManagerState.FINAL.equals(console.appman.getApplicationState())) {
				Thread.sleep(5000);
			}
			//success = console.appman.isSuccessful();
		} catch (InterruptedException ex) {
			log.error("thread principal interrompida...", ex);
			success = false;
		}

		log.debug("\t ******************************************");
		log.debug("\t *** EXECUÇÃO TERMINADA COM SUCESSO!!!! ***");
		log.debug("\t ******************************************");

		if (jobId != null) {
			DBHelper.registerAppEnd(jobId, success);
		}

		debugTempoExecucao("vindn " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));

		// tentando matar o timer que o exehda deixa rodando...
//		AppManUtil.exitApplication();
	}
	
	private static void debugTempoExecucao(String str) throws IOException {
		FileOutputStream fout = new FileOutputStream("tempoExecucao.txt", true);
		PrintStream out = new PrintStream(fout);
		out.println(str);
		out.close();
		fout.close();
	}
}
