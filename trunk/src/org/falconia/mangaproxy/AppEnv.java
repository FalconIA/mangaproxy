package org.falconia.mangaproxy;

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

	public static String getExternalDataDirectory() {
		return String.format("%s/Android/data/%s/", Environment
				.getExternalStorageDirectory().getPath(), AppConst.APP_PACKAGE);
	}

}
