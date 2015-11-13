package bgpvis;

import static bgpvis.util.StringUtil.*;

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
	 * AS are separated by whitespace in the output AS path.
	 */
	private static final String OUTPUT_AS_SEPARATOR = " ";
	
	/**
	 * AS are separated by whitespace in the AS path. Comma and curly braces apply to AS set.
	 */
	private static final CharMatcher INPUT_AS_SEPARATOR = CharMatcher.WHITESPACE.or(CURLY_BRACES).or(COMMA);

	private AsPath() {
		// Private constructor, not meant to be instantiated
	}
	
	public static List<String> asList(String asPath) {
		return split(asPath, CharMatcher.WHITESPACE);
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
	
	public static Set<String> asSet(List<String> asPaths) {
		int size = asPaths.size();
		Set<String> ret = new HashSet<String>(size);
		for (String path : asPaths) {
			ret.addAll(asSet(path));
		}
		return ret;
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
		return join(result, OUTPUT_AS_SEPARATOR);
	}

	public static ValidationResult validate(String asPath) {
		boolean attributePresent = true;
		return validate(asPath, attributePresent);
	}

	/**
	 * AS path must not be null or empty string. AS path must not be empty.
	 * First token of the AS path must match the ASPATH attribute name if
	 * present. AS must be a number.
	 * 
	 * @param asPath
	 * @return
	 */
	public static ValidationResult validate(String asPath,
			boolean attributePresent) {
		List<String> errors = new ArrayList<String>();
		if (Strings.isNullOrEmpty(asPath)) {
			errors.add("AS path must not be null or an empty string");
			return new ValidationResult(errors, AsPath.class.getName(), asPath);
		}
		List<String> tokens = split(asPath, INPUT_AS_SEPARATOR);
		if (tokens.isEmpty()) {
			errors.add("AS path must contain at least 1 token");
			return new ValidationResult(errors, AsPath.class.getName(), asPath);
		}
		int size = tokens.size();
		if (attributePresent && size == 1) {
			errors.add("AS path must contain at least 1 AS");
			return new ValidationResult(errors, AsPath.class.getName(), asPath);
		}
		String token;
		if (attributePresent) {
			token = tokens.get(0);
			if (!token.equals(ASPATH_ATTRIBUTE)) {
				errors.add(concat(
						"Attribute label is present, so first token of AS path must be [",
						ASPATH_ATTRIBUTE, "]"));
			}
		}
		for (int i = 1; i < size; i++) {
			token = tokens.get(i);

			// AS token is a number

			if (!ASCII_DIGITS.matchesAllOf(token)) {
				errors.add("AS must be a number");
			}
		}
		return new ValidationResult(errors, AsPath.class.getName(), asPath);
	}

}
