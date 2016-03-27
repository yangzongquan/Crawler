package com.crawler.aiwei;

import java.io.File;

public class FileUtil {
	
	public static long getSizeOfDirectory(final File file) {
		if (file.isFile()) {
			return file.length();
		}
		final File[] children = file.listFiles();
		long total = 0;
		if (children != null) {
			for (final File child : children) {
				total += getSizeOfDirectory(child);
			}
		}
		return total;
	}
}
