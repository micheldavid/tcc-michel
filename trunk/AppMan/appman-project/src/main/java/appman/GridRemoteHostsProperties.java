/*
 * Created on 15/09/2004
 */
package appman;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.isam.exehda.HostId;
import org.isam.exehda.ResourceName;
import org.isam.exehda.services.CellInformationBase;

/**
 * @author lucasa
 */
public class GridRemoteHostsProperties
{
	private static final Log log = LogFactory.getLog(GridRemoteHostsProperties.class);
	private static final String GRID_NODES_FILE = "gridnodes.properties";
	private ArrayList<String> hosts;
	private int roundNumber = 0;
	private int loadedFromCib = 0;

	public GridRemoteHostsProperties(String filesection) {
		hosts = loadHosts(filesection);
	}

	private ArrayList<String> loadHosts(String fileSection) {
		try {
			InputStream fis = this.getClass().getClassLoader().getResourceAsStream(GRID_NODES_FILE);
			Properties props = new Properties();
			props.load(fis);
			fis.close();
			ArrayList<String> targetHosts = new ArrayList<String>();
			String hosts = props.getProperty(fileSection).trim();
			log.debug("hosts loaded from file (" + GRID_NODES_FILE + "): section " + fileSection + " = " + hosts);

			targetHosts.addAll(Arrays.asList(hosts.split(";")));
			return targetHosts;
		} catch (IOException ex) {
			throw new IllegalStateException("impossível ler " + GRID_NODES_FILE);
		}
	}

	public ArrayList<String> getHosts() {
		return hosts;
	}

	/**
	 * Retorna host randômico da lista de hosts válidos
	 */
	public HostId getRandomHost() {
		Random rand = new Random();
		int i = Math.abs(rand.nextInt() % hosts.size());
		return HostId.parseId("hostid:" + hosts.get(i));
	}

	public int getHostCount() {
		return hosts.size();
	}

	/**
	 * @author dalto
	 */
	public HostId getRoundRobinHost() {
		return HostId.parseId("hostid:" + hosts.get(getRoundRobinPosition()));
	}

	private synchronized int getRoundRobinPosition() {
		try {
			return roundNumber++ % hosts.size();
		} finally {
			if (roundNumber >= hosts.size()) roundNumber = 0;
		}
	}

	/**
	 * Recarrega a cada N chamadas, sendo N o número de hosts encontrado.
	 * Nota mental: tentar fazer algo mais inteligente
	 */
	public void loadComputingHosts() {
		if (loadedFromCib == 0)
			hosts = loadHostsFromCib();
		loadedFromCib = (loadedFromCib + 1) % hosts.size();
	}
	
	private ArrayList<String> loadHostsFromCib() {
		CellInformationBase cib = AppManUtil.getCellInformationBase();
		ResourceName[] resources = cib.selectByType("host", (ResourceName.NameSpace) null, -1);
		log.debug("GridSchedule number target hosts in the Cell: " + resources.length);

		ArrayList<String> machines = new ArrayList<String>();
		for (ResourceName rn : resources) {
			machines.add(rn.getCommonName().getInstance() + "." + rn.getNameSpace().getCommonName().getInstance());
			// queueAllMachines.add("hostid:"+resources[i].getSimpleName()+"."+HostId.getLocalHost().getCell().getName());
			log.debug("GridSchedule target hosts in the Cell: " + rn.toString());
		}
		return machines;
	}
}
