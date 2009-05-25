/*
 * Created on 17/12/2004
 */
package appman;


import java.rmi.RemoteException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.isam.exehda.ApplicationId;
import org.isam.exehda.HostId;
import org.isam.exehda.ObjectId;

import appman.event.AppManConsoleLifeCycleListener;
import appman.event.AppManConsoleLifeCycleListening;
import appman.event.DataSourceLifeCycleListener;
import appman.event.LogFactoryLifeCycleListener;
import appman.event.PortalIntegrationLifeCycleListener;
import appman.event.ProfilerLifeCycleListener;

/**
 * @author lucasa
 */
@AppManConsoleLifeCycleListening( { LogFactoryLifeCycleListener.class, ProfilerLifeCycleListener.class,
	DataSourceLifeCycleListener.class, PortalIntegrationLifeCycleListener.class })
public class AppManConsole implements AppManConsoleRemote
{
	private static final Log log = LogFactory.getLog(AppManConsole.class);
	private ArrayList<AppManConsoleLifeCycleListener> listeners;
	private ApplicationManagerRemote appman = null;
	private ApplicationId appId = null;

	/**
	 * ID da aplicação. Atualmente usado pelo portal, mas pode servir de integração para outros projetos.
	 */
	private Integer id;

	/**
	 * Indicador de sucesso
	 */
	private boolean success = false;

	/**
	 *
	 * Este método obtém e guarda localmente o identificador EXEHDA da aplicação. 
	 * Esse identificador é usado posteriormente com a chamada
	 * Executor.runAction(...), para garantir que a ação a ser executada o seja
	 * dentro do contexto de execução correto (basicamente, classloader
	 * correto, permissões).
	 *
	 */
	public AppManConsole(Integer id)
        {
		this.id = id;
            appId = AppManUtil.getExecutor().currentApplication();
    		log.debug("AppManConsole Application: " + appId);
    		listeners = new ArrayList<AppManConsoleLifeCycleListener>();
    		try {
	    		AppManConsoleLifeCycleListening anotation = getClass().getAnnotation(
					AppManConsoleLifeCycleListening.class);
				for (Class<? extends AppManConsoleLifeCycleListener> clz : anotation.value()) {
					listeners.add(clz.newInstance());
				}
    		} catch (Exception ex) {
    			throw new Error("inicializando LifeCycleListeners", ex);
    		}
        }	

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public ApplicationId getAppId() {
		return appId;
	}

	public void setAppId(ApplicationId appId) {
		this.appId = appId;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public void runApplicationManagerRemote(String filepath) throws RemoteException
	{
				try
				{					
						appman = this.createApplicationManager("appman");
						GridFileService fileservice = new GridFileService("AppManConsole");
						appman.addApplicationDescriptionRemote(fileservice.fileToByteArray(filepath));
						log.debug("Wagner Iniciando Appman");
						appman.runApplicationManager();
						log.debug("Wagner terminando Appman");
						success = true;
				}catch (RemoteException e1)
				{
					log.error(e1, e1);
					//VDN
                    //AppManUtil.exitApplication("Tolerância a Falhas: ERRO FATAL NÃO TOLERADO", e1);
				}
				catch (Exception e2)
				{
					log.error(e2, e2);
					//VDN
                    //AppManUtil.exitApplication(null, e2);
				}
	}	
	private ApplicationManagerRemote createApplicationManager(String appmanId)
        {
            try
            {
					
                GeneralObjectActivator activator = new GeneralObjectActivator("ApplicationManager",
                                                                              new Class[] {ApplicationManagerRemote.class},
                                                                              new String[] {"ApplicationManagerRemote"},
                                                                              false);
						
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
        	log.error(e, e);
        	//VDN
            //AppManUtil.exitApplication("Tolerância a Falhas: ERRO FATAL NÃO TOLERADO", e);
//             Debug.debug("Tolerância a Falhas: ERRO FATAL NÃO TOLERADO!", true);
//             Debug.debug(e, true);
//             System.exit(0);
            }
            return null;
        }

	public static void main(String[] args) throws Exception
	{
		Integer jobId = null;
		if (args.length >= 2) jobId = Integer.valueOf(args[1]);
		AppManConsole console = new AppManConsole(jobId);

		// listeners de inicialização
		for (AppManConsoleLifeCycleListener l : console.listeners)
			l.applicationStart(console);

		try {
			console.runApplicationManagerRemote(args[0]);
			boolean isFinal = console.appman.getApplicationState() == ApplicationManager.ApplicationManager_FINAL;
			log.info("AppState == FINAL? " + isFinal);
			log.info("EXECUÇÃO TERMINADA COM SUCESSO");

		} catch (Exception ex) {
			log.error("EXECUÇÃO TERMINADA COM ERRO", ex);
		}

		// para matar o timer que o exehda deixa rodando...
		AppManUtil.exitApplication();

		// listeners de finalização
		for (int i = console.listeners.size() - 1; i >= 0; i--)
			console.listeners.get(i).applicationEnd(console);
	}
}
