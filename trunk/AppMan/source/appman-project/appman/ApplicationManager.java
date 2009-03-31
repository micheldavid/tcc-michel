/*
 * Created on 28/05/2004
 * 
 */
package appman;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.isam.exehda.ObjectId;

import appman.clustering.ClusteringPhase;
import appman.clustering.DAG_DSC;
import appman.parser.ApplicationDescription;
import appman.parser.SimpleParser;
import edu.berkeley.guir.prefuse.Display;
import edu.berkeley.guir.prefusex.force.DragForce;
import edu.berkeley.guir.prefusex.force.ForceSimulator;
import edu.berkeley.guir.prefusex.force.NBodyForce;
import edu.berkeley.guir.prefusex.force.SpringForce;

/**
 * @author lucasa@gmail.com
 * @author rbrosinha (200611)
 */
public class ApplicationManager implements Runnable, ApplicationManagerRemote, Serializable {
	private static final long serialVersionUID = -1484662542541018941L;

	static final int ApplicationManager_EXECUTING = 1;

	static final int ApplicationManager_FINAL = 2;

	static final int ApplicationManager_READY = 0;

	private int graphIdGenerator = 0;

	private ApplicationDescription appDescription;

	private String appmanId; // id

	protected ClusteringPhase cp;

	private long downloadTimeOfSM = 0;

	private List graphs; // List of graphs

	private String myContactAddress = null; // this object contact address

	private List newgraphs; // List of new graphs

	private boolean runsystem; // to begin to send graphs to remotes

	private int scheduleLoop = 0; // to schedule submission managers at circle

	private int state = ApplicationManager_READY;

	private int submanId = 0; // to create new IDs

	private List smList;

	public ApplicationManager() {
		this("Default Application Manager");
	}

	public ApplicationManager(String id) {
		appmanId = new String(id);
		smList = new Vector();
		graphs = new Vector();
		newgraphs = new Vector();
		runsystem = false;
		state = ApplicationManager_READY;
	}

	public void addApplicationDescription(byte[] filedata) throws Exception {
		Debug.log(this + "\tAdding application description byte array.");
		Debug.log(this + "\tStarting application description downloading and parsing.");
		GridFileService fileservice = new GridFileService(this.appmanId);
		String name = "graph.dag";
		fileservice.uploadFile(filedata, name);
		File file = fileservice.getFile(name);
		String[] args = new String[1];
		args[0] = file.getAbsolutePath();
		ApplicationDescription appdesc = SimpleParser.parseGRIDADL(args);
		Debug.log(this + "\tApplication description downloading and parsing done.");

		Debug.log(this + "\tStarting application clustering.");
		cp = ((DAG_DSC) appdesc.getDAG()).getCP();

		Vector clusterP = cp.getCluster();
		int nclusters = cp.getNumberOfLevels();

		String[] clusters = new String[nclusters];
		for (int i = 0; i < nclusters; i++) {
			clusters[i] = "cluster[" + String.valueOf(i) + "]";
		}

		GraphGenerator.clusteringPhaseAlgorithm(clusterP, clusters, appdesc);// VDN
		Debug.log(this + "\t Number of generated clusters " + clusters.length + ".");
		String graph_name[] = new String[nclusters];
		for (int j = 0; j < clusters.length; j++) {
			graph_name[j] = "grafo[" + String.valueOf(graphIdGenerator++) + "]";
			addApplicationDescription(graph_name[j], clusters[j], appdesc);
		}

		for (Iterator iter = newgraphs.iterator(); iter.hasNext();) {
			Graph g = (Graph) iter.next();
			Debug.log(this + "\tChecking graph " + g.getGraphId() + ".");
			List l = g.getTaskList();
			for (Iterator iterator = l.iterator(); iterator.hasNext();) {
				Task t = (Task) iterator.next();
				Debug.log(this + "\tChecking task [" + t + "]" + t.getTaskId() + ".");
			}
		}

		Debug.log(this + "\tApplication clustering done.");
	}

