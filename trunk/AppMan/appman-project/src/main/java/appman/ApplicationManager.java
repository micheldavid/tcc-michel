/*
 * Created on 28/05/2004
 */
package appman;

import java.io.File;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Random;
import java.util.Vector;

import org.isam.exehda.ApplicationId;
import org.isam.exehda.ObjectId;

import appman.clustering.ClusteringPhase;
import appman.clustering.DAG_DSC;
import appman.log.Debug;
import appman.parser.ApplicationDescription;
import appman.parser.SimpleParser;
import appman.task.Task;
import appman.task.TaskState;
import edu.berkeley.guir.prefuse.Display;
import edu.berkeley.guir.prefusex.force.DragForce;
import edu.berkeley.guir.prefusex.force.ForceSimulator;
import edu.berkeley.guir.prefusex.force.NBodyForce;
import edu.berkeley.guir.prefusex.force.SpringForce;

/**
 * @author lucasa@gmail.com
 */
public class ApplicationManager implements Runnable, ApplicationManagerRemote, Serializable {

	private static final long serialVersionUID = 440529620112600733L;

	private String appmanId; // id

	/** List of available SMs */
	private Vector submissionmanagerList;
	/** Seed for creating new unique IDs for instantiated SMs */
	private int submanId = 0;
	/** helper counter used to implement round-robin scheduling over available SMs */
	private int schedule_loop = 0;
	/** Means: choosing any of the available SMs would be ok */
	private static final String SCHED_ANY_SM = "";

	/** List of graphs already scheduled to some SM. */
	private Vector graphs;
	/** List of graphs yet to be scheduled tom some SM */
	private Vector newgraphs;

	/** Flags whether to start sending graphs to remotes machines(SubmissionManagers) */
	private boolean runsystem;

	static final int ApplicationManager_FINAL = 2;
	static final int ApplicationManager_READY = 0;
	static final int ApplicationManager_EXECUTING = 1;

	/** Current status of the execution, either READY, EXECUTING or FINAL */
	private int state = ApplicationManager_READY;

	private ApplicationManagerTimer timeInfo = null;

	//VDN
	ApplicationDescription appDescription;
	protected ClusteringPhase cp;
	//
	// EXEHDA stuff
	//
	/** ID of the appman application running in EXEHDA */
	ApplicationId applicationId = null;
	/** URL used to obtain a remote reference to this object */
	String my_contact_address = null;

	public ApplicationManager(String id) {
		initialize(id, AppManUtil.getExecutor().currentApplication());
	}

	public ApplicationManager(String id, ApplicationId appid) {
		initialize(id, appid);
	}

	public ApplicationManager() {
		initialize("Default",null);
//		Thread thread = new Thread(this);
//		thread.start();
		Debug.newDebugFile("APPMANLOG", "appman.log");
	}

	private void initialize(String id, ApplicationId appid) {
		appmanId = id;
		submissionmanagerList = new Vector();
		timeInfo = new ApplicationManagerTimer();
		if (appid!=null) {
			applicationId = appid;
		}

		graphs = new Vector();
		newgraphs = new Vector();

		runsystem = false;

		state = ApplicationManager_READY;
		
		Debug.debug("ApplicationManager "+id+" created.", true);
	}

	public synchronized String getInfoRemote() throws RemoteException {
		return getInfo();
	}

	public void PrintInfo() throws RemoteException {
		Debug.debug(getInfo());
	}

	public String getInfo() throws RemoteException {
		String str = "";
		str += "\n------------ Application Manager [" + appmanId
		+ "]-------------.";
		str += "\nApplication Manager execution status: "
			+ getApplicationStatePercentCompleted() * 100 + " %";
		for (int i = 0; i < submissionmanagerList.size(); i++) {
			SubmissionManagerRemote smr = (SubmissionManagerRemote) submissionmanagerList
			.elementAt(i);
			str += "\nApplicationManager manage Submission Manager: "
				+ smr.getSubmissionManagerIdRemote();
		}
		for (int j = 0; j < graphs.size(); j++) {
			Graph g = (Graph) graphs.elementAt(j);
			str += "\nApplicationManager execute Graph [" + g.getGraphId()
			+ "] by Submission Manager [" + g.getSubmissionManagerId()
			+ "]";
			str += "\nGraph [" + g.getGraphId() + "] execution status: "
			+ g.getStatePercentCompleted();
		}
		str += "\n-------------------------";
		return str;
	}

