/*
 * GRAND - kayser@cos.ufrj.br,vdalto@gmail.com (c) 2004
 * DAG_DSC.java - stores the DAG (Directed Acyclic Graph) of the application
 * 2004/04/30; 2004/11/15; 2005/04/01
 */

package appman.clustering;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import appman.OutputXML;

/**
 * It is a version of DAG that stores nodes' weight and also edges' weight.
 */
public class DAG_DSC extends AbstractDAG implements Serializable {

	private static final long serialVersionUID = 514066862412628229L;
	private static final Log log = LogFactory.getLog(DAG_DSC.class);

	/** to implementent iterator interface */
	protected int iteratorIndex = 0;

	// Attributes related to DAG

	/** to implementent iterator interface */
	private List nodesCopy;
	private Hashtable hashIndex;
	/**
	 * This structure is something like that: <br>
	 * |DAGNode1| -> |DAGEdge1| -> |DAGEdge2| -> |DAGEdge3| <br>
	 * |DAGNode2| -> |DAGEdge4| <br>
	 * |DAGNode3| -> |DAGEdge5| -> |DAGEdge6| ->... <br>
	 * ......... -> .......... <br>
	 *  
	 */
	Vector dagNodes; //list of DAGNode, similar to DAG's nodesName
	
	 //private int[] dagColors;
     
	 public static final int GREEN = 0;
	 public static final int YELLOW = 1;
	 public static final int BLUE = 2;
	 public static final int RED = 3;
	 
	 //public transient ShowWindow win;
	 public Vector cluster;//Tera um valor aki somente depois q chamarem o particionamento clusteringPhase. 
	 protected ClusteringPhase cp;


	/**
	 * Constructor; only available; requires the number of nodes.
	 * 
	 * @param numberOfNodes
	 *            specifies the number of nodes of this graph
	 */
	public DAG_DSC(int numberOfNodes) {

		dagNodes = new Vector(numberOfNodes);
		
		//dagColors =  new int[numberOfNodes];
		// TODO: pending..
		//win = new ShowWindow();
		hashIndex = new Hashtable(numberOfNodes);
	}

	/**
	 * Inserts a node into the DAG.
	 * 
	 * @param name
	 *            identifier of the node
	 * @param weight
	 *            node's weight
	 */
	@Override
	public DAGNode putNodeName(String name, double weight) {

		DAGNode dn = new DAGNode();

		dn.nodeName = name;
		
		if( name.compareTo("") == 0){
			dn.nodeName = "task"+dagNodes.size();
		}


		dn.nodeIndex = this.dagNodes.size(); // added in the end of dagNodes list
		dn.weight = weight;
		dn.status = RED;
		
		this.dagNodes.add(dn);
		
		hashIndex.put(name, new Integer((dagNodes).indexOf(dn)) );
		
		
		//dagColors[dagNodes.size()-1] = RED;

		return dn;
	}

	/**
	 * Inserts a node into the DAG with a default weight one.
	 * 
	 * @param name
	 *            identifier of the node
	 */
	@Override
	public DAGNode putNodeName(String name) {

        // 1 is the default value
		DAGNode dn = this.putNodeName(name,1);
		hashIndex.put(name, new Integer((dagNodes).indexOf(dn)) );
		return dn;
	}

	/**
	 * Inserts a edge into the DAG.
	 * 
	 * @param name
	 *            source of the edge
	 * @param incoming
	 *            destination of the edge
	 * @param weight
	 *            edge's weight
	 */
	@Override
	public void insertEdges(String name, String incoming, double weight) {

		DAGNode dn_in;
		DAGNode dn_out;
		DAGEdge de1 = new DAGEdge();
		DAGEdge de2 = new DAGEdge();
// TODO: ao inves de dois DAGEdge, usar o mesmo, marcando la origem e destino ao inves de apenas um nome
		
		dn_in = getNodeByName(name);
		dn_out = getNodeByName(incoming);

		de1.nodeName = incoming;
		de1.nodeIndex = dn_out.nodeIndex; // pkvm 2005/04/06: was dn_in.nodeIndex
		de1.weight = weight;
//		((List) dn.edgesWeight).add(de);
//		((List) dn.edges).add(incoming);
		((List) dn_in.predecessors).add(de1);
		((List) dn_in.predecessorsName).add(incoming);

		de2.nodeName = name;
		de2.nodeIndex = dn_in.nodeIndex; // pkvm 2005/04/06: was dn_out.nodeIndex;
		de2.weight = weight;
//		((List) dn.edgesWeight).add(de);
//		((List) dn.edges).add(incoming);
		((List) dn_out.successors).add(de2);
		((List) dn_out.successorsName).add(name);
	}

