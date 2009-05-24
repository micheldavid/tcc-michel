/*
 * Created on 08/06/2004
 */
package appman;

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

	private static final String CONFIG_FILE = "gridnodes.properties";
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
        smGridHosts = new GridRemoteHostsProperties(CONFIG_FILE, HINT_SUBMISSION_MANAGER_NODE);
        resultsGridHosts = new GridRemoteHostsProperties(CONFIG_FILE, HINT_FINAL_RESULTS_NODE);
//         dedicatedComputeGridHosts = new GridRemoteHostsProperties(CONFIG_FILE, HINT_DEDICATED_COMPUTE_NODE);
        sharedComputeGridHosts = new GridRemoteHostsProperties(CONFIG_FILE, HINT_SHARED_COMPUTE_NODE);
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
	public HostId chooseCreationHost(String clsName,
									  Object[] params,
									  Object hint,
									  java.util.Vector avoidedHosts)
	{
        try
        {
            HostId hostid = null;                        
            log.debug("GridSchedule clsName: "+ clsName +", params: "+params+", HINT: " + hint +", avoidedHosts: "+avoidedHosts);

            if ( HINT_SUBMISSION_MANAGER_NODE.equals(hint) ) {
                    // O laço abaixo é necessario pois o nodo selecionado em um passo
                    // anterior pode estar indisponivel/down, situacao na qual ele deve
                    // ter sido incluido no avoidedHosts (nodos problematicos) e deve ser
                    // evitado
                hostid = smGridHosts.getRoundRobinHost();

                    // FIX ME: o while abaixo pode bloquear para sempre se todos os nodos
                    // foram incluidos no avoidedHosts
                while(avoidedHosts.contains(hostid)) {
                	hostid = smGridHosts.getRoundRobinHost();
                }
            }
            else if ( HINT_FINAL_RESULTS_NODE.equals(hint) ) {
                    // O laço abaixo é necessario pois o nodo selecionado em um passo
                    // anterior pode estar indisponivel/down, situacao na qual ele deve
                    // ter sido incluido no avoidedHosts (nodos problematicos) e deve ser
                    // evitado
                hostid = resultsGridHosts.getRoundRobinHost();

                    // FIXME: o while abaixo pode bloquear para sempre se todos os nodos
                    // foram incluidos no avoidedHosts
                while(avoidedHosts.contains(hostid)) {
                	hostid = resultsGridHosts.getRoundRobinHost();
                }
            }
                // PKVM - VDN - 2005/12/19 - included to limit the number of tasks per cpu
            else if ( HINT_DEDICATED_COMPUTE_NODE.equals(hint) ) {
                //=> limita a uma tarefa por maquina
            }
            else { // HINT_SHARED_COMPUTE_NODE
                
            	// 2006/02/03 - isso so precisa fazer uma vez?
//                CellInformationBase cib = AppManUtil.getCellInformationBase();
//                ResourceName[] resources = cib.selectByType("host", (ResourceName.NameSpace)null, -1);
//                Debug.debug("GridSchedule number target hosts in the Cell: " + resources.length, true);
//                for(int i=0;i<resources.length; i++)
//                {
//                    Debug.debug("GridSchedule target hosts in the Cell [" + i + "]: " + resources[i].getSimpleName(), true);
//                }
            	// 2006/02/03 mudou de aleatorio pra Round Robin
//                Random rand = new Random();
//                int i = rand.nextInt(resources.length);
//                Debug.debug("GridSchedule target Host scheduled: " + resources[i].getSimpleName(), true);
//                hostid = HostId.parseId("hostid:"+resources[i].getSimpleName()+"."+HostId.getLocalHost().getCell().getName());
            	
                    // O laço abaixo é necessario pois o nodo selecionado em um passo
                    // anterior pode estar indisponivel/down, situacao na qual ele deve
                    // ter sido incluido no avoidedHosts (nodos problematicos) e deve ser
                    // evitado
                hostid = sharedComputeGridHosts.getRoundRobinComputeHost();

                    // FIX ME: o while abaixo pode bloquear para sempre se todos os nodos
                    // foram incluidos no avoidedHosts
                while(avoidedHosts.contains(hostid)) {
                	hostid = smGridHosts.getRoundRobinHost();
                }
            }
            log.debug("GridSchedule["+hint+"] choosed host: " + hostid);

            return hostid;
        }

        catch (Exception e)
        {
        	log.error(e, e);
            throw new NullPointerException("Error in HostId "+ e);
        }
	}

	public HostId chooseMigrationHost(ObjectId oh, Object hint, Vector avoided_hosts )
        {
            return null;
        }
}
