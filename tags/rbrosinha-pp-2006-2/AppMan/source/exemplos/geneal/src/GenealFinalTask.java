import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class GenealFinalTask {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			int numberOfSegments = Integer.parseInt(args[0]);
			List results = new ArrayList();

			for (int i = 1; i <= numberOfSegments; i++) {

				BufferedReader br = new BufferedReader(new FileReader(new File("geneal-result-" + i + ".txt")));
				String line = null;
				while ((line = br.readLine()) != null) {
					String[] lineSplit = line.split(";");
					results.add(new SequenceAlignResult(lineSplit[0].trim(), lineSplit[1].trim(), Integer.parseInt(lineSplit[2].trim()), lineSplit[3].trim()));
				}
				br.close();
			}
			Collections.sort(results, new GenealFinalTask().new ResultComparator());

			PrintWriter pw = new PrintWriter(new FileWriter(new File("geneal-final-result.txt")));
			int i = 1;
			for (Iterator iter = results.iterator(); iter.hasNext();) {
				SequenceAlignResult result = (SequenceAlignResult) iter.next();
				pw.println(i + ")\t" + result.getSource() + "\t" + result.getValue());
				pw.println("s: " + result.getSAligned());
				pw.println("t: " + result.getTAligned());
				pw.println();
				i++;
			}
			pw.close();

		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(-1);
		}
	}

	class ResultComparator implements Comparator {

		public int compare(Object obj0, Object obj1) {
			int v0 = ((SequenceAlignResult) obj0).getValue();
			int v1 = ((SequenceAlignResult) obj1).getValue();
			return (v0 == v1) ? 0 : (v0 < v1) ? -1 : 1;
		}

	}
}
