package appman;

// Copyright 2003 ISAM Team <http://www.inf.ufrgs.br/~isam/>
//
// This file is part of ISAM.
//
// ISAM is free software; you can redistribute it and/or modify it under the
// terms of the GNU General Public License as published by the Free Software
// Foundation; either version 2 of the License, or (at your option) any later
// version.
//
// ISAM is distributed in the hope that it will be useful, but WITHOUT ANY
// WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
// A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with ISAM; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//

import java.awt.Frame;
import java.awt.Panel;
import java.awt.Checkbox;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.util.Hashtable;

import org.isam.exehda.Exehda;
import org.isam.exehda.services.Collector;
import org.isam.exehda.services.Collector.Sensor;
import org.isam.exehda.services.Collector.MonitoringData;
import org.isam.exehda.services.Collector.ConsumerId;
import org.isam.exehda.services.Collector.MonitoringConsumer;



public class CollectorDemo2
    extends Frame
    implements WindowListener, MonitoringConsumer, ItemListener
{
    private Panel sensorsPane;
    private Hashtable sensorsByBox;
    private Collector collector;
    private Sensor[] sensors;
    private ConsumerId id;

    
    public void windowOpened(WindowEvent windowEvent) {}

    public void windowClosing(WindowEvent windowEvent)
        {
            System.exit(0);
        }

    public void windowClosed(WindowEvent windowEvent) {}

    public void windowIconified(WindowEvent windowEvent) {}

    public void windowDeiconified(WindowEvent windowEvent) {}

    public void windowActivated(WindowEvent windowEvent) {}

    public void windowDeactivated(WindowEvent windowEvent) {}
    
    
    public CollectorDemo2()
        {
            super("Collector Demo 2");
            addWindowListener(this);
            
            Dimension d = new Dimension(200, 200);
            
            sensorsPane = new Panel();
            sensorsPane.setLayout(new GridLayout(0,3));

            this.setBounds(50,50,100,100);
            this.add(sensorsPane);

            collector = (Collector) Exehda.getService(Collector.SERVICE_NAME);

            this.id = collector.addConsumer(this);

            initSensors();
        }

    private void initSensors()
        {
            this.sensorsByBox = new Hashtable();
            
            this.sensors = collector.getSensors();
            for ( int i=0; i< sensors.length; i++ ) {
                addSensor(sensors[i]);
            }
        }

    private void addSensor(Sensor s)
        {
            Checkbox cb = new Checkbox(s.getName().getSimpleName());
            cb.setState(collector.isSensorEnabled(id, s));
            cb.addItemListener(this);
            sensorsPane.add(cb);
            sensorsByBox.put(cb,s);
        }

    public void update( long timeStamp, MonitoringData[] data )
        {
            System.out.println("\nTime stamp: "+timeStamp);
            for (int i=0; i<data.length; i++) {
                if ( data[i] != null ) {
                    System.out.print(data[i].getSensor().getSimpleName());
                    System.out.print(" \t= ");
                    System.out.println(data[i].getString());
                }
            }
        }

    public void itemStateChanged(ItemEvent ev)
        {            
            Checkbox cb = (Checkbox) ev.getSource();
            Sensor s = (Sensor) sensorsByBox.get(cb);
            

            collector.setSensorEnabled(id,
                                       s,
                                       (ItemEvent.SELECTED == ev.getStateChange()));
            
        }

    public static void
    main( String[] args ) throws Exception
        {
            CollectorDemo2 frame = new CollectorDemo2();

            frame.pack();
            frame.setVisible(true);
        }
}