	public void addApplicationDescription(String graphId, String clusterId, ApplicationDescription appdesc) {
		Debug.log(this + "\tAdding application description " + appdesc + ", graph " + graphId + " and cluster " + clusterId + ".");
		addGraph(new Graph(graphId, clusterId, appdesc));
	}

	public synchronized void addApplicationDescriptionRemote(byte[] filedata) throws RemoteException {
		try {
			addApplicationDescription(filedata);
		} catch (Exception e) {
			throw new RemoteException(e.toString());
		}
	}

	public synchronized void addApplicationDescriptionRemote(String graphId, String clusterId, ApplicationDescription appdesc) throws RemoteException {
		addApplicationDescription(graphId, clusterId, appdesc);
	}

	public void addGraph(Graph g) {
		synchronized (newgraphs) {
			newgraphs.add(g);
		}
		Debug.debug("ApplicationManager add a new graph: " + g.getGraphId(), true);
	}

	public synchronized void addGraphRemote(Graph g) throws RemoteException {
		addGraph(g);
	}

	// VDN:27/1/2006
	public long calculateDownloadTimeNow() throws RemoteException {
		long plus = 0;

		for (int i = 0; i < smList.size(); i++) {
			plus += ((SubmissionManagerRemote) smList.get(i)).getDownloadTimeOfTasksManagers();
		}

		downloadTimeOfSM = plus;

		return plus;
	}

	public void computeApplicationExecutionTimes() {
		String file_path = "tasks-execution-" + this.appmanId + ".trace";
		// Debug.newDebugFile("TASK\tERROR-RETRY-TIMES\tCREATED-TIME\tSUBMITED-TIME\tSTARTED_TIME\tFINISHED_TIME\tEXECUTION-TIME",
		// file_path);
		for (int i = 0; i < graphs.size(); i++) {
			Graph g = (Graph) graphs.get(i);
			Vector tasks = g.getTaskList();
			// Debug.debug("ApplicationManager begin time: " + time_begin, true);
			for (int j = 0; j < tasks.size(); j++) {
				Task t = (Task) tasks.get(j);
				long time = t.getTimeEnd() - t.getTimeStart();
				Debug.debug("ApplicationManager task " + t.getTaskId() + " submit time: " + t.getTimeSubmit(), true);
				// Debug.debugToFile("\n"+t.getTaskId() + "\t"+t.getRetryTimes()+ "\t"+
				// (t.getTimeTaskCreated()) + "\t"+
				// ((float)((t.getTimeSubmit()-time_begin)/1000)) + "\t"+
				// ((float)((t.getTimeStart()-time_begin)/1000))
				// +"\t"+(float)(time/1000), file_path, true);
				// Debug.debugToFile("\n"+t.getTaskId() + "\t"+t.getRetryTimes()+ "\t"+
				// (t.getTimeTaskCreated()) + "\t"+ (t.getTimeSubmit()) + "\t"+
				// ((float)((t.getTimeStart()-time_begin)/1000))
				// +"\t"+(float)(time/1000), file_path, true);
				Debug.debugToFile("\n" + t.getTaskId() + "\t" + t.getRetryTimes() + "\t" + (t.getTimeTaskCreated()) + "\t" + (t.getTimeSubmit()) + "\t" + (t.getTimeTaskStart()) + "\t" + (t.getTimeTaskEnd()) + "\t" + (float) (time) + "\t" + (float) (time / 1000), file_path, true);
				Debug.debugToFile(t.getMySubmissionManagerRemoteContactAddress() + "\n", file_path, true);

			}
		}
	}

	private SubmissionManagerRemote createNewSubmissionManager(String ignoredSubId) throws RemoteException {
		SubmissionManagerRemote smRemote = null;
		String subId = String.valueOf(++submanId);
		// String subId = ignoredSubId;
		Debug.debug(this + "\tCreating new submission manager using id " + subId + " and ignoring passed id " + ignoredSubId + ".");
		try {
			AppManUtil.getExecutor().setHeuristic(GridSchedule.getInstance());
			smRemote = createNewSubmissionManagerObject(subId);
		} catch (Exception e) {
			RemoteException re = new RemoteException("Exception creating submission manager");
			re.fillInStackTrace();
			throw re;
		}
		return smRemote;
	}