	/**
	 * Inserts a edge into the DAG with default weight of one.
	 * 
	 * @param name
	 *            source of the edge
	 * @param incoming
	 *            destination of the edge
	 */
	@Override
	public void insertEdges(String name, String incoming) {
/*
		DAGNode dn;
		DAGEdge de = new DAGEdge();

		dn = getNodeByName(name);

		de.nodeName = incoming;
		de.nodeIndex = dn.nodeIndex;
		de.weight = 1; // default value

//		((List) dn.edgesWeight).add(de);
//		((List) dn.edges).add(incoming);
		((List) dn.predecessors).add(de);
		((List) dn.predecessorsName).add(incoming);
*/
		DAGNode dn_in;
		DAGNode dn_out;
		DAGEdge de1 = new DAGEdge();
		DAGEdge de2 = new DAGEdge();
// TODO: ao inves de dois DAGEdge, usar o mesmo, marcando la origem e destino ao inves de apenas um nome
		
		dn_in = getNodeByName(name);
		dn_out = getNodeByName(incoming);

		de1.nodeName = incoming;
		de1.nodeIndex = dn_out.nodeIndex;
		de1.weight = 1; // default value
//		((List) dn.edgesWeight).add(de);
//		((List) dn.edges).add(incoming);
		((List) dn_in.predecessors).add(de1);
		((List) dn_in.predecessorsName).add(incoming);

		de2.nodeName = name;
		de2.nodeIndex = dn_in.nodeIndex;
		de2.weight = 1; // default value
//		((List) dn.edgesWeight).add(de);
//		((List) dn.edges).add(incoming);
		((List) dn_out.successors).add(de2);
		((List) dn_out.successorsName).add(name);
	}

	/**
	 * Auxiliar method used by insertEdges. It gets a reference to a specific
	 * node.
	 * 
	 * @param name
	 *            idenfication of the node
	 */
	// pkvm: troquei de private para public
	public DAGNode getNodeByName(String name) {
		
		
		DAGNode dn = null;

		for (int i = 0; i < dagNodes.size(); i++) {
			if ((((DAGNode) dagNodes.get(i)).nodeName).compareTo(name) == 0) {
				dn = ((DAGNode) dagNodes.get(i));
				return dn;
			}
		}
		return dn;
		
	}
	
	/**
	 * Get the DAGNode object throught of node name in the original DAG. (HashTable) 
	 * Inserido por VDN:18/07/05
	 * 
	 */
	public DAGNode getNodeOfDAGOriginalByName(String name) {
		Integer index = (Integer)hashIndex.get(name);
		return ((DAGNode) dagNodes.get( index.intValue() ));
		
	}

    /**
     * 
     */
	 public DAGNode getNodeByIndex(int index) {
	 	return ((DAGNode) dagNodes.get(index));
     }

	
	/**
	 * Auxiliar method used by several methods including blevel and tlevel
	 * methods.
	 * 
	 * @param name
	 *            idenfication of the node
	 * @return node index; returns -1 if index not found
	 */
	public int getIndexByName(String name) {
		
		int index = -1;
		for (int i = 0; i < dagNodes.size(); i++) {
			if ((((DAGNode) dagNodes.get(i)).nodeName).compareTo(name) == 0) {
				index = i;
				return index;
			}
		}
		return index;
		
	}
	
	/**
	 * Get the index in Original DAG throught of the node name (HashTable)
	 * @since 18/07/05
	 */
	public int getIndexOfDAGOriginalByName(String name) {
		Integer index = (Integer)hashIndex.get(name);
		return index.intValue();
		
	}

	/**
	 * Auxiliar method used by DSC
	 * 
	 * @param name
	 *            idenfication of the node
	 * @return node index; returns -1 if index not found
	 */
	public String getNameByIndex(int n) {

		String name = ((DAGNode) dagNodes.get(n)).nodeName;
		return name;

	}

	/**
	 * @returns number of nodes
	 */
	@Override
	public int getNumberOfNodes() {

		return dagNodes.size();

	}

