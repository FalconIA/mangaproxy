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
			mCharset = Plugins.getPlugin(getSiteId()).getCharset();

			mListItem = getLayoutInflater().inflate(R.layout.list_item_load,
					null);
			mProgress = (ProgressBar) mListItem.findViewById(R.id.mpbProgress);
			mMessage = (TextView) mListItem.findViewById(R.id.mtvMessage);
			mDescribe = (TextView) mListItem.findViewById(R.id.mtvDescribe);
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
				AppUtils.logE(this, "Downloaded empty source.");
				setNoItemsMessage(String
						.format(getString(R.string.ui_error_on_download),
								getSiteName()));
				setMode(MODE_DOWNLOAD_ERROR);
				return;
			}

			String source = EncodingUtils.getString(result, 0, result.length,
					mCharset);
			// AppUtils.logV(this, source);

			setMode(MODE_PROCESS);
			ActivityMangaList.this.mSourceProcessTask = new SourceProcessTask(
					this);
			ActivityMangaList.this.mSourceProcessTask.execute(source);
		}

		@Override
		public void onDownloadProgressUpdate(int value, int total) {
			mMessage.setText(String.format(
					getString(R.string.ui_download_page), (value) / 1024.0f));
		}

		@Override
		public int onSourceProcess(String source) {
			MangaList mangaList = mGenre.getMangaList(source);
			int size = mangaList.size();
			if (size > 0) {
				mMangaList.addAll(mangaList);
			}
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
						.setMangaList(mMangaList);
				mPageLoaded++;
				AppUtils.popupMessage(ActivityMangaList.this, String.format(
						getString(R.string.popup_loaded_page), mPageLoaded));
				setMode(MODE_DEFAULT);
			} else {
				setMode(MODE_PROCESS_ERROR);
				return;
			}
			if (mPageLoaded == mPageMax) {
				getListView().removeFooterView(getFooter());
				setCustomTitle(mGenre.displayname);
			}
		}

		public View getFooter() {
			return mListItem;
		}

		public void setMode(int mode) {
			mMode = mode;
			switch (mMode) {
			case MODE_DEFAULT:
				if (mPageLoaded < mPageMax) {
					setCustomTitle(String.format("%s (%d/%d)",
							mGenre.displayname, mPageLoaded, mPageMax));
				}
				setProgressBarIndeterminateVisibility(false);
				mProgress.setVisibility(View.GONE);
				mMessage.setText(R.string.ui_load_next_page);
				mDescribe.setText(String.format("(%d/%d)", mPageLoaded + 1,
						mPageMax));
				break;
			case MODE_DOWNLOAD:
				setProgressBarIndeterminateVisibility(true);
				mProgress.setVisibility(View.VISIBLE);
				mMessage.setText(String.format(
						getString(R.string.ui_download_page), 0.0f));
				mDescribe.setText(String.format("(%s)",
						getString(R.string.ui_click_to_cancel)));
				break;
			case MODE_PROCESS:
				setProgressBarIndeterminateVisibility(true);
				mProgress.setVisibility(View.VISIBLE);
				mMessage.setText(R.string.ui_process_page);
				mDescribe.setText(String.format("(%d/%d)", mPageLoaded + 1,
						mPageMax));
				break;
			case MODE_DOWNLOAD_ERROR:
				setProgressBarIndeterminateVisibility(false);
				mProgress.setVisibility(View.GONE);
				mMessage.setText(R.string.ui_error);
				mDescribe.setText(String.format("(%s)", String.format(
						getString(R.string.ui_fail_to_download),
						mPageLoaded + 1)));
				break;
			case MODE_PROCESS_ERROR:
				setProgressBarIndeterminateVisibility(false);
				mProgress.setVisibility(View.GONE);
				mMessage.setText(R.string.ui_error);
				mDescribe.setText(String.format("(%s)", String
						.format(getString(R.string.ui_fail_to_process),
								mPageLoaded + 1)));
				break;
			}
		}

		public void click() {
			switch (mMode) {
			case MODE_DEFAULT:
				AppUtils.logV(this, "click() @MODE_DEFAULT");
				AppUtils.logD(this, "Download Page " + (mPageLoaded + 1));
				mDownloader = new DownloadTask(this);
				mDownloader.execute(mGenre.getUrl(mPageLoaded + 1));
				break;
			case MODE_DOWNLOAD:
				AppUtils.logV(this, "click() @MODE_DOWNLOAD");
				if (mDownloader != null
						&& mDownloader.getStatus() == AsyncTask.Status.RUNNING) {
					mDownloader.cancel(true);
				}
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
			if (mDownloader != null
					&& mDownloader.getStatus() == AsyncTask.Status.RUNNING) {
				AppUtils.logD(this, "Cancel DownloadTask.");
				mDownloader.cancel(true);
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
		mPageLoaded = 0;
		mPageMax = 0;
	}

	@Override
	public int getSiteId() {
		return mGenre.siteId;
	}

	@Override
	public String getSiteName() {
		return mGenre.getSiteName();
	}

	@Override
	String getSiteDisplayname() {
		return mGenre.getSiteDisplayname();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mGenre = IntentHandler.getGenre(this);
		if (mGenre == null) {
			finish();
		}

		setContentView(R.layout.activity_manga_list);
		setCustomTitle(mGenre.displayname);

		// this.mbShowProcessDialog = false;

		setupListView(new MangaListAdapter(this));

		findViewById(R.id.mvgSearch).setVisibility(View.GONE);

		if (!mProcessed) {
			loadMangaList();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (mNextPageDownloader != null) {
			mNextPageDownloader.cancelDownload();
		}
		outState.putSerializable(BUNDLE_KEY_PAGE_MAX, mPageMax);
		outState.putSerializable(BUNDLE_KEY_PAGE_LOADED, mPageLoaded);
		outState.putSerializable(BUNDLE_KEY_MANGA_LIST, mMangaList);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		mPageMax = savedInstanceState.getInt(BUNDLE_KEY_PAGE_MAX);
		mPageLoaded = savedInstanceState.getInt(BUNDLE_KEY_PAGE_LOADED);
		mMangaList = (MangaList) savedInstanceState
				.getSerializable(BUNDLE_KEY_MANGA_LIST);
		if (mPageLoaded < mPageMax) {
			showFooter();
		}
		((MangaListAdapter) mListAdapter).setMangaList(mMangaList);
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AppUtils.logV(this, "onCreateDialog()");
		Dialog dialog;
		switch (id) {
		case DIALOG_DOWNLOAD_ID:
			dialog = mSourceDownloader
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
		if (mNextPageDownloader != null
				&& view == mNextPageDownloader.getFooter()) {
			mNextPageDownloader.click();
		} else {
			ActivityChapterList.IntentHandler.startActivityMangaList(this,
					mMangaList.getAt(position));
		}
	}

	@Override
	public int onSourceProcess(String source) {
		super.onSourceProcess(source);
		mMangaList = mGenre.getMangaList(source);
		if (mPageMax == 0) {
			mPageMax = mMangaList.pageIndexMax;
		}
		return mMangaList.size();
	}

	@Override
	public void onPostSourceProcess(int size) {
		if (size > 0) {
			mPageLoaded++;
		}
		if (mPageLoaded < mPageMax) {
			showFooter();
		}
		((MangaListAdapter) mListAdapter).setMangaList(mMangaList);
		getListView().requestFocus();

		super.onPostSourceProcess(size);
	}

	@Override
	protected void startProcessSource(String source) {
		startProcessSource(source, true);
	}

	private void startProcessSource(String source, boolean writeCache) {
		if (writeCache && mGenre.isGenreAll()) {
			AppCache.writeCacheForData(source, mUrl);
			AppUtils.popupMessage(this, R.string.popup_cache_save_allmanga);
		}
		super.startProcessSource(source);
	}

	private void loadMangaList() {
		mUrl = mGenre.getUrl();
		if (mGenre.isGenreAll() && AppCache.checkCacheForData(mUrl, 3600)) {
			String source = AppCache.readCacheForData(mUrl);
			AppUtils.popupMessage(ActivityMangaList.this,
					R.string.popup_cache_load_allmanga);
			startProcessSource(source, false);
		} else {
			mSourceDownloader = new SourceDownloader();
			mSourceDownloader.download(mUrl);
		}
	}

	private void showFooter() {
		mNextPageDownloader = new NextPageDownloader();
		getListView()
				.addFooterView(mNextPageDownloader.getFooter(), null, true);
		setListAdapter(mListAdapter);
	}

}
