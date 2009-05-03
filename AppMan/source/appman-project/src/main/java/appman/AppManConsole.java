/*
 * Created on 17/12/2004
 */
package appman;


import java.io.FileWriter;
import java.rmi.RemoteException;

import org.isam.exehda.ApplicationId;
import org.isam.exehda.HostId;
import org.isam.exehda.ObjectId;

/**
 * @author lucasa
 */

public class AppManConsole implements AppManConsoleRemote
{
	ApplicationManagerRemote appman = null;
	ApplicationId aid = null;
		
	/**
	 *
	 * Este método obtém e guarda localmente o identificador EXEHDA da aplicação. 
	 * Esse identificador é usado posteriormente com a chamada
	 * Executor.runAction(...), para garantir que a ação a ser executada o seja
	 * dentro do contexto de execução correto (basicamente, classloader
	 * correto, permissões).
	 *
	 */
	public AppManConsole() {
		aid = AppManUtil.getExecutor().currentApplication();
		Debug.debug("AID: " + aid, true);
		Debug.debug("AppManConsole created.", true);
	}

	public void runApplicationManagerRemote(String filepath) throws RemoteException {
		try {
			appman = this.createApplicationManager("appman");
			GridFileService fileservice = new GridFileService("AppManConsole");
			appman.addApplicationDescriptionRemote(fileservice.fileToByteArray(filepath));
			appman.startApplicationManager();
			while (appman.getApplicationStatePercentCompleted() < 1) {
				Debug.debug(appman.getInfoRemote(), true);
				try {
					Thread.sleep(5000);
				} catch (Exception e) {
					e.printStackTrace(System.out);
				}
			}
		} catch (RemoteException e1) {
			e1.printStackTrace(System.out);
		} catch (Exception e2) {
			e2.printStackTrace(System.out);
		}
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
	private ApplicationManager createLocalApplicationManager() throws RemoteException
        {
            return new ApplicationManager();
        }
	
//	public AppManConsoleRemote NewConsole()
//	{
//		try
//		{						
//				Executor ex = (Executor) Exehda.getService("executor");						
//				aid = ex.currentApplication();
//				Debug.debug("AID: "+aid, true);
//				ObjectHandle h = ex.createObject(aid,
//												 AppManConsole.class,
//												 null,
//												 null,
//												 HostId.getLocalHost());
//												 
//					// if h is null, so get some error in the remote object
//					if(h == null)
//					{
//						RemoteException e = new RemoteException("Host falhou");
//						throw e;
//					}
//							
//					AppManConsoleRemote stub = (AppManConsoleRemote)h.getStub();					
//					Debug.debug("AppManConsole remote created.", true);
//					Debug.debug(stub, true);
//					return stub ;
//							
//		}catch (Exception e)
//		{
//			Debug.debug("Tolerância a Falhas: ERRO FATAL NÃO TOLERADO!", true);
//			Debug.debug(e, true);
//			e.printStackTrace();
//			System.exit(0);
//		}
//		return null;		
//	}
	
	public static void main(String[] args) throws Exception {
		FileWriter out = new FileWriter("tempoExecucao.txt", true);
		out.write("vindn " + System.currentTimeMillis() + "\n");

		AppManConsole console = new AppManConsole();

		console.runApplicationManagerRemote(args[0]);

		while (!ApplicationManagerState.FINAL.equals(console.appman.getApplicationState())) {
			Thread.sleep(5000);
		}
		System.out.println("\t ******************************************");
		System.out.println("\t *** EXECUÇÃO TERMINADA COM SUCESSO!!!! ***");
		System.out.println("\t ******************************************");

		out.write("vindn " + System.currentTimeMillis() + "\n");
		out.close();
	}
}
