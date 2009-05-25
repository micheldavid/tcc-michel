/*
 * Created on 08/06/2004
 */
package appman;

import java.util.Arrays;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.isam.exehda.HostId;
import org.isam.exehda.ObjectId;
import org.isam.exehda.services.Executor.SchedulingHeuristic;

/**
 * @author lucasa
 */
public class GridSchedule implements SchedulingHeuristic {
	private static final Log log = LogFactory.getLog(GridSchedule.class);
	private static final long serialVersionUID = 6545673083638233546L;

	public static final String HINT_FINAL_RESULTS_NODE       = "grid.targetHosts.host-final-results";
    public static final String HINT_DEDICATED_COMPUTE_NODE   = "grid.targetHosts.oneTaskPerCPU";
    public static final String HINT_SHARED_COMPUTE_NODE      = "grid.targetHosts.localscheduler";
    public static final String HINT_SUBMISSION_MANAGER_NODE  = "grid.targetHosts.submissionmanagers.hosts";
    
    private static GridSchedule impl = null;

    private GridRemoteHostsProperties smGridHosts;
    private GridRemoteHostsProperties resultsGridHosts;
//     private GridRemoteHostsProperties dedicatedComputeGridHosts;
    private GridRemoteHostsProperties sharedComputeGridHosts;

    /**
	 * não há problema em instanciar 2x, pois não temos estado
	 */
    public static GridSchedule getInstance() {
		if (impl == null) {
			impl = new GridSchedule();
		}
		return impl;
	}
		
	private GridSchedule()
	{
        smGridHosts = new GridRemoteHostsProperties(HINT_SUBMISSION_MANAGER_NODE);
        resultsGridHosts = new GridRemoteHostsProperties(HINT_FINAL_RESULTS_NODE);
//         dedicatedComputeGridHosts = new GridRemoteHostsProperties(HINT_DEDICATED_COMPUTE_NODE);
        sharedComputeGridHosts = new GridRemoteHostsProperties(HINT_SHARED_COMPUTE_NODE);
	}
	
	/**
	 * The method GridSchedule.chooseCreationHost() is used to choose where 
	 * a remote object will be instantiated. It is always a random choice.
	 * However, for creating a submission manager or for selecting a machine 
	 * for storing final results, the machine is choosen according the machines 
	 * listed in gridnodes.properties,
	 * respectively <I>grid.targetHosts.submissionmanagers.hosts</I> and
	 * <I>grid.targetHosts.host-final-results</I> properties.
	 * <P>
	 * This method in AppMan is used for scheduling purposes. Scheduling according
	 * to GRAND model is done in two steps:
	 * <OL>
	 *   <LI>Submission machine scheduling, which is done in the user machine
	 *    (where ApplicationManager runs). In this step, it is chosen the host 
	 *    where a new SubmissionMachine will be instantiated. AppMan chooses at
	 *    random considering the set of nodes defined in gridnodes.properties file.
	 *   <LI>Local scheduling, which is done in the submission machine. The TaskManager
	 *   decides where to execute the tasks available in its list of tasks.
	 *   AppMan chooses a host at random from the set of hosts of the ISAM cell to 
	 *   which the SubmissionManager (associated to this TaskManager) belongs.
	 *   This local scheduling should be changed if a communication interface is
	 *   implemented to a local resource manager such as PBS, Globus, and/or Condor.
	 *   (This local scheduling is a kind of simple RMS)  
	 *  </OL>
	 *  The GridSchedule.chooseCreationHost method decides which scheduling step to use
	 *  through the information passed by <I>Executor</I> service (<I>hint</I> parameter).
	 */
	public HostId chooseCreationHost(String clsName, Object[] params, Object hint, Vector avoidedHosts) {
		log.debug("GridSchedule clsName: " + clsName + ", params: " + (params == null ? null : Arrays.asList(params))
			+ ", HINT: " + hint + ", avoidedHosts: " + avoidedHosts);

		// decidindo qual conjunto de hosts usar
		GridRemoteHostsProperties hosts = sharedComputeGridHosts; // HINT_SHARED_COMPUTE_NODE
		if (HINT_SUBMISSION_MANAGER_NODE.equals(hint)) {
			hosts = smGridHosts;
		} else if (HINT_FINAL_RESULTS_NODE.equals(hint)) {
			hosts = resultsGridHosts;
		} else if (HINT_DEDICATED_COMPUTE_NODE.equals(hint)) {
			// TODO limitar a uma tarefa por máquina
		} else {
			hosts.loadComputingHosts();
		}

		// buscando um host não evitado
		HostId hostid = hosts.getRoundRobinHost();
		for (int i = 0; avoidedHosts.contains(hostid); i++) {
			hostid = hosts.getRoundRobinHost();
			if (i > hosts.getHostCount())
				throw new IllegalStateException("todos os hosts evitados: " + hosts.getHosts());
		}

		return hostid;
	}

	public HostId chooseMigrationHost(ObjectId oh, Object hint, Vector avoided_hosts )
        {
            return null;
        }
}