	private SubmissionManagerRemote createNewSubmissionManagerObject(String smId) {
		SubmissionManagerRemote smRemote = null;
		try {
			Debug.log(this + "\tCreating a new submission manager object.");
			GeneralObjectActivator activator = new GeneralObjectActivator("SubmissionManager", new Class[] { SubmissionManagerRemote.class }, new String[] { "SubmissionManagerRemote" }, true);
			ObjectId oxID = AppManUtil.getExecutor().createObject(SubmissionManager.class, new Object[] { smId, myContactAddress }, activator, new String("grid.targetHosts.submissionmanagers.hosts"));
			smRemote = (SubmissionManagerRemote) GeneralObjectActivator.getRemoteObjectReference(oxID, SubmissionManagerRemote.class, "SubmissionManagerRemote");
			smRemote.setMyObjectRemoteContactAddress(activator.getContactAddress(oxID, "SubmissionManagerRemote"));
		} catch (Exception e) {
			Debug.log("Exception", e);
		}
		return smRemote;
	}

	// VDN
	public ApplicationDescription getApplicationDescription() throws RemoteException {
		appDescription = appman.parser.SimpleParser.appDescription;
		return appDescription;
	}

	public int getApplicationState() {
		return state;
	}

	public float getApplicationStatePercentCompleted() {
		synchronized (graphs) {
			int n = graphs.size();
			float sum = 0;
			for (int i = 0; i < graphs.size(); i++) {
				sum += ((Graph) graphs.get(i)).getStatePercentCompleted();
			}
			if (n > 0)
				return sum / n;
			else
				return 0;
		}
	}

	public float getApplicationStatePercentCompletedRemote() throws RemoteException {
		return getApplicationStatePercentCompleted();
	}

	public int getApplicationStateRemote() throws RemoteException {
		return getApplicationState();
	}

	// VDN:27/1/2006
	public long getDownloadTimeOfSM() throws RemoteException {
		return downloadTimeOfSM;
	}

	public synchronized Graph getGraph(String graphId) {
		// synchronized (graphs)
		// {
		for (int i = 0; i < graphs.size(); i++) {
			if (((Graph) graphs.get(i)).getGraphId().compareTo(graphId) == 0) {
				return (Graph) graphs.get(i);
			}
		}
		// }

		// synchronized(newgraphs)
		// {
		for (int i = 0; i < newgraphs.size(); i++) {
			if (((Graph) newgraphs.get(i)).getGraphId().compareTo(graphId) == 0) {
				return (Graph) newgraphs.get(i);
			}
		}
		// }

		return null;
	}

	public String getInfo() throws RemoteException {
		String str = "";
		str += "\n------------ Application Manager [" + appmanId + "]-------------.";
		str += "\nApplication Manager execution status: " + getApplicationStatePercentCompleted() * 100 + " %";
		for (int i = 0; i < smList.size(); i++) {
			SubmissionManagerRemote smr = (SubmissionManagerRemote) smList.get(i);
			str += "\nApplicationManager manage Submission Manager: " + smr.getSubmissionManagerIdRemote();
		}
		for (int j = 0; j < graphs.size(); j++) {
			Graph g = (Graph) graphs.get(j);
			str += "\nApplicationManager execute Graph [" + g.getGraphId() + "] by Submission Manager [" + g.getSubmissionManagerId() + "]";
			str += "\nGraph [" + g.getGraphId() + "] execution status: " + g.getStatePercentCompleted();
		}
		str += "\n-------------------------";
		return str;
	}

	public synchronized String getInfoRemote() throws RemoteException {
		return getInfo();
	}

