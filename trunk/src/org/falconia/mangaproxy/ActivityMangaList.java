package org.falconia.mangaproxy;

import org.apache.http.util.EncodingUtils;
import org.falconia.mangaproxy.data.Genre;
import org.falconia.mangaproxy.data.MangaList;
import org.falconia.mangaproxy.plugin.Plugins;
import org.falconia.mangaproxy.task.DownloadTask;
import org.falconia.mangaproxy.task.OnDownloadListener;
import org.falconia.mangaproxy.task.OnSourceProcessListener;
import org.falconia.mangaproxy.task.SourceProcessTask;
import org.falconia.mangaproxy.ui.MangaListAdapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

public final class ActivityMangaList extends ActivityBase {

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

	private final class NextPageDownloader implements OnDownloadListener,
			OnSourceProcessListener {

		public static final int MODE_DEFAULT = 0;
		public static final int MODE_DOWNLOAD = 1;
		public static final int MODE_PROCESS = 2;
		public static final int MODE_DOWNLOAD_ERROR = 3;
		public static final int MODE_PROCESS_ERROR = 4;

		private DownloadTask mDownloader;
		private final String mCharset;

		private View mListItem;
		private ProgressBar mProgress;
		private TextView mMessage;
		private TextView mDescribe;

		private int mMode;

		public NextPageDownloader() {
			this.mCharset = Plugins.getPlugin(getSiteId()).getCharset();

			this.mListItem = getLayoutInflater().inflate(
					R.layout.list_item_load, null);
			this.mProgress = (ProgressBar) this.mListItem
					.findViewById(R.id.mpbProgress);
			this.mMessage = (TextView) this.mListItem
					.findViewById(R.id.mtvMessage);
			this.mDescribe = (TextView) this.mListItem
					.findViewById(R.id.mtvDescribe);
			setMode(MODE_DEFAULT);
		}

		@Override
		public void onPreDownload() {
			AppUtils.logV(this, "onPreDownload()");
			setMode(MODE_DOWNLOAD);
		}

		@Override
		public void onPostDownload(byte[] result) {
			AppUtils.logV(this, "onPostDownload()");
			if (result == null || result.length == 0) {
				setNoItemsMessage(String
						.format(getString(R.string.ui_error_on_download),
								getSiteName()));
				setMode(MODE_DOWNLOAD_ERROR);
				return;
			}

			String source = EncodingUtils.getString(result, 0, result.length,
					this.mCharset);
			// AppUtils.logV(this, source);

			setMode(MODE_PROCESS);
			ActivityMangaList.this.mSourceProcessTask = new SourceProcessTask(
					this);
			ActivityMangaList.this.mSourceProcessTask.execute(source);
		}

		@Override
		public void onDownloadProgressUpdate(int value, int total) {
			this.mMessage.setText(String.format(
					getString(R.string.ui_download_page), (value) / 1024.0f));
		}

		@Override
		public int onSourceProcess(String source) {
			MangaList mangaList = ActivityMangaList.this.mGenre
					.getMangaList(source);
			int size = mangaList.size();
			if (size > 0)
				ActivityMangaList.this.mMangaList.addAll(mangaList);
			return size;
		}

		@Override
		public void onPreSourceProcess() {
			AppUtils.logV(this, "onPreSourceProcess()");
		}

		@Override
		public void onPostSourceProcess(int size) {
			AppUtils.logV(this, "onPostSourceProcess()");
			if (size > 0) {
				((MangaListAdapter) ActivityMangaList.this.mListAdapter)
						.setMangaList(ActivityMangaList.this.mMangaList);
				ActivityMangaList.this.mPageLoaded++;
				AppUtils.popupMessage(ActivityMangaList.this, String.format(
						getString(R.string.popup_loaded_page),
						ActivityMangaList.this.mPageLoaded));
				setMode(MODE_DEFAULT);
			} else {
				setMode(MODE_PROCESS_ERROR);
				return;
			}
			if (ActivityMangaList.this.mPageLoaded == ActivityMangaList.this.mPageMax) {
				getListView().removeFooterView(getFooter());
				setCustomTitle(ActivityMangaList.this.mGenre.displayname);
			}
		}

		public View getFooter() {
			return this.mListItem;
		}

		public void setMode(int mode) {
			this.mMode = mode;
			switch (this.mMode) {
			case MODE_DEFAULT:
				if (ActivityMangaList.this.mPageLoaded < ActivityMangaList.this.mPageMax)
					setCustomTitle(String.format("%s (%d/%d)",
							ActivityMangaList.this.mGenre.displayname,
							ActivityMangaList.this.mPageLoaded,
							ActivityMangaList.this.mPageMax));
				setProgressBarIndeterminateVisibility(false);
				this.mProgress.setVisibility(View.GONE);
				this.mMessage.setText(R.string.ui_load_next_page);
				this.mDescribe.setText(String.format("(%d/%d)",
						ActivityMangaList.this.mPageLoaded + 1,
						ActivityMangaList.this.mPageMax));
				break;
			case MODE_DOWNLOAD:
				setProgressBarIndeterminateVisibility(true);
				this.mProgress.setVisibility(View.VISIBLE);
				this.mMessage.setText(String.format(
						getString(R.string.ui_download_page), 0.0f));
				this.mDescribe.setText(String.format("(%s)",
						getString(R.string.ui_click_to_cancel)));
				break;
			case MODE_PROCESS:
				setProgressBarIndeterminateVisibility(true);
				this.mProgress.setVisibility(View.VISIBLE);
				this.mMessage.setText(R.string.ui_process_page);
				this.mDescribe.setText(String.format("(%d/%d)",
						ActivityMangaList.this.mPageLoaded + 1,
						ActivityMangaList.this.mPageMax));
				break;
			case MODE_DOWNLOAD_ERROR:
				setProgressBarIndeterminateVisibility(false);
				this.mProgress.setVisibility(View.GONE);
				this.mMessage.setText(R.string.ui_error);
				this.mDescribe.setText(String.format("(%s)", String.format(
						getString(R.string.ui_fail_to_download),
						ActivityMangaList.this.mPageLoaded + 1)));
				break;
			case MODE_PROCESS_ERROR:
				setProgressBarIndeterminateVisibility(false);
				this.mProgress.setVisibility(View.GONE);
				this.mMessage.setText(R.string.ui_error);
				this.mDescribe.setText(String.format("(%s)", String.format(
						getString(R.string.ui_fail_to_process),
						ActivityMangaList.this.mPageLoaded + 1)));
				break;
			}
		}

		public void click() {
			switch (this.mMode) {
			case MODE_DEFAULT:
				AppUtils.logV(this, "click() @MODE_DEFAULT");
				AppUtils.logD(this, "Download Page "
						+ (ActivityMangaList.this.mPageLoaded + 1));
				this.mDownloader = new DownloadTask(this);
				this.mDownloader.execute(ActivityMangaList.this.mGenre
						.getUrl(ActivityMangaList.this.mPageLoaded + 1));
				break;
			case MODE_DOWNLOAD:
				AppUtils.logV(this, "click() @MODE_DOWNLOAD");
				if (this.mDownloader != null
						&& this.mDownloader.getStatus() == AsyncTask.Status.RUNNING)
					this.mDownloader.cancel(true);
				setMode(MODE_DEFAULT);
				break;
			case MODE_DOWNLOAD_ERROR:
			case MODE_PROCESS_ERROR:
				AppUtils.logV(this, "click() @MODE_ERROR");
				setMode(MODE_DEFAULT);
				break;
			}
		}

		public void cancelDownload() {
			if (this.mDownloader != null
					&& this.mDownloader.getStatus() == AsyncTask.Status.RUNNING) {
				AppUtils.logD(this, "Cancel DownloadTask.");
				this.mDownloader.cancel(true);
			}
		}
	}

