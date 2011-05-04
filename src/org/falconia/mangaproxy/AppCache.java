package org.falconia.mangaproxy;

import java.io.File;
import java.io.IOException;

public final class AppCache {

	public static boolean checkCacheForData(String url) {
		String key = hashKey(url);
		try {
			File data = new File(getCacheDirectory() + key);
			return data.exists();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private static String hashKey(String url) {
		return Integer.toHexString(url.hashCode()).toUpperCase();
	}

	private static String getCacheDirectory() throws IOException {
		if (!AppEnv.isExternalStorageMounted())
			throw new IOException("SD Card is not mounted.");
		return AppEnv.getExternalDataDirectory() + "Cache/";
	}

}
