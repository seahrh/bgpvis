package bgpvis.util;

import static bgpvis.util.StringUtil.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public final class MyFileWriter {
	private static final Logger log = LoggerFactory.getLogger(MyFileWriter.class);

	private MyFileWriter() {
		// Private constructor, not meant to be instantiated
	}

	public static File write(List<String> lines, String filePath)
			throws IOException {
		String content = concat(lines, "\n");
		return write(content, filePath);
	}

	public static File write(String content, String filePath)
			throws IOException {
		if (Strings.isNullOrEmpty(filePath)) {
			throw new IllegalArgumentException(
					"File path must not be null or empty string.");
		}
		File file = new File(filePath);
		write(content, file);
		return file;
	}

	public static void write(String content, File file) throws IOException {
		if (content == null) {
			throw new IllegalArgumentException("Content must not be null.");
		}
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(file));
			bw.write(content);
		} finally {
			if (bw != null) {
				bw.close();
			}
		}
	}

}
