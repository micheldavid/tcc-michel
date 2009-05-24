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
public class AppManConsole implements AppManConsoleRemote
{
	private static final Log log = LogFactory.getLog(AppManConsole.class);
	private ApplicationManagerRemote appman = null;
	private ApplicationId aid = null;

	/**
	 *
	 * Este método obtém e guarda localmente o identificador EXEHDA da aplicação. 
	 * Esse identificador é usado posteriormente com a chamada
	 * Executor.runAction(...), para garantir que a ação a ser executada o seja
	 * dentro do contexto de execução correto (basicamente, classloader
	 * correto, permissões).
	 *
	 */
	public AppManConsole()
        {
            aid = AppManUtil.getExecutor().currentApplication();
    		log.debug("AppManConsole Application: " + aid);
        }	
	public void runApplicationManagerRemote(String filepath) throws RemoteException
	{				
				try
				{					
						appman = this.createApplicationManager("appman");
						GridFileService fileservice = new GridFileService("AppManConsole");
						appman.addApplicationDescriptionRemote(fileservice.fileToByteArray(filepath));
						appman.startApplicationManager();
						System.out.println("Wagner Iniciando Appman");
						while(appman.getApplicationStatePercentCompleted() < 1)
						{
							log.debug(appman.getInfoRemote());
							try
							{		
								Thread.sleep(5000);
							} catch (Exception e)
							{
								e.printStackTrace(System.out);
								//VDN
                                //AppManUtil.exitApplication(null, e); 
							}
						}	
						System.out.println("Wagner terminando Appman");
				}catch (RemoteException e1)
				{
					e1.printStackTrace(System.out);
					//VDN
                    //AppManUtil.exitApplication("Tolerância a Falhas: ERRO FATAL NÃO TOLERADO", e1);
				}
				catch (Exception e2)
				{
					e2.printStackTrace(System.out);
					//VDN
                    //AppManUtil.exitApplication(null, e2);
				}
			

				
				//System.out.println("\tAKIII DEVERIA TERMINAR!!!!!!!!\n");
	}	
	private ApplicationManagerRemote createApplicationManager(String appmanId)
        {
            try
            {
					
                GeneralObjectActivator activator = new GeneralObjectActivator("ApplicationManager",
                                                                              new Class[] {ApplicationManagerRemote.class},
                                                                              new String[] {"ApplicationManagerRemote"},
                                                                              true);
						
                ObjectId h = AppManUtil.getExecutor().createObject(ApplicationManager.class,
                                                                   new Object[] {appmanId},
                                                                   activator,
                                                                   HostId.getLocalHost());
												 
                    // if h is null, so get some error in the remote object
                if(h == null)
                {
                    RemoteException e = new RemoteException("Host falhou");
                    throw e;
                }
							
                    //ApplicationManagerRemote stub = (ApplicationManagerRemote)h.getStub();
                ApplicationManagerRemote stub = (ApplicationManagerRemote)GeneralObjectActivator.getRemoteObjectReference(
                    h, ApplicationManagerRemote.class, "ApplicationManagerRemote");
            
                //stub.setStubRemote(stub);
            String contact = activator.getContactAddress(0);
//            Debug.debug("Tolerância a Falhas: ERRO FATAL NÃO TOLERADO!", true);
            stub.setMyObjectContactAddressRemote(contact);
            return stub ;
							
        }catch (Exception e)
        {
        	//VDN
            //AppManUtil.exitApplication("Tolerância a Falhas: ERRO FATAL NÃO TOLERADO", e);
//             Debug.debug("Tolerância a Falhas: ERRO FATAL NÃO TOLERADO!", true);
//             Debug.debug(e, true);
//             e.printStackTrace();
//             System.exit(0);
            }
            return null;
        }

	public static void main(String[] args) throws Exception
	{
		long startupTime = System.currentTimeMillis();
		debugTempoExecucao("Início da execução (" + System.currentTimeMillis()
				+ "): " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));

		Integer jobId = null;
		if (args.length >= 2) jobId = Integer.valueOf(args[1]);

		AppManConsole console = new AppManConsole();
		if (jobId != null) {
			DBHelper.registerAppId(console.aid.toResourceName().getSimpleName(), jobId);
		}

		boolean success = true;
		try {
			console.runApplicationManagerRemote(args[0]);
			boolean isFinal = console.appman.getApplicationState() == ApplicationManager.ApplicationManager_FINAL;
			log.info("AppState == FINAL? " + isFinal);
			log.info("EXECUÇÃO TERMINADA COM SUCESSO");

		} catch (Exception ex) {
			success = false;
			log.error("EXECUÇÃO TERMINADA COM ERRO", ex);
		}

		if (jobId != null) {
			DBHelper.registerAppEnd(jobId, success);
		}

		long endTime = System.currentTimeMillis();
		debugTempoExecucao("Fim da execução (" + endTime
				+ "): " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(endTime)));
		debugTempoExecucao("Tempo de execução: " + (endTime - startupTime)
				+ " - " + formatTimeSpan(endTime - startupTime));

		// tentando matar o timer que o exehda deixa rodando...
//		AppManUtil.exitApplication();

		LogFactory.releaseAll();
	}

	private static String formatTimeSpan(long timeMillis) {
		String time = "." + (timeMillis % 1000);
		long secs = (long) Math.floor(timeMillis / 1000);
		long mins = secs / 60;
		secs = secs % 60;
		return mins + ":" + (secs < 10 ? "0" : "") + secs + time;
	}

	private static void debugTempoExecucao(String str) throws IOException {
		FileOutputStream fout = new FileOutputStream("tempoExecucao.txt", true);
		PrintStream out = new PrintStream(fout);
		out.println(str);
		out.close();
		fout.close();
		log.info(str);
	}
}