	public void startApplicationManager() throws RemoteException {
		timeInfo.setTimeExecution(System.currentTimeMillis());
		timeInfo.setTimeBegin(System.currentTimeMillis());
		runsystem = true;
		state = ApplicationManager_EXECUTING;
	}

	public void addGraph(Graph g) {
		synchronized (newgraphs) {
			newgraphs.add(g);
		}
		Debug.debug("ApplicationManager add a new graph: " + g.getGraphId(),
				true);
	}

	public int getApplicationState() throws RemoteException {
		return state;
	}

	public float getApplicationStatePercentCompleted() {
		synchronized (graphs) {
			int n = graphs.size();
			float sum = 0;
			for (int i = 0; i < graphs.size(); i++) {
				sum += ((Graph) graphs.elementAt(i)).getStatePercentCompleted();
			}
			if (n > 0)
				return sum / n;
			else
				return 0;
		}
	}

	/**
	 * Returns the graph with ID <code>graphId</code> in the graphs or newgraphs
	 * array.
	 *
	 * @param graphId a <code>String</code> value
	 * @return a <code>Graph</code> value
	 */
	public Graph getGraph(String graphId) {
		for (int i = 0; i < graphs.size(); i++) {
			if (((Graph) graphs.elementAt(i)).getGraphId().compareTo(graphId) == 0) {
				return (Graph) graphs.elementAt(i);
			}
		}

		for (int i = 0; i < newgraphs.size(); i++) {
			if (((Graph) newgraphs.elementAt(i)).getGraphId()
					.compareTo(graphId) == 0) {
				return (Graph) newgraphs.elementAt(i);
			}
		}

		return null;
	}

