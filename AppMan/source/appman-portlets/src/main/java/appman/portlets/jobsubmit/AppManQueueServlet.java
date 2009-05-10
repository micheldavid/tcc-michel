package appman.portlets.jobsubmit;

import java.util.ArrayList;

import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import appman.portlets.AppManDBHelper;
import appman.portlets.AppManHelper;
import appman.portlets.AppManLdapHelper;
import appman.portlets.LdapSession;
import appman.portlets.model.AppManJob;

public class AppManQueueServlet extends HttpServlet implements Runnable {

	private static final long serialVersionUID = 4657355264348463610L;

	private static final Log log = LogFactory.getLog(AppManQueueServlet.class);

	private long waitTime;

	private LdapSession session = null;
	private Thread verifier = null;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		waitTime = Long.parseLong(config.getInitParameter("polling.time"));

		try {
			session = AppManLdapHelper.createSession();
		} catch (NamingException e) {
			log.error("abrindo sessão do ldap", e);
		}
		verifier = new Thread(this);
		verifier.start();
	}

	@Override
	public void destroy() {
		super.destroy();

		try {
			session.close();
		} catch (NamingException e) {
			log.error("fechando sessão do ldap", e);
		}
		verifier.interrupt();
	}

	/**
	 * seta todas tarefas para finalizadas no LDAP
	 */
	private void finishAllJobs() throws Exception {
		ArrayList<String> appIds = AppManLdapHelper.searchApplications(session, null, "running");
		for (String appId : appIds) {
			AppManHelper.stopApplication(session, appId);
		}
	}

	public void run() {
		try {
			while (!verifier.isInterrupted()) {
				try {
					if (!AppManHelper.isExehdaRunning()) {
						finishAllJobs();
					} else {
						AppManJob toRun = AppManDBHelper.findJobToRun();
						if (toRun != null) {
							// verifica se existem tarefas a serem executadas no BD
							// se existir, verificar se existem tarefas rodando no LDAP
							if (!AppManDBHelper.hasRunningJobs() && !AppManLdapHelper.hasRunningApps(session)) {
								AppManHelper.startJob(toRun);
								while (!AppManDBHelper.isJobFinished(toRun.getId()))
									Thread.sleep(waitTime);
								AppManLdapHelper.finalizeApplication(session, AppManDBHelper.getAppId(toRun.getId()));
								AppManHelper.organizeFinishedJob(toRun);
							}
						}
					}
				} catch (InterruptedException ex) {
					throw ex;
				} catch (Exception ex) {
					log.error("verificando tarefas na lista", ex);
				}
				Thread.sleep(waitTime);
			}
		} catch (InterruptedException ex) {
			log.error("esperando " + waitTime, ex);
		}
	}
}