	/**
	 * @returns the weight of a specific node
	 */
	public double getNodeWeight(String node) {

		DAGNode dn = new DAGNode();
		dn = getNodeByName(node);

		return dn.weight;

	}

	/**
	 * @returns the weight of a specific node
	 */
	public double getNodeWeight(int node) {

		DAGNode dn = new DAGNode();
		dn = getNodeByIndex(node);

		return dn.weight;

	}

	/**
	 * @returns the weight of a specific edge A -> B
	 */
	public double getEdgeWeight(String A, String B) {

		DAGNode dn = new DAGNode();
		List edgesWeight = new Vector();
		double weight = -1;

		dn = getNodeByName(A);
		edgesWeight = dn.predecessors;

		for (int i = 0; i < edgesWeight.size(); i++) {
			if ((((DAGEdge) edgesWeight.get(i)).nodeName)
					.compareTo(B) == 0) {
				weight = ((DAGEdge) edgesWeight.get(i)).weight;
			}
		}

		return weight;

	}

	/**
	 * @returns the weight of a specific edge A -> B
	 */
	public double getEdgeWeight(int A, int B) {

		DAGNode dn = new DAGNode();
		List edgesWeight = new Vector();
		double weight = -1;

		dn = getNodeByIndex(A);
		edgesWeight = dn.predecessors;

		for (int i = 0; i < edgesWeight.size(); i++) {
			if ((((DAGEdge) edgesWeight.get(i)).nodeName)
					.compareTo(getNodeByIndex(B).nodeName) == 0) {
				weight = ((DAGEdge) edgesWeight.get(i)).weight;
			}
		}

		return weight;

	}

	
	/**
	 * Gets nodes that does not have predecessors. @return list of nodes
	 * (index); null string delimits the end of the list.
	 */
	public String[] getNodeWithoutPredecessors() {

		String levels[] = new String[dagNodes.size()];
		int noAdj = 0;

		for (int i = 0; i < dagNodes.size(); i++) {
			levels[i] = null;
		}

		for (int i = 0; i < dagNodes.size(); i++) {
			DAGNode dn = (DAGNode) dagNodes.get(i);

//			if ((dn.edges == null) || dn.edges.isEmpty()) {
			if ((dn.predecessorsName == null) || dn.predecessorsName.isEmpty()) {
				levels[noAdj] = dn.nodeName;
				//log.debug("O nodo "+ ((DAGNode)
				// nodesCopy.get(i)).nodeName+" nao tem adjacencias!
				// ("+noAdj+")" );
				noAdj++;
			}

		}

		return levels;

	}

	/*
	 * -----------REMOVE UM NODO ESPECIFICADO-----------------------
	 * 
	 * public void removeNode(String nodeName) {
	 * 
	 * int i = 0; //varre lista de nodos int j = 0; //varre lista de adjacencias
	 * int r = 0; //contador da lista de vertices a ser removidos DAGNode dn;
	 * int index;
	 * 
	 * //log.debug(" nodesNameCopy : " +nodesNameCopy.size()+ " - " //
	 * +index);
	 * 
	 * //log.debug("Removendo "+nodeName);
	 * 
	 * for (i = 0; i < nodesCopy.size(); i++) { dn = (DAGNode) nodesCopy.get(i);
	 * if ((dn.edges != null) && !(dn.edges.isEmpty())) { int limit =
	 * dn.edges.size(); for (j = 0; j < limit; j++) {
	 * 
	 * if (((String) dn.edges.get(j)).compareTo(nodeName) == 0) {
	 * dn.edges.remove(j); //log.debug("Removeu: //
	 * "+nodesNameCopy.get(index)+" em // "+nodesNameCopy.get(i)); j =
	 * nodesCopy.size() + 1; //break no for }
	 *  }
	 *  } }
	 * 
	 * //log.debug("Removeu: "+nodesNameCopy.get(index));
	 * 
	 * nodesCopy.remove(getIndexByNameCopy(nodeName));
	 *  }
	 */

