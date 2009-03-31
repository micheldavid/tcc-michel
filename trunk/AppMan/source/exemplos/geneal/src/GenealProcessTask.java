import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GenealProcessTask {

	public static void main(String[] args) {
		try {
			int taskIdx = Integer.parseInt(args[0]);
			String inputSequence = args[1];
			BufferedReader br = new BufferedReader(new FileReader(new File("geneal-database-segment-" + taskIdx + ".txt")));
			String line = null;
			List lineList = new ArrayList();
			while ((line = br.readLine()) != null) {
				lineList.add(line);
			}
			br.close();
			SequenceAligner aligner = new SequenceAligner(inputSequence, (String[]) lineList.toArray(new String[0]), 10);
			aligner.process();
			List results = aligner.getResults();
			PrintWriter pw = new PrintWriter(new FileWriter("geneal-result-" + taskIdx + ".txt"));
			for (Iterator iter = results.iterator(); iter.hasNext();) {
				SequenceAlignResult result = (SequenceAlignResult) iter.next();
				pw.println(result.getSAligned() + ";" + result.getTAligned() + ";" + result.getValue() + ";" + result.getSource());
			}
			pw.flush();
			pw.close();
			System.exit(aligner.getExitCode());
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(-1);
		}

	}

}