	public synchronized SubmissionManagerRemote getSubmissionManagerRemote(String subId) throws RemoteException {
		for (int i = 0; i < smList.size(); i++) {
			if (((SubmissionManagerRemote) smList.get(i)).getSubmissionManagerIdRemote().compareTo(subId) == 0) {
				return (SubmissionManagerRemote) smList.get(i);
			}
		}
		return null;
	}

	/*
	 * Retorna uma refer�ncia remota do servi�o de transfer�ncia de arquivos de
	 * uma tarefa
	 */
	public String getTaskGridFileServiceContactAddressRemote(String taskId) throws RemoteException {
		for (int i = 0; i < graphs.size(); i++) {
			Graph local = (Graph) graphs.get(i);
			Task t = local.getTask(taskId);
			if (t != null) {
				try {
					SubmissionManagerRemote sm = getSubmissionManagerRemote(local.getSubmissionManagerId());
					String gfsr = sm.getMyObjectRemoteContactAddress();
					return gfsr;
				} catch (RemoteException e) {
					Debug.debug(e);
					e.printStackTrace();
					throw e;
				}
			}
		}
		return null;
	}

	public synchronized boolean isTaskOutputsRemoteAvailable(String taskId) throws RemoteException, java.net.SocketException {
		// Debug.debug("ApplicationManager isDisponibleTaskOutputsRemote locating
		// task ["+taskId+"] in "+graphs.size()+" graphs");
		for (int i = 0; i < graphs.size(); i++) {
			Graph g = (Graph) graphs.get(i);
			Task t = g.getTask(taskId);
			// Debug.debug("ApplicationManager isDisponibleTaskOutputsRemote looking
			// for task ["+taskId+"] in graph ["+g.getGraphId()+"]");
			if (t != null) {
				if (t.getTaskState() == Task.TASK_FINAL) {
					Debug.debug("ApplicationManager isTaskOutputsRemoteAvailable task [" + t.getTaskId() + "] status: " + t.getTaskStateString());
					return true;
				}
			}
		}
		return false;
	}

