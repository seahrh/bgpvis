package bgpvis.validation;

import static bgpvis.util.StringUtil.*;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;

public final class AsPathValidator {
	private static final Logger log = LoggerFactory.getLogger(AsPathValidator.class);
	private static String ASPATH_ATTRIBUTE = "ASPATH:";
	private static int ASPATH_MIN_NUMBER_OF_TOKENS = 3;

	private AsPathValidator() {
		// Private constructor, not meant to be instantiated
	}

	public static Optional<List<String>> validate(String asPath) {
		List<String> errors = new ArrayList<String>();
		if (Strings.isNullOrEmpty(asPath)) {
			errors.add("AS path must not be null or an empty string");
			return Optional.of(errors);
		}
		if (containsAsSet(asPath)) {
			errors.add("AS path must not contain an AS set as denoted by curly braces");
		}
		List<String> tokens = split(asPath, CharMatcher.WHITESPACE);
		String token = tokens.get(0);
		int size = tokens.size();
		if (size < ASPATH_MIN_NUMBER_OF_TOKENS) {
			errors.add(concat("There must be at least ",
					ASPATH_MIN_NUMBER_OF_TOKENS,
					" tokens in the AS path, comprising of the ASPATH attribute and 2 AS"));
		}
		if (!token.equals(ASPATH_ATTRIBUTE)) {
			errors.add(concat("First token of AS path must be ",
					ASPATH_ATTRIBUTE));
		}
		if (errors.isEmpty()) {
			return Optional.absent();
		}
		return Optional.of(errors);
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

}
