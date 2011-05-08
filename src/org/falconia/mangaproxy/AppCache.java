package org.falconia.mangaproxy;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

public final class AppCache {

	public static final String TAG = "AppCache";
	public static final String NEW_LINE;

	static {
		NEW_LINE = System.getProperty("line.separator");
	}

	public static boolean checkCacheForData(String url, long cacheMinutes) {
		String key = hashKey(url);
		cacheMinutes = (cacheMinutes <= 0 ? Long.MAX_VALUE : cacheMinutes * 1000);
		try {
			File file = getExternalCacheFile(key);
			return file.exists()
					&& System.currentTimeMillis() - file.lastModified() <= cacheMinutes;
		} catch (IOException e) {
		}
		return false;
	}

	public static boolean checkCacheForData(String url) {
		return checkCacheForData(url, 0);
	}

	public static boolean checkCacheForImage(String url, String type, long cacheMinutes) {
		String key = hashKey(url);
		cacheMinutes = (cacheMinutes <= 0 ? Long.MAX_VALUE : cacheMinutes * 1000);
		try {
			File file = getExternalCacheImageFile(key, type);
			return file.exists()
					&& System.currentTimeMillis() - file.lastModified() <= cacheMinutes;
		} catch (IOException e) {
		}
		return false;
	}

	public static boolean checkCacheForImage(String url, String type) {
		return checkCacheForImage(url, type, 0);
	}

	public static boolean writeCacheForData(String data, String url) {
		AppUtils.logD(TAG, String.format("Write cache for: %s", url));
		String key = hashKey(url);
		File file;
		try {
			file = getExternalCacheFile(key);
		} catch (IOException e) {
			return false;
		}
		if (!createNewFileWithPath(file)) {
			return false;
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(data);
			writer.flush();
			writer.close();
			AppUtils.logD(TAG, String.format("Wrote file: %s", file.getPath()));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			AppUtils.logE(TAG, String.format("Cannot write cache: %s", file.getPath()));
		}
		return false;
	}

	public static boolean writeCacheForImage(byte[] data, String url, String type) {
		AppUtils.logD(TAG, String.format("Write cache for: %s", url));
		String key = hashKey(url);
		File file;
		try {
			file = getExternalCacheImageFile(key, type);
		} catch (IOException e) {
			return false;
		}
		if (!createNewFileWithPath(file)) {
			return false;
		}
		try {
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
			out.write(data);
			out.flush();
			out.close();
			AppUtils.logD(TAG, String.format("Wrote file: %s", file.getPath()));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			AppUtils.logE(TAG, String.format("Cannot write cache: %s", file.getPath()));
		}
		return false;
	}

	public static String readCacheForData(String url) {
		AppUtils.logD(TAG, String.format("Read cache for: %s", url));
		String key = hashKey(url);
		File file;
		try {
			file = getExternalCacheFile(key);
		} catch (IOException e) {
			return null;
		}
		try {
			if (!file.exists()) {
				AppUtils.logE(TAG, String.format("File not exists: %s", file.getPath()));
				return null;
			}
			StringBuilder data = new StringBuilder();
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				data.append(line + NEW_LINE);
			}
			reader.close();
			AppUtils.logD(TAG, String.format("Read file: %s", file.getPath()));
			return data.toString();
		} catch (IOException e) {
			e.printStackTrace();
			AppUtils.logE(TAG, String.format("Cannot read cache: %s", file.getPath()));
		}
		return null;
	}

	public static Bitmap readCacheForImage(String url, String type) {
		AppUtils.logD(TAG, String.format("Read cache for: %s", url));
		String key = hashKey(url);
		File file;
		try {
			file = getExternalCacheImageFile(key, type);
		} catch (IOException e) {
			return null;
		}
		try {
			if (!file.exists()) {
				AppUtils.logE(TAG, String.format("File not exists: %s", file.getPath()));
				return null;
			}
			Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
			AppUtils.logD(TAG, String.format("Read file: %s", file.getPath()));
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
			AppUtils.logE(TAG, String.format("Cannot read cache: %s", file.getPath()));
		}
		return null;
	}

	private static String hashKey(String url) {
		return Integer.toHexString(url.hashCode()).toUpperCase();
	}

	private static File getExternalCacheFile(String path) throws IOException {
		if (!AppEnv.isExternalStorageMounted()) {
			String msg = "SD Card is not mounted.";
			AppUtils.logE(TAG, msg);
			throw new IOException(msg);
		}
		File dir = AppEnv.getExternalCacheDir();
		if (!TextUtils.isEmpty(path)) {
			dir = new File(dir, path);
		}
		return dir;
	}

	private static File getExternalCacheImageFile(String filename, String type) throws IOException {
		File dir = getExternalCacheFile("images");
		if (TextUtils.isEmpty(filename) || TextUtils.isEmpty(type)) {
			return null;
		}
		dir = new File(dir, String.format("%s/%s", type, filename));
		return dir;
	}

	private static boolean createNewFileWithPath(File file) {
		if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
			AppUtils.logE(TAG, String.format("Cannot create path: %s", file.getParentFile()));
			return false;
		}
		try {
			file.delete();
			return file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			AppUtils.logE(TAG, String.format("Cannot create file: %s", file.getPath()));
			return false;
		}
	}
}