	public void run() {
		Debug.log(this + "\tRun.");
		float percentCompleted = 0;

		while (this.getApplicationStatePercentCompleted() < 1) {
			Debug.log(this + "\tPercent completed: " + this.getApplicationStatePercentCompleted() + ".");
			if (runsystem == true) {
				synchronized (newgraphs) {
					Debug.log(this + "\tStarting polling.");
					while (!newgraphs.isEmpty()) {
						Graph graph = (Graph) newgraphs.get(newgraphs.size() - 1);
						try {
							Debug.log(this + "\tAssigning graph " + graph.getGraphId() + " to a submission manager.");
							scheduleGraph(graph);
							Debug.log(this + "\tAssigned graph " + graph.getGraphId() + " to submission manager " + graph.getSubmissionManagerId() + ".");
							synchronized (graphs) {
								graphs.add(graph);
								Debug.log(this + "\tAdded graph " + graph.getGraphId() + " to the graphs list.");
							}
						} catch (Exception e) {
							Debug.log("Exception", e);
						}
						newgraphs.remove(graph);
					}
				}

				for (Iterator iter = graphs.iterator(); iter.hasNext();) {
					Graph graph = (Graph) iter.next();
					Debug.log(this + "\tStarting update of graph " + graph.getGraphId() + ".");
					SubmissionManagerRemote smRemote = null;
					Graph graphRemote = null;
					if (graph.getStatePercentCompleted() < 1) {
						try {
							Debug.log(this + "\tContacting submission manager " + graph.getSubmissionManagerId() + " to update graph " + graph.getGraphId() + ".");
							smRemote = getSubmissionManagerRemote(graph.getSubmissionManagerId());
							graphRemote = smRemote.getGraphRemote(graph.getGraphId());
						} catch (RemoteException e) {
							// Tolerancia a Falhas
							// Se o Submission Manager nao responder entao remove o grafo da
							// lista e adiciona o grafo novamente no Application Manager com
							// um novo SubMan escalonado
							Debug.log(e.getMessage(), e);
							try {
								Debug.log(this + "\tUnable to get remote graph from submission manager " + graph.getSubmissionManagerId() + ".");
								scheduleGraph(graph);
								addGraph(graph); // RBR: pq adicionar de novo???
							} catch (Exception e2) {
								Debug.log(e2.getMessage(), e2);
								AppManUtil.exitApplication("Failed to reschedule graph " + graph + " to a submission manager.", e2);
							}
						}

						if (graphRemote != null) {
							try {
								Debug.log(this + "\tRetrieved graph " + graphRemote.getGraphId() + " from remote submission manager " + graphRemote.getSubmissionManagerId() + ".");
								if (graphRemote.getStatePercentCompleted() != graph.getStatePercentCompleted()) {
									updateGraph(graph, graphRemote);
								}
							} catch (Exception e) {
								AppManUtil.exitApplication("Failed to update graph " + graph + ".", e);
							}
						}
					} else {
						Debug.log(this + "\tGraph " + graph.getGraphId() + " is already executed.");
					}
					Debug.log(this + "\tUpdate of graph " + graph.getGraphId() + " done.");
				} // fim for
			} // end if

			if (percentCompleted < getApplicationStatePercentCompleted()) {
				percentCompleted = getApplicationStatePercentCompleted();
				Debug.debug("ApplicationManager Application graphs completed: " + getApplicationStatePercentCompleted(), true);
			}

			try {
				Thread.sleep(5000);
			} catch (Exception e) {
				Debug.log("Exception", e);
			}

		} // end while

		Debug.log(this + "\tExecution completed.");

		state = ApplicationManager_FINAL;

		computeApplicationExecutionTimes();
		// Debug.debug("ApplicationManager cleaning Application Files! ", true);
		Debug.debug("ApplicationManager set all Submission Managers TO DIE! ", true);
		setAllSubmissionManagersToDie();

		// VDN
		// time_execution = System.currentTimeMillis() - time_execution;
		// Debug.debug("ApplicationManager Application completed time: " + (float)
		// time_execution / 1000 + " seconds", true);
		appDescription = appman.parser.SimpleParser.appDescription;
		FileWriter parserOut;

		try {
			parserOut = new FileWriter("parseOut.txt", true);
			// parserOut.write("Scheduler Time(ms): " + ((float) time_schedule_total)
			// + "\n");
			// parserOut.write("Scheduler Time2(ms):
			// "+((float)time_schedule_total2)+"\n");
			parserOut.write("Download Time(s): " + ((long) getDownloadTimeOfSM() / 1000) + "\n");
			// parserOut.write("Total Time(s): " + ((float) time_execution / 1000) +
			// "\n");
			parserOut.close();
			// appDescription.grappaOut.close(); VDN:4/1/06

		} catch (IOException e) {
			Debug.log("[APPMAN-ApplicationManager.java]: " + e);
		}

		// AppManUtil.exitApplication();
		// ((Executor) Exehda.getService(Executor.SERVICE_NAME)).exitApplication();
	}// end run

	// private SubmissionManagerRemote scheduleSubmissionManager() {
	// return scheduleSubmissionManager("");
	// }

