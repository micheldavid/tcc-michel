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
import appman.log.Debug;

//import org.isam.exehda.services.ObjectSeed.Activator;

/**
 * Refactored in 2006/01/10 by VDN and PKVM
 * 
 * @author lucasa
 *  
 */
public class MyTask extends Task implements Serializable {
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

		Debug.debug("Task " + this.getName() + " (" + this.getDescription()
				+ ") remote  executing.");

		try {
			GeneralObjectActivator activator = createGridTask(where);
			ObjectId h = createObjectId(where, activator);

			createGridFileService(h);
			
			//String contact_address = activator.getContactAddress(h,
			// "GridFileServiceRemote");
			//setRemoteGridFileServiceContactAddress(contact_address); //
			// guarda a refer�ncia do objeto remoto numa vari�vel da classe

			Debug.debug("MyTask [" + this.getTaskId()
					+ "] remote GridTask created", true);
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
			System.out.println("[VINDN] Download Time: "+(finalTime - initialTime));
			Debug.debug("[VINDN] Download Time: "+(finalTime - initialTime), true);
			
			Debug.debug("Mytask [" + this.getTaskId()
					+ "] SET GridTask to RUN:" + gridtaskremote, true);
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
                    Debug.debug("Failed to get remote task end status: "+e+". Will retry...");
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
				Debug.debug("Tentando obter novamente o gridtaskremote.getSuccess() - java.net.SocketException", true);
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
		Debug.debug("Mytask Task [" + this.getTaskId() + "]  RETRY ["
				+ this.getRetryTimes() + "]  - Success: OK", true);
		// if the task type is FINAL, then transfer the output files to
		// the user machine, to get the results
		if (this.getTaskType() == TaskType.TASK_TYPE_FINAL) {
			where = new String("grid.targetHosts.host-final-results"); // all
																	   // grid
																	   // resources
			Debug
					.debug(
							"MyTask ["
									+ this.getTaskId()
									+ "] create remote GridFileService in Cell to transfer the results",
							true);

			activator = new GeneralObjectActivator("GridFileService",
					new Class[] { GridFileServiceRemote.class },
					new String[] { "GridFileServiceRemote" }, false);

			Debug.debug("MyTask [" + this.getTaskId()
					+ "] Activator remote GridFileService: "
					+ activator, true);

			h = AppManUtil.getExecutor().createObject(
					GridFileService.class, new Object[] {}, // a seed
															// para o
															// servi�o
															// GridFileService
															// � o nome
															// da tarefa
					activator, where);

			Debug.debug("MyTask [" + this.getTaskId()
					+ "] remote GridFileService created: " + h, true);

			// if h is null, so get some error in the remote object
			if (h == null) {
				Debug.debug("MyTask [" + this.getTaskId()
						+ "] remote GridFileService created FAILED: "
						+ h, true);
				RemoteException e = new RemoteException(
						"\nExehda Create remote object failed!");
				throw e;
			}

			Debug
					.debug(
							"MyTask ["
									+ this.getTaskId()
									+ "] remote reference GridFileServiceRemote looking.",
							true);
			GridFileServiceRemote result_gridfileservice = (GridFileServiceRemote) GeneralObjectActivator
					.getRemoteObjectReference(h,
							GridFileServiceRemote.class,
							"GridFileServiceRemote");

			Debug.debug("MyTask [" + this.getTaskId()
					+ "] remote GridFileServiceRemote created", true);
			Debug
					.debug(
							"MyTask ["
									+ this.getTaskId()
									+ "] results going to be transfered to user machine!",
							true);
			DataFile[] results_datafile = this.getFiles().getOutputFiles();
			for (int i = 0; i < results_datafile.length; i++) {
				String filepath = results_datafile[i].getName();
				Debug.debug("Transfering file: " + filepath, true);
				result_gridfileservice.uploadFile(gridfileservice
						.downloadFile(filepath), filepath);
			}
			Debug.debug("MyTask [" + this.getTaskId()
					+ "] results transfered to user machine!", true);
		}
	}

