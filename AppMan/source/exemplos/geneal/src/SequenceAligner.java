import java.net.InetAddress;
import java.util.ArrayList;

public class SequenceAligner {

	private final int exitCode = 0;

	private final int g = -2;

	private int cursor;

	private ArrayList workerResults;

	private String s;

	private String[] tDB;

	private int maxResults;

	/**
	 * Constructor
	 * 
	 */
	public SequenceAligner(String s, String[] tDB, int maxResults) {
		this.s = s;
		this.tDB = tDB;
		this.maxResults = maxResults;
		this.cursor = 0;
		this.workerResults = new ArrayList();
	}

	/**
	 * getExitCode
	 * 
	 */
	public int getExitCode() {
		return this.exitCode;
	}

	/**
	 * getParams
	 * 
	 */
	public Object getParams() {
		return new Integer(tDB.length);
	}

	/**
	 * hasNext
	 * 
	 */
	public boolean hasNext() {

		if (this.cursor < this.tDB.length) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * next
	 * 
	 */
	public void next() {
		this.cursor++;
	}

	/**
	 * 
	 * 
	 */
	public void process() {

		// first step: fill the matrix
		String s = this.s;
		String t = this.tDB[this.cursor];
		int a[][] = this.sim(s, t);

		// second step: align the sequences
		StringBuffer sAligned = new StringBuffer("");
		StringBuffer tAligned = new StringBuffer("");
		this.align(s, sAligned, s.length(), t, tAligned, t.length(), a);

		// third step: select the best results
		int value = a[s.length()][t.length()];
		this.addResult(sAligned.toString(), tAligned.toString(), value);
	}

	/**
	 * sim
	 * 
	 */
	private int[][] sim(String s, String t) {

		int m = s.length();
		int n = t.length();
		int[][] a = new int[m + 1][n + 1];

		for (int i = 0; i <= m; i++) {
			a[i][0] = i * g;
		}

		for (int j = 0; j <= n; j++) {
			a[0][j] = j * g;
		}

		for (int i = 1; i <= m; i++) {
			for (int j = 1; j <= n; j++) {
				a[i][j] = this.maxValue(a[i - 1][j] + g, a[i - 1][j - 1] + this.p(s, i, t, j), a[i][j - 1] + g);
			}
		}

		return a;
	}

	/**
	 * maxValue
	 * 
	 */
	private int maxValue(int value1, int value2, int value3) {

		return Math.max(Math.max(value1, value2), value3);
	}

	/**
	 * p
	 * 
	 */
	private int p(String s, int i, String t, int j) {

		if (s.charAt(i - 1) == t.charAt(j - 1)) {
			return 1;
		} else {
			return -1;
		}
	}

	/**
	 * align
	 * 
	 */
	private void align(String s, StringBuffer sAligned, int i, String t, StringBuffer tAligned, int j, int[][] a) {

		if ((i == 0) && (j == 0)) {
			sAligned.delete(0, sAligned.length());
			tAligned.delete(0, tAligned.length());

		} else if ((i > 0) && (a[i][j] == (a[i - 1][j] + g))) {
			this.align(s, sAligned, i - 1, t, tAligned, j, a);
			sAligned.append(s.charAt(i - 1));
			tAligned.append("-");

		} else if ((i > 0) && (j > 0) && (a[i][j] == (a[i - 1][j - 1] + this.p(s, i, t, j)))) {
			this.align(s, sAligned, i - 1, t, tAligned, j - 1, a);
			sAligned.append(s.charAt(i - 1));
			tAligned.append(t.charAt(j - 1));

		} else {
			this.align(s, sAligned, i, t, tAligned, j - 1, a);
			sAligned.append("-");
			tAligned.append(t.charAt(j - 1));
		}
	}

	/**
	 * addResult
	 * 
	 */
	public void addResult(String sAligned, String tAligned, int value) {
		String hostname = "none";
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
		}
		SequenceAlignResult result = new SequenceAlignResult(sAligned, tAligned, value, hostname);
		int idxResult = 0;

		for (int i = 0; i < this.workerResults.size(); i++) {

			if (result.getValue() > ((SequenceAlignResult) this.workerResults.get(i)).getValue()) {
				idxResult = i;
				break;
			} else {
				idxResult++;
			}
		}

		if (idxResult < this.maxResults) {

			this.workerResults.add(idxResult, result);
			if (this.workerResults.size() > this.maxResults) {
				this.workerResults.remove(this.workerResults.size() - 1);
			}
		}
	}

	/**
	 * getResults
	 * 
	 */
	public ArrayList getResults() {
		return this.workerResults;
	}
}