	// VDN: 13/01/2006
	// todo metodo ficou como sync, mas o q deveria ser sync era o acesso ao
	// submissionmanagerList
	private synchronized SubmissionManagerRemote scheduleSubmissionManager(String subId) {
		int i = 0;
		SubmissionManagerRemote smRemote = null;

		try {
			if (smList.size() == 0) {
				if (subId.equalsIgnoreCase("")) {
					Debug.debug("ApplicationManager need to create a new SubmissionManager: " + String.valueOf(submanId), true);
					createNewSubmissionManager(String.valueOf(submanId));
					submanId++;
				} else {
					Debug.debug("ApplicationManager need to create a new SubmissionManager: " + subId, true);
					createNewSubmissionManager(subId);
				}
			}

			if (subId.equalsIgnoreCase("") == false) {
				if (getSubmissionManagerRemote(subId) != null) {
					smRemote = getSubmissionManagerRemote(subId);
				} else {
					createNewSubmissionManager(subId);
					smRemote = getSubmissionManagerRemote(subId);
				}
			} else {
				i = scheduleLoop % smList.size();
				scheduleLoop++;
				smRemote = (SubmissionManagerRemote) smList.get(i);
			}

			Debug.debug("ApplicationManager scheduling a SubmissionManager", true);
			smRemote.getIsAliveRemote();
			Debug.debug("ApplicationManager scheduled the SubmissionManager [" + smRemote.getSubmissionManagerIdRemote() + "]", true);

		} catch (RemoteException e) {
			/*
			 * Toler�ncia a Falhas se o Submission Manager remoto escolhido n�o
			 * responder ao ping isAlive, ent�o remove este da lista e recursivamente
			 * realiza outro escalonamento
			 */
			Debug.debug("Tolerancia a Falhas - " + e);
			Debug.debug("ApplicationManager Scheduling SubmissionManager ERROR, removing fault SubmissionManager from the list");
			smList.remove(smRemote);
			smRemote = scheduleSubmissionManager("NO-ID");
		}

		return smRemote;
	}

	/**
	 * Replaces scheduleSubmissionManager.
	 * 
	 * @param graph
	 *          graph to be scheduled
	 * @return submission manager remote reference where this graph was
	 *         scheduled/assigned
	 */
	private synchronized void scheduleGraph(Graph graph) throws RemoteException {
		SubmissionManagerRemote smRemote = null;
		String graphId = graph.getGraphId();
		try {
			Debug.log(this + "\tScheduling graph " + graphId + ".");

			// if (smList.isEmpty()) {
			// Debug.log(this + "\tCreating a new submission manager because
			// the list is empty.");
			// smRemote = createNewSubmissionManager(graphId);
			// smList.add(smRemote);
			// } else {
			// int i = scheduleLoop % smList.size();
			// scheduleLoop++;
			// smRemote = (SubmissionManagerRemote) smList.get(i);
			// }

			if (smList.size() == 0) {
				if (graphId.equalsIgnoreCase("")) {
					Debug.log(this + "\tNeed to create a new submission manager " + String.valueOf(submanId) + " because the list is empty and graph id is empty.");
					smRemote = createNewSubmissionManager(String.valueOf(submanId));
					smList.add(smRemote);
					submanId++;
				} else {
					Debug.log(this + "\tNeed to create a new submission manager " + graphId + " because the list is empty and graph id is not empty.");
					smRemote = createNewSubmissionManager(graphId);
					smList.add(smRemote);
				}
			}

			if (!graphId.equalsIgnoreCase("")) {
				smRemote = getSubmissionManagerRemote(graphId);
				if (smRemote == null) {
					smRemote = createNewSubmissionManager(graphId);
					smList.add(smRemote);
				}
			} else {
				int i = scheduleLoop % smList.size();
				scheduleLoop++;
				smRemote = (SubmissionManagerRemote) smList.get(i);
			}

			Debug.debug("ApplicationManager scheduling a SubmissionManager", true);
			smRemote.getIsAliveRemote();
			Debug.debug("ApplicationManager scheduled the SubmissionManager [" + smRemote.getSubmissionManagerIdRemote() + "]", true);

			graph.setSubmissionManagerId(smRemote.getSubmissionManagerIdRemote());
			smRemote.addGraphRemote(graph);

		} catch (RemoteException e) {
			/*
			 * Tolerancia a Falhas se o Submission Manager remoto escolhido nao
			 * responder ao ping isAlive, entao remove este da lista e recursivamente
			 * realiza outro escalonamento
			 */
			Debug.debug("Tolerancia a Falhas - " + e);
			Debug.debug("ApplicationManager Scheduling SubmissionManager ERROR, removing fault SubmissionManager from the list");
			smList.remove(smRemote);
			scheduleGraph(graph);
		}
	}

