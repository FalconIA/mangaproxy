package org.falconia.mangaproxy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.CRC32;

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
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.widget.ImageView;
import android.widget.ScrollView;
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

		private String mUrl;

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
				// TODO Debug
				printDebug(mUrl, "Caching");
				AppCache.writeCacheForData(source, mUrl);
				processImgServersSource(source);
				break;
			}
		}

		@Override
		public void onDownloadProgressUpdate(int value, int total) {
		}

		public void download(String url) {
			mUrl = url;
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
			if (result == null || result.length == 0) {
				AppUtils.logE(this, "Downloaded empty source.");
				setMessage(String.format(getString(R.string.ui_error_on_download), getSiteName()));
				// TODO Retry to downlaod
				return;
			}
			if (checkDummyPic(result)) {
				AppUtils.logE(this, "Dummy picture: " + mUrl);
			}
			if (!AppCache.writeCacheForImage(result, mUrl, TYPE)) {
				AppUtils.popupMessage(ActivityChapter.this,
						String.format(getString(R.string.popup_fail_to_cache_page), mPageIndex));
				mBitmap = BitmapFactory.decodeByteArray(result, 0, result.length);
			}
			mIsDownloaded = true;
			mIsDownloading = false;
			// TODO Debug
			printDebug(mUrl, "Downloaded");
			notifyPageDownloaded(this);
		}

		@Override
		public void onDownloadProgressUpdate(int value, int total) {

		}

		public boolean checkCache() {
			boolean cached = AppCache.checkCacheForImage(mUrl, TYPE);
			AppUtils.logV(this, String.format("Cached: %b", cached));
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
				// TODO Debug
				printDebug(mUrl, "Cached");
				notifyPageDownloaded(this);
				return;
			}
			AppUtils.logI(this, String.format("Download image: %s", mUrl));
			mDownloader = new DownloadTask(this, mChapter.getUrl());
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
			// TODO Debug
			printDebug(mUrl, "Loading cache");
			return AppCache.readCacheForImage(mUrl, TYPE);
		}
	}

	private static final String BUNDLE_KEY_IS_PROCESSED = "BUNDLE_KEY_IS_PROCESSED";
	private static final HashSet<String> DUMMY_PIC_CRC32 = new HashSet<String>();

	private SourceDownloader mSourceDownloader;

	private Manga mManga;
	private Chapter mChapter;

	private String[] mPageUrls;
	private HashMap<Integer, Page> mPages;
	private int mPageCurrent;

	private ImageView mPage;
	private TextView mtvDebug;
	private ScrollView msvScroller;

	private boolean mProcessed;

	public ActivityChapter() {
		mProcessed = false;
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
		mtvDebug = (TextView) findViewById(R.id.mtvDebug);
		msvScroller = (ScrollView) findViewById(R.id.msvScroller);

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
		// TODO Debug
		printDebug(mChapter.getUrl(), "Downloading");
		mSourceDownloader = new SourceDownloader(SourceDownloader.MODE_CHAPTER);
		mSourceDownloader.download(mChapter.getUrl());
	}

	private void processChapterSource(String source) {
		mPageUrls = mChapter.getPageUrls(source);

		if (mPageUrls == null) {
			setMessage(String.format(getString(R.string.ui_error_on_process), getSiteName()));
			return;
		}

		String urlImgServers = mChapter.getDynamicImgServersUrl();
		if (TextUtils.isEmpty(urlImgServers)) {
			// TODO Warning message
		} else {
			if (AppCache.checkCacheForData(urlImgServers, 3600)) {
				// TODO Debug
				printDebug(urlImgServers, "Loading cache");
				source = AppCache.readCacheForData(urlImgServers);
				processImgServersSource(source);
			} else {
				// TODO Debug
				printDebug(urlImgServers, "Downloading");
				mSourceDownloader.cancelDownload();
				mSourceDownloader = new SourceDownloader(SourceDownloader.MODE_IMG_SERVERS);
				mSourceDownloader.download(urlImgServers);
			}
		}
	}

	private void processImgServersSource(String source) {
		if (mChapter.setDynamicImgServers(source)) {
			return;
		}

		if (!mChapter.hasDynamicImgServer()) {
			AppUtils.logE(this, "No DynamicImgServer.");
			return;
		}

		mPages = new HashMap<Integer, Page>();
		String imgServer = mChapter.getDynamicImgServer();
		// TODO Debug
		printDebug(imgServer, "Get Img Server");

		for (int i = 0; i < mPageUrls.length; i++) {
			String url = (imgServer + mPageUrls[i]).replaceAll("(?<!http:)//", "/");
			Page page = new Page(i + 1, url);
			mPages.put(i + 1, page);
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
		if (pageIndex - mPageCurrent <= AppConst.IMG_PRELOAD_MAX && pageIndex < mPages.size()) {
			mPages.get(pageIndex).download();
		}
	}

	private void notifyPageDownloaded(Page page) {
		AppUtils.logI(this, String.format("Notify that Page %d downloaded.", page.mPageIndex));

		// current page
		if (page.mPageIndex == mPageCurrent) {
			Bitmap bitmap = page.getBitmap();
			if (bitmap != null) {
				mPage.setImageBitmap(bitmap);
			} else {
				AppUtils.logE(this, "Invalid bitmap.");
			}
		}

		// preload page
		preloadPage(page.mPageIndex + 1);
	}

	private boolean checkDummyPic(byte[] data) {
		if (DUMMY_PIC_CRC32.size() == 0) {
			String[] array = getResources().getStringArray(R.array.dummy_pic_crc32);
			for (int i = 0; i < array.length; i++) {
				DUMMY_PIC_CRC32.add(array[i]);
			}
		}
		CRC32 crc = new CRC32();
		crc.update(data);
		String hash = Integer.toHexString((int) crc.getValue()).toUpperCase();
		return DUMMY_PIC_CRC32.contains(hash);
	}

	private void printDebug(String msg, String tag) {
		mtvDebug.append("\n");
		SpannableString text;
		if (TextUtils.isEmpty(tag)) {
			text = new SpannableString(msg);
		} else {
			text = new SpannableString(tag + ": " + msg);
			text.setSpan(new StyleSpan(Typeface.BOLD), 0, tag.length() + 1, 0);
		}
		mtvDebug.append(text);
		mtvDebug.post(new Runnable() {
			@Override
			public void run() {
				msvScroller.smoothScrollBy(0, 100);
				// msvScroller.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
	}

	// private void printDebug(String msg) {
	// printDebug(msg, null);
	// }
}
