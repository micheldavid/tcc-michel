/*
 * Created on 17/12/2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package appman;


import java.io.FileWriter;
import java.rmi.RemoteException;

import org.isam.exehda.ApplicationId;
import org.isam.exehda.HostId;
import org.isam.exehda.ObjectId;

/**
 * @author lucasa
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
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
	public AppManConsole()
        {
            aid = AppManUtil.getExecutor().currentApplication();
            Debug.debug("AID: "+aid, true);
            Debug.debug("AppManConsole created.", true);
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
							Debug.debug(appman.getInfoRemote(), true);
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
                    //AppManUtil.exitApplication("Toler�ncia a Falhas: ERRO FATAL N�O TOLERADO", e1);
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
//            Debug.debug("Toler�ncia a Falhas: ERRO FATAL N�O TOLERADO!", true);
            stub.setMyObjectContactAddressRemote(contact);
            return stub ;
							
        }catch (Exception e)
        {
        	//VDN
            //AppManUtil.exitApplication("Toler�ncia a Falhas: ERRO FATAL N�O TOLERADO", e);
//             Debug.debug("Toler�ncia a Falhas: ERRO FATAL N�O TOLERADO!", true);
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
//			Debug.debug("Toler�ncia a Falhas: ERRO FATAL N�O TOLERADO!", true);
//			Debug.debug(e, true);
//			e.printStackTrace();
//			System.exit(0);
//		}
//		return null;		
//	}
	
	public static void main(String[] args) throws Exception
	{
		FileWriter out;
		out = new FileWriter("tempoExecucao.txt",true);
		
			try {
				out.write("vindn " + System.currentTimeMillis()+ "\n" );
				
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			AppManConsole console = new AppManConsole();	
			//System.out.println("AKIII: "+args[0]);
			
			console.runApplicationManagerRemote(args[0]);
			
			while(console.appman.getApplicationState() != ApplicationManager.ApplicationManager_FINAL)
            {
                Thread.sleep(5000);
            }
			System.out.println("\t ******************************************");
			System.out.println("\t *** EXECUÇÃO TERMINADA COM SUCESSO!!!! ***");
			System.out.println("\t ******************************************");
			//VDN
           // AppManUtil.exitApplication();
			try {
				out.write("vindn " +System.currentTimeMillis()+ "\n" );				
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			
			
	}
}
