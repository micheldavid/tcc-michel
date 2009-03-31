import java.io.Serializable;

public class SequenceAlignResult implements Serializable {

	private static final long serialVersionUID = 4301718409665292526L;

	private String sAligned;

	private String tAligned;

	private int value;

	private String source;

	/**
	 * Constructor
	 * 
	 */
	public SequenceAlignResult(String sAligned, String tAligned, int value, String source) {

		this.sAligned = sAligned;
		this.tAligned = tAligned;
		this.value = value;
		this.source = source;
	}

	/**
	 * setSAligned
	 * 
	 */
	public void setSAligned(String sAligned) {
		this.sAligned = sAligned;
	}

	/**
	 * getSAligned
	 * 
	 */
	public String getSAligned() {
		return this.sAligned;
	}

	/**
	 * setTAligned
	 * 
	 */
	public void setTAligned(String tAligned) {
		this.tAligned = tAligned;
	}

	/**
	 * getTAligned
	 * 
	 */
	public String getTAligned() {
		return this.tAligned;
	}

	/**
	 * setValue
	 * 
	 */
	public void setValue(int value) {
		this.value = value;
	}

	/**
	 * getValue
	 * 
	 */
	public int getValue() {
		return this.value;
	}

	/**
	 * setSource
	 * 
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * getSource
	 * 
	 */
	public String getSource() {
		return this.source;
	}

	/**
	 * toString
	 * 
	 */
	public String toString() {

		String ln = System.getProperty("line.separator");

		StringBuffer sb = new StringBuffer();
		sb.append("source: " + this.source);
		sb.append(ln);
		sb.append("score: " + this.value);
		sb.append(ln);
		sb.append("sAligned = " + this.sAligned);
		sb.append(ln);
		sb.append("tAligned = " + this.tAligned);
		sb.append(ln);

		return sb.toString();
	}
}
