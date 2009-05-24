package appman.clustering;

import java.io.Serializable;
import java.util.*;

public class DAGNode implements Cloneable, Serializable{

	private static final long serialVersionUID = 6517054966199226876L;
	public String nodeName;
	public int nodeIndex;
	public double weight;
	//protected Vector edges; // vector of string???
	// public Vector edgesWeight; // vector of DAGEdge???
	/** predecessors of this node. It is a vector of DAGEdge */
	protected Vector predecessors; // era edges
	protected Vector predecessorsName; // => acho que nao precisa!!!
	/** predecessors of this node. It is a vector of DAGEdge */
	protected Vector successors;
	protected Vector successorsName; // => acho que nao precisa!!!
	public int 	  status; // add vdn - used in show window
		
	
    public String executable;

	public DAGNode(){
		//edges = new Vector();
		//edgesWeight = new Vector();
		predecessors = new Vector();
		predecessorsName = new Vector();
		successors = new Vector();
		successorsName = new Vector();

	}
	
	/**
	 * 
	 */
	public Vector getPRED(){
		return predecessors;
	}

	/**
	 * 
	 */
	public Vector getSUCC(){
		return successors;
	}
	
	/**
	 *  
	 */
	@Override
	public Object clone() {
		DAGNode copy = new DAGNode();
		copy.nodeName = this.nodeName;
		copy.weight = this.weight;
		copy.nodeIndex = this.nodeIndex;
		//copy.edges = (Vector) this.edges.clone();
		//copy.edgesWeight = (Vector) this.edgesWeight.clone();
		copy.successors = (Vector) this.successors.clone();
		copy.successorsName = (Vector) this.successorsName.clone();
		copy.predecessors = (Vector) this.predecessors.clone();
		copy.predecessorsName = (Vector) this.predecessorsName.clone();
		return copy;
	}
	
//  vdn 2005/03/18
    public void setExecutable(String executable) {
        this.executable = executable;
    }
                                                                                          
//      vdn 2005/03/18
    public String setExecutable() {
        return this.executable;
    }
}