	/**
	 * Removes a node from the original copy (it removes and cannot undo).
	 */
	public void removeNode(String nodeName) {

		int i = 0; //varre lista de nodos
		int j = 0; //varre lista de adjacencias
		int r = 0; //contador da lista de vertices a ser removidos
		DAGNode dn;
		int index;

		//log.debug(" nodesNameCopy : " +nodesNameCopy.size()+ " - "
		// +index);

		for (i = 0; i < dagNodes.size(); i++) {
			dn = (DAGNode) dagNodes.get(i);
			// troquei edges por predecessorsName
			if ((dn.predecessorsName != null) && !(dn.predecessorsName.isEmpty())) {
				int limit = dn.predecessorsName.size();
				for (j = 0; j < limit; j++) {

					if (((String) dn.predecessorsName.get(j)).compareTo(nodeName) == 0) {
						dn.predecessorsName.remove(j);
						//log.debug("Removeu:
						// "+nodesNameCopy.get(index)+" em
						// "+nodesNameCopy.get(i));
						j = dagNodes.size() + 1; //break no for
					}

				}
			}
		}

		log.debug("Removeu: " + nodeName);

		dagNodes.remove(getIndexByName(nodeName));

	}

	/**
	 * 
	 * get the list of nodes without adjacence and remove
	 */
	public void removeNodesWithoutAdjacence() {

		String nodesWithoutAdjacence[];
		nodesWithoutAdjacence = getNodeWithoutPredecessors();

		int i = 0;
		while ((i < nodesWithoutAdjacence.length)
				&& (nodesWithoutAdjacence[i] != null)) {
			removeNode(nodesWithoutAdjacence[i]);
			i++;
		}

	}
	