	private static final String BUNDLE_KEY_PAGE_MAX = "BUNDLE_KEY_PAGE_MAX";
	private static final String BUNDLE_KEY_PAGE_LOADED = "BUNDLE_KEY_PAGE_LOADED";
	private static final String BUNDLE_KEY_MANGA_LIST = "BUNDLE_KEY_MANGA_LIST";

	private Genre mGenre;
	private MangaList mMangaList;
	private String mUrl;
	private int mPageMax;
	private int mPageLoaded;

	private NextPageDownloader mNextPageDownloader;

	// private FadeAnimation mhFadeAnim;
	// private final IOnFadeEndListener mhOnFadeInEnd;
	// private final IOnFadeEndListener mhOnFadeOutEnd;

	public ActivityMangaList() {
		this.mPageLoaded = 0;
		this.mPageMax = 0;
	}

	@Override
	public int getSiteId() {
		return this.mGenre.siteId;
	}

	@Override
	public String getSiteName() {
		return this.mGenre.getSiteName();
	}

	@Override
	String getSiteDisplayname() {
		return this.mGenre.getSiteDisplayname();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.mGenre = IntentHandler.getGenre(this);
		if (this.mGenre == null)
			finish();

		setContentView(R.layout.activity_manga_list);
		setCustomTitle(this.mGenre.displayname);

		// this.mbShowProcessDialog = false;

		setupListView(new MangaListAdapter(this));

		findViewById(R.id.mvgSearch).setVisibility(View.GONE);

		if (!this.mProcessed)
			loadMangaList();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (this.mNextPageDownloader != null)
			this.mNextPageDownloader.cancelDownload();
		outState.putSerializable(BUNDLE_KEY_PAGE_MAX, this.mPageMax);
		outState.putSerializable(BUNDLE_KEY_PAGE_LOADED, this.mPageLoaded);
		outState.putSerializable(BUNDLE_KEY_MANGA_LIST, this.mMangaList);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		this.mPageMax = savedInstanceState.getInt(BUNDLE_KEY_PAGE_MAX);
		this.mPageLoaded = savedInstanceState.getInt(BUNDLE_KEY_PAGE_LOADED);
		this.mMangaList = (MangaList) savedInstanceState
				.getSerializable(BUNDLE_KEY_MANGA_LIST);
		if (this.mPageLoaded < this.mPageMax)
			showFooter();
		((MangaListAdapter) this.mListAdapter).setMangaList(this.mMangaList);
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AppUtils.logV(this, "onCreateDialog()");
		Dialog dialog;
		switch (id) {
		case DIALOG_DOWNLOAD_ID:
			dialog = this.mSourceDownloader
					.createDownloadDialog(R.string.source_of_genre_list);
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
		if (this.mNextPageDownloader != null
				&& view == this.mNextPageDownloader.getFooter())
			this.mNextPageDownloader.click();
		else
			ActivityChapterList.IntentHandler.startActivityMangaList(this,
					this.mMangaList.getAt(position));
	}

	@Override
	public int onSourceProcess(String source) {
		super.onSourceProcess(source);
		this.mMangaList = this.mGenre.getMangaList(source);
		if (this.mPageMax == 0)
			this.mPageMax = this.mMangaList.pageIndexMax;
		return this.mMangaList.size();
	}

	@Override
	public void onPostSourceProcess(int size) {
		if (size > 0)
			this.mPageLoaded++;
		if (this.mPageLoaded < this.mPageMax)
			showFooter();
		((MangaListAdapter) this.mListAdapter).setMangaList(this.mMangaList);
		getListView().requestFocus();

		super.onPostSourceProcess(size);
	}

	@Override
	protected void startProcessSource(String source) {
		startProcessSource(source, true);
	}

	private void startProcessSource(String source, boolean writeCache) {
		if (writeCache)
			AppCache.writeCacheForData(this.mUrl, source);
		super.startProcessSource(source);
	}

	private void loadMangaList() {
		this.mUrl = this.mGenre.getUrl();
		if (this.mGenre.isGenreAll() && AppCache.checkCacheForData(this.mUrl, 3600)) {
			String source = AppCache.readCacheForData(this.mUrl);
			startProcessSource(source, false);
		} else {
			this.mSourceDownloader = new SourceDownloader();
			this.mSourceDownloader.download(this.mUrl);
		}
	}

	private void showFooter() {
		this.mNextPageDownloader = new NextPageDownloader();
		getListView().addFooterView(this.mNextPageDownloader.getFooter(), null,
				true);
		setListAdapter(this.mListAdapter);
	}

}
