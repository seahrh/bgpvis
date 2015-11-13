package bgpvis.util;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
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
	public static final CharMatcher LEFT_CURLY_BRACE = CharMatcher.is('{');
	public static final CharMatcher RIGHT_CURLY_BRACE = CharMatcher.is('}');
	public static final CharMatcher CURLY_BRACES = LEFT_CURLY_BRACE.or(RIGHT_CURLY_BRACE);

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
		String val = Strings.nullToEmpty(s);
		return CharMatcher.WHITESPACE.trimFrom(val);
	}

	public static String concat(Object... objs) {
		StringBuilder sb = new StringBuilder();
		for (Object obj : objs) {
			sb.append(obj);
		}
		return sb.toString();
	}
	
	public static String join(Iterable<?> parts, String separator) {
		boolean skipNulls = true;
		return join(parts, separator, skipNulls);
	}
	
	public static String join(Iterable<?> parts, String separator, boolean skipNulls) {
		Joiner j = Joiner.on(separator);
		if (skipNulls) {
			j.skipNulls();
		}
		return j.join(parts);
	}

	public static List<String> split(String s, CharMatcher separator, boolean omitEmptyStrings) {
		Splitter sp = Splitter.on(separator).trimResults();
		if (omitEmptyStrings) {
			sp.omitEmptyStrings();
		}
		return sp.splitToList(s);
	}

	public static List<String> split(String s,
			CharMatcher separator) {
		boolean omitEmptyStrings = true;
		return split(s, separator, omitEmptyStrings);
	}

	public static boolean truthy(String s) {
		String val = trim(s).toLowerCase();
		if (TRUTHY_VALUES.contains(val)) {
			return true;
		}
		return false;
	}
}