	/**
	 *  
	 */
	@Override
	public boolean isEmpty() {

		if ((this.dagNodes == null) || (this.dagNodes.size() == 0)) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 *  
	 */
	public void dump() {

		String node;
		String adj;
		List list;

		log.debug("[GRAND]\tDAG dump:");

		if (dagNodes != null) {
			String debug = "";
			for (int i = 0; i < dagNodes.size(); i++) {
				node = ((DAGNode) dagNodes.get(i)).nodeName;
				debug += "   " + node + " (incoming list): ";
//				list = ((DAGNode) dagNodes.get(i)).edges;
				list = ((DAGNode) dagNodes.get(i)).predecessorsName;
				if (list != null) {
					for (int j = 0; j < list.size(); j++) {
						adj = (String) list.get(j);
						debug += " " + adj;
					}
				}
				log.debug(debug);
			}
		}

	}
	
	
	
	/** COLORS
	 * The application task graph is presented with four possible colors <br>
	 * for each node which represents a task: <br>
	 * (a) red: it depends on a data not yet available; <br> 
	 * (b) yellow: task ready to execute, waiting for a free resource; <br>
	 * (c) green: represents a running task; <br>
	 * (d) blue: a finished task (it was already executed). <br>
     */
	
	/**
	 * Create a clusteringPhase and apply the Clustering algorithm. 
	 */
	public Vector clustering(){
		cp = new ClusteringPhase(this);
		
		cluster = cp.clustering();
		return cp.getCluster();
	}
	
	/**
	 * Get ClusteringPhase
	 * @return
	 */
	public ClusteringPhase getCP(){
		return cp;
	}
	
	/**
	 * Change node color(status).
	 *<BR>
	 * Used to display graph; different colors represent state of the node execution <br>
     * 0 - green - executing <br>
     * 1 - yellow - waiting an available cpu; ready to execute <br>
     * 2 - blue - finished <br>
     * 3 - red - waiting for dependent task <br>
     * 
     */
	public void changeColor(int index, int newColor ){

	   //dagColors[index] = newColor;
       ((DAGNode) dagNodes.get(index)).status = newColor;//inserida vindn 9/05
	   //win.changeColorNode(((DAGNode)(this.dagNodes.get(index))).nodeName, colorToString(newColor));
    }

    
   
    
    /**
     *  Write a file in /tmp with the graph description in the DOT language.
     * @throws IOException erro escrevendo arquivo
     */
	public void dumpGraphiz() throws IOException {

		String node;
		String adj;
		List list;
		String color = "red";
		String filename = "graphiz.dot";

		/** dot file used to display graph */
		// um pouco menos dependente da plataforma em que está sendo executado...
		PrintStream out = new PrintStream(filename, "UTF-8");
		out.println("digraph G {");

		//writing clusters in dot format
		if(cp != null){
			//cluster = cp.clustering();
			for(int i=0; i < cluster.size(); i++){
				out.println("subgraph cluster"+i+"{");
				for(int j=0; j < ((Vector)cluster.get(i)).size(); j++){

					node = ((String)((Vector)cluster.get(i)).get(j));
					out.println(node+" [color="+ color +", style=filled];");
				}
				out.println("}");
			}
		}

		///writing edges in Dot format
		if (dagNodes != null) {
			for (int i = 0; i < dagNodes.size(); i++) {
				node = ((DAGNode) dagNodes.get(i)).nodeName;
			
				list = ((DAGNode) dagNodes.get(i)).predecessors;
				if (list != null) {
					for (int j = 0; j < list.size(); j++) {
						log.debug("====> "+list.get(j));
						//adj = (String) list.get(j); => 2005/05/06: estava assim, talvez a linha de baixo introduza um erro
						adj = ((DAGEdge) list.get(j)).nodeName;
						out.println(adj+"->"+node);
					}
				}
			}
		}

		out.print("}");
		out.close();

		//Executar o DOT para gerar o arquivo com as coordenadas para a leitura do GRAPPA
		//Tem q ter o DOT instalado senao nao funciona...
		//Esta parte esta agora no Reader.java
		/*
		try {
			// Execute a command - run a unix script to get DAG formatting
			//String directory = "../../dot/";
			String directory = "../../dot/dotneato/";
			String directoryTMP = "/tmp/";
			//String formatCommand = directory+"formatDemo.sh  < "+directoryTMP+"graphiz.dot > "+directoryTMP+"teste333.dot";
			String formatCommand = directory+"dot "+directoryTMP+"graphiz.dot -o "+directoryTMP+"coordenadas.dot";
			String[] commands = new String[]{"/bin/sh", "-c", formatCommand};

			Process child = Runtime.getRuntime().exec(commands);
            // Get the input stream and read from it
			// Used to sincronize command execution 
			InputStream in = child.getInputStream();
	        in.close();
			/////////////////////////////////////////////////

		} catch (IOException e) {log.debug("ERRRO NA EXECUCAO!!!");}
		*/

		//VDN:Teste
		////////
		OutputXML oxml = new OutputXML();
		oxml.createManifest();
		oxml.writeManifest();
		oxml.closeManifest();
		oxml.createTasks();
		oxml.writeTasks();
		oxml.closeTasks();
		oxml.createEdges();
		oxml.writeEdges();
		oxml.closeEdges();
		oxml.createClusters();
		oxml.writeClusters();
		oxml.closeClusters();
		////////
	}

	
	
	private String colorToString(int intColor) {
		String color="";
		switch( intColor ){
		case GREEN : 
			color = "green";	
			break;
			
		case YELLOW:
			color = "yellow";
			break;
			
		case BLUE:
			color = "blue";
			break;
			
		case RED: 
			color = "red";
			break;
		}
		
		return color;
	}	 
	

	

    /**
     * 
     */
    public List getDAGcopy(){
	   return dagNodes;
	}

	/**
	 * from Iterator interface; Returns true if the iteration has more elements.
	 */
	@Override
	public boolean hasNext() {
		if ((iteratorIndex >= 0) && (iteratorIndex < nodesCopy.size())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * from Iterator interface; Returns the next element in the iteration.
	 */
	@Override
	public Object next() {
		Object element = nodesCopy.get(iteratorIndex);
		iteratorIndex++;
		return element;
	}

	/**
	 * from Iterator interface; Removes from the underlying collection the last
	 * element returned by the iterator (optional operation). This method can be
	 * called only once per call to next.
	 */
	@Override
	public void remove() {
		this.nodesCopy.remove(iteratorIndex - 1);
	}

	/**
	 * to use Iterator interface. Is it necessary??
	 */
	public void resetIterator() {
		nodesCopy = this.getCopy(); // get a copy of dagNodes
		iteratorIndex = 0;
	}

	/**
	 * from Cloneable interface
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {

		Object myclone = super.clone();

		Vector localNodesCopy = this.getCopy();
		((DAG_DSC) myclone).dagNodes = localNodesCopy;

		return myclone;

	}

	/**
	 * Makes a copy of dagNodes attribute (DAG nodes and edges)
	 */
	public Vector getCopy() {

		Vector localNodesCopy = (Vector) dagNodes.clone();
		for (DAGNode node : (Vector<DAGNode>) dagNodes) {
			try {
				localNodesCopy.add(node.clone());
			} catch (CloneNotSupportedException e) {
				throw new Error("DAGNode não implementa Cloneable", e);
			}
		}
		return localNodesCopy;

	}
}
