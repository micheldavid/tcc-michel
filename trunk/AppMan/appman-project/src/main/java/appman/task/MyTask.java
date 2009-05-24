/*
 * Created on 13/05/2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package appman.task;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.isam.exehda.ApplicationId;
import org.isam.exehda.ObjectId;

import appman.AppManUtil;
import appman.DataFile;
import appman.GeneralObjectActivator;
import appman.GridFileService;
import appman.GridFileServiceRemote;
import appman.GridSchedule;
import appman.GridTask;
import appman.GridTaskRemote;
import appman.SubmissionManager;
import appman.SubmissionManagerRemote;

//import org.isam.exehda.services.ObjectSeed.Activator;

/**
 * Refactored in 2006/01/10 by VDN and PKVM
 * 
 * @author lucasa
 *  
 */
public class MyTask extends Task implements Serializable {
	private static final Log log = LogFactory.getLog(MyTask.class);
	private static final long serialVersionUID = -2992989365373513990L;

	/**
	 * @param id
	 * @param desc
	 * @param str
	 * @param input
	 * @param output
	 */
	
	private int retry = 0;
		
	
	public MyTask(String submanid, String taskid, String task_desc,
			String task_name, Vector input, Vector output, String cmd) {
		super(submanid, taskid, task_desc, task_name, input, output, cmd);
	}

	public MyTask(String submanid, String taskid, String task_desc,
			String task_name, String cmd) {
		super(submanid, taskid, task_desc, task_name, cmd);
	}

	transient GridTaskRemote gridtaskremote = null;

	transient GridFileServiceRemote gridfileservice = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see appman.Task#execute()
	 */
	public void execute() throws Exception {
		String where = "grid.targetHosts.localscheduler";

		log.debug("Task " + this.getName() + " (" + this.getDescription()
				+ ") remote  executing.");

		try {
			GeneralObjectActivator activator = createGridTask(where);
			ObjectId h = createObjectId(where, activator);

			createGridFileService(h);
			
			//String contact_address = activator.getContactAddress(h,
			// "GridFileServiceRemote");
			//setRemoteGridFileServiceContactAddress(contact_address); //
			// guarda a referência do objeto remoto numa variável da classe

			log.debug("MyTask [" + this.getTaskId()
					+ "] remote GridTask created");
			timeInfo.setTimeStart(new Date());
			timeInfo.setTimeTaskStart(new Date());

			// this is one important part of the task instantiation: if the
			// input files cannot be transfered from a web site, or are already 
			// available locally, no task can star executing
		
			//VDN : 27/01/06
			long initialTime = System.currentTimeMillis();
			downloadInputFiles();
			long finalTime = System.currentTimeMillis();
			
			timeInfo.setDownloadTimeOfFiles( finalTime - initialTime );
			log.debug("[VINDN] Download Time: "+(finalTime - initialTime));
			
			log.debug("Mytask [" + this.getTaskId()
					+ "] SET GridTask to RUN:" + gridtaskremote);
            // start remote execution
			gridtaskremote.setRun(true); 


                //
                // XXX: trying to reduce remote pooling by using a blocking primitive
                // with timeout
                //
            final int GET_END_POOLING_MILLIS = 5000;
            final int GET_END_TIMEOUT_SECONDS = 30;
            while (true) {
                try {
                    if ( gridtaskremote.getEnd(GET_END_TIMEOUT_SECONDS) == true ) {
                        break;
                    }
                    else {
                        Thread.sleep(GET_END_POOLING_MILLIS);
                    }
                } catch (Exception e) {
                    log.error("Failed to get remote task end status: "+e+". Will retry...", e);
                }
            }
			
			// remote execution finished, check if there were any errors

			// TODO o codigo abaixo ficou horrivel... dar uma ajeitada - 
			// incluido para pegar o java.net.SocketException lancado pelo gridtaskremote.getSuccess()
			while (true) {
			try {

 			  if (gridtaskremote.getSuccess() == false) {
				gridtaskremote.setDie();
				throw new RemoteException("\nMyTask [" + this.getTaskId()
						+ "] RETRY [" + this.getRetryTimes()
						+ "] - GridTask Job failed: "
						+ gridtaskremote.getErrorMessage());
			   } else {
				
				transferOutputFiles();
				// VDN 2006/01/13 - include statement bellow
				//gridtaskremote.setDie();
				break;
			   } 
			} catch (Exception esocket) {
				log.error("Tentando obter novamente o gridtaskremote.getSuccess() - java.net.SocketException", esocket);
			}
			}

		} catch (RemoteException e1) {
			if (gridtaskremote != null) // I don't know if this code works
			{
				try {
					gridtaskremote.setDie();
				} catch (RemoteException re) {
					throw e1;
				}
			}
			throw e1;
		}

	}

