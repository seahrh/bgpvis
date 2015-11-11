package bgpvis.etl;

import static bgpvis.util.StringUtil.*;

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
import bgpvis.validation.AsPathValidator;
import bgpvis.validation.ValidationResult;

import com.google.common.base.Strings;
import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;

public final class BgpPreprocessor {
	private static final Logger log = LoggerFactory.getLogger(BgpPreprocessor.class);
	

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
		List<String> asPaths = new ArrayList<String>();
		Set<String> asSet = new HashSet<String>();
		String asPath;
		
		try {
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				validation = AsPathValidator.validate(line);
				if (validation.hasErrors()) {
					log.warn("{}", validation);
					continue;
				}
				
				// Skip AS paths that contain AS set
				
				if (containsAsSet(line)) {
					continue;
				}
				asPath = asPath(line);
				asPaths.add(asPath);
				asSet.addAll(asSet(asPath));
			}
			List<String> out = new ArrayList<String>(asPaths);
			out.add(concat("Number of ASes: ", asSet.size()));
			out.add(concat("Number of AS paths: ", asPaths.size()));
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
	
	/**
	 * Curly braces in the AS path indicates the presence of AS set.
	 * 
	 * @param asPath
	 * @return
	 */
	private static boolean containsAsSet(String asPath) {
		if (CURLY_BRACES.matchesAnyOf(asPath)) {
			return true;
		}
		return false;
	}
	
	private static String asPath(String line) {
		final String PREFIX = "ASPATH: ";
		final int STARTING_INDEX = PREFIX.length();
		return line.substring(STARTING_INDEX);
	}
	
	private static Set<String> asSet(String asPath) {
		return new HashSet<String>(split(asPath, CharMatcher.WHITESPACE));
	}

}
