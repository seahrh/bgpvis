package bgpvis;

import static bgpvis.util.StringUtil.concat;
import static bgpvis.util.StringUtil.split;
import static bgpvis.util.StringUtil.trim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bgpvis.util.MyFileWriter;

import com.google.common.base.CharMatcher;

public final class AsClassifier {
	private static final Logger log = LoggerFactory.getLogger(AsClassifier.class);
	private static final String IN_FILE_PATH = System.getProperty("bgp.in.file");
	private static final String OUT_FILE_PATH = System.getProperty("bgp.out.file");
	private static final int EXPECTED_NUMBER_OF_EDGES = 60000;
	private static final String STUB = "stub";
	private static final String REGIONAL_ISP = "regional ISP";
	private static final String DENSE_CORE = "dense core";
	private static final String TRANSIT_CORE = "transit core";
	private static final String OUTER_CORE = "outer core";

	private AsClassifier() {
		// Private constructor, not meant to be instantiated
	}

	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
		BufferedReader br = null;
		File file = new File(IN_FILE_PATH);
		String line = "";
		List<List<String>> in = new ArrayList<>(EXPECTED_NUMBER_OF_EDGES);
		List<String> tokens;
		try {
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				line = trim(line);
				tokens = split(line, CharMatcher.WHITESPACE);
				in.add(tokens);
			}
			Map<String, Map<String, String>> relationships = relationships(in);
			log.info("Start state: {} relationships", relationships.size());

			Set<String> stubs = AsGraph.stubs(relationships);
			relationships = AsGraph.removeStubs(relationships);
			log.info("Removed {} stubs: {} relationships left", stubs.size(),
					relationships.size());

			Set<String> isps = AsGraph.regionalIsps(relationships, stubs);
			relationships = AsGraph.removeRegionalIsps(relationships, stubs);
			log.info("Removed {} regional ISPs: {} relationships left",
					isps.size(), relationships.size());

			Set<String> denseCores = AsGraph.denseCores(relationships);
			Set<String> transitCores = AsGraph.transitCores(relationships,
					denseCores);
			Set<String> outerCores = AsGraph.outerCores(relationships,
					denseCores, transitCores);
			int nDenseCores = denseCores.size();
			int nTransitCores = transitCores.size();
			int nOuterCores = outerCores.size();
			int nCores = nDenseCores + nTransitCores + nOuterCores;
			log.info(
					"Total {} cores: {} dense cores, {} transit cores, {} outer cores",
					nCores, nDenseCores, nTransitCores, nOuterCores);

			List<String> out = format(stubs, STUB);
			out.addAll(format(isps, REGIONAL_ISP));
			out.addAll(format(denseCores, DENSE_CORE));
			out.addAll(format(transitCores, TRANSIT_CORE));
			out.addAll(format(transitCores, OUTER_CORE));
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

	private static List<String> format(Set<String> ases, String classLabel) {
		List<String> ret = new ArrayList<>(ases.size());
		for (String as : ases) {
			ret.add(concat(as, " ", classLabel));
		}
		return ret;
	}

	/**
	 * Returns a nested Map representation of the relationship graph.
	 * 
	 * @param relationships
	 * @return
	 */
	private static Map<String, Map<String, String>> relationships(
			List<List<String>> relationships) {
		Map<String, Map<String, String>> ret = new HashMap<>(
				relationships.size());
		String as1, as2, relationship;
		Map<String, String> temp;
		for (List<String> edge : relationships) {
			as1 = edge.get(0);
			as2 = edge.get(1);
			relationship = edge.get(2);
			temp = ret.get(as1);
			if (temp == null) {
				temp = new HashMap<>();
			}
			temp.put(as2, relationship);
			ret.put(as1, temp);
		}
		return ret;
	}

}