	/**
	 * @throws RemoteException
	 */
	private void transferOutputFiles() throws RemoteException {
		String where;
		GeneralObjectActivator activator;
		ObjectId h;
		log.debug("Mytask Task [" + this.getTaskId() + "]  RETRY ["
				+ this.getRetryTimes() + "]  - Success: OK");
		// if the task type is FINAL, then transfer the output files to
		// the user machine, to get the results
		if (this.getTaskType() == TaskType.TASK_TYPE_FINAL) {
			where = "grid.targetHosts.host-final-results"; // all grid resources
			log.debug("MyTask [" + this.getTaskId()
				+ "] create remote GridFileService in Cell to transfer the results");

			activator = new GeneralObjectActivator("GridFileService",
					new Class[] { GridFileServiceRemote.class },
					new String[] { "GridFileServiceRemote" }, false);

			log.debug("MyTask [" + this.getTaskId()
					+ "] Activator remote GridFileService: "
					+ activator);

			h = AppManUtil.getExecutor().createObject(
					GridFileService.class, new Object[] {}, // a seed
															// para o
															// serviço
															// GridFileService
															// é o nome
															// da tarefa
					activator, where);

			log.debug("MyTask [" + this.getTaskId()
					+ "] remote GridFileService created: " + h);

			// if h is null, so get some error in the remote object
			if (h == null) {
				log.error("MyTask [" + this.getTaskId()
						+ "] remote GridFileService created FAILED: "
						+ h);
				RemoteException e = new RemoteException(
						"\nExehda Create remote object failed!");
				throw e;
			}

			log.debug(
							"MyTask ["
									+ this.getTaskId()
									+ "] remote reference GridFileServiceRemote looking.");
			GridFileServiceRemote result_gridfileservice = (GridFileServiceRemote) GeneralObjectActivator
					.getRemoteObjectReference(h,
							GridFileServiceRemote.class,
							"GridFileServiceRemote");

			log.debug("MyTask [" + this.getTaskId()
					+ "] remote GridFileServiceRemote created");
			log
					.debug(
							"MyTask ["
									+ this.getTaskId()
									+ "] results going to be transfered to user machine!");
			DataFile[] results_datafile = this.getFiles().getOutputFiles();
			for (int i = 0; i < results_datafile.length; i++) {
				String filepath = results_datafile[i].getName();
				log.debug("Transfering file: " + filepath);
				result_gridfileservice.uploadFile(gridfileservice
						.downloadFile(filepath), filepath);
			}
			log.debug("MyTask [" + this.getTaskId()
					+ "] results transfered to user machine!");
		}
	}

