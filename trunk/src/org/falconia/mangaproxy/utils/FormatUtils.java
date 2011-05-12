package org.falconia.mangaproxy.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;

import org.falconia.mangaproxy.App;

import android.util.Log;

public final class FormatUtils {

	public enum FileSizeUnit {
		b, B, KB, MB, GB, TB
	}

	public static final String[] FileSizeUnits = { "k", "B", "KB", "MB", "GB", "TB" };

	public static String getFileSize(double size, FileSizeUnit in) {
		switch (in) {
		case TB:
			size *= 1024;
		case GB:
			size *= 1024;
		case MB:
			size *= 1024;
		case KB:
			size *= 1024;
		case B:
			break;
		case b:
			size /= 8;
		}

		int i;
		for (i = 1; size >= 1000; i++) {
			size /= 1024;
		}

		return String.format("%.3f%s", size, FileSizeUnits[i]);
	}

	public static String getFileSize(String file, String charset) {
		int size = 0;
		try {
			size = file.getBytes(charset).length;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Log.e(App.NAME, e.toString() + ": " + e.getMessage());
		}
		return getFileSize(size, FileSizeUnit.B);
	}

	public static String getFileSizeBtoKB(double size) {
		return String.format("%.3fKB", size / 1024d);
	}

	public static int year2to4(int year) {
		if (year > 100) {
			throw new InvalidParameterException("Invalid year digits.");
		}
		return year < 50 ? year + 2000 : year + 1900;
	}

}
