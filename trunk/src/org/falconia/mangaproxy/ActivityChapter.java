package org.falconia.mangaproxy;

import org.falconia.mangaproxy.data.Chapter;
import org.falconia.mangaproxy.data.Manga;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ActivityChapter extends Activity {

	public final static class IntentHandler {

		private static final String BUNDLE_KEY_MANGA_DATA = "BUNDLE_KEY_MANGA_DATA";
		private static final String BUNDLE_KEY_CHAPTER_DATA = "BUNDLE_KEY_CHAPTER_DATA";

		private static Intent getIntent(Context context, Manga manga,
				Chapter chapter) {
			Bundle bundle = new Bundle();
			bundle.putSerializable(BUNDLE_KEY_MANGA_DATA, manga);
			bundle.putSerializable(BUNDLE_KEY_CHAPTER_DATA, chapter);
			Intent i = new Intent(context, ActivityChapter.class);
			i.putExtras(bundle);
			return i;
		}

		protected static Manga getManga(ActivityChapter activity) {
			return (Manga) activity.getIntent().getExtras()
					.getSerializable(BUNDLE_KEY_MANGA_DATA);
		}

		protected static Chapter getChapter(ActivityChapter activity) {
			return (Chapter) activity.getIntent().getExtras()
					.getSerializable(BUNDLE_KEY_CHAPTER_DATA);
		}

		public static void startActivityChapter(Context context, Manga manga,
				Chapter chapter) {
			context.startActivity(getIntent(context, manga, chapter));
		}

	}

	private Manga mManga;
	private Chapter mChapter;
	private int mPageMax;
	private int mPageCurrent;

	public ActivityChapter() {
		this.mPageMax = 0;
		this.mPageCurrent = 1;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.mManga = IntentHandler.getManga(this);
		this.mChapter = IntentHandler.getChapter(this);
		if (this.mManga == null || this.mChapter == null)
			finish();

		setContentView(R.layout.activity_chapter);
		setTitle(getCustomTitle());

	}

	protected String getCustomTitle() {
		return String.format("%s - %s - %s - " + getString(R.string.ui_page),
				this.mManga.getSiteDisplayname(), this.mManga.displayname,
				this.mChapter.displayname, this.mPageCurrent);
	}

}
