package org.falconia.mangaproxy;

import java.io.File;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;

public final class App extends Application {

	public enum ZoomMode {
		FIT_WIDTH_OR_HEIGHT(0), FIT_WIDTH_AUTO_SPLIT(1), FIT_WIDTH(2), FIT_HEIGHT(3), FIT_SCREEN(4);

		public static ZoomMode fromValue(int value) {
			switch (value) {
			case 0:
				return FIT_WIDTH_OR_HEIGHT;
			case 1:
				return FIT_WIDTH_AUTO_SPLIT;
			case 2:
				return FIT_WIDTH;
			case 3:
				return FIT_HEIGHT;
			case 4:
				return FIT_SCREEN;
			default:
				return null;
			}
		}

		private final int value;

		ZoomMode(int value) {
			this.value = value;
		}

		public int value() {
			return value;
		}
	}

	public static int DEBUG = 0;

	public static Context CONTEXT;
	public static String NAME;
	public static String PACKAGE;
	public static String VERSION_NAME;
	public static int VERSION_CODE;

	public static SharedPreferences APP_PREFERENCES = null;

	public static File APP_FILES_DIR;
	public static File APP_CACHE_DIR;
	public static File APP_EXTERNAL_FILES_DIR;
	public static File APP_EXTERNAL_CACHE_DIR;

	public static AppSQLite DATABASE;

	public static boolean FIRST_START = true;

	// Genre
	public static String UI_GENRE_ALL_TEXT = "All";

	// Manga
	public static String UI_CHAPTER_COUNT = "Chapters: %s";
	public static String UI_AUTHOR = "Author: %s";
	public static String UI_LAST_UPDATE = "Update: %tF";

	// Chapter
	public static int TIME_AUTO_HIDE = 5000;

	// Settings
	// public static int IMG_PRELOAD_MAX = 2;
	public static float WIDTH_AUTO_SPLIT_MARGIN = .2f;
	public static int MAX_RETRY_DOWNLOAD_IMG = 3;

	static {

	}

	public static SharedPreferences getSharedPreferences() {
		try {
			if (APP_PREFERENCES == null) {
				APP_PREFERENCES = PreferenceManager.getDefaultSharedPreferences(CONTEXT);
			}
			return APP_PREFERENCES;
		} catch (NullPointerException e) {
			e.printStackTrace();
			AppUtils.logE("App", "Null SharedPreferences.");
		}
		return null;
	}

	public static boolean getFavoriteAutoUpdate() {
		return getSharedPreferences().getBoolean("bFavoriteAutoUpdate", false);
	}

	public static int getPageOrientation() {
		return Integer.parseInt(getSharedPreferences().getString("iPageOrientation", "2"));
	}

	public static ZoomMode getPageZoomMode() {
		return ZoomMode.fromValue(Integer.parseInt(getSharedPreferences().getString(
				"iPageZoomMode", "0")));
	}

	public static void setPageZoomMode(ZoomMode mode) {
		APP_PREFERENCES.edit().putString("iPageZoomMode", "" + mode.value()).commit();
	}

	public static int getPreloadPages() {
		return Integer.parseInt(getSharedPreferences().getString("iPreloadPages", "2"));
	}

	public static boolean getShowChangelog() {
		return getSharedPreferences().getInt("iVersionCode", -1) < VERSION_CODE;
	}

	public static void setVersionCode() {
		APP_PREFERENCES.edit().putInt("iVersionCode", VERSION_CODE).commit();
	}

	@Override
	public void onCreate() {
		CONTEXT = this;
		NAME = getString(R.string.app_name);
		PACKAGE = getClass().getPackage().getName();
		try {
			VERSION_NAME = getPackageManager().getPackageInfo(PACKAGE, 0).versionName;
			VERSION_CODE = getPackageManager().getPackageInfo(PACKAGE, 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			AppUtils.logE("App", "Fail to get version code.");
		}

		APP_FILES_DIR = getFilesDir();
		APP_CACHE_DIR = getCacheDir();
		APP_EXTERNAL_FILES_DIR = CONTEXT.getExternalFilesDir(null);
		APP_EXTERNAL_CACHE_DIR = CONTEXT.getExternalCacheDir();

		DATABASE = new AppSQLite(CONTEXT);

		// Genre
		UI_GENRE_ALL_TEXT = getString(R.string.genre_all);

		// Manga
		UI_CHAPTER_COUNT = getString(R.string.ui_chapter_count);
		UI_AUTHOR = getString(R.string.ui_author);
		UI_LAST_UPDATE = getString(R.string.ui_last_update);

		// Crash Handler
		Thread.setDefaultUncaughtExceptionHandler(new CrashExceptionHandler());
	}
}