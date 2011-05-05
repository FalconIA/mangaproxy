package org.falconia.mangaproxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public final class AppCache {

	public static final String TAG = "AppCache";
	public static final String NEW_LINE;

	static {
		NEW_LINE = System.getProperty("line.separator");
	}

	public static boolean checkCacheForData(String url, long cacheMinutes) {
		String key = hashKey(url);
		cacheMinutes = (cacheMinutes == 0 ? Long.MAX_VALUE : cacheMinutes) * 1000;
		try {
			File file = new File(getCacheDirectory() + key);
			return file.exists()
					&& System.currentTimeMillis() - file.lastModified() <= cacheMinutes;
		} catch (IOException e) {
		}
		return false;
	}

	public static boolean checkCacheForData(String url) {
		return checkCacheForData(url, 0);
	}

	public static boolean writeCacheForData(String url, String data) {
		String key = hashKey(url);
		File file;
		try {
			file = new File(getCacheDirectory() + key);
		} catch (IOException e) {
			return false;
		}
		if (file.exists())
			file.delete();
		if (!createNewFileWithPath(file))
			return false;
		try {
			FileWriter writer = new FileWriter(file);
			AppUtils.logD(TAG,
					String.format("Write file using %s.", writer.getEncoding()));
			writer.write(data);
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			AppUtils.logE(TAG, "Cannot write cache to file.");
		}
		return false;
	}

	public static String readCacheForData(String url) {
		String key = hashKey(url);
		File file;
		try {
			file = new File(getCacheDirectory() + key);
		} catch (IOException e) {
			return null;
		}
		try {
			if (!file.exists())
				return null;
			StringBuilder data = new StringBuilder();
			FileReader fileReader = new FileReader(file);
			BufferedReader reader = new BufferedReader(fileReader);
			AppUtils.logD(
					TAG,
					String.format("Read file using %s.",
							fileReader.getEncoding()));
			String line;
			while ((line = reader.readLine()) != null)
				data.append(line + NEW_LINE);
			return data.toString();
		} catch (IOException e) {
			e.printStackTrace();
			AppUtils.logE(TAG, "Cannot read cache from file.");
		}
		return null;
	}

	private static String hashKey(String url) {
		return Integer.toHexString(url.hashCode()).toUpperCase();
	}

	private static String getCacheDirectory() throws IOException {
		if (!AppEnv.isExternalStorageMounted()) {
			String msg = "SD Card is not mounted.";
			AppUtils.logE(TAG, msg);
			throw new IOException(msg);
		}
		return AppEnv.getExternalDataDirectory() + "Cache/";
	}

	private static boolean createNewFileWithPath(File file) {
		if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
			AppUtils.logE(TAG, "Cannot create path to write cache.");
			return false;
		}
		try {
			file.createNewFile();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			AppUtils.logE(TAG, "Cannot create file to write cache.");
			return false;
		}
	}
}
