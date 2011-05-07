package org.falconia.mangaproxy;

import java.util.HashMap;

import org.apache.http.util.EncodingUtils;
import org.falconia.mangaproxy.data.Chapter;
import org.falconia.mangaproxy.data.Manga;
import org.falconia.mangaproxy.plugin.Plugins;
import org.falconia.mangaproxy.task.DownloadTask;
import org.falconia.mangaproxy.task.OnDownloadListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

public final class ActivityChapter extends Activity {

	public final static class IntentHandler {

		private static final String BUNDLE_KEY_MANGA_DATA = "BUNDLE_KEY_MANGA_DATA";
		private static final String BUNDLE_KEY_CHAPTER_DATA = "BUNDLE_KEY_CHAPTER_DATA";

		private static Intent getIntent(Context context, Manga manga, Chapter chapter) {
			Bundle bundle = new Bundle();
			bundle.putSerializable(BUNDLE_KEY_MANGA_DATA, manga);
			bundle.putSerializable(BUNDLE_KEY_CHAPTER_DATA, chapter);
			Intent i = new Intent(context, ActivityChapter.class);
			i.putExtras(bundle);
			return i;
		}

		protected static Manga getManga(ActivityChapter activity) {
			return (Manga) activity.getIntent().getExtras().getSerializable(BUNDLE_KEY_MANGA_DATA);
		}

		protected static Chapter getChapter(ActivityChapter activity) {
			return (Chapter) activity.getIntent().getExtras()
					.getSerializable(BUNDLE_KEY_CHAPTER_DATA);
		}

		public static void startActivityChapter(Context context, Manga manga, Chapter chapter) {
			context.startActivity(getIntent(context, manga, chapter));
		}

	}

	private final class SourceDownloader implements OnDownloadListener {

		protected static final int MODE_CHAPTER = 0;
		protected static final int MODE_IMG_SERVERS = 1;

		private final DownloadTask mDownloader;

		private final int mMode;
		private final String mCharset;

		public SourceDownloader(int mode) {
			mDownloader = new DownloadTask(this);
			mMode = mode;
			mCharset = Plugins.getPlugin(getSiteId()).getCharset();
		}

		@Override
		public void onPreDownload() {
			AppUtils.logV(this, "onPreDownload()");
			setProgressBarIndeterminateVisibility(true);
		}

		@Override
		public void onPostDownload(byte[] result) {
			AppUtils.logV(this, "onPostDownload()");
			setProgressBarIndeterminateVisibility(false);
			if (result == null || result.length == 0) {
				AppUtils.logE(this, "Downloaded empty source.");
				setMessage(String.format(getString(R.string.ui_error_on_download), getSiteName()));
				return;
			}

			String source = EncodingUtils.getString(result, 0, result.length, mCharset);

			switch (mMode) {
			case MODE_CHAPTER:
				processChapterSource(source);
				break;
			case MODE_IMG_SERVERS:
				processImgServersSource(source);
				break;
			}
		}

		@Override
		public void onDownloadProgressUpdate(int value, int total) {
		}

		public void download(String url) {
			mDownloader.execute(url);
		}

		public void cancelDownload() {
			if (mDownloader != null && mDownloader.getStatus() == AsyncTask.Status.RUNNING) {
				AppUtils.logD(this, "Cancel DownloadTask.");
				mDownloader.cancel(true);
			}
		}
	}

	private final class Page implements OnDownloadListener {

		private static final String TYPE = "page";

		private DownloadTask mDownloader;

		private final int mPageIndex;
		private final String mUrl;

		private boolean mIsDownloaded;
		private boolean mIsDownloading;

		private transient Bitmap mBitmap;

		public Page(int pageIndex, String url) {
			mPageIndex = pageIndex;
			mUrl = url;
			mIsDownloaded = false;
			mIsDownloading = false;

			checkCache();
		}

		@Override
		public void onPreDownload() {
			AppUtils.logV(this, "onPreDownload()");
			mIsDownloading = true;
		}

