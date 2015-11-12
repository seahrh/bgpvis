package bgpvis.etl;

import static bgpvis.util.StringUtil.concat;
import static bgpvis.AsPath.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bgpvis.util.MyFileWriter;
import bgpvis.validation.ValidationResult;

public final class BgpPreprocessor {
	private static final Logger log = LoggerFactory.getLogger(BgpPreprocessor.class);
	
	/**
	 * Use for collection sizing
	 */
	private static final int EXPECTED_NUMBER_OF_INPUT_AS_PATHS = 4286060 * 2;

	private BgpPreprocessor() {
		// Private constructor, not meant to be instantiated
	}

	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
		String inFilePath = System.getProperty("bgp.in.file");
		String outFilePath = System.getProperty("bgp.out.file");
		BufferedReader br = null;
		File file = new File(inFilePath);
		String line = "";
		ValidationResult validation;
		List<String> out = new ArrayList<String>(EXPECTED_NUMBER_OF_INPUT_AS_PATHS);
		Set<String> asSet = new HashSet<String>(EXPECTED_NUMBER_OF_INPUT_AS_PATHS);
		Set<String> pathSet = new HashSet<String>(EXPECTED_NUMBER_OF_INPUT_AS_PATHS);
		String asPath;

		try {
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				validation = validate(line);
				if (validation.hasErrors()) {
					log.warn("{}", validation);
					continue;
				}

				// Skip AS paths that contain AS set

				if (containsAsSet(line)) {
					continue;
				}
				
				// Convert the line to AS path output format
				
				asPath = removeAttributePrefix(line);
				
				// Remove duplicate ASes from a path
				
				asPath = removeDuplicateAs(asPath);
				
				// Duplicate path
				
				if (pathSet.contains(asPath)) {
					continue;
				}
				pathSet.add(asPath);
				out.add(asPath);
				asSet.addAll(asSet(asPath));
			}
			out.add(concat("Number of ASes: ", asSet.size()));
			out.add(concat("Number of AS paths: ", pathSet.size()));
			file = MyFileWriter.write(out, outFilePath);
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
