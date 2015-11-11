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
	private static final String NAME = "AS Path";
	private static final String ASPATH_ATTRIBUTE = "ASPATH:";
	private static final int ASPATH_MIN_NUMBER_OF_TOKENS = 3;

	private AsPathValidator() {
		// Private constructor, not meant to be instantiated
	}

	public static ValidationResult validate(String asPath) {
		List<String> errors = new ArrayList<String>();		
		if (Strings.isNullOrEmpty(asPath)) {
			errors.add("AS path must not be null or an empty string");
			return new ValidationResult(errors, NAME, asPath);
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
			errors.add(concat("First token of AS path must be [",
					ASPATH_ATTRIBUTE, "]"));
		}
		return new ValidationResult(errors, NAME, asPath);
	}

	

}
