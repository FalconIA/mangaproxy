package org.falconia.mangaproxy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public final class ActivityInit extends Activity implements ITag {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		// initialize static members
		AppConst.APP_NAME = getString(R.string.app_name);
		AppConst.APP_PACKAGE = getClass().getPackage().getName();
		AppConst.GENRE_ALL_TEXT = getString(R.string.genre_all);
		AppConst.UI_CHAPTER_COUNT = getString(R.string.ui_chapter_count);
		AppConst.UI_LAST_UPDATE = getString(R.string.ui_last_update);

		super.onCreate(savedInstanceState);
		AppUtils.logV(this, "onCreate()");
		setContentView(R.layout.main);
		setTitle(String.format("%s (Alpha, Test only)", AppConst.APP_NAME));

		startActivity(new Intent(this, ActivityFavoriteList.class));
	}

	@Override
	protected void onStart() {
		super.onStart();
		AppUtils.logV(this, "onStart()");
	}

	@Override
	protected void onResume() {
		// System.gc();
		super.onResume();
		AppUtils.logV(this, "onResume()");
	}

	@Override
	protected void onPause() {
		super.onPause();
		AppUtils.logV(this, "onPause()");
	}

	@Override
	protected void onStop() {
		super.onStop();
		AppUtils.logV(this, "onStop()");
		finish();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		AppUtils.logV(this, "onRestart()");
	}

	@Override
	protected void onDestroy() {
		// System.gc();
		super.onDestroy();
		AppUtils.logV(this, "onDestroy()");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		AppUtils.logV(this, "onSaveInstanceState()");
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		AppUtils.logV(this, "onRestoreInstanceState()");
	}

	@Override
	public String getTag() {
		return getClass().getSimpleName();
	}

}