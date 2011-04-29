package org.falconia.mangaproxy;

import java.util.ArrayList;

import org.falconia.mangaproxy.data.Genre;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

public class ActivityInit extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(getTag(), "onCreate()");
		setContentView(R.layout.main);

		if (popWindow == null) {
			View view = View.inflate(this, R.layout.debug_popup, null);
			popWindow = new PopupWindow(view, LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			popWindow.update(0, 0, 240, 600);
			// popWindow.setTouchable(false);
			tvDebug = (TextView) view.findViewById(R.id.mtvDebug);
		}

		onInit();
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.i(getTag(), "onStart()");
	}

	@Override
	protected void onResume() {
		// System.gc();
		super.onResume();
		Log.i(getTag(), "onResume()");
	}

	@Override
	protected void onPause() {
		ActivityInit.popWindow.dismiss();
		super.onPause();
		Log.i(getTag(), "onPause()");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(getTag(), "onStop()");
		finish();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.i(getTag(), "onRestart()");
	}

	@Override
	protected void onDestroy() {
		// System.gc();
		super.onDestroy();
		Log.i(getTag(), "onDestroy()");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.i(getTag(), "onSaveInstanceState()");
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Log.i(getTag(), "onRestoreInstanceState()");
	}

	public void onInit() {
		Genre.GENRE_ALL_TEXT = getString(R.string.genre_all);
		findViewById(R.id.mtvDebug).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				popWindow.showAtLocation(findViewById(R.id.main),
						Gravity.CENTER, 0, 0);
			}
		});

		debugPrintLine("Debug:");
		startActivity(new Intent(this, ActivityFavoriteList.class));
	}

	public static PopupWindow popWindow;
	public static TextView tvDebug;
	private final static int SATRT = 0;
	private final static int LENGTH = 0;
	private final static String DELIMITER = "\n";
	private final static String DELIMITER2 = "\n";

	public static void debugClear() {
		tvDebug.clearComposingText();
	}

	public static void debugPrintLine(String text) {
		tvDebug.append(text + "\n");
		Log.i("DEBUG", text);
		final ScrollView scroller = (ScrollView) tvDebug.getParent();
		scroller.post(new Runnable() {
			@Override
			public void run() {
				scroller.fullScroll(View.FOCUS_DOWN);
			}
		});
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
		debugPrintLine(TextUtils.join(delimiter, tokens)
				+ (delimiter2 == DELIMITER2 ? "" : delimiter2));
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

	protected String getTag() {
		return getClass().getSimpleName();
	}

}