import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class GenealInitialTask {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// gera como saída um arquivo geneal-database-segments.txt
		// e N arquivos contendo os segmentos da base

		try {
			// recebe como argumento N, o numero de segmentos em que a base deve ser
			// dividida
			int numberOfSegments = Integer.parseInt(args[0]);
			System.out.println("Number of segments: " + numberOfSegments);

			// assume que existe no workdir um arquivo geneal-database.txt
			File databaseFile = new File("geneal-database.txt");

			// calcula o tamanho médio dos segmentos
			long databaseSize = databaseFile.length();
			System.out.println("Database size: " + databaseSize);
			long segmentSize = databaseSize / numberOfSegments;
			System.out.println("Segment size:" + segmentSize);

			// le o arquivo da base
			BufferedReader br = new BufferedReader(new FileReader(databaseFile));

			PrintWriter pw = null;
			String line = null;
			int currentSegmentSize = 0;
			int segmentIdx = 1;
			boolean startNewSegment = true;
			while ((line = br.readLine()) != null) {
				if (startNewSegment) {
					pw = new PrintWriter(new FileWriter(new File("geneal-database-segment-" + segmentIdx + ".txt")));
				}
				pw.println(line);
				currentSegmentSize += line.length();
				if (((currentSegmentSize + line.length()) >= segmentSize) && segmentIdx < numberOfSegments) {
					pw.flush();
					pw.close();
					segmentIdx++;
					currentSegmentSize = 0;
					startNewSegment = true;
				} else {
					startNewSegment = false;
				}
			}
			pw.flush();
			pw.close();
			br.close();

			System.exit(0);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(-1);
		}

	}

}
