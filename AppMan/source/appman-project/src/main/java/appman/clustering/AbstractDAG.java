/*
 * GRAND - kayser@cos.ufrj.br (c) 2004
 * AbstractDAG.java 
 * 2004/11/03
 */

package appman.clustering;
import java.io.Serializable;
import java.util.*;

/**
 * This abstract class is used to define which methods must be availabe when storing
 *  the DAG (Directed Acyclic Graph) of the application.
 */
public abstract class AbstractDAG implements Iterator, Cloneable, Serializable {
	private static final long serialVersionUID = -3558438505548128467L;

	//
	// minimum DAG interface to be implemented by subclasses
	public abstract DAGNode putNodeName(String name);
	public abstract DAGNode putNodeName(String name, double weight);
	public abstract void insertEdges(String name, String incoming);
	public abstract void insertEdges(String name, String incoming, double weight);
	public abstract int getNumberOfNodes();
	public abstract boolean isEmpty();
	//
	// from Iterator interface
	public abstract boolean hasNext();
	public abstract Object next();
	public abstract void remove();
	
	
	
	//
	// from Clonable interface
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
