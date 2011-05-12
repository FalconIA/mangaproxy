package org.falconia.mangaproxy;

import java.io.File;

import android.app.Application;
import android.content.Context;

public final class App extends Application {

	public enum ZoomMode {
		FIT_SCREEN, FIT_HEIGHT, FIT_WIDTH, FIT_WIDTH_AUTO_SPLIT
	}

	public static int DEBUG = -1;

	public static Context CONTEXT;
	public static String NAME;
	public static String PACKAGE;

	public static File APP_FILES_DIR;
	public static File APP_CACHE_DIR;
	public static File APP_EXTERNAL_FILES_DIR;
	public static File APP_EXTERNAL_CACHE_DIR;

	public static AppSQLite DATABASE;

	// Genre
	public static String UI_GENRE_ALL_TEXT = "All";

	// Manga
	public static String UI_CHAPTER_COUNT = "Chapters: %s";
	public static String UI_LAST_UPDATE = "Update: %tF";

	// Settings
	public static int IMG_PRELOAD_MAX = 2;
	public static float WIDTH_AUTO_SPLIT_MARGIN = .2f;

	static {

	}

	@Override
	public void onCreate() {
		CONTEXT = getApplicationContext();
		NAME = getString(R.string.app_name);
		PACKAGE = getClass().getPackage().getName();

		APP_FILES_DIR = getFilesDir();
		APP_CACHE_DIR = getCacheDir();
		APP_EXTERNAL_FILES_DIR = CONTEXT.getExternalFilesDir(null);
		APP_EXTERNAL_CACHE_DIR = CONTEXT.getExternalCacheDir();

		DATABASE = new AppSQLite(CONTEXT);

		// Genre
		UI_GENRE_ALL_TEXT = getString(R.string.genre_all);

		// Manga
		UI_CHAPTER_COUNT = getString(R.string.ui_chapter_count);
		UI_LAST_UPDATE = getString(R.string.ui_last_update);
	}
}