	public void setAllSubmissionManagersToDie() {
		Debug.log("[AM] SET ALL AM TO DIE: ");

		long plus = 0;
		for (int i = 0; i < smList.size(); i++) {
			try {
				((SubmissionManagerRemote) smList.get(i)).setDieRemote();
				plus += ((SubmissionManagerRemote) smList.get(i)).getDownloadTimeOfTasksManagers();
				Debug.debug("[AM " + i + "] TIME DOWNLOAD: " + ((SubmissionManagerRemote) smList.get(i)).getDownloadTimeOfTasksManagers(), true);
			} catch (RemoteException e) {

			}
		}

		setDownloadTimeOfSM(plus);
	}

	// VDN:27/1/2006
	private void setDownloadTimeOfSM(long downloadTime) {
		downloadTimeOfSM = downloadTime;
	}

	public void setMyObjectContactAddress(String contact) {
		myContactAddress = contact;
	}

	public void setMyObjectContactAddressRemote(String contact) throws RemoteException {
		setMyObjectContactAddress(contact);
	}

	public Display startAppGUI(String graphId) {
		Debug.debug("ApplicatinManager GUI Interface loading...", true);
		while (this.getGraph(graphId) == null) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				Debug.debug(e, true);
				e.printStackTrace();
			}
		}

		Graph g = this.getGraph(graphId);

		Debug.debug("ApplicatinManager GUI Interface creating display for graph: " + g.PrintInfo(), true);

		ForceSimulator fsim = new ForceSimulator();
		fsim.addForce(new NBodyForce(-0.4f, -1f, 0.9f));
		fsim.addForce(new SpringForce(4E-5f, 75f));
		fsim.addForce(new DragForce(-0.005f));

		ForceDemo fdemo = new ForceDemo(g.getGraph(), fsim);
		fdemo.runDemo();

		return null;
	}

	public Display startAppGUIRemote(String graphId) throws RemoteException {
		return startAppGUI(graphId);
	}

	public void startApplicationManager() {
		// time_execution = System.currentTimeMillis();
		// time_begin = System.currentTimeMillis();
		runsystem = true;
		state = ApplicationManager_EXECUTING;
	}

	public void startApplicationManagerRemote() throws RemoteException {
		startApplicationManager();
	}

	public Display startDefaultAppGUI() {
		Debug.debug("ApplicatinManager Default GUI Interface loading...", true);

		ForceSimulator fsim = new ForceSimulator();
		fsim.addForce(new NBodyForce(-0.4f, -1f, 0.9f));
		fsim.addForce(new SpringForce(4E-5f, 75f));
		fsim.addForce(new DragForce(-0.005f));

		GraphGenerator generator = new GraphGenerator("");
		edu.berkeley.guir.prefuse.graph.Graph gt = generator.getRandomTreeDirected(3, 3, (float) 0.7);
		ForceDemo fdemo = new ForceDemo(gt, fsim);
		Debug.debug("ApplicatinManager GUI Interface creating display for DEFAULT graph: " + gt.getNodeCount() + " nodes, " + gt.getEdgeCount() + " edges", true);

		fdemo.runDemo();

		return null;
	}

	// atualiza os dados do grafo, usando as informa��es que vieram de um
	// submission manager
	// TODO: A fun��o updateGraph da classe Application Manager, necessita de
	// tratamento especial para os dados de outros submission managers
	public synchronized void updateGraph(Graph graphLocal, Graph graphRemote) {
		graphLocal.setDataFileList(graphRemote.getDataFileList());
		graphLocal.setTaskList(graphRemote.getTaskList());
		graphLocal.updateGraphNodesInternalData();
		Debug.log(this + "\tGraph [" + graphLocal.getGraphId() + "] updated, " + graphLocal.getStatePercentCompleted() + " execution completed.");
	}

}
