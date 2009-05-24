/*
 * GRAND - kayser@cos.ufrj.br (c) 2004
 * SimpleParserDumpVisitor - implements SimpleParserVisitor interface
 *                         - allows the tree to be analysed
 * 2004/07/09
 */

package appman.parser;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SimpleParserDumpVisitor implements SimpleParserVisitor {
	private static final Log log = LogFactory.getLog(SimpleParserDumpVisitor.class);
   private int indent = 0;
   private ApplicationDescription appDescription;
   private SymbolTable  symbolTable;

   // store range variable inside foreach command
   private Object rangeVariable; 

   public SimpleParserDumpVisitor(ApplicationDescription appDescription,
                                  SymbolTable symbolTable) {
       this.appDescription = appDescription;
       this.symbolTable = symbolTable;
   }

   private String indentString() {
	   StringBuilder sb = new StringBuilder();
     for (int i = 0; i < indent; ++i) {
       sb.append(" ");
     }
     return sb.toString();
   }

   public Object visit(SimpleNode node, Object data) {
	   log.debug(indentString() +node +
		       ": acceptor not unimplemented in subclass?");
     ++indent;
     data = node.childrenAccept(this, data);
     --indent;
     return data;
   }

   /*
    * Root Node (entry point)
    */
   public Object visit(ASTRoot node, Object data) {
      ///log.debug(indentString() +"<" + node+">");
      ++indent;
      data = node.childrenAccept(this, data);
      --indent;
      return data;
   }

   /*
    * Task Node
    */
    public Object visit(ASTTask node, Object data) {

      String oldTaskName = node.getTaskName();
      node.setTaskName(this.evaluateString(node.getTaskName()));
      ///log.debug(indentString() +"<" + node+">: "+node.getTaskName());
      
      Vector oldInput = node.getInput();
      node.setInput(this.evaluateVectorOfString(node.getInput()));

      Vector oldOutput = node.getOutput();
      node.setOutput(this.evaluateVectorOfString(node.getOutput()));
      
      ///////////////16/05/05
      String oldExec = node.getExecutable();
      String s = this.evaluateString(node.getExecutable());
      /// log.debug("   executable - after evaluateString= "+s);
      node.setExecutable(s);
      //////////////
      node.addTask(appDescription);

      // PKVM 17/11/2005 - correction to foreach, 
      //   was missing setExecutable to old value
      node.setTaskName(oldTaskName);
      node.setInput(oldInput);
      node.setOutput(oldOutput);
      node.setExecutable(oldExec);

      ++indent;
      data = node.childrenAccept(this, data);
       --indent;
     return data;
   }

   private String evaluateString(String s) {
      String result = "";
      StringTokenizer st = new StringTokenizer(s,"${");
      String st_end="";
      /// log.debug("   < "+s+" >");
      while (st.hasMoreTokens()) {
         StringTokenizer stSubString = new StringTokenizer(st.nextToken(), "}");
         String subString = stSubString.nextToken();
         /// log.debug("   << "+subString+" >>");

         Object obj = symbolTable.getVariableValue(subString);

         
         if (obj != null) {
	        result= result+obj;
         } else {
            result= result+subString;
         }

         if (stSubString.hasMoreTokens()) {
            st_end = stSubString.nextToken();
	     } else {
            st_end="";
	     }
         result = result+st_end;
      }
      
      /// log.debug("   <<<< "+result+" >>>>");
      return result;
   }

   private Vector evaluateVectorOfString(Vector v) {
       Vector newVector = new Vector();
       
       Enumeration e = v.elements();
       while (e.hasMoreElements()) {
          String s = evaluateString((String)e.nextElement());
          StringTokenizer st = new StringTokenizer(s,";");
          while (st.hasMoreTokens()) {
             String file = st.nextToken();
             newVector.addElement(file);
             ///log.debug("string: "+s+"  file: "+file);
	  }
       }

       return newVector;
   }

   /*
    * Foreach Node
    */
    public Object visit(ASTForeachNode node, Object data) {
	///log.debug(indentString() +"<" + node+">");
        ++indent;

        for (int i=0; i<node.getNumberOfIterations(); i++) {
	       ///log.debug("variable="+node.getVariableName()+" value="+node.getRangeElementAt(i));
           symbolTable.putVariable(node.getVariableName(),node.getRangeElementAt(i));
           data = node.childrenAccept(this, data);
	}

        --indent;
	return data;
    }

   /*
    * Assignment Node
    */
    public Object visit(ASTAssignment node, Object data){
	///log.debug(indentString() +"<" + node+">");
        ++indent;
        
        AssignmentBody b= node.getAssignmentBody();
        b.updateSymbolTable(node.getVariableName(),symbolTable);

        data = node.childrenAccept(this, data);
        --indent;
        return data;
    }


}

