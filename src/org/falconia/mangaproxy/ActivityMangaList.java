package org.falconia.mangaproxy;

import org.falconia.mangaproxy.data.Genre;
import org.falconia.mangaproxy.data.MangaList;
import org.falconia.mangaproxy.task.GetSourceTask;
import org.falconia.mangaproxy.task.ProcessDataTask;
import org.falconia.mangaproxy.ui.MangaListAdapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

public class ActivityMangaList extends ActivityBase {

	public final static class IntentHandler {

		private static final String BUNDLE_KEY_GENRE_DATA = "BUNDLE_KEY_GENRE_DATA";

		private static Intent getIntent(Context context, Genre genre) {
			Bundle bundle = new Bundle();
			bundle.putSerializable(BUNDLE_KEY_GENRE_DATA, genre);
			Intent i = new Intent(context, ActivityMangaList.class);
			i.putExtras(bundle);
			return i;
		}

		protected static Genre getGenre(ActivityMangaList activity) {
			return (Genre) activity.getIntent().getExtras()
					.getSerializable(BUNDLE_KEY_GENRE_DATA);
		}

		public static void startActivityMangaList(Context context, Genre genre) {
			context.startActivity(getIntent(context, genre));
		}

		public static void startActivityAllMangaList(Context context, int siteId) {
			context.startActivity(getIntent(context, Genre.getGenreAll(siteId)));
		}

	}

	protected static final String BUNDLE_KEY_MANGA_LIST = "BUNDLE_KEY_MANGA_LIST";

	private Genre mGenre;
	private MangaList mMangaList;
	private int mPageIndexMax;
	private int mPageIndexCurrent;

	// private FadeAnimation mhFadeAnim;
	// private final IOnFadeEndListener mhOnFadeInEnd;
	// private final IOnFadeEndListener mhOnFadeOutEnd;

	public ActivityMangaList() {
		this.mPageIndexCurrent = 1;
		this.mPageIndexMax = 0;
	}

	@Override
	public String getSiteName() {
		return this.mGenre.getSiteName();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.mGenre = IntentHandler.getGenre(this);
		if (this.mGenre == null)
			finish();

		setContentView(R.layout.activity_manga_list);
		setTitle(String.format("%s - %s", this.mGenre.getSiteDisplayname(),
				this.mGenre.displayname));

		this.mGetSourceTask = new GetSourceTask(this.mGenre.siteId, this);
		this.mProcessDataTask = new ProcessDataTask(this);
		// this.mbShowProcessDialog = false;

		setupListView(new MangaListAdapter(this, this.mGenre.siteId));

		findViewById(R.id.mvgSearch).setVisibility(View.GONE);

		if (!this.mProcessed)
			loadMangaList();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(BUNDLE_KEY_MANGA_LIST, this.mMangaList);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		this.mMangaList = (MangaList) savedInstanceState
				.getSerializable(BUNDLE_KEY_MANGA_LIST);
		((MangaListAdapter) this.mListAdapter).setMangaList(this.mMangaList);
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_DOWNLOAD_ID:
			dialog = createDownloadDialog(R.string.source_of_genre_list);
			break;
		case DIALOG_PROCESS_ID:
			dialog = createProcessDialog(R.string.source_of_genre_list);
			break;
		default:
			dialog = super.onCreateDialog(id);
		}
		return dialog;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		showDialog(DIALOG_DOWNLOAD_ID);
	}

	@Override
	public int onProcess(String source) {
		this.mMangaList = this.mGenre.getMangaList(source,
				this.mPageIndexCurrent);
		if (this.mPageIndexMax == 0)
			this.mPageIndexMax = this.mMangaList.getPageIndexMax();
		return this.mMangaList.size();
	}

	@Override
	public void onPostProcess(int result) {
		super.onPostProcess(result);
		((MangaListAdapter) this.mListAdapter).setMangaList(this.mMangaList);
		getListView().requestFocus();
	}

	private void loadMangaList() {
		this.mGenre.getMangaListSource(this.mGetSourceTask,
				this.mPageIndexCurrent);
	}

}