	/**
	 * @throws RemoteException
	 * @throws ConnectException
	 * @throws InterruptedException
	 * @throws MalformedURLException
	 */
	private void downloadInputFiles() throws RemoteException, ConnectException, InterruptedException, MalformedURLException {

		log.debug("MyTask : starting method downloadInputFiles...");

		// TODO: neste metodo teria que verificar se esta em rede local, neste caso nao
		// precisaria transferir se estiver no NFS
		DataFile[] datafile = this.getFiles().getInputFiles();
		int numDowloads = 0;
		String lastFilePath = ""; //VDN
		for (int i = 0; i < datafile.length; i++) {
			String filepath = datafile[i].getName();
			log.debug("File path: " + filepath);

			MyTask remote_task = (MyTask) datafile[i].getFromTask();
			if (remote_task != null) {
				byte[] buffer;
				//int retry = 0; VDN
				while (true) {
					try {
						log.debug("MyTask [" + this.getTaskId()
								+ "] try to install input files");
						// se a tarefa for estrangeira (de outro grafo)
						// então baixe o arquivo usando a referência remota
						// do serviço de arquivos do grid task
						if (remote_task.getState().getCode() == TaskState.TASK_FOREIGN_FINAL) {
							// esta referência remota foi atualizada pelo
							// submission manager <-- application manager
							// <-- task <-- grid task
							String contact_address_remote = remote_task
									.getSubmissionManagerContactAddress();
							SubmissionManagerRemote smr = (SubmissionManagerRemote) GeneralObjectActivator
									.getRemoteObjectReference(
											contact_address_remote,
											SubmissionManagerRemote.class);
							log
									.debug(
											"MyTask ["
													+ this.getTaskId()
													+ "] going to get remote file service from a foreign submission manager that owns the task ["
													+ remote_task
															.getTaskId()
													+ "]: " + smr);
							buffer = smr.downloadFileFromGridTask(
									remote_task.getTaskId(), filepath);
						} else // senão baixe o arquivo de forma
							   // convencional
						{
							buffer = remote_task.downloadFile(filepath);
						}
						gridfileservice.uploadFile(buffer, filepath);
						log.debug("MyTask Task[" + this.getTaskId()
								+ "] upload remote file[" + filepath
								+ "] upload remote file to to GridTask.");
						break;
					} catch (ConnectException e) {
						recoverConnectException(filepath, remote_task, retry, e);
					}
				}
			} else {
				//int retry = 0;
				retry=0;

				//while (true) {
				//TODO: Ver ser o while abaixo eh necessario.


				while (retry <= Task.MAX_RETRY_TIMES) 
				{
					//if ((smr==null) || ( !((SubmissionManager)smr).ID.URLFileExists(filepath) )) //VDN + PKVM 2006/02/06
					if( !SubmissionManager.ID.URLFileExists(filepath) )//VDN
	//				if(true)//VDN
					{
						log.debug("MyTask - URLFileExists retornou false - retry="+retry+" task="+this.getName());
						
						if( (filepath.indexOf("http") != -1) || (filepath.indexOf("ftp") != -1))
						{
							if( downloadFileFromURL( filepath ) ) {
								storeCurrentURL( filepath ); //PKVM inclui if...
								break;
							}
						}
						else
						{
							log.debug("MyTask - URLFileExists retornou true - retry="+retry+" task="+this.getName());

							if( copyFileFromDir(filepath) ) {
                                storeCurrentURL( filepath );
								break;
							}
						}
						
					
					} else {
//						log.debug("Download de Arquivo ja Feito, copiando de "+SubmissionManager.ID.getLocalPathFromURL(filepath));
						if (copyFileFromDir(SubmissionManager.ID.getLocalPathFromURL(filepath)))
							break;
					}
					
					//ImproveDownload.setLastFilePath(filepath);
					//ImproveDownload.setLocalCopyFilePath(????)
				}
			}
		}
	}

