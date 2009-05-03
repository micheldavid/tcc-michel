/*
 * Created on 15/09/2004
 *
 */
package appman;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import org.isam.exehda.HostId;
import org.isam.exehda.ResourceName;
import org.isam.exehda.services.CellInformationBase;

/**
 * @author lucasa
 *
 */
public class GridRemoteHostsProperties
{
	private GridRemoteHostsFileProperties fileProperties;
	private ArrayList hosts;
	private Vector queue;
	private Vector queueAllMachines;
	private int roundNumber=0;
	
	public GridRemoteHostsProperties(String filename, String filesection)
	{
		fileProperties = new GridRemoteHostsFileProperties(filename, filesection);
		hosts = fileProperties.getTargetHosts();
		queue = new Vector();
		
		for (int i = 0; i < hosts.size(); i++) {
			queue.add(hosts.get(i));
		}
		
		
		// 2006/02/03
//        CellInformationBase cib = AppManUtil.getCellInformationBase();
//        ResourceName[] resources = cib.selectByType("host", (ResourceName.NameSpace)null, -1);
//        Debug.debug("GridSchedule number target hosts in the Cell: " + resources.length, true);
//        queueAllMachines = new Vector();
//        for(int i=0;i<resources.length; i++)
//        {
//        	queueAllMachines.add((String)resources[i].getSimpleName());
//            Debug.debug("GridSchedule target hosts in the Cell [" + i + "]: " + resources[i].getSimpleName(), true);
//        }


	}
	/*
	 *  Atualiza estado dos hosts.
	 * Em caso de erro, exceção, remove o host da lista de hosts disponíveis
	 */
/*	
	public void updateHostsProperties()
	{
		//System.out.println("GridRemoteHostProperties - updateHostsProperties");
		for(int i = 0; i < hosts.size(); i++)
		{
			try
			{			
				HostId targetHost = HostId.getByName((String) hosts.get(i));
			} catch (java.net.UnknownHostException e)
			{
				Debug.debug("GridRemoteHostProperties Host Error, removing [ "+ (String) hosts.get(i) +" ] from the hosts list");
				hosts.remove(i);
			}
		}
	}
*/
	/*
	 * Retorna host randômico da lista de hosts válidos
	 */ 
	public HostId getRandomHost()
	{	
		Random rand = new Random();
		int i = Math.abs(rand.nextInt() % hosts.size());
		HostId targetHost = null;
		try
		{
			String host =  "hostid:"+(String)hosts.get(i);
			targetHost = HostId.parseId(host);
		} catch (Exception e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(0);
		}
				
		return targetHost;
	}
	
	/**
	 * 
	 * @author dalto
	 *
	 * 
	 * Window - Preferences - Java - Code Style - Code Templates
	 */
	
	public synchronized HostId getRoundRobinHost()
	{
		HostId targetHost = null;
		String first = (String)queue.get(0);
		queue.remove(0);
		queue.add(first);
		try {
			String host =  "hostid:"+first;
			targetHost = HostId.parseId(host);
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			System.exit(0);

		}
		
		return targetHost;
		
	}

	// talvez nao seja necessario...apagar depois...
	public HostId getRoundRobinComputeHostCib()
	{
      CellInformationBase cib = AppManUtil.getCellInformationBase();
      ResourceName[] resources = cib.selectByType("host", (ResourceName.NameSpace)null, -1);
      Debug.debug("GridSchedule number target hosts in the Cell: " + resources.length, true);

		
		HostId targetHost = null;
		if (roundNumber>resources.length) {
			roundNumber=0;
		}
		String first = resources[roundNumber].getSimpleName();
		roundNumber++;
		try {
			String host =  "hostid:"+first;
			targetHost = HostId.parseId(host);
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			System.exit(0);

		}
		
		return targetHost;
		
	}

	public synchronized HostId getRoundRobinComputeHost()
	{
            // FIX ME: periodicamente deve zerar a lista e atualizar a partir da
            // CIB. **Importante** não deve zerar sempre ou o RR da forma como está
            // implementado abaixo não vai funcionar!.
		if (queueAllMachines==null) {
			queueAllMachines = new Vector();
			getHostFromCib();
		}
		HostId targetHost = null;
		String first = (String)queueAllMachines.get(0);
		System.out.println("getRoundRobin first "+first);
		queueAllMachines.remove(0);
		queueAllMachines.add(first);

		try {
			//System.out.println("getRoundRobin host "+host);
			targetHost = HostId.parseId("hostid:"+first+"."+HostId.getLocalHost().getCell().getName());
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			System.exit(0);

		}
		
		return targetHost;
		
	}
	
	private void getHostFromCib() {

    CellInformationBase cib = AppManUtil.getCellInformationBase();
    ResourceName[] resources = cib.selectByType("host", (ResourceName.NameSpace)null, -1);
    Debug.debug("GridSchedule number target hosts in the Cell: " + resources.length, true);
    
    for(int i=0;i<resources.length; i++)
    {
    	queueAllMachines.add(resources[i].getSimpleName());
    	//queueAllMachines.add("hostid:"+resources[i].getSimpleName()+"."+HostId.getLocalHost().getCell().getName());
        Debug.debug("GridSchedule target hosts in the Cell [" + i + "]: " + resources[i].getSimpleName(), true);
    }
	}
	/*
	public HostId getHost(int i)
	{	
		HostId targetHost = null;
		i = i % hosts.size();
		try
		{
			targetHost = targetHost = HostId.getByName((String)hosts.get(i));
		} catch (Exception e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(0);
		}
		return targetHost;
	}	
	public HostId getFirstHost()
	{		
		HostId targetHost = null;
		try
		{
			targetHost = HostId.getByName((String) hosts.get(0));
		} catch (Exception e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(0);
		}
			
		return targetHost;
	}
*/
}
