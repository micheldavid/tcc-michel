/*
 * GRAND - kayser@cos.ufrj.br (c) 2004
 * AssignmentBody.java - stores information about variables, to allow a 
 *    correct value assignment during tree visiting
 * 2004/08/02
 */

package appman.parser;

import java.util.LinkedList;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class  AssignmentBody {
	private static final Log log = LogFactory.getLog(AssignmentBody.class);
   // possible variable types
   public static final int STRING   = 0;
   public static final int CONSTANT = 1;
   public static final int LIST     = 2;
   public static final int VARIABLE = 3;
   public static final int OPERATION= 4;

   private int type;

   // ground set to true means that a String or Constant value is already 
   // set to this variable; set to false means it has some variable(s) to
   // to be evaluated
    //   private boolean ground; // default is true

   // value to the assigned to the variable
   private LinkedList value;

   public AssignmentBody() {
       //       ground = true; 
       value = new LinkedList();
   }


   public void setAsString() {
      this.type = STRING;
   }

   public void setAsConstant() {
      this.type = CONSTANT;
   }

   public void setAsList() {
      this.type = LIST;
   }

   public void setAsVariable() {
      this.type = VARIABLE;
   }

   public void setAsOperation() {
      this.type = OPERATION;
   }

   public void addValue(int type, Object value) {
      //  addLast: Appends the given element to the end of the list
      Node node = new Node(type, value); 
      this.value.addLast(node);
   }	

   public void updateSymbolTable(String variable, SymbolTable symbolTable) {
      symbolTable.putVariable(variable, this.evaluate(symbolTable)) ;
   }

   public Object evaluate(SymbolTable symbolTable){
      // getFirst: Returns the first element in the list
       // Node n = (Node) this.value.getFirst();    

      //  listIterator : Returns a list-iterator of the elements in
      // this list (in proper sequence), starting at the specified 
      // position in the list.
      ListIterator l = value.listIterator(0);
      Node n = (Node) l.next(); 
      ///log.debug("\tFirst element in the assignment: "+n.value);

      switch (this.type) {
        case STRING:
	    return n.value;
        case CONSTANT:
	    return n.value;
        case LIST:
	    return n.value;
        case VARIABLE:
            return symbolTable.getVariable(""+n.value);
        case OPERATION:
            Object evaluatedValue = new Object();
            Node firstOp = n;
            Node operator;
            Node secondOp;
           // calculate
            while (l.hasNext()){
              operator = (Node) l.next();
              secondOp = (Node) l.next();
              firstOp = this.calculate(firstOp, secondOp, operator, symbolTable);
              evaluatedValue = firstOp.value;
	    }
            log.debug("\tAssigment: "+evaluatedValue);
            return evaluatedValue;
      }

      return new Object(); // error!
   }

   private Node calculate(Node firstOp, Node secondOp, Node operator, SymbolTable symbolTable) {
       Node result = new Node(STRING,"");
       if ((firstOp.type == STRING) || (secondOp.type == STRING)) {
          if ((operator.value).equals("+")) {
	     result.type = STRING;             
             result.value = ""+firstOp.getValueAsString(symbolTable)+
                               secondOp.getValueAsString(symbolTable);
	  }
       } else if ((firstOp.type == VARIABLE) && (secondOp.type == VARIABLE)) {
	     result.type = STRING;             
             result.value = ""+firstOp.getValueAsString(symbolTable)+
                               secondOp.getValueAsString(symbolTable);
	     /*
       } else if ((firstOp.type == CONSTANT) || (secondOp.type == CONSTANT)) {
	     result.type = CONSTANT;             
             result.value = ""+firstOp.getValueAsString(symbolTable)+
                               secondOp.getValueAsString(symbolTable);
	     */
       }

      /*
      switch (n.type) {
        case STRING:
	    return n.value;
        case CONSTANT:
	    return n.value;
        case VARIABLE:
            return symbolTable.getVariable(n.value);
        case OPERATION:
            // calculate
            ;
      }
      */

      return result;
   }

  /*
   * Node class
   */
   public class Node {
      int type;
      Object value;

      public Node (int type, Object value) {
         this.type = type;
         this.value = value;
      }

      public String getValueAsString(SymbolTable symbolTable) {
	 switch (this.type) {
	 case STRING: 
	    return toStringWithoutQuotationMark(""+this.value);
           case CONSTANT:
	    return ""+this.value;
           case OPERATION:
            return ""+this.value;
           case VARIABLE:
            return toStringWithoutQuotationMark(""+symbolTable.getVariable(""+this.value));
	 }
         return ""; // error!
      }

      private String toStringWithoutQuotationMark(String s) {
         ///log.debug ("\t\tstring: "+s);
      	 if (s.length()==0) return s;
      	 
         if ((s.charAt(0) != '\"') && (s.charAt(s.length()-1) != '\"')) {
            // nothing to do
	    return s;
         } else{
            String value = s.substring(1,s.length() -1);
	    ///log.debug ("\t\tsubstring: "+value);
            return value;
         }

      }
   }


}
