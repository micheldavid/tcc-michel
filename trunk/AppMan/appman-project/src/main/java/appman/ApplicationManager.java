/*
 * Created on 28/05/2004
 */
package appman;

import java.io.File;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class ApplicationManager implements ApplicationManagerRemote, SubmissionManagerExecuteHandler, Serializable {

	private static final Log log = LogFactory.getLog(ApplicationManager.class);
	private static final long serialVersionUID = 440529620112600733L;

	private String appmanId; // id

	/** List of available SMs */
	private LinkedHashMap<String, SubmissionManagerRemote> submissionmanagerList;
	private AtomicInteger submissionManagersExecutando;
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

	static final int ApplicationManager_FINAL = 2;
	static final int ApplicationManager_READY = 0;
	static final int ApplicationManager_EXECUTING = 1;

	/** Current status of the execution, either READY, EXECUTING or FINAL */
	private int state = ApplicationManager_READY;

	private ApplicationManagerTimer timeInfo = null;
	
	private Semaphore smFinalizado = new Semaphore(0);

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
		submissionmanagerList = new LinkedHashMap<String, SubmissionManagerRemote>();
		submissionManagersExecutando = new AtomicInteger(0);
		timeInfo = new ApplicationManagerTimer();
		if (appid!=null) {
			applicationId = appid;
		}

		graphs = new Vector();
		newgraphs = new Vector();

		state = ApplicationManager_READY;
		
		log.debug("ApplicationManager "+id+" created.");
	}

	public synchronized String getInfoRemote() throws RemoteException {
		return getInfo();
	}

	public void PrintInfo() throws RemoteException {
		log.debug(getInfo());
	}

	public String getInfo() throws RemoteException {
		String str = "";
		str += "\n------------ Application Manager [" + appmanId
		+ "]-------------.";
		str += "\nApplication Manager execution status: "
			+ getApplicationStatePercentCompleted() * 100 + " %";
		synchronized (submissionmanagerList) {
			for (String id : submissionmanagerList.keySet()) {
				str += "\nApplicationManager manage Submission Manager: " + id;
			}
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

	public void addGraph(Graph g) {
		synchronized (newgraphs) {
			newgraphs.add(g);
		}
		log.debug("ApplicationManager add a new graph: " + g.getGraphId());
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
				if (t.getState().equals(TaskState.TASK_FINAL)) {
					log.debug("ApplicationManager isTaskOutputsRemoteAvailable task ["
							+ t.getTaskId()
							+ "] status: "
							+ t.getState().toString());
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
					log.error(e, e);
					throw e;
				}
			}
		}
		return null;
	}

	public SubmissionManagerRemote getSubmissionManagerRemote(String subId) throws RemoteException {
		synchronized (submissionmanagerList) {
			return submissionmanagerList.get(subId);
		}
	}

	public String getSubmissionManagerId(SubmissionManagerRemote smr) throws RemoteException {
		synchronized (submissionmanagerList) {
			for (Entry<String, SubmissionManagerRemote> e : submissionmanagerList.entrySet())
				if (e.getValue() == smr) return e.getKey();
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
						log.debug("ApplicationManager need to create a new SubmissionManager: " + subId);
						subr = exehdaCreateNewSubmissionManager(subId);
						submissionmanagerList.put(subId, subr);
					} else {
						ArrayList<SubmissionManagerRemote> smrs = new ArrayList<SubmissionManagerRemote>(submissionmanagerList.values());
						subr = smrs.get(schedule_loop++ % smrs.size());
					}
				} else { // a specific SM instance has been requested
					subr = getSubmissionManagerRemote(subId);
					if (subr == null) {
						log.debug("ApplicationManager need to create a new SubmissionManager: " + subId);
						subr = exehdaCreateNewSubmissionManager(subId);
						submissionmanagerList.put(subId, subr);
					}
				}

				log.debug("ApplicationManager scheduling a SubmissionManager");
				subr.getIsAliveRemote();
				log.debug("ApplicationManager scheduled the SubmissionManager [" + subId + "]");

			} catch (RemoteException e) {
				// Tolerancia a Falhas
				// se o Submission Manager remoto escolhido nao responder ao ping isAlive, entao 
				// remove este da lista e  recursivamente realiza outro escalonamento
				//
				log.warn("Tolerancia a Falhas - " + e, e);
				log
					.debug("ApplicationManager Scheduling SubmissionManager ERROR, removing fault SubmissionManager from the list");
				submissionmanagerList.remove(subId);
				submissionManagersExecutando.decrementAndGet();
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
			log.error("Error on parser: " + e, e);
			throw new Exception("Error on parser GRID-DAG file: " + e, e);
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
		nclusters = clusters.length; //VDN
		log.debug("\t\tVDN: " + nclusters);
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
		log.debug("ApplicationManager add a new ApplicationDescription");
		// cria um grafo default randômico
		Graph g = new Graph(graphId, clusterId, appdesc);
		addGraph(g);
	}

	public void setAllSubmissionManagersToDie() {
		log.debug("[AM] SET ALL AM TO DIE: ");

		long total = 0;
		synchronized (submissionmanagerList) {
			for (SubmissionManagerRemote smr : submissionmanagerList.values()) {
				try {
					smr.setDieRemote();
					long downloadTime = smr.getDownloadTimeOfTasksManagers();
					total += downloadTime;
					log.debug("[AM " + applicationId + "] TIME DOWNLOAD: " + downloadTime);
				} catch (RemoteException e) {
					log.error(e, e);
				}
			}
		}

		timeInfo.setDownloadTimeOfSM(total);
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
			log.debug("ApplicationManager begin time: " + timeInfo.getTimeBegin());
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
	public void runApplicationManager() throws RemoteException {
		timeInfo.setTimeExecution(System.currentTimeMillis());
		timeInfo.setTimeBegin(System.currentTimeMillis());
		state = ApplicationManager_EXECUTING;

		log.debug("ApplicationManager thread run.");
		float percent_completed = 0;

		try {
			do {
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
								subman = this.getSubmissionManagerRemote(local.getSubmissionManagerId());
								log.debug("ApplicationManager contacting SubmissionManager [" + local.getSubmissionManagerId()
									+ "] to update remote graph: " + local.getGraphId());
								remote = subman.getGraphRemote(local.getGraphId());
							} catch (RemoteException e) {
								// Tolerância a Falhas
								// Se o Submission Manager não responder então
								// remove o grafo da lista
								// adiciona o grafo novamente no Application Manager com um novo SubMan escalonado									
								log.warn("Tolerância a Falhas - " + e, e);
								//System.exit(0);
								try {
									subman = scheduleSubmissionManager();

									local.setSubmissionManagerId(getSubmissionManagerId(subman));
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

								log.debug("local graph [" + local.getGraphId()
										+ "] UPDATED, " + "percent completed="
										+ local.getStatePercentCompleted());
							}
						}
					}
				}


				float pc = getApplicationStatePercentCompleted();
				if (percent_completed < pc) {
					percent_completed = pc;
					log.debug("ApplicationManager Application graphs completed: " + pc);
				}
				if (percent_completed != 1f) {
					smFinalizado.acquire();
					smFinalizado.drainPermits();
				}

			} while (percent_completed < 1f); // end while

			// aguardando a finalização de todos os submission managers
			// só finaliza depois de enviar os resultados para o nó de resultados,
			// enquanto que os grafos finalizam antes.
			// quem sabe isso só é necessário porque não temos certeza de quando terminou a tarefa
			// isso também pode afetar tarefas dependentes
			for (;;) {
				if (submissionManagersExecutando.get() == 0) break;
				smFinalizado.acquire();
			}
		} catch (InterruptedException e) {
			log.error("interrompido aguardando execução dos SMs", e);
		}

		state = ApplicationManager_FINAL;

		computeApplicationExecutionTimes();
		//		Debug.debug("ApplicationManager cleaning Application Files! ", true);
		log.debug("ApplicationManager set all Submission Managers TO DIE! ");
		setAllSubmissionManagersToDie();

		timeInfo.setTimeExecution(System.currentTimeMillis()
				- timeInfo.getTimeExecution());
		log
		.debug("ApplicationManager Application completed time: "
				+ (float) timeInfo.getTimeExecution() / 1000
				+ " seconds");
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
		log.debug("ApplicatinManager GUI Interface loading...");
		while (this.getGraph(graphId) == null) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				log.error(e, e);
			}
		}

		Graph g = this.getGraph(graphId);

		log.debug(
				"ApplicatinManager GUI Interface creating display for graph: "
				+ g.PrintInfo());

		ForceSimulator fsim = new ForceSimulator();
		fsim.addForce(new NBodyForce(-0.4f, -1f, 0.9f));
		fsim.addForce(new SpringForce(4E-5f, 75f));
		fsim.addForce(new DragForce(-0.005f));

		ForceDemo fdemo = new ForceDemo(g.getGraph(), fsim);
		fdemo.runDemo();

		return null;
	}

	public Display startDefaultAppGUI() {
		log.debug("ApplicatinManager Default GUI Interface loading...");

		ForceSimulator fsim = new ForceSimulator();
		fsim.addForce(new NBodyForce(-0.4f, -1f, 0.9f));
		fsim.addForce(new SpringForce(4E-5f, 75f));
		fsim.addForce(new DragForce(-0.005f));

		GraphGenerator generator = new GraphGenerator("");
		edu.berkeley.guir.prefuse.graph.Graph gt = generator
		.getRandomTreeDirected(3, 3, (float) 0.7);
		ForceDemo fdemo = new ForceDemo(gt, fsim);
		log.debug(
				"ApplicatinManager GUI Interface creating display for DEFAULT graph: "
				+ gt.getNodeCount() + " nodes, " + gt.getEdgeCount()
				+ " edges");

		fdemo.runDemo();

		return null;
	}

	////////////////////////////////////////////////////////////////
	// refactoring
	////////////////////////////////////////////////////////////////

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
					String smId = ng.getGraphId();
					subman = scheduleSubmissionManager(smId);
					timeInfo.setTimeScheduleEnd(System.currentTimeMillis());
					timeInfo.setTimeScheduleTotal(timeInfo
							.getTimeScheduleTotal()
							+ (timeInfo.getTimeScheduleEnd() - timeInfo
									.getTimeScheduleBegin()));

					// acessing the graph to the SM
					try {
						ng.setSubmissionManagerId(smId);
						subman.addGraphRemote(ng);
						graphs.add(ng);

						submissionManagersExecutando.incrementAndGet();
						new SubmissionManagerExecuteThread(smId + ":graph:" + ng.getGraphId(), subman, this).start();
						log.debug("graph[" + smId + "] scheduled to SM[" + smId + "]");
					} catch (RemoteException re) {
						log.error("Failed to assign graph to SM=" + subman
								+ ". Graph will be re-scheduled", re);
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
	 * Invokes EXEHDA to remotely instantiate a new SM.
	 *
	 * TODO: move this code to GridToolkit for better encapsulation
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

		try {
			log.debug("criando um novo submission manager remoto");

			GeneralObjectActivator gactivator = new GeneralObjectActivator(
					"SubmissionManager",
					new Class[] { SubmissionManagerRemote.class },
					new String[] { "SubmissionManagerRemote" }, false);

			ObjectId oxID = AppManUtil.getExecutor().createObject(
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
			log.error("exehdaCreateNewSubmissionManager failed due to" + e, e);
		}
		return null;
	}

	public void submissionManagerFinished(SubmissionManagerExecuteThread thread, Exception ex) {
		submissionManagersExecutando.decrementAndGet();
		if (ex != null) {
			log.error("executando submission manager", ex);
		}
		smFinalizado.release();
	}
}
