package org.falconia.mangaproxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.falconia.mangaproxy.data.Chapter;
import org.falconia.mangaproxy.data.Manga;
import org.falconia.mangaproxy.data.Site;
import org.falconia.mangaproxy.plugin.Plugins;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public final class ActivityInit extends Activity implements OnClickListener {

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// initialize static members
		AppConst.APP_NAME = getString(R.string.app_name);
		AppConst.APP_PACKAGE = getClass().getPackage().getName();
		AppConst.APP_FILES_DIR = getFilesDir();
		AppConst.APP_CACHE_DIR = getCacheDir();
		AppConst.GENRE_ALL_TEXT = getString(R.string.genre_all);
		AppConst.UI_CHAPTER_COUNT = getString(R.string.ui_chapter_count);
		AppConst.UI_LAST_UPDATE = getString(R.string.ui_last_update);

		super.onCreate(savedInstanceState);
		AppUtils.logV(this, "onCreate()");

		setContentView(R.layout.main);
		setTitle(String.format("%s (Alpha, Test only)", AppConst.APP_NAME));


		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open(
					"alpha.txt")));
			StringBuilder builder = new StringBuilder();
			String text;
			while ((text = reader.readLine()) != null) {
				builder.append(text + AppCache.NEW_LINE);
			}
			reader.close();
			text = builder.toString().trim();
			if (!TextUtils.isEmpty(text)) {
				((TextView) findViewById(R.id.mtvMain)).setText(text);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		Button mbtn1 = (Button) findViewById(R.id.mbtn1);
		Button mbtn2 = (Button) findViewById(R.id.mbtn2);
		Button mbtn3 = (Button) findViewById(R.id.mbtn3);
		mbtn1.setText("Normal Mode");
		mbtn2.setText("Direct to PageView");
		mbtn3.setText("Zoom PageView Debug");
		mbtn1.setOnClickListener(this);
		mbtn2.setOnClickListener(this);
		mbtn3.setOnClickListener(this);


		AppConst.DEBUG = -1;
		sitchMode(AppConst.DEBUG);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.mbtn1:
			sitchMode(0);
			break;
		case R.id.mbtn2:
			sitchMode(1);
			break;
		case R.id.mbtn3:
			sitchMode(2);
			break;
		}
	}

	private void sitchMode(int mode) {
		switch (mode) {
		// normal
		case 0:
			startActivity(new Intent(this, ActivityFavoriteList.class));
			break;
		// start ActivityChapter
		case 1:
			Site site = new Site(Plugins.getPlugin(1000));
			Manga manga = new Manga("174", "魔法先生", null, site.getSiteId());
			manga.isCompleted = false;
			manga.chapterCount = 323;
			Chapter chapter = new Chapter("73996", "魔法先生323集", manga);
			chapter.typeId = Chapter.TYPE_ID_CHAPTER;
			chapter.setDynamicImgServerId(3);
			ActivityChapter.IntentHandler.startActivityChapter(this, manga, chapter);
			break;
		// start DebugActivity
		case 2:
			startActivity(new Intent(this, DebugActivity.class));
			break;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		AppUtils.logV(this, "onStart()");
	}

	@Override
	protected void onResume() {
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
		System.gc();

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

}