/*
 * Created on 28/05/2004
 * 
 */
package appman;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

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
public class ApplicationManager implements ApplicationManagerRemote, Serializable, SubmissionManagerExecuteHandler {

	private static final long serialVersionUID = 440529620112600733L;
	private static final Log log = LogFactory.getLog(ApplicationManager.class);

	private String appmanId; // id

	/** List of available SMs */
	private Vector<SubmissionManagerRemote> submissionmanagerList;
	/** Seed for creating new unique IDs for instantiated SMs */
	private int submanId = 0;
	/** helper counter used to implement round-robin scheduling over available SMs */
	private int schedule_loop = 0;

	/** List of graphs already scheduled to some SM. */
	private Vector graphs;
	/** List of graphs yet to be scheduled tom some SM */
	private Vector newgraphs;

	/** Current status of the execution, either READY, EXECUTING or FINAL */
	private ApplicationManagerState state = ApplicationManagerState.READY;

	private ApplicationManagerTimer timeInfo = null;

	private Object lockCompletedSM = new Object();
	private int completedSM = 0;
	private ArrayList<SubmissionManagerExecuteThread> smRunning;

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
	}

	private void initialize(String id, ApplicationId appid) {
		Debug.newDebugFile("APPMANLOG", "appman.log");

		appmanId = id;
		submissionmanagerList = new Vector<SubmissionManagerRemote>();
		smRunning = new ArrayList<SubmissionManagerExecuteThread>();
		timeInfo = new ApplicationManagerTimer();
		if (appid!=null) {
			applicationId = appid;
		}

		graphs = new Vector();
		newgraphs = new Vector();

		state = ApplicationManagerState.READY;
		
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
		for (int i = 0; i < submissionmanagerList.size(); i++) {
			SubmissionManagerRemote smr = submissionmanagerList.elementAt(i);
			str += "\nApplicationManager manage Submission Manager: " + smr.getSubmissionManagerIdRemote();
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
		state = ApplicationManagerState.EXECUTING;

		runApplicationManager();
	}

	public void addGraph(Graph g) {
		newgraphs.add(g);
		log.debug("ApplicationManager add a new graph: " + g.getGraphId());
	}

	public ApplicationManagerState getApplicationState() throws RemoteException {
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
		//log.debug("ApplicationManager isDisponibleTaskOutputsRemote locating task ["+taskId+"] in "+graphs.size()+" graphs");
		for (int i = 0; i < graphs.size(); i++) {
			Graph g = (Graph) graphs.elementAt(i);
			Task t = g.getTask(taskId);
			//log.debug("ApplicationManager isDisponibleTaskOutputsRemote looking for task ["+taskId+"] in graph ["+g.getGraphId()+"]");
			if (t != null) {
				if (t.getState().getCode() == TaskState.TASK_FINAL) {
					log.debug("ApplicationManager isTaskOutputsRemoteAvailable task ["
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
					log.warn(e, e);
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
				SubmissionManagerRemote candidate = submissionmanagerList.elementAt(i);
				if (candidate.getSubmissionManagerIdRemote().equals(subId)) {
					return candidate;
				}
			}
		}
		return null;
	}

	/**
	 * @param subId null para escolher qualquer um
	 * @return SM disponível
	 */
	private SubmissionManagerRemote scheduleSubmissionManager(String subId) {

		synchronized (submissionmanagerList) {

			SubmissionManagerRemote subr = null;
			try {

				// either return the specific SM requested or do a round-robin selection
				// among the SMs available
				if (subId == null) { // round-robin
					if (submissionmanagerList.isEmpty()) {
						subId = String.valueOf(submanId++);
						log.debug("ApplicationManager need to create a new SubmissionManager: " + subId);
						subr = createNewSubmissionManager(subId);
						submissionmanagerList.add(subr);
					} else {
						subr = submissionmanagerList.elementAt(schedule_loop++ % submissionmanagerList.size());
						subr.getIsAliveRemote();
					}
				} else { // a specific SM instance has been requested
					subr = getSubmissionManagerRemote(subId);
					if (subr == null) {
						log.debug("ApplicationManager need to create a new SubmissionManager: " + subId);
						subr = createNewSubmissionManager(subId);
						submissionmanagerList.add(subr);
					} else {
						subr.getIsAliveRemote();
					}
				}

				log.debug("ApplicationManager scheduled the SubmissionManager ["
					+ subr.getSubmissionManagerIdRemote() + "]");

			} catch (IOException e) {
				// Tolerancia a Falhas
				// se o Submission Manager remoto escolhido nao responder ao ping isAlive, entao 
				// remove este da lista e  recursivamente realiza outro escalonamento
				log.warn("Chamada ao SM " + subr + " falhou, removendo SM " + subId, e);
				submissionmanagerList.removeElement(subr);
				subr = scheduleSubmissionManager();
			}

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
			throw new RemoteException(e.toString(), e);
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
			log.warn("Error on parser: " + e, e);
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
		log.debug("VDN: " + clusters.length);
		nclusters = clusters.length; //VDN
		log.debug("VDN: " + nclusters);
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
		addGraph(new Graph(graphId, clusterId, appdesc));
	}

	public void setAllSubmissionManagersToDie() {
		log.debug("[AM] SET ALL AM TO DIE: ");

		long plus = 0;
		for (int i = 0; i < submissionmanagerList.size(); i++) {
			try {
				((SubmissionManagerRemote) submissionmanagerList.elementAt(i))
				.setDieRemote();
				plus += ((SubmissionManagerRemote) submissionmanagerList
						.elementAt(i)).getDownloadTimeOfTasksManagers();
				log.debug("[AM " + i + "] TIME DOWNLOAD: "
					+ ((SubmissionManagerRemote) submissionmanagerList.elementAt(i)).getDownloadTimeOfTasksManagers());
			} catch (IOException ex) {
				log.info("erro em setDieRemota - ignorado", ex);
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
			log.debug("ApplicationManager begin time: "
					+ timeInfo.getTimeBegin());
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
	private void runApplicationManager() throws RemoteException {
		float percent_completed = 0;

		while (this.getApplicationStatePercentCompleted() < 1) {
			// schedule graphs pending in the newgraphs queue
			schedulePendingGraphs();
			// atualiza os dados dos grafos, baixando o grafo atualizado do submission manager remoto
			for (int i = 0; i < graphs.size(); i++) {
				Graph local = (Graph) graphs.elementAt(i);
				if (local.getStatePercentCompleted() < 1) {

					SubmissionManagerRemote subman = null;
					Graph remote = null;
					try {
						subman = this.getSubmissionManagerRemote(local.getSubmissionManagerId());
						log.debug("ApplicationManager contacting SubmissionManager ["
							+ subman.getSubmissionManagerIdRemote() + "] to update remote graph: "
							+ local.getGraphId());
						remote = subman.getGraphRemote(local.getGraphId());
					} catch (IOException e) {
						// Tolerância a Falhas
						// Se o Submission Manager não responder então remove o grafo da lista e adiciona o
						// grafo novamente no Application Manager com um novo SubMan escalonado
						log.warn("Tolerância a Falhas - " + e, e);
						try {
							subman = scheduleSubmissionManager();

							local.setSubmissionManagerId(subman.getSubmissionManagerIdRemote());
							graphs.remove(i);
							i--;
							this.addGraph(local);
						} catch (Exception e2) {
							AppManUtil.exitApplication("Tolerância a Falhas: ERRO FATAL NÃO TOLERADO", e2);
						}
					}

					// update local graph copy with the new state grabbed from the remote SM
					if (remote != null) {
						local.copy(remote);

						__debug__("local graph [" + local.getGraphId() + "] UPDATED, percent completed="
							+ local.getStatePercentCompleted());
					}
				}
			}

			if (percent_completed < getApplicationStatePercentCompleted()) {
				percent_completed = getApplicationStatePercentCompleted();
				log.debug("ApplicationManager Application graphs completed: "
						+ getApplicationStatePercentCompleted());
			}
			if (percent_completed == 1f) continue;

			log.warn("\n\n\n\nWAITING SM...");
			synchronized (lockCompletedSM) {
				try {
					if (completedSM == 0) {
						lockCompletedSM.wait();
					}
					completedSM = 0;
				} catch (InterruptedException e) {
					log.error("esperando sinal dos SMs", e);
				}
			}
			log.warn("\n\n\n\nCONTINUING...");
//			try {
//				Thread.sleep(5000);
//			} catch (InterruptedException e) {
//				log.error("aguardando SMs", e);
//			}

		} // end while

		state = ApplicationManagerState.FINAL;

		computeApplicationExecutionTimes();
		log.debug("ApplicationManager set all Submission Managers TO DIE! ");
		setAllSubmissionManagersToDie();

		timeInfo.setTimeExecution(System.currentTimeMillis()
				- timeInfo.getTimeExecution());
		log.debug("ApplicationManager Application completed time: " + (float) timeInfo.getTimeExecution() / 1000
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
				log.warn(e, e);
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

	private final void __debug__(String msg) {
		__debug__(msg, null);
	}

	private final void __debug__(String msg, Throwable th) {
		if (th == null) {
			log.debug("[APPMAN] " + msg, th);
		} else {
			log.warn("[APPMAN] " + msg, th);
		}
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
					} catch (IOException re) {
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
		return scheduleSubmissionManager(null);
	}

	/**
	 * Instantiate a new SM with the given ID, returning a reference to it.
	 *
	 * @param subId a <code>String</code> value
	 * @return a <code>SubmissionManagerRemote</code> value
	 * @exception RemoteException if an error occurs
	 */
	private final SubmissionManagerRemote createNewSubmissionManager(String subId) throws RemoteException {
		log.debug("ApplicationManager creating new remote SubmissionManager: " + subId);

		return exehdaCreateNewSubmissionManager(subId);
	}

	/**
	 * Invokes EXEHDA to remotely instantiate a new SM.
	 *
	 * TO DO: move this code to GridToolkit for better encapsulation
	 *
	 * @param smId a <code>String</code> value
	 * @return a <code>SubmissionManagerRemote</code> value
	 * @throws RemoteException erro remoto
	 */
	private final SubmissionManagerRemote exehdaCreateNewSubmissionManager(String smId) throws RemoteException {
		// O metodo setHeuristic() eh justamente o metodo que instala a heuristica
		// de escalonamento especifica do AppMan. Hoje essa heuristica desconsidera
		// aquele escalonador de proposito geral, fazendo o trabalho completo
		// sozinha. Uma melhoria do prototipo seria fazer ela interagir c/ o
		// servico Scheduler. Na epoca foi implementado dessa forma pq a integracao
		// do Scheduler ao sistema ainda nao estava concluida/estavel.

		AppManUtil.getExecutor().setHeuristic(GridSchedule.getInstance());

		GeneralObjectActivator gactivator = new GeneralObjectActivator("SubmissionManager",
			new Class[] { SubmissionManagerRemote.class }, new String[] { "SubmissionManagerRemote" }, true);

		ObjectId oxID = AppManUtil.getExecutor().createObject(SubmissionManager.class,
			new Object[] { smId, my_contact_address }, gactivator, GridSchedule.HINT_SUBMISSION_MANAGER_NODE);

		SubmissionManagerRemote stub = (SubmissionManagerRemote) GeneralObjectActivator.getRemoteObjectReference(oxID,
			SubmissionManagerRemote.class, "SubmissionManagerRemote");

		stub.setMyObjectRemoteContactAddress(gactivator.getContactAddress(oxID, "SubmissionManagerRemote"));

		synchronized (smRunning) {
			SubmissionManagerExecuteThread thread = new SubmissionManagerExecuteThread(smId, stub, this);
			smRunning.add(thread);
			thread.start();
		}
		return stub;
	}

	public void runSubmissionManagerFinished(SubmissionManagerExecuteThread thread, Exception ex) {
		synchronized (smRunning) {
			smRunning.remove(thread);
		}
		synchronized (lockCompletedSM) {
			completedSM++;
			lockCompletedSM.notify();
		}
		if (ex != null) {
			log.error("SM " + thread.getSubmissionManagerId() + " falhou", ex);
		} else {
			log.debug("SM " + thread.getSubmissionManagerId() + " terminou a execução", ex);
		}
	}
}