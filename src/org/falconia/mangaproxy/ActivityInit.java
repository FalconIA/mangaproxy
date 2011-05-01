package org.falconia.mangaproxy;

import org.falconia.mangaproxy.data.Genre;
import org.falconia.mangaproxy.data.Manga;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ActivityInit extends Activity {

	public static String APP_NAME;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		// initialize static members
		ActivityInit.APP_NAME = getString(R.string.app_name);
		Genre.GENRE_ALL_TEXT = getString(R.string.genre_all);
		Manga.UI_CHAPTER_COUNT = getString(R.string.ui_chapter_count);
		Manga.UI_LAST_UPDATE = getString(R.string.ui_last_update);

		super.onCreate(savedInstanceState);
		Log.i(getTag(), "onCreate()");
		setContentView(R.layout.main);
		setTitle(String.format("%s (Alpha, Test only)", APP_NAME));

		startActivity(new Intent(this, ActivityFavoriteList.class));
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

	protected String getTag() {
		return getClass().getSimpleName();
	}

}