	public synchronized boolean isTaskOutputsRemoteAvailable(String taskId)
	throws RemoteException, java.net.SocketException {
		//Debug.debug("ApplicationManager isDisponibleTaskOutputsRemote locating task ["+taskId+"] in "+graphs.size()+" graphs");
		for (int i = 0; i < graphs.size(); i++) {
			Graph g = (Graph) graphs.elementAt(i);
			Task t = g.getTask(taskId);
			//Debug.debug("ApplicationManager isDisponibleTaskOutputsRemote looking for task ["+taskId+"] in graph ["+g.getGraphId()+"]");
			if (t != null) {
				if (t.getState().getCode() == TaskState.TASK_FINAL) {
					Debug
					.debug("ApplicationManager isTaskOutputsRemoteAvailable task ["
							+ t.getTaskId()
							+ "] status: "
							+ t.getState().getName());
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * Retorna uma referência remota do serviço de transferência de arquivos de uma tarefa
	 */
	public String getTaskGridFileServiceContactAddressRemote(String taskId)
	throws RemoteException {
		for (int i = 0; i < graphs.size(); i++) {
			Graph local = (Graph) graphs.elementAt(i);
			Task t = local.getTask(taskId);
			if (t != null) {
				try {
					SubmissionManagerRemote sm = getSubmissionManagerRemote(local
							.getSubmissionManagerId());
					String gfsr = sm.getMyObjectRemoteContactAddress();
					return gfsr;
				} catch (RemoteException e) {
					Debug.debug(e, true);
					throw e;
				}
			}
		}
		return null;
	}

	public SubmissionManagerRemote getSubmissionManagerRemote(String subId)
	throws RemoteException {
		synchronized (submissionmanagerList) {
			for (int i = 0; i < submissionmanagerList.size(); i++) {
				SubmissionManagerRemote candidate = (SubmissionManagerRemote) submissionmanagerList
				.elementAt(i);
				if (candidate.getSubmissionManagerIdRemote().equals(subId)) {
					return candidate;
				}
			}
		}
		return null;
	}

	private SubmissionManagerRemote scheduleSubmissionManager(String subId) {

		SubmissionManagerRemote subr = null;

		synchronized (submissionmanagerList) {

			try {
				// either return the specific SM requested or do a round-robin selection
				// among the SMs available
				if (SCHED_ANY_SM.equals(subId)) { // round-robin
					if (submissionmanagerList.isEmpty()) {
						subId = String.valueOf(submanId++);
						Debug.debug(
								"ApplicationManager need to create a new SubmissionManager: "
								+ subId, true);
						subr = createNewSubmissionManager(subId);
						submissionmanagerList.add(subr);
					} else {
						subr = (SubmissionManagerRemote) submissionmanagerList
						.elementAt(schedule_loop++
								% submissionmanagerList.size());
					}
				} else { // a specific SM instance has been requested
					subr = getSubmissionManagerRemote(subId);
					if (subr == null) {
						Debug.debug(
								"ApplicationManager need to create a new SubmissionManager: "
								+ subId, true);
						subr = createNewSubmissionManager(subId);
						submissionmanagerList.add(subr);
					}
				}

				Debug.debug(
						"ApplicationManager scheduling a SubmissionManager",
						true);
				subr.getIsAliveRemote();
				Debug.debug(
						"ApplicationManager scheduled the SubmissionManager ["
						+ subr.getSubmissionManagerIdRemote() + "]",
						true);

			} catch (RemoteException e) {
				// Tolerancia a Falhas
				// se o Submission Manager remoto escolhido nao responder ao ping isAlive, entao 
				// remove este da lista e  recursivamente realiza outro escalonamento
				//
				Debug.debug("Tolerancia a Falhas - " + e);
				Debug
				.debug("ApplicationManager Scheduling SubmissionManager ERROR, removing fault SubmissionManager from the list");
				submissionmanagerList.removeElement(subr);
				subr = scheduleSubmissionManager();
			}

			System.gc();

			return subr;
		}
	}

	public void setMyObjectContactAddress(String contact) {
		my_contact_address = contact;
	}

	public void setMyObjectContactAddressRemote(String contact)
	throws RemoteException {
		setMyObjectContactAddress(contact);
	}

	public synchronized void addApplicationDescriptionRemote(byte[] filedata)
	throws RemoteException {
		try {
			addApplicationDescription(filedata);
		} catch (Exception e) {
			throw new RemoteException(e.toString());
		}
	}

	public void addApplicationDescription(byte[] filedata) throws Exception {
		GridFileService fileservice = new GridFileService(this.appmanId);
		String name = "graph.dag";
		fileservice.uploadFile(filedata, name);
		File file = fileservice.getFile(name);
		String[] args = new String[1];
		args[0] = file.getAbsolutePath();
		ApplicationDescription appdesc = null;
		try {
			appdesc = SimpleParser.parseGRIDADL(args);
		} catch (Exception e) {
			Debug.debug("Error on parser: " + e);
			Debug.debug(e);
			e.printStackTrace();
			throw new Exception("Error on parser GRID-DAG file: " + e);
		}

		//VDN: particiona
		cp = ((DAG_DSC) appdesc.getDAG()).getCP();

		Vector clusterP = cp.getCluster();
		int nclusters = cp.getNumberOfLevels();

		String[] clusters = new String[nclusters];
		for (int i = 0; i < nclusters; i++) {
			clusters[i] = "cluster" + String.valueOf(i);
		}

		Random rand = new Random();
		//GraphGenerator.clusteringAlgorithm(clusters, appdesc); //VDN Comentou
		//VDN: defino o numero de clusters depois do particionamento. 
		GraphGenerator.clusteringPhaseAlgorithm(clusterP, clusters, appdesc);//VDN Inseriu
		System.out.println("\t\t\n\nVDN: " + clusters.length + "\n");
		nclusters = clusters.length; //VDN
		System.out.println("\t\t\n\nVDN: " + nclusters + "\n");
		String graph_name[] = new String[nclusters];
		for (int j = 0; j < nclusters; j++) {
			graph_name[j] = "grafo" + String.valueOf(rand.nextInt());
			addApplicationDescription(graph_name[j], clusters[j], appdesc);
		}
	}

	public synchronized void addApplicationDescriptionRemote(String graphId,
			String clusterId, ApplicationDescription appdesc)
	throws RemoteException {
		addApplicationDescription(graphId, clusterId, appdesc);
	}

	public void addApplicationDescription(String graphId, String clusterId,
			ApplicationDescription appdesc) {
		Debug
		.debug("ApplicationManager add a new ApplicationDescription",
				true);
		// cria um grafo default randômico
		Graph g = new Graph(graphId, clusterId, appdesc);
		addGraph(g);
	}

	public void setAllSubmissionManagersToDie() {
		System.out.println("[AM] SET ALL AM TO DIE: ");
		Debug.debug("[AM] SET ALL AM TO DIE: ", true);

		long plus = 0;
		for (int i = 0; i < submissionmanagerList.size(); i++) {
			try {
				((SubmissionManagerRemote) submissionmanagerList.elementAt(i))
				.setDieRemote();
				plus += ((SubmissionManagerRemote) submissionmanagerList
						.elementAt(i)).getDownloadTimeOfTasksManagers();
				Debug
				.debug(
						"[AM "
						+ i
						+ "] TIME DOWNLOAD: "
						+ ((SubmissionManagerRemote) submissionmanagerList
								.elementAt(i))
								.getDownloadTimeOfTasksManagers(),
								true);
			} catch (RemoteException e) {

			}
		}

		timeInfo.setDownloadTimeOfSM(plus);
	}

	public void computeApplicationExecutionTimes() {
		String file_path = "tasks-execution-" + this.appmanId + ".trace";
		Debug
		.newDebugFile(
				"TASK\tERROR-RETRY-TIMES\tCREATED-TIME\tSUBMITED-TIME\tSTARTED_TIME\tFINISHED_TIME\tEXECUTION-TIME",
				file_path);
		for (int i = 0; i < graphs.size(); i++) {
			Graph g = (Graph) graphs.elementAt(i);
			Vector tasks = g.getTaskList();
			Debug.debug("ApplicationManager begin time: "
					+ timeInfo.getTimeBegin(), true);
			for (int j = 0; j < tasks.size(); j++) {
				Task t = (Task) tasks.elementAt(j);
				t.getTimeInfo().printTraceInfo(t,file_path);
			}
		}
	}


	/**
	 * Main-thread. Stays in loop (2 steps) until the computation is completed:
	 *
	 * <ol>
	 *
	 * <li> First, it consumes graphs from the newgraphs queue, scheduling
	 * them to same of the available SMs.
	 *
	 * <li> Second, updates the local mirror structure of already scheduled graphs by
	 * contacting the corresponding SM.
	 *
	 * </ol>
	 */
	public void run() {
		Debug.debug("ApplicationManager thread run.");
		float percent_completed = 0;

		do {
			if (runsystem == true) {
				// schedule graphs pending in the newgraphs queue
				schedulePendingGraphs();
				// atualiza os dados dos grafos, baixando o grafo atualizado do submission manager remoto
				if (graphs.size() > 0) {
					int i = 0;
					while (i < graphs.size()) {
						Graph local = (Graph) graphs.elementAt(i);
						i++;
						SubmissionManagerRemote subman = null;
						Graph remote = null;
						if (local.getStatePercentCompleted() < 1) {
							try {
								subman = this.getSubmissionManagerRemote(local
										.getSubmissionManagerId());
								Debug
								.debug(
										"ApplicationManager contacting SubmissionManager ["
										+ subman
										.getSubmissionManagerIdRemote()
										+ "] to update remote graph: "
										+ local.getGraphId(),
										true);
								remote = subman.getGraphRemote(local
										.getGraphId());
							} catch (RemoteException e) {
								// Tolerância a Falhas
								// Se o Submission Manager não responder então
								// remove o grafo da lista
								// adiciona o grafo novamente no Application Manager com um novo SubMan escalonado									
								Debug.debug("Tolerância a Falhas - " + e, true);
								e.printStackTrace();
								//System.exit(0);
								try {
									subman = scheduleSubmissionManager();

									local.setSubmissionManagerId(subman
											.getSubmissionManagerIdRemote());
									graphs.remove(local);
									this.addGraph(local);
								} catch (Exception e2) {
									AppManUtil
									.exitApplication(
											"Tolerância a Falhas: ERRO FATAL NÃO TOLERADO",
											e2);
								}
							}

							// update local graph copy with the new state grabbed from the remote SM
							if (remote != null) {
								local.copy(remote);

								__debug__("local graph [" + local.getGraphId()
										+ "] UPDATED, " + "percent completed="
										+ local.getStatePercentCompleted());
							}
						}
					}
				}

			}

//			float pc = getApplicationStatePercentCompleted();
			if (percent_completed < getApplicationStatePercentCompleted()) {
				percent_completed = getApplicationStatePercentCompleted();
				Debug.debug("ApplicationManager Application graphs completed: "
						+ getApplicationStatePercentCompleted(), true);
			}
			if (getApplicationStatePercentCompleted() != 1f) try {
				Thread.sleep(5000);
			} catch (Exception e) {
				Debug.debug(e, true);
				e.printStackTrace();
			}

		} while (percent_completed < 1f); // end while

		state = ApplicationManager_FINAL;

		computeApplicationExecutionTimes();
		//		Debug.debug("ApplicationManager cleaning Application Files! ", true);
		Debug.debug("ApplicationManager set all Submission Managers TO DIE! ",
				true);
		setAllSubmissionManagersToDie();

		timeInfo.setTimeExecution(System.currentTimeMillis()
				- timeInfo.getTimeExecution());
		Debug
		.debug("ApplicationManager Application completed time: "
				+ (float) timeInfo.getTimeExecution() / 1000
				+ " seconds", true);
		appDescription = appman.parser.SimpleParser.appDescription;

		//VDN
		timeInfo.printFinishTimeInfo();

		//         AppManUtil.exitApplication();
		// 		((Executor) Exehda.getService(Executor.SERVICE_NAME)).exitApplication();
	}// end run

	//VDN
	public ApplicationDescription getApplicationDescription()
	throws RemoteException {
		appDescription = appman.parser.SimpleParser.appDescription;
		return appDescription;
	}

	public Display startAppGUIRemote(String graphId) throws RemoteException {
		return startAppGUI(graphId);
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

		Debug.debug(
				"ApplicatinManager GUI Interface creating display for graph: "
				+ g.PrintInfo(), true);

		ForceSimulator fsim = new ForceSimulator();
		fsim.addForce(new NBodyForce(-0.4f, -1f, 0.9f));
		fsim.addForce(new SpringForce(4E-5f, 75f));
		fsim.addForce(new DragForce(-0.005f));

		ForceDemo fdemo = new ForceDemo(g.getGraph(), fsim);
		fdemo.runDemo();

		return null;
	}

	public Display startDefaultAppGUI() {
		Debug.debug("ApplicatinManager Default GUI Interface loading...", true);

		ForceSimulator fsim = new ForceSimulator();
		fsim.addForce(new NBodyForce(-0.4f, -1f, 0.9f));
		fsim.addForce(new SpringForce(4E-5f, 75f));
		fsim.addForce(new DragForce(-0.005f));

		GraphGenerator generator = new GraphGenerator("");
		edu.berkeley.guir.prefuse.graph.Graph gt = generator
		.getRandomTreeDirected(3, 3, (float) 0.7);
		ForceDemo fdemo = new ForceDemo(gt, fsim);
		Debug.debug(
				"ApplicatinManager GUI Interface creating display for DEFAULT graph: "
				+ gt.getNodeCount() + " nodes, " + gt.getEdgeCount()
				+ " edges", true);

		fdemo.runDemo();

		return null;
	}

	////////////////////////////////////////////////////////////////
	// refactoring
	////////////////////////////////////////////////////////////////

	private final void __debug__(String msg) {
		Debug.debug("[APPMAN] " + msg, true);
	}

	/**
	 * Assigns graphs yet to scheduled to some available SM.
	 *
	 */
	private final void schedulePendingGraphs() {
		// TODO: limit the number of scheduling to 2x the number of pending
		// graphs to not lock forever

		// while there are graphs to be scheduled
		while (!newgraphs.isEmpty()) {
			SubmissionManagerRemote subman = null;
			// schedule the next pending graph
			synchronized (graphs) {
				Graph ng = (Graph) newgraphs.remove(0);

				try {
					// select a candidate SM
					timeInfo.setTimeScheduleBegin(System.currentTimeMillis());
					subman = scheduleSubmissionManager(ng.getGraphId());
					timeInfo.setTimeScheduleEnd(System.currentTimeMillis());
					timeInfo.setTimeScheduleTotal(timeInfo
							.getTimeScheduleTotal()
							+ (timeInfo.getTimeScheduleEnd() - timeInfo
									.getTimeScheduleBegin()));

					// assing the graph to the SM
					try {
						String smId = subman.getSubmissionManagerIdRemote();
						ng.setSubmissionManagerId(smId);
						subman.addGraphRemote(ng);
						graphs.add(ng);

						__debug__("graph[" + ng.getGraphId()
								+ "] scheduled to SM[" + smId + "]");
					} catch (RemoteException re) {
						__debug__("Failed to assign graph to SM=" + subman
								+ ". Graph will be re-scheduled");
						// return the graph to the pending graphs pool
						ng.setSubmissionManagerId(null);
						newgraphs.add(ng);
					}
				} catch (Exception e) {
					AppManUtil.exitApplication(
							"FATAL ERROR while scheduling graphs to SMs", e);
				}
			}
		}
	}

	/**
	 * Select a SM through a RR scheduling over the available SMs.
	 *
	 * @return a <code>SubmissionManagerRemote</code> value
	 */
	private final SubmissionManagerRemote scheduleSubmissionManager() {
		return scheduleSubmissionManager(SCHED_ANY_SM);
	}

	/**
	 * Instantiate a new SM with the given ID, returning a reference to it.
	 *
	 * @param subId a <code>String</code> value
	 * @return a <code>SubmissionManagerRemote</code> value
	 * @exception RemoteException if an error occurs
	 */
	private final SubmissionManagerRemote createNewSubmissionManager(
			String subId) throws RemoteException {
		SubmissionManagerRemote sub = null;
		try {
			Debug.debug(
					"ApplicationManager creating new remote SubmissionManager: "
					+ subId, true);

			sub = exehdaCreateNewSubmissionManager(subId);
		} catch (Exception e2) {
			AppManUtil.exitApplication(null, e2);
		} finally {
			Debug.debug("DEBUG: " + sub, true);

			if (sub == null) {
				throw new RemoteException(
				"Failed to instantiate new Submission Manager");
			}
			return sub;
		}
	}

	/**
	 * Invokes EXEHDA to remotely instantiate a new SM.
	 *
	 * TO DO: move this code to GridToolkit for better encapsulation
	 *
	 * @param smId a <code>String</code> value
	 * @return a <code>SubmissionManagerRemote</code> value
	 */
	private final SubmissionManagerRemote exehdaCreateNewSubmissionManager(
			String smId) {
		// O metodo setHeuristic() eh justamente o metodo que instala a heuristica
		// de escalonamento especifica do AppMan. Hoje essa heuristica desconsidera
		// aquele escalonador de proposito geral, fazendo o trabalho completo
		// sozinha. Uma melhoria do prototipo seria fazer ela interagir c/ o
		// servico Scheduler. Na epoca foi implementado dessa forma pq a integracao
		// do Scheduler ao sistema ainda nao estava concluida/estavel.

		AppManUtil.getExecutor().setHeuristic(GridSchedule.getInstance());

		ObjectId oxID;
		try {
			Debug.debug("DEBUG createNewSubmissionManagerAction TRY", true);

			GeneralObjectActivator gactivator = new GeneralObjectActivator(
					"SubmissionManager",
					new Class[] { SubmissionManagerRemote.class },
					new String[] { "SubmissionManagerRemote" }, true);

			oxID = AppManUtil.getExecutor().createObject(
					SubmissionManager.class,
					new Object[] { smId, my_contact_address }, gactivator,
					GridSchedule.HINT_SUBMISSION_MANAGER_NODE);

			SubmissionManagerRemote stub = (SubmissionManagerRemote) GeneralObjectActivator
			.getRemoteObjectReference(oxID,
					SubmissionManagerRemote.class,
					"SubmissionManagerRemote");

			stub.setMyObjectRemoteContactAddress(gactivator.getContactAddress(
					oxID, "SubmissionManagerRemote"));

			return stub;
		} catch (Exception e) {
			__debug__("ERROR: exehdaCreateNewSubmissionManager failed due to"
					+ e);
			e.printStackTrace();
		}
		return null;
	}
}