		@Override
		public void onPostDownload(byte[] result) {
			AppUtils.logV(this, "onPostDownload()");
			if (!AppCache.writeCacheForImage(result, mUrl, TYPE)) {
				AppUtils.popupMessage(ActivityChapter.this,
						String.format(getString(R.string.popup_fail_to_cache_page), mPageIndex));
				mBitmap = BitmapFactory.decodeByteArray(result, 0, result.length);
			}
			mIsDownloaded = true;
			mIsDownloading = false;
			notifyPageDownloaded(this);
		}

		@Override
		public void onDownloadProgressUpdate(int value, int total) {

		}

		public boolean checkCache() {
			boolean cached = AppCache.checkCacheForImage(mUrl, TYPE);
			AppUtils.logI(this, String.format("Cached: %b", cached));
			// this.mIsDownloaded = this.mIsDownloaded || cached;
			return cached;
		}

		public boolean isDownload() {
			if (mIsDownloading) {
				return false;
			}
			if (mIsDownloaded) {
				return true;
			}
			if (mBitmap != null) {
				return true;
			}
			return checkCache();
		}

		public void download() {
			if (mIsDownloading) {
				return;
			}
			if (isDownload()) {
				notifyPageDownloaded(this);
				return;
			}
			AppUtils.logV(this, String.format("Download image: %s", mUrl));
			mDownloader = new DownloadTask(this);
			mDownloader.execute(mUrl);
		}

		public void cancelDownload() {
			if (mDownloader != null && mDownloader.getStatus() == AsyncTask.Status.RUNNING) {
				AppUtils.logD(this, "Cancel DownloadTask.");
				mDownloader.cancel(true);
			}
		}

		public void recycle() {
			if (mBitmap != null) {
				mBitmap.recycle();
			}
		}

		public Bitmap getBitmap() {
			if (mBitmap != null) {
				return mBitmap;
			}
			return AppCache.readCacheForImage(mUrl, TYPE);
		}
	}

	private static final String BUNDLE_KEY_IS_PROCESSED = "BUNDLE_KEY_IS_PROCESSED";

	private SourceDownloader mSourceDownloader;

	private Manga mManga;
	private Chapter mChapter;

	private String[] mPageUrls;
	private HashMap<Integer, Page> mPages;
	private int mPageMax;
	private int mPageCurrent;

	private ImageView mPage;

	private boolean mProcessed;

	public ActivityChapter() {
		mProcessed = false;
		mPageMax = 0;
		mPageCurrent = 0;
	}

	public int getSiteId() {
		return mManga.siteId;
	}

	public String getSiteName() {
		return mManga.getSiteName();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppUtils.logV(this, "onCreate()");

		mProcessed = getProcessed(savedInstanceState);
		mManga = IntentHandler.getManga(this);
		mChapter = IntentHandler.getChapter(this);
		if (mManga == null || mChapter == null) {
			finish();
		}

		setContentView(R.layout.activity_chapter);
		setTitle(getCustomTitle());

		mPage = (ImageView) findViewById(R.id.mivPage);

		if (!mProcessed) {
			loadChapter();
		}
	}

	@Override
	protected void onStop() {
		super.onDestroy();
		AppUtils.logV(this, "onStop()");

		stopTask();
	}

	@Override
	protected void onDestroy() {
		System.gc();

		super.onDestroy();
		AppUtils.logV(this, "onDestroy()");

		if (mPages != null) {
			for (int key : mPages.keySet()) {
				Page page = mPages.get(key);
				if (page != null) {
					page.recycle();
				}
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		AppUtils.logV(this, "onSaveInstanceState()");
		outState.putBoolean(BUNDLE_KEY_IS_PROCESSED, mProcessed);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		AppUtils.logV(this, "onRestoreInstanceState()");
	}

	private boolean getProcessed(Bundle savedInstanceState) {
		boolean processed = false;
		if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_KEY_IS_PROCESSED)) {
			processed = savedInstanceState.getBoolean(BUNDLE_KEY_IS_PROCESSED);
		}
		return processed;
	}

