package org.falconia.mangaproxy;

import java.io.File;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Environment;

public final class AppEnv {

	public static boolean isExternalStorageMounted() {
		return Environment.getExternalStorageState().startsWith("mounted");
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService("connectivity");
		return manager.getActiveNetworkInfo() != null
				&& manager.getActiveNetworkInfo().isAvailable();
	}

	public static File getExternalFilesDir() {
		return new File(Environment.getExternalStorageDirectory(),
				String.format("Android/data/%s", AppConst.APP_PACKAGE));
	}

	public static File getExternalCacheDir() {
		return new File(getExternalFilesDir(), "cache");
	}

}
