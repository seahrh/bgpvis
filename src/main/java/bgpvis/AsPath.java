package bgpvis;

import static bgpvis.util.StringUtil.CURLY_BRACES;
import static bgpvis.util.StringUtil.concat;
import static bgpvis.util.StringUtil.split;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bgpvis.validation.ValidationResult;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

public final class AsPath {
	private static final Logger log = LoggerFactory.getLogger(AsPath.class);
	private static final String ASPATH_ATTRIBUTE = "ASPATH:";
	/**
	 * Min 2 tokens in the AS path (ASPATH attribute and 1 AS)
	 */
	private static final int ASPATH_MIN_NUMBER_OF_TOKENS = 2;
	/**
	 * AS are separated by whitespace in the AS path.
	 */
	public static final String AS_SEPARATOR = " ";

	private AsPath() {
		// Private constructor, not meant to be instantiated
	}

	/**
	 * Curly braces in the AS path indicates the presence of AS set.
	 * 
	 * @param asPath
	 * @return
	 */
	public static boolean containsAsSet(String asPath) {
		if (CURLY_BRACES.matchesAnyOf(asPath)) {
			return true;
		}
		return false;
	}

	/**
	 * Remove the ASPATH attribute name prefix
	 * 
	 * @param line
	 * @return
	 */
	public static String removeAttributePrefix(String line) {
		return line.substring(ASPATH_ATTRIBUTE.length());
	}

	/**
	 * Get unique ASes from a AS path
	 * 
	 * @param asPath
	 * @return
	 */
	public static Set<String> asSet(String asPath) {
		return new HashSet<String>(split(asPath, CharMatcher.WHITESPACE));
	}

	/**
	 * Compresses AS prepending by removing duplicate ASes that appear in
	 * sequence in a AS path.
	 * 
	 * @param asPath
	 * @return
	 */
	public static String removeDuplicateAs(String asPath) {
		List<String> asList = split(asPath, CharMatcher.WHITESPACE);
		int nAs = asList.size();
		List<String> result = new ArrayList<String>(nAs);
		String as = "";
		String prev = "";
		for (int i = 0; i < nAs; i++) {
			prev = as;
			as = asList.get(i);

			// Skip duplicate ASes that appear in sequence

			if (prev.equals(as)) {
				continue;
			}

			result.add(as);
		}
		return concat(result, AS_SEPARATOR);
	}

	/**
	 * AS path must not be null or empty string. AS path must have at least 2
	 * tokens; comprising of the ASPATH attribute and 1 AS. First token of the
	 * AS path must match the ASPATH attribute name.
	 * 
	 * @param asPath
	 * @return
	 */
	public static ValidationResult validate(String asPath) {
		List<String> errors = new ArrayList<String>();
		if (Strings.isNullOrEmpty(asPath)) {
			errors.add("AS path must not be null or an empty string");
			return new ValidationResult(errors,  AsPath.class.getName(), asPath);
		}
		List<String> tokens = split(asPath, CharMatcher.WHITESPACE);
		String token = tokens.get(0);
		int size = tokens.size();
		if (size < ASPATH_MIN_NUMBER_OF_TOKENS) {
			errors.add(concat("There must be at least ",
					ASPATH_MIN_NUMBER_OF_TOKENS,
					" tokens in the AS path, comprising of the ASPATH attribute and 1 AS"));
		}
		if (!token.equals(ASPATH_ATTRIBUTE)) {
			errors.add(concat("First token of AS path must be [",
					ASPATH_ATTRIBUTE, "]"));
		}
		return new ValidationResult(errors, AsPath.class.getName(), asPath);
	}

}