	private void stopTask() {
		if (mSourceDownloader != null) {
			mSourceDownloader.cancelDownload();
		}
		if (mPages != null) {
			for (int key : mPages.keySet()) {
				Page page = mPages.get(key);
				if (page != null) {
					page.cancelDownload();
				}
			}
		}
	}

	private String getCustomTitle() {
		String title = String.format("%s - %s - %s", mManga.getSiteDisplayname(),
				mManga.displayname, mChapter.displayname);
		if (mPageCurrent > 0) {
			title = String.format("%s - " + getString(R.string.ui_page), title, mPageCurrent);
		}
		return title;
	}

	protected void setMessage(String msg) {
		((TextView) findViewById(R.id.mtvMessage)).setText(msg);
	}

	private void loadChapter() {
		mSourceDownloader = new SourceDownloader(SourceDownloader.MODE_CHAPTER);
		mSourceDownloader.download(mChapter.getUrl());
	}

	private void processChapterSource(String source) {
		// ((TextView) findViewById(R.id.mtvDebug)).append("\n" + source);

		mPageUrls = mChapter.getPageUrls(source);

		if (mPageUrls != null) {
			mPageMax = mPageUrls.length;
		} else {
			setMessage(String.format(getString(R.string.ui_error_on_process), getSiteName()));
			return;
		}

		for (String url : mPageUrls) {
			((TextView) findViewById(R.id.mtvDebug)).append("\n" + url);
		}

		String urlImgServers = mChapter.getDynamicImgServersUrl();
		if (TextUtils.isEmpty(urlImgServers)) {
		} else {
			((TextView) findViewById(R.id.mtvDebug)).append("\n" + urlImgServers);
			mSourceDownloader.cancelDownload();
			mSourceDownloader = new SourceDownloader(SourceDownloader.MODE_IMG_SERVERS);
			mSourceDownloader.download(urlImgServers);
		}
	}

	private void processImgServersSource(String source) {
		// ((TextView) findViewById(R.id.mtvDebug)).append("\n" + source);

		if (mChapter.setDynamicImgServers(source)) {
			return;
		}

		if (!mChapter.hasDynamicImgServer()) {
			AppUtils.logE(this, "No DynamicImgServer.");
			return;
		}

		mPages = new HashMap<Integer, Page>();
		String imgServer = mChapter.getDynamicImgServer();
		((TextView) findViewById(R.id.mtvDebug)).append("\n" + imgServer);

		for (int i = 0; i < mPageUrls.length; i++) {
			String url = (imgServer + mPageUrls[i]).replaceAll("(?<!http:)//", "/");
			Page page = new Page(i + 1, url);
			mPages.put(i + 1, page);
			((TextView) findViewById(R.id.mtvDebug)).append("\n" + url);
		}

		chagePage(1);
	}

	private void chagePage(int pageIndex) {
		AppUtils.logI(this, String.format("chagePage(%d)", pageIndex));
		mPageCurrent = pageIndex;
		Page page = mPages.get(mPageCurrent);
		page.download();
	}

	private void preloadPage(int pageIndex) {
		for (int i = 1; i <= AppConst.IMG_PRELOAD_MAX; i++) {
			if (pageIndex + i > mPageMax) {
				break;
			}
			mPages.get(pageIndex + i).download();
		}
	}

	private void notifyPageDownloaded(Page page) {
		AppUtils.logI(this, "Notify that Page downloaded.");
		if (page.mPageIndex == mPageCurrent) {
			// preload next page
			preloadPage(mPageCurrent);
			// current page
			Bitmap bitmap = page.getBitmap();
			if (bitmap != null) {
				mPage.setImageBitmap(bitmap);
			} else {
				AppUtils.logE(this, "Invalid bitmap.");
			}
		}
	}
}
