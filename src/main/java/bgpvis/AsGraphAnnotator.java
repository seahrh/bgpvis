package bgpvis;

import static bgpvis.AsPath.validate;
import static bgpvis.util.StringUtil.concat;
import static bgpvis.util.StringUtil.trim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bgpvis.util.MyFileWriter;
import bgpvis.validation.ValidationResult;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

public final class AsGraphAnnotator {
	private static final Logger log = LoggerFactory.getLogger(AsGraphAnnotator.class);
	private static final String IN_FILE_PATH = System.getProperty("bgp.in.file");
	private static final String OUT_FILE_PATH = System.getProperty("bgp.out.file");
	private static final int DEGREE_SIZE_RATIO = Integer.parseInt(System.getProperty("bgp.in.degree-size-ratio"));
	private static final int TRANSIT_COUNT_THRESHOLD = Integer.parseInt(System.getProperty("bgp.in.transit-count-threshold"));

	/**
	 * ASPATH attribute name is not present in Task 2 input file.
	 */
	private static final boolean ASPATH_ATTRIBUTE_PRESENT = false;

	/**
	 * Use for collection sizing
	 */
	private static final int EXPECTED_NUMBER_OF_AS_PATHS = 649412 * 2;

	private AsGraphAnnotator() {
		// Private constructor, not meant to be instantiated
	}

	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
		BufferedReader br = null;
		File file = new File(IN_FILE_PATH);
		String line = "";
		ValidationResult validation;
		List<String> asPaths = new ArrayList<String>(
				EXPECTED_NUMBER_OF_AS_PATHS);
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

			// Phase 1 of Task 3 Algorithm 1
			// Get neighbours of each AS

			Multimap<String, String> neighbours = AsGraph.neighboursByAs(asPaths);

			// Map of AS to its node degree

			Map<String, Integer> nodeDegreeByAs = AsGraph.nodeDegreeByAs(neighbours);
			log.info("Count node degree of all ASes: Done!");

			// Phase 2 of Task 3 Algorithm 1
			// Count the number of entries that infer an AS pair having a
			// transit relationship

			Multiset<String> transitCustomerToProvider = AsGraph.transitCustomerToProvider(
					asPaths, nodeDegreeByAs);
			log.info("Count transit relationships: Done!");

			// Phase 3 of Task 3 Algorithm 1
			// Assign relationships to AS pairs
			
			Table<String, String, String> relationships = AsGraph.annotateRelationship(asPaths, transitCustomerToProvider, TRANSIT_COUNT_THRESHOLD);
			log.info("Annotate relationships: Done!");
			
			List<String> out = toString(relationships);
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
	
	private static List<String> toString(Table<String, String, String> relationships) {
		List<String> ret = new ArrayList<String>(relationships.size());
		String values[];
		for (Cell<String, String, String> cell: relationships.cellSet()) {
			values = new String[] {cell.getRowKey(), cell.getColumnKey(), cell.getValue()};
		    ret.add(concat(values, " ")); 
		}
		return ret;
	}

}