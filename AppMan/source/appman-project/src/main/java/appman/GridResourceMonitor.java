/*
 * Created on 10/12/2004
  */
package appman;

// import org.isam.exehda.Exehda;
import org.isam.exehda.services.Collector;
import org.isam.exehda.services.Collector.Sensor;
import org.isam.exehda.services.Collector.MonitoringData;
import org.isam.exehda.services.Collector.ConsumerId;
import org.isam.exehda.services.Collector.MonitoringConsumer;

import appman.log.Debug;

/**
 * @author lucasa
 *
 */
public class GridResourceMonitor extends Thread implements MonitoringConsumer
{
	private String name = "";
	private String earlyhistory = "";
	private String history = "";
	private boolean end = false;
	private Collector collector;
	private Sensor sensor;
	private ConsumerId id;
	
	private float average_list[];
	private int average_num = 0;	
	private int average_max = 5;
	
	private long lasttimestamp = 0;
	private long time = 0;
	
	private boolean load = false;
		
	public GridResourceMonitor(String str)
	{
		try
		{
			name = str;
			collector = AppManUtil.getCollector();
			this.id = collector.addConsumer(this);
			this.sensor = collector.getSensor("CPU_OCCUP_USER");
			collector.setSensorEnabled(this.id,sensor,true);			
		    average_list = new float[average_max];
			Debug.debug("GridResourceMonitor ["+name+"] created");
			load = true;
		} catch (Exception e)
		{
			Debug.debug("GridResourceMonitor ["+name+"] creation FAILED");
		}
	}
	public void endMonitor()
	{
		end = true;
	}
	public void startMonitor()
	{
		if(load)
		{
			this.start();
			end = false;
			Debug.newDebugFile("#GridResourceMonitor Data: " + name, "subman-"+name+"-monitor.data");
			Debug.debugToFile("\n#TIME\tData: Load CPU", "subman-"+name+"-monitor.data", true);
		}
	}
	private float calcAverage(float values[])
	{
		float sum = 0;
		for(int i=0; i<values.length; i++)
		{
			sum+= values[i];
		}
		return sum/values.length;
	}
	
	public void update( long timeStamp, MonitoringData[] data )
	{
		String s = "\n----- GridResourceMonitor ["+name+"]-----";
		float d = (timeStamp - lasttimestamp)/1000;		
		if(lasttimestamp > 0)
			time+= d;
		else
			time = 0;
		
		s+= "\nTime stamp: " + d + " seconds";
		s+= "\nTime counter: " + time + " seconds";
		
		for (int i=0; i<data.length; i++)
		{
			if ( data[i] != null )
			{
				s+= "\n";
				s+= data[i].getSensorName().getSimpleName();
				s+= " \t= ";
				s+= data[i].getString();
				String value = data[i].getString();
				average_list[average_num] = Float.parseFloat(value);
				average_num++;
				if(average_num == average_max)
				{
					float avg = calcAverage(average_list);
					s+= " \nAverage ["+data[i].getSensorName().getSimpleName()+"] in " + average_max +" values: " + avg;
					Debug.debugToFile("\n" + time + "\t" + avg, "subman-"+name+"-monitor.data", true);	
				}
				average_num = average_num % average_max;
			}
		}
		s+= "\n--------------------------------";
		earlyhistory+= s;
		lasttimestamp = timeStamp;
	}
	public String getHistory()
	{
		return history; 
	}
	@Override
	public void run()
	{
		Debug.debug("GridResourceMonitor ["+name+"] thread run.");
		while(!end)
		{
			history+= earlyhistory; 
			earlyhistory = "";
			try
			{				
				Thread.sleep(5000);
			} catch (Exception e) {
				Debug.debug(e, e);
				System.exit(0);
			}
			Debug.debug(earlyhistory);
		}
	}

}
