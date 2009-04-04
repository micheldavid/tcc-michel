package appman.parser;
/*
 * GRAND - kayser@cos.ufrj.br (c) 2004
 * DAG.java - stores the DAG (Directed Acyclic Graph) of the application
 * 2004/04/30
 */

import java.io.Serializable;
import java.util.*;

  /**
   *
   */
  public class  DAG implements Serializable
  {
    private List nodes; // taskArray 
    private List nodesName; // taskArray 
    
    public DAG(int numberOfNodes) {
       nodes = new Vector(numberOfNodes);
       for (int i=0; i<numberOfNodes; i++) {
          nodes.add(null);
       }
       nodesName = new Vector(numberOfNodes);
       System.out.println("[GRAND]\tNew DAG");
    }

    /**
     *
     */
    public void putNodeName(String name){
       this.nodesName.add(name);
    }
    
    /**
     *
     */
    public void insertEdges(String name, String incoming) {
       List edges = this.getIncomingEdgesByName(name);
       edges.add(incoming);
    }

    /**
     *
     */
    private List getIncomingEdgesByName(String name){
       List nodesList = null;
       int index = this.nodesName.indexOf(name);
       // System.out.println("!!!!! "+ index);
       nodesList = (List) this.nodes.get(index);
       if (nodesList == null) {
          nodesList = new ArrayList();
          this.nodes.add(index,nodesList);
       }
       return nodesList;
    }


  }
