package bgpvis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NodeDegreeRanker {
	private static final Logger log = LoggerFactory.getLogger(NodeDegreeRanker.class);

	/**
	 * Use for collection sizing
	 */
	private static final int EXPECTED_NUMBER_OF_INPUT_AS_PATHS = 649413 * 2;

	private NodeDegreeRanker() {
		// Private constructor, not meant to be instantiated
	}

	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
		String inFilePath = System.getProperty("bgp.in.file");
		String outFilePath = System.getProperty("bgp.out.file");
		BufferedReader br = null;
		File file = new File(inFilePath);
		String line = "";
		try {
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}
		long elapsedTime = System.currentTimeMillis() - startTime;
		log.info("Done! Run time: {}s\n", elapsedTime / 1000);
	}

}
