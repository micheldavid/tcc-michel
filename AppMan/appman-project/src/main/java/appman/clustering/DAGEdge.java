package appman.clustering;

import java.io.Serializable;

public class DAGEdge implements Serializable{
	private static final long serialVersionUID = -6065360176490690678L;
	public String nodeName;
	public int nodeIndex;
	public double weight;
}