	/**
	 * PKVM 2006/02/06: method included to allow ImproveDownload to update its data structures
	 */
	private void storeCurrentURL(String filepath) {
		String localfile = filepath.substring(filepath
				.lastIndexOf("/") + 1);
		
		String dir = "";
		try {
			dir = gridfileservice.getDefaultDir()+"/"+localfile;
			SubmissionManager.ID.setLastURLFilePath(filepath, dir);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		log.debug("MyTask: Added "+filepath+" to DowloadImprove");

	}

	/**
	 * 
	 * @param h
	 * @throws RemoteException
	 * @throws MalformedURLException
	 * 
	 * If download was done then return true else return false.
	 * 
	 * Refactory by VDN
	 */
	
	private boolean downloadFileFromURL(String filepath) throws RemoteException, MalformedURLException{
		boolean noError = false;
		
		try {
			log.debug("MyTask  [" + this.getTaskId()
					+ "] try to install web files");
			URL fileurl = new URL(filepath); 							
			String localfile = filepath.substring(filepath
					.lastIndexOf("/") + 1);
			// TODO: Alterar aqui - fazer os downloads passarem
			// pela maquina de submissao
			// com otimização de arquivos já baixados
			gridfileservice.installURLFile(filepath, localfile,
					false);
			//numDowloads++;
			//log.debug("\t[VIND]NUMERO DE DOWNLOADS: "
			//		+ numDowloads);
			log.debug("Mytask [" + this.getTaskId()
					+ "] GridTask installURLFile Sucess OK: "
					+ filepath + " -> " + localfile);
			noError = true;
		} catch (MalformedURLException eurl) {
			gridtaskremote.setDie();
			log.error("Mytask [" + this.getTaskId()
					+ "]  installURLFile Error:" + eurl, eurl);
			log.error("Mytask [" + this.getTaskId()
					+ "]  installURLFile URL[" + filepath
					+ "] Error", eurl);
			throw eurl;
		} catch (ConnectException ec) {
			log.error("Mytask [" + this.getTaskId()
					+ "] installURLFile Error:" + ec, ec);
			log.error("Mytask [" + this.getTaskId()
					+ "] installURLFile RETRY:" + retry, ec);
			retry++;
			throw ec;
		} catch (RemoteException ec) {
			log.error("Mytask [" + this.getTaskId()
					+ "] installURLFile Error:" + ec, ec);
			log.error("Mytask [" + this.getTaskId()
					+ "] installURLFile RETRY:" + retry, ec);
			retry++;
			throw ec;
		}
		
		return noError;
		
	}
	
	
	/**
	 * 
	 * @param h
	 * @throws RemoteException
	 */
	
	
	private boolean copyFileFromDir( String filepath ) throws MalformedURLException, RemoteException{
		boolean sucess = false;
		
		try {
			log.debug("MyTask  [" + this.getTaskId()
					+ "] try to install web files");
			//URL fileurl = new URL(filepath); 							
			String localfile = filepath.substring(filepath
					.lastIndexOf("/") + 1);
			// TODO: Alterar aqui - fazer os downloads passarem
			// pela maquina de submissao
			// com otimização de arquivos já baixados
			gridfileservice.installURLFile(filepath, localfile,
					false);
			//numDowloads++;
			//log.debug("\t[VIND]NUMERO DE DOWNLOADS: "
			//		+ numDowloads);
			log.debug("Mytask [" + this.getTaskId()
					+ "] GridTask installURLFile Sucess OK: "
					+ filepath + " -> " + localfile);
			sucess = true;
		} catch (ConnectException ec) {
			log.error("Mytask [" + this.getTaskId()
					+ "] installURLFile Error:" + ec, ec);
			log.error("Mytask [" + this.getTaskId()
					+ "] installURLFile RETRY:" + retry, ec);
			retry++;
			throw ec;
		} catch (RemoteException ec) {
			log.error("Mytask [" + this.getTaskId()
					+ "] installURLFile Error:" + ec, ec);
			log.error("Mytask [" + this.getTaskId()
					+ "] installURLFile RETRY:" + retry, ec);
			retry++;
			throw ec;
		}
		
		return sucess;
	}
	
	/**
	 * @param h
	 */
	private void createGridFileService(ObjectId h) {
		gridfileservice = (GridFileServiceRemote) GeneralObjectActivator
				.getRemoteObjectReference(h, GridFileServiceRemote.class,
						"GridFileServiceRemote");
		setRemoteGridTaskFileService(gridfileservice);
		log.debug("MyTask [" + this.getTaskId()
				+ "] remote GridFileServiceRemote created");
	}

	/**
	 * @param filepath
	 * @param remote_task
	 * @param retry
	 * @param e
	 * @throws ConnectException
	 * @throws InterruptedException
	 */
	private void recoverConnectException(String filepath, MyTask remote_task, int retry, ConnectException e) throws ConnectException, InterruptedException {
		if (retry > 10) // tenta 10 vezes no maximo
		{
			/*
			 * Tolerância a Falhas Se a tarefa remota que
			 * possui o arquivo de dependência falhar, então
			 * aborta esta execução e seta a tarefa que
			 * falhou como DEPENDENT e seta o estado dos
			 * arquivos da tarefa que falhou como não
			 * existentes
			 */
			log.warn("Tolerância a Falhas - " + e, e);
			remote_task.getFiles().setAllOutputFileAsNotExist();
			remote_task.setState(TaskState.getInstance(TaskState.TASK_DEPENDENT));
			log.debug("Task setting state: " + remote_task.getState().getName());
			//this.setTaskState(Task.TASK_DEPENDENT);
			log
					.debug(
							"MyTask Task ["
									+ remote_task
											.getTaskId()
									+ "] Error while transfering dependent files ["
									+ filepath
									+ "] Trying to put the task as DEPENDENT state again.");
			log
					.debug(
							"MyTask Task ["
									+ this.getTaskId()
									+ "] - Error, the dependent files are lost! This task is in DEPENDENTstate again.");
			throw e;
		} else {
			log
					.debug(
							"MyTask Task ["
									+ this.getTaskId()
									+ "] - Error, the dependent files are lost! RETRY: "
									+ retry);
			Thread.sleep(10000);
			AppManUtil.exitApplication();
			// 												((Executor)
			// Exehda.getService(Executor.SERVICE_NAME)).exitApplication();
		}
	}

	/**
	 * @return
	 * @throws RemoteException
	 */
	private GeneralObjectActivator createGridTask(String where) throws RemoteException {
		GridSchedule sched = GridSchedule.getInstance();
		AppManUtil.getExecutor().setHeuristic(sched);
		ApplicationId aid = AppManUtil.getExecutor().currentApplication();

		log.debug("MyTask [" + this.getTaskId()
				+ "] create remote GridTask in Cell");

		GeneralObjectActivator activator = new GeneralObjectActivator(
				"GridTask", new Class[] { GridTaskRemote.class,
						GridFileServiceRemote.class }, new String[] {
						"GridTaskRemote", "GridFileServiceRemote" }, true);

		log.debug("MyTask [" + this.getTaskId()
				+ "] Activator remote GridTask: " + activator);

		return activator;
	}

	/**
	 * @param where
	 * @param activator
	 * @throws RemoteException
	 */
	private ObjectId createObjectId(String where, GeneralObjectActivator activator) throws RemoteException {
		ObjectId h = AppManUtil.getExecutor().createObject(
				GridTask.class,
				new Object[] { (Task) this, this.getCommandLine(),
						this.getName() }, // a seed para o serviço
										  // GridFileService é o nome da
										  // tarefa
				activator, where);

		log.debug("MyTask [" + this.getTaskId()
				+ "] remote GridTaskRemote created: " + h);

		// if h is null, so get some error in the remote object
		if (h == null) {
			log.error("MyTask [" + this.getTaskId()
					+ "] remote GridTaskRemote created FAILED: " + h);
			RemoteException e = new RemoteException(
					"\nExehda Create remote object failed!");
			throw e;
		}

		log.debug("MyTask [" + this.getTaskId()
				+ "] remote reference GridTaskRemote looking.");
		gridtaskremote = (GridTaskRemote) GeneralObjectActivator
				.getRemoteObjectReference(h, GridTaskRemote.class,
						"GridTaskRemote");
		log.debug("MyTask [" + this.getTaskId()
				+ "] remote reference GridTaskRemote created: "
				+ gridtaskremote);
		
		return h;
	}

	public void setToDie() {
		try {
			log.debug("Mytask Task [" + this.getTaskId() + "]  SET TO DIE ["
					+ this.getRetryTimes() + "]");
			if (gridtaskremote != null)
				gridtaskremote.setDie();
		} catch (RemoteException e) {
			log.error("Mytask Task [" + this.getTaskId() + "]  SET TO DIE ["
					+ this.getRetryTimes() + "] FATAL Error:" + e, e);
		}
	}

	/*
	 * Esta implementaçao do downloadFile é necessária para efetuar o correto
	 * download dos arquivos presentes na máquina remota, onde o objeto GridTask
	 * foi criado Assim, os arquivos são baixados da máquina remota, para a
	 * máquina de submissão por RMI
	 */
	public byte[] downloadFile(String filepath) throws RemoteException {
		try {
			if (getRemoteGridTaskFileService() != null) {
				GridFileServiceRemote rfs = getRemoteGridTaskFileService();
				return rfs.downloadFile(filepath);
			} else
				throw new RemoteException("Remote grid file service Error!");
		} catch (RemoteException e1) {
			throw e1;
		} catch (Exception e2) {
			AppManUtil.exitApplication("Mytask Task [" + this.getTaskId()
					+ "]  RETRY [" + this.getRetryTimes() + "] Erro:" + e2, e2);
			// 		   Debug.debug("Mytask Task ["+this.getTaskId()+"] RETRY
			// ["+this.getRetryTimes()+"] Erro:" + e2, true);
			// 		   e2.printStackTrace();
			// 		   ((Executor)
			// Exehda.getService(Executor.SERVICE_NAME)).exitApplication();
		}

		return null;
	}
}
