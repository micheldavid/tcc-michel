package appman.parser;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import att.grappa.Graph;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaConstants;
import att.grappa.GrappaPanel;
import att.grappa.GrappaSupport;
import att.grappa.Parser;

public class ShowWindow
	implements GrappaConstants, Serializable
{
	private static final Log log = LogFactory.getLog(ShowWindow.class);
	private static final long serialVersionUID = -4665409762428565088L;

	public DemoFrame  frame  = null;

    public final static String SCRIPT = "formatDemo";

    public Graph graphCopy = null;
    Parser programCopy;
    /*
    public static void main(String[] args) {
	InputStream input = System.in;
	if(args.length > 1) {
	    log.error("USAGE: java Demo12 [input_graph_file]");
	    System.exit(1);
	} else if(args.length == 1) {
	    if(args[0].equals("-")) {
		input = System.in;
	    } else {
		try {
		    input = new FileInputStream(args[0]);
		} catch(FileNotFoundException fnf) {
		    log.error(fnf.toString(), fnf);
		    System.exit(1);
		}
	    }
	}
	Demo12 demo = new Demo12();
	demo.doDemo(input);
    }
    */
    	
    public ShowWindow() {
    }

    public void changeColorNode( String node, String color)
    {
    
    	Graph graph = programCopy.getGraph();//frame.graph;
		att.grappa.Node n2 = graph.findNodeByName(node);
		n2.setAttribute("color", color);
		graph.repaint();
    }
    
    public void showDAG(String file) {
 	InputStream input = System.in;
		try {
		    input = new FileInputStream(file);
		} catch(FileNotFoundException fnf) {
			log.error(fnf.toString(), fnf);
		    System.exit(1);
		}
	    
	
	//Demo12 demo = new Demo12();
	
	this.doDemo(input);
   }
    
    
    void doDemo(InputStream input) {
	Parser program = new Parser(input,System.err);
	try {
	    //program.debug_parse(4);
	    program.parse();
	} catch(Exception ex) {
		log.error("Exception: " + ex.getMessage(), ex);
	    System.exit(1);
	}
	att.grappa.Graph graph = null;

	graph = program.getGraph();

	log.error("The graph contains " + graph.countOfElements(GrappaConstants.NODE|GrappaConstants.EDGE|GrappaConstants.SUBGRAPH) + " elements.");

	graph.setEditable(true);
	//graph.setMenuable(true);
	graph.setErrorWriter(new PrintWriter(System.err,true));
	//graph.printGraph(new PrintWriter(System.out));

	log.error("bbox=" + graph.getBoundingBox().getBounds().toString());

	frame = new DemoFrame(graph);

	frame.setVisible(true);
	
	//////////// Codigo vindn
	/*
	try {
		long numMillisecondsToSleep = 5000; // 0.5 seconds
		Thread.sleep(numMillisecondsToSleep);
	} catch (InterruptedException e) {
		log.debug("Erro no sleep");
	}

	Node n2 = graph.findNodeByName("C");
	n2.setAttribute("color", "red");
	graph.repaint();
	*/
	/////////////////////////
	
	graphCopy = graph;
	programCopy = program;

    }


    class DemoFrame extends JFrame implements ActionListener
    {
		private static final long serialVersionUID = 937529245941815589L;
	GrappaPanel gp;
	Graph graph = null;

	JButton layout = null;
	JButton printer = null;
	JButton draw = null;
	JButton quit = null;
	JPanel panel = null;
  
	public DemoFrame(Graph graph) {
	    super("DemoFrame");
	    this.graph = graph;

	    setSize(600,400);
	    setLocation(100,100);

	    addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent wev) {
			Window w = wev.getWindow();
			w.setVisible(false);
			w.dispose();
			System.exit(0);
		    }
		});

	    JScrollPane jsp = new JScrollPane();
	    jsp.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

	    gp = new GrappaPanel(graph);
	    gp.addGrappaListener(new GrappaAdapter());
	    gp.setScaleToFit(false);

	    java.awt.Rectangle bbox = graph.getBoundingBox().getBounds();
  
	    GridBagLayout gbl = new GridBagLayout();
	    GridBagConstraints gbc = new GridBagConstraints();

	    gbc.gridwidth = GridBagConstraints.REMAINDER;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    gbc.anchor = GridBagConstraints.NORTHWEST;

	    panel = new JPanel();
	    panel.setLayout(gbl);

	    draw = new JButton("Draw");
	    gbl.setConstraints(draw,gbc);
	    panel.add(draw);
	    draw.addActionListener(this);

	    layout = new JButton("Layout");
	    gbl.setConstraints(layout,gbc);
	    panel.add(layout);
	    layout.addActionListener(this);

	    printer = new JButton("Print");
	    gbl.setConstraints(printer,gbc);
	    panel.add(printer);
	    printer.addActionListener(this);

	    quit = new JButton("Quit");
	    gbl.setConstraints(quit,gbc);
	    panel.add(quit);
	    quit.addActionListener(this);

	    getContentPane().add("Center", jsp);
	    getContentPane().add("West", panel);

	    setVisible(true);
	    jsp.setViewportView(gp);
	}

	public void actionPerformed(ActionEvent evt) {
	    if(evt.getSource() instanceof JButton) {
		JButton tgt = (JButton)evt.getSource();
		if(tgt == draw) {
		    graph.repaint();
		} else if(tgt == quit) {
		    System.exit(0);
		} else if(tgt == printer) {
		    graph.printGraph(System.out);
		    System.out.flush();
		} else if(tgt == layout) {
		    Object connector = null;
		    try {
			connector = Runtime.getRuntime().exec(ShowWindow.SCRIPT);
		    } catch(Exception ex) {
			log.error("Exception while setting up Process: " + ex.getMessage() + "\nTrying URLConnection...", ex);
			connector = null;
		    }
		    if(connector == null) {
			try {
			    connector = (new URL("http://www.research.att.com/~john/cgi-bin/format-graph")).openConnection();
			    URLConnection urlConn = (URLConnection)connector;
			    urlConn.setDoInput(true);
			    urlConn.setDoOutput(true);
			    urlConn.setUseCaches(false);
			    urlConn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			} catch(Exception ex) {
			    log.error("Exception while setting up URLConnection: " + ex.getMessage() + "\nLayout not performed.", ex);
			    connector = null;
			}
		    }
		    if(connector != null) {
			if(!GrappaSupport.filterGraph(graph,connector)) {
				log.error("ERROR: somewhere in filterGraph");
			}
			if(connector instanceof Process) {
			    try {
				int code = ((Process)connector).waitFor();
				if(code != 0) {
					log.error("WARNING: proc exit code is: " + code);
				}
			    } catch(InterruptedException ex) {
			    	log.error("Exception while closing down proc: " + ex.getMessage(), ex);
			    }
			}
			connector = null;
		    }
		    graph.repaint();
		}
	    }
	}
    }
}