	/**
	 * @throws RemoteException
	 * @throws ConnectException
	 * @throws InterruptedException
	 * @throws MalformedURLException
	 */
	private void downloadInputFiles() throws RemoteException, ConnectException, InterruptedException, MalformedURLException {

		Debug.debug("MyTask : starting method downloadInputFiles...",true);

		// TODO: neste metodo teria que verificar se esta em rede local, neste caso nao
		// precisaria transferir se estiver no NFS
		DataFile[] datafile = this.getFiles().getInputFiles();
		int numDowloads = 0;
		String lastFilePath = new String(); //VDN
		for (int i = 0; i < datafile.length; i++) {
			String filepath = datafile[i].getName();
			Debug.debug("File path: " + filepath);

			MyTask remote_task = (MyTask) datafile[i].getFromTask();
			if (remote_task != null) {
				byte[] buffer;
				//int retry = 0; VDN
				while (true) {
					try {
						Debug.debug("MyTask [" + this.getTaskId()
								+ "] try to install input files", true);
						// se a tarefa for estrangeira (de outro grafo)
						// ent�o baixe o arquivo usando a refer�ncia remota
						// do servi�o de arquivos do grid task
						if (remote_task.getState().getCode() == TaskState.TASK_FOREIGN_FINAL) {
							// esta refer�ncia remota foi atualizada pelo
							// submission manager <-- application manager
							// <-- task <-- grid task
							String contact_address_remote = remote_task
									.getSubmissionManagerContactAddress();
							SubmissionManagerRemote smr = (SubmissionManagerRemote) GeneralObjectActivator
									.getRemoteObjectReference(
											contact_address_remote,
											SubmissionManagerRemote.class);
							Debug
									.debug(
											"MyTask ["
													+ this.getTaskId()
													+ "] going to get remote file service from a foreign submission manager that owns the task ["
													+ remote_task
															.getTaskId()
													+ "]: " + smr, true);
							buffer = smr.downloadFileFromGridTask(
									remote_task.getTaskId(), filepath);
						} else // sen�o baixe o arquivo de forma
							   // convencional
						{
							buffer = remote_task.downloadFile(filepath);
						}
						gridfileservice.uploadFile(buffer, filepath);
						Debug.debug("MyTask Task[" + this.getTaskId()
								+ "] upload remote file[" + filepath
								+ "] upload remote file to to GridTask.",
								true);
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
						Debug.debug("MyTask - URLFileExists retornou false - retry="+retry+" task="+this.getName(),true);
						
						if( (filepath.indexOf("http") != -1) || (filepath.indexOf("ftp") != -1))
						{
							if( downloadFileFromURL( filepath ) ) {
								storeCurrentURL( filepath ); //PKVM inclui if...
								break;
							}
						}
						else
						{
							Debug.debug("MyTask - URLFileExists retornou true - retry="+retry+" task="+this.getName(),true);

							if( copyFileFromDir(filepath) ) {
                                storeCurrentURL( filepath );
								break;
							}
						}
						
					
					} else {
//						System.out.println("Download de Arquivo ja Feito, copiando de "+SubmissionManager.ID.getLocalPathFromURL(filepath));
//						Debug.debug("Download de Arquivo ja Feito, copiando de "+SubmissionManager.ID.getLocalPathFromURL(filepath), true);
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
		
		Debug.debug("MyTask: Added "+filepath+" to DowloadImprove",true);

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
			Debug.debug("MyTask  [" + this.getTaskId()
					+ "] try to install web files", true);
			URL fileurl = new URL(filepath); 							
			String localfile = filepath.substring(filepath
					.lastIndexOf("/") + 1);
			// TODO: Alterar aqui - fazer os downloads passarem
			// pela maquina de submissao
			// com otimiza��o de arquivos j� baixados
			gridfileservice.installURLFile(filepath, localfile,
					false);
			//numDowloads++;
			//System.out.println("\t[VIND]NUMERO DE DOWNLOADS: "
			//		+ numDowloads);
			Debug.debug("Mytask [" + this.getTaskId()
					+ "] GridTask installURLFile Sucess OK: "
					+ filepath + " -> " + localfile, true);
			noError = true;
		} catch (MalformedURLException eurl) {
			gridtaskremote.setDie();
			Debug.debug("Mytask [" + this.getTaskId()
					+ "]  installURLFile Error:" + eurl, true);
			Debug.debug("Mytask [" + this.getTaskId()
					+ "]  installURLFile URL[" + filepath
					+ "] Error", true);
			eurl.printStackTrace();
			throw eurl;
		} catch (ConnectException ec) {
			Debug.debug("Mytask [" + this.getTaskId()
					+ "] installURLFile Error:" + ec, true);
			Debug.debug("Mytask [" + this.getTaskId()
					+ "] installURLFile RETRY:" + retry, true);
			ec.printStackTrace();
			retry++;
			throw ec;
		} catch (RemoteException ec) {
			Debug.debug("Mytask [" + this.getTaskId()
					+ "] installURLFile Error:" + ec, true);
			Debug.debug("Mytask [" + this.getTaskId()
					+ "] installURLFile RETRY:" + retry, true);
			ec.printStackTrace();
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
			Debug.debug("MyTask  [" + this.getTaskId()
					+ "] try to install web files", true);
			//URL fileurl = new URL(filepath); 							
			String localfile = filepath.substring(filepath
					.lastIndexOf("/") + 1);
			// TODO: Alterar aqui - fazer os downloads passarem
			// pela maquina de submissao
			// com otimiza��o de arquivos j� baixados
			gridfileservice.installURLFile(filepath, localfile,
					false);
			//numDowloads++;
			//System.out.println("\t[VIND]NUMERO DE DOWNLOADS: "
			//		+ numDowloads);
			Debug.debug("Mytask [" + this.getTaskId()
					+ "] GridTask installURLFile Sucess OK: "
					+ filepath + " -> " + localfile, true);
			sucess = true;
		} catch (ConnectException ec) {
			Debug.debug("Mytask [" + this.getTaskId()
					+ "] installURLFile Error:" + ec, true);
			Debug.debug("Mytask [" + this.getTaskId()
					+ "] installURLFile RETRY:" + retry, true);
			ec.printStackTrace();
			retry++;
			throw ec;
		} catch (RemoteException ec) {
			Debug.debug("Mytask [" + this.getTaskId()
					+ "] installURLFile Error:" + ec, true);
			Debug.debug("Mytask [" + this.getTaskId()
					+ "] installURLFile RETRY:" + retry, true);
			ec.printStackTrace();
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
		Debug.debug("MyTask [" + this.getTaskId()
				+ "] remote GridFileServiceRemote created", true);
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
			 * Toler�ncia a Falhas Se a tarefa remota que
			 * possui o arquivo de depend�ncia falhar, ent�o
			 * aborta esta execu��o e seta a tarefa que
			 * falhou como DEPENDENT e seta o estado dos
			 * arquivos da tarefa que falhou como n�o
			 * existentes
			 */
			Debug.debug("Toler�ncia a Falhas - " + e, true);
			e.printStackTrace();
			remote_task.getFiles().setAllOutputFileAsNotExist();
			remote_task.setState(TaskState.getInstance(TaskState.TASK_DEPENDENT));
			Debug.debug("Task setting state: " + remote_task.getState().getName());
			//this.setTaskState(Task.TASK_DEPENDENT);
			Debug
					.debug(
							"MyTask Task ["
									+ remote_task
											.getTaskId()
									+ "] Error while transfering dependent files ["
									+ filepath
									+ "] Trying to put the task as DEPENDENT state again.",
							true);
			Debug
					.debug(
							"MyTask Task ["
									+ this.getTaskId()
									+ "] - Error, the dependent files are lost! This task is in DEPENDENTstate again.",
							true);
			throw e;
		} else {
			Debug
					.debug(
							"MyTask Task ["
									+ this.getTaskId()
									+ "] - Error, the dependent files are lost! RETRY: "
									+ retry, true);
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

		Debug.debug("MyTask [" + this.getTaskId()
				+ "] create remote GridTask in Cell", true);

		GeneralObjectActivator activator = new GeneralObjectActivator(
				"GridTask", new Class[] { GridTaskRemote.class,
						GridFileServiceRemote.class }, new String[] {
						"GridTaskRemote", "GridFileServiceRemote" }, true);

		Debug.debug("MyTask [" + this.getTaskId()
				+ "] Activator remote GridTask: " + activator, true);

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
						this.getName() }, // a seed para o servi�o
										  // GridFileService � o nome da
										  // tarefa
				activator, where);

		Debug.debug("MyTask [" + this.getTaskId()
				+ "] remote GridTaskRemote created: " + h, true);

		// if h is null, so get some error in the remote object
		if (h == null) {
			Debug.debug("MyTask [" + this.getTaskId()
					+ "] remote GridTaskRemote created FAILED: " + h, true);
			RemoteException e = new RemoteException(
					"\nExehda Create remote object failed!");
			throw e;
		}

		Debug.debug("MyTask [" + this.getTaskId()
				+ "] remote reference GridTaskRemote looking.", true);
		gridtaskremote = (GridTaskRemote) GeneralObjectActivator
				.getRemoteObjectReference(h, GridTaskRemote.class,
						"GridTaskRemote");
		Debug.debug("MyTask [" + this.getTaskId()
				+ "] remote reference GridTaskRemote created: "
				+ gridtaskremote, true);
		
		return h;
	}

	public void setToDie() {
		try {
			Debug.debug("Mytask Task [" + this.getTaskId() + "]  SET TO DIE ["
					+ this.getRetryTimes() + "]", true);
			if (gridtaskremote != null)
				gridtaskremote.setDie();
		} catch (RemoteException e) {
			Debug.debug("Mytask Task [" + this.getTaskId() + "]  SET TO DIE ["
					+ this.getRetryTimes() + "] FATAL Error:" + e, true);
			e.printStackTrace();
		}
	}

	/*
	 * Esta implementa�ao do downloadFile � necess�ria para efetuar o correto
	 * download dos arquivos presentes na m�quina remota, onde o objeto GridTask
	 * foi criado Assim, os arquivos s�o baixados da m�quina remota, para a
	 * m�quina de submiss�o por RMI
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
