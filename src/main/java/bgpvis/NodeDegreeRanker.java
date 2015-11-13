package bgpvis;

import static bgpvis.AsPath.validate;
import static bgpvis.util.StringUtil.trim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bgpvis.util.MyFileWriter;
import bgpvis.validation.ValidationResult;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

public final class NodeDegreeRanker {
	private static final Logger log = LoggerFactory.getLogger(NodeDegreeRanker.class);
	private static final String IN_FILE_PATH = System.getProperty("bgp.in.file");
	private static final String OUT_FILE_PATH = System.getProperty("bgp.out.file");
	private static final int TOP_K = Integer.parseInt(System.getProperty("bgp.in.top-k"));
		
	/**
	 * ASPATH attribute name is not present in Task 2 input file. 
	 */
	private static final boolean ASPATH_ATTRIBUTE_PRESENT = false;

	/**
	 * Use for collection sizing
	 */
	private static final int EXPECTED_NUMBER_OF_AS_PATHS = 649412 * 2;

	private NodeDegreeRanker() {
		// Private constructor, not meant to be instantiated
	}

	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
		
		BufferedReader br = null;
		File file = new File(IN_FILE_PATH);
		String line = "";
		ValidationResult validation;
		List<String> asPaths = new ArrayList<String>(EXPECTED_NUMBER_OF_AS_PATHS);
		try {
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				line = trim(line);
				
				// Skip lines that are not AS paths 
				// (such as the last two lines in Task 1 output file)
				
				validation = validate(line, ASPATH_ATTRIBUTE_PRESENT); 
				if (validation.hasErrors()) {
					log.warn("{}", validation);
					continue;
				}
				asPaths.add(line);
			}
			
			// Get neighbours of each AS
			
			Multimap<String, String> neighbours = AsGraph.neighboursByAs(asPaths);
			
			// Index AS by node degree (number of adjacent neighbours)
			
			TreeMultimap<Integer, String> asByNodeDegree = AsGraph.asByNodeDegree(neighbours);
			
			// Get top k ASes by largest node degree
			
			List<String> out = AsGraph.top(asByNodeDegree, TOP_K);
			file = MyFileWriter.write(out, OUT_FILE_PATH);
			log.info("Saved {}", file.getAbsolutePath());
		} finally {
			if (br != null) {
				br.close();
			}
		}
		long elapsedTime = System.currentTimeMillis() - startTime;
		log.info("Done! Run time: {}s\n", elapsedTime / 1000);
	}
	
	

}
