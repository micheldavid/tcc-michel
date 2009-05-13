/*
 * Created on 22/06/2004
 *
 */
package appman;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import appman.task.Task;
import edu.berkeley.guir.prefuse.graph.DefaultNode;

/**
 * @author lucasa
 * @since 22/08/2004
 */
public class GraphNode extends DefaultNode implements Serializable {
	private static final long serialVersionUID = 6044576322442087817L;

	private Task nodedata;

	public GraphNode(Task data) {
		nodedata = data;
	}
	
	public Task getNodeData() {
		return nodedata;
	}

	public void setNodeData(Task o) {
		nodedata = o;
	}

	/**
	 * Returns a copy of the object, or null if the object cannot
	 * be serialized.
	 */
	public static Object copy(Object orig) {
		try {
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
			return in.readObject();
		} catch(Exception e) {
			throw new Error("impossível clonar GraphNode", e);
		}
	}
}
