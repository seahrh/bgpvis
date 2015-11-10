package bgpvis.util;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

public final class StringUtil {
	private static final Logger log = LoggerFactory.getLogger(StringUtil.class);

	public static final CharMatcher ASCII_DIGITS = CharMatcher.inRange('0', '9');
	public static final CharMatcher LATIN_LETTERS_LOWER_CASE = CharMatcher.inRange(
			'a', 'z');
	public static final CharMatcher LATIN_LETTERS_UPPER_CASE = CharMatcher.inRange(
			'A', 'Z');
	public static final CharMatcher LATIN_LETTERS = LATIN_LETTERS_LOWER_CASE.or(LATIN_LETTERS_UPPER_CASE);
	public static final CharMatcher HYPHEN = CharMatcher.is('-');
	public static final CharMatcher UNDERSCORE = CharMatcher.is('_');
	public static final CharMatcher COMMA = CharMatcher.is(',');
	public static final CharMatcher SEMI_COLON = CharMatcher.is(';');
	public static final CharMatcher TAB = CharMatcher.is('\t');
	public static final CharMatcher HASHTAG = CharMatcher.is('#');

	private static final Set<String> TRUTHY_VALUES = Sets.newHashSet("y",
			"yes", "1");

	private StringUtil() {
		// Private constructor; not meant to be instantiated
	}

	/**
	 * Trim whitespace according to latest Unicode standard (different from
	 * JDK's spec).
	 * 
	 * @param s
	 * @return
	 */
	public static String trim(String s) {
		return CharMatcher.WHITESPACE.trimFrom(s);
	}

	/**
	 * Normalize the string by trimming it. Avoids NullPointerException as nulls
	 * are transformed to an empty string. 'Blank' strings that contain only
	 * whitespace or control characters, will be transformed to an empty string.
	 * 
	 * @param s
	 * @return
	 */
	public static String norm(String s) {
		s = Strings.nullToEmpty(s);
		return trim(s);
	}

	public static String concat(Object... objs) {
		StringBuilder sb = new StringBuilder();
		for (Object obj : objs) {
			sb.append(obj);
		}
		return sb.toString();
	}

	public static List<String> split(String s, CharMatcher separator) {
		String msg;
		if (s == null) {
			msg = "string must not be null";
			log.error(msg);
			throw new IllegalArgumentException(msg);
		}
		return Splitter.on(separator)
			.trimResults()
			.splitToList(s);
	}

	public static List<String> splitAndOmitEmptyStrings(String s,
			CharMatcher separator) {
		String msg;
		if (s == null) {
			msg = "string must not be null";
			log.error(msg);
			throw new IllegalArgumentException(msg);
		}
		return Splitter.on(separator)
			.trimResults()
			.omitEmptyStrings()
			.splitToList(s);
	}

	public static boolean truthy(String s) {
		String val = norm(s).toLowerCase();
		if (TRUTHY_VALUES.contains(val)) {
			return true;
		}
		return false;
	}
}
