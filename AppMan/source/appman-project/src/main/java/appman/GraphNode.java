/*
 * Created on 22/06/2004
 *
 */
package appman;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import edu.berkeley.guir.prefuse.graph.DefaultNode;

/**
 * @author lucasa
 * @since 22/08/2004
 */
public class GraphNode extends DefaultNode implements Serializable
{
	private Object nodedata;
	public GraphNode(Object data)
	{
		nodedata = data;		
	}
	
	public Object getNodeData()
	{
		return nodedata;
	}
	
	public void setNodeData(Object o)
	{
		nodedata = o;
	}
	
	/**
		 * Returns a copy of the object, or null if the object cannot
		 * be serialized.
		 */
		public static Object copy(Object orig)
		{
			Object obj = null;
			try
			{
				// Write the object out to a byte array
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(bos);
				out.writeObject(orig);
				out.flush();
				out.close();

				// Make an input stream from the byte array and read
				// a copy of the object back in.
				ObjectInputStream in = new ObjectInputStream(
					new ByteArrayInputStream(bos.toByteArray()));
				obj = in.readObject();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
			catch(ClassNotFoundException cnfe) {
				cnfe.printStackTrace();
			}
			return obj;
		}
}
