package org.falconia.mangaproxy;

import java.io.File;

import android.content.Context;

public final class AppConst {

	public enum ZoomMode {
		FIT_SCREEN, FIT_HEIGHT, FIT_WIDTH, FIT_WIDTH_AUTO_SPLIT
	}

	public static int DEBUG;

	public static Context APP_CONTEXT;
	public static String APP_NAME;
	public static String APP_PACKAGE;
	public static File APP_FILES_DIR;
	public static File APP_CACHE_DIR;

	// Genre
	public static String GENRE_ALL_TEXT = "All";

	// Manga
	public static String UI_CHAPTER_COUNT = "Chapters: %s";
	public static String UI_LAST_UPDATE = "Update: %tF";

	// Settings
	public static int IMG_PRELOAD_MAX = 2;
	public static float WIDTH_AUTO_SPLIT_MARGIN = .2f;
}
