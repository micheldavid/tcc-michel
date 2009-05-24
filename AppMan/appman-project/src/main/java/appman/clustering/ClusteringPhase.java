/**
 * @author vindn
 */
package appman.clustering;
import java.util.*;
import java.io.*;


public class ClusteringPhase implements Serializable{

	private static final long serialVersionUID = 499369438336117595L;
	//protected DAG_DSC dag_dsc;
	protected DAG_DSC nodes;
	protected Vector cluster;
	protected final int CLUSTER_SIZE_LIMIT=10; //Numero maximo de nodos por nivel
	protected int numberOfLevels=0;
	
	public ClusteringPhase( DAG_DSC dag_dsc ){
		
		//dag_dsc = Dag_dsc;
		try {
		   nodes = (DAG_DSC)dag_dsc.clone();
		} catch (Exception e) {
		   System.out.println("[GRAND]\tError while partitioning - error cloning DAG");
		   System.out.println(e);
		   System.exit(1);
		}
		cluster = new Vector();
	}
	
	
	public Vector clustering(){
		
		
		Vector newLevel;
		DAGNode dn;
		/*
		 * cluster
		 * Vector[0] ->VectorNodeName[0]->VectorNodeName[1]->VectorNodeName[2]  //level 1
		 * Vector[1] ->VectorNodeName[0]->VectorNodeName[1]->VectorNodeName[2]  //level 2
		 * Vector[2] ->VectorNodeName[0]->VectorNodeName[1]->VectorNodeName[2]  //level 3
		 * ......    ->      ......     ->     ....        ->  ......           //level n+1 
		 */
		
		while( (nodes != null) && (!nodes.isEmpty()) ){
			
			String nodesWithoutPred[] = nodes.getNodeWithoutPredecessors();
			newLevel = new Vector();
						
			int i=0;
			int counter = 0;
			while( (i < nodesWithoutPred.length) && (nodesWithoutPred[i] != null) ){

				newLevel.add( nodesWithoutPred[i] );
				//System.out.println("NODOOOOO"+nodesWithoutPred[i]);
				nodes.removeNode( nodesWithoutPred[i] );
				i++;
				counter++;
				if ((counter == CLUSTER_SIZE_LIMIT) && (i<nodesWithoutPred.length) ) {
					if( (newLevel != null) && (nodesWithoutPred.length != 0) ){
						cluster.add( newLevel );
						System.out.println("DEBUG: cluster created = "+cluster.size());
					} else {
						System.out.println("ASSERT: newLevel="+newLevel+" nodesWithoutPred.length "+nodesWithoutPred.length);
					}
					newLevel = new Vector();
					counter = 0;					
				}

			}
			
			
			if( (newLevel != null) && (nodesWithoutPred.length != 0) ){
				cluster.add( newLevel );
				System.out.println("DEBUG: cluster created = "+cluster.size());
			} else {
				System.out.println("ASSERT: newLevel="+newLevel+" nodesWithoutPred.length "+nodesWithoutPred.length);
			}
		}
		
		/*
		for(int i=0; i < cluster.size(); i++ ){
			System.out.print("Level "+i+" ");
			for(int j=0; j < ((Vector)(cluster.get(i))).size(); j++  ){
				System.out.print(((Vector)(cluster.get(i))).get(j) + " " );
			}
			System.out.print("\n");
		}
		*/
		numberOfLevels = cluster.size();
		return cluster;
		
	}
	
	public int getNumberOfLevels(){
		return numberOfLevels;
	}
	
	public Vector getCluster(){
		return cluster;
	}
}