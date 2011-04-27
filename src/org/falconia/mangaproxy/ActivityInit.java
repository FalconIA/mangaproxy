package org.falconia.mangaproxy;

import java.util.ArrayList;

import org.falconia.mangaproxy.data.Genre;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

public class ActivityInit extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		onInit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		finish();
	}

	public void onInit() {
		Genre.GENRE_TEXT_ALL = getString(R.string.genre_all);

		debug = false;
		if (debug) {
			setContentView(R.layout.main);
			txtDebug = (TextView) findViewById(R.id.txtDebug);

			org.falconia.mangaproxy.plugin.IPlugin plugin = org.falconia.mangaproxy.plugin.Plugins
					.getPlugin(1000);
			// plugin.getGenreList();
			// plugin.getAllMangaList();
		} else
			startActivity(new Intent(this, ActivityFavoriteList.class));
	}

	public static boolean debug = false;
	public static TextView txtDebug;
	private final static int SATRT = 0;
	private final static int LENGTH = 0;
	private final static String DELIMITER = "\n";
	private final static String DELIMITER2 = "\n";

	public static void debugClear() {
		if (!debug)
			return;
		txtDebug.clearComposingText();
	}

	public static void debugPrint(String text) {
		if (!debug)
			return;
		txtDebug.append(text);
		final ScrollView scroller = (ScrollView) txtDebug.getParent();
		scroller.post(new Runnable() {
			@Override
			public void run() {
				scroller.fullScroll(View.FOCUS_DOWN);
			}
		});
	}

	public static void debugPrintLine(String text) {
		debugPrint(text + "\n");
	}

	public static void debugPrintArrayList(ArrayList<String> array) {
		debugPrintArrayList(array, SATRT, LENGTH, DELIMITER, DELIMITER2);
	}

	public static void debugPrintArrayList(ArrayList<String> array, int start) {
		debugPrintArrayList(array, start, LENGTH, DELIMITER, DELIMITER2);
	}

	public static void debugPrintArrayList(ArrayList<String> array, int start,
			int length) {
		debugPrintArrayList(array, start, length, DELIMITER, DELIMITER2);
	}

	public static void debugPrintArrayList(ArrayList<String> array,
			String delimiter) {
		debugPrintArrayList(array, SATRT, LENGTH, delimiter, DELIMITER2);
	}

	public static void debugPrintArrayList(ArrayList<String> array, int start,
			String delimiter) {
		debugPrintArrayList(array, start, LENGTH, delimiter, DELIMITER2);
	}

	public static void debugPrintArrayList(ArrayList<String> array, int start,
			int length, String delimiter) {
		debugPrintArrayList(array, start, length, delimiter, DELIMITER2);
	}

	public static void debugPrintArrayList(ArrayList<String> array,
			String delimiter, String delimiter2) {
		debugPrintArrayList(array, SATRT, LENGTH, delimiter, delimiter2);
	}

	public static void debugPrintArrayList(ArrayList<String> array, int start,
			String delimiter, String delimiter2) {
		debugPrintArrayList(array, start, LENGTH, delimiter, delimiter2);
	}

	public static void debugPrintArrayList(ArrayList<String> array, int start,
			int length, String delimiter, String delimiter2) {
		length = length > 0 ? start + length : array.size() + length;
		ArrayList<String> tokens = new ArrayList<String>();
		for (int i = start; i < array.size() && i < length; i++)
			tokens.add(array.get(i));
		debugPrint(TextUtils.join(delimiter, tokens) + delimiter2);
	}

	public static void debugPrintMatchAll(ArrayList<ArrayList<String>> array) {
		debugPrintMatchAll(array, SATRT, LENGTH, DELIMITER, DELIMITER2);
	}

	public static void debugPrintMatchAll(ArrayList<ArrayList<String>> array,
			int start) {
		debugPrintMatchAll(array, start, LENGTH, DELIMITER, DELIMITER2);
	}

	public static void debugPrintMatchAll(ArrayList<ArrayList<String>> array,
			int start, int length) {
		debugPrintMatchAll(array, start, length, DELIMITER, DELIMITER2);
	}

	public static void debugPrintMatchAll(ArrayList<ArrayList<String>> array,
			String delimiter) {
		debugPrintMatchAll(array, SATRT, LENGTH, delimiter, DELIMITER2);
	}

	public static void debugPrintMatchAll(ArrayList<ArrayList<String>> array,
			int start, String delimiter) {
		debugPrintMatchAll(array, start, LENGTH, delimiter, DELIMITER2);
	}

	public static void debugPrintMatchAll(ArrayList<ArrayList<String>> array,
			int start, int length, String delimiter) {
		debugPrintMatchAll(array, start, length, delimiter, DELIMITER2);
	}

	public static void debugPrintMatchAll(ArrayList<ArrayList<String>> array,
			String delimiter, String delimiter2) {
		debugPrintMatchAll(array, SATRT, LENGTH, delimiter, delimiter2);
	}

	public static void debugPrintMatchAll(ArrayList<ArrayList<String>> array,
			int start, String delimiter, String delimiter2) {
		debugPrintMatchAll(array, start, LENGTH, delimiter, delimiter2);
	}

	public static void debugPrintMatchAll(ArrayList<ArrayList<String>> array,
			int start, int length, String delimiter, String delimiter2) {
		// ArrayList<String> tokens = new ArrayList<String>();
		for (int i = 0; i < array.size(); i++)
			debugPrintArrayList(array.get(i), start, length, delimiter,
					delimiter2);
		if (delimiter2 != DELIMITER2)
			debugPrintLine("");
	}

}