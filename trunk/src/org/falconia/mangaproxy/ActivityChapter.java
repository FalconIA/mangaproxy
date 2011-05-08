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
import org.falconia.mangaproxy.utils.FormatUtils;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public final class ActivityChapter extends Activity implements OnClickListener, OnCancelListener {

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
			switch (mMode) {
			case MODE_CHAPTER:
				showDialog(DIALOG_LOADING_ID);
				break;
			case MODE_IMG_SERVERS:
				mLoadingDialog.setMessage(String.format(
						getString(R.string.dialog_loading_imgsvrs_message_format), "0.000KB"));
				break;
			}
		}

		@Override
		public void onPostDownload(byte[] result) {
			AppUtils.logV(this, "onPostDownload()");
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
			if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
				String message = null;
				String filesize = FormatUtils.getFileSizeBtoKB(value);
				switch (mMode) {
				case MODE_CHAPTER:
					message = String.format(
							getString(R.string.dialog_loading_chapter_message_format), filesize);
					break;
				case MODE_IMG_SERVERS:
					message = String.format(
							getString(R.string.dialog_loading_imgsvrs_message_format), filesize);
					break;
				}
				if (message != null) {
					mLoadingDialog.setMessage(message);
				}
			}
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

			if (mPageIndex == mPageCurrent) {
				mtvDownloading.setText(String.format(getString(R.string.ui_downloading_page),
						mPageCurrent));
				mvgStatusBar.setVisibility(View.VISIBLE);
			}
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
			if (mPageIndex == mPageCurrent) {
				mvgStatusBar.setVisibility(View.GONE);
			}
			// TODO Debug
			printDebug(mUrl, "Downloaded");
			notifyPageDownloaded(this);
		}

		@Override
		public void onDownloadProgressUpdate(int value, int total) {
			if (mPageIndex == mPageCurrent) {
				if (value == 0) {
					mpbDownload.setMax(total);
				}
				mpbDownload.setProgress(value);
				mtvDownloaded.setText(String.format("%s / %s", FormatUtils.getFileSizeBtoKB(value),
						FormatUtils.getFileSizeBtoKB(total)));
			}
		}

		public boolean checkCache() {
			boolean cached = AppCache.checkCacheForImage(mUrl, TYPE);
			// this.mIsDownloaded = this.mIsDownloaded || cached;
			return cached;
		}

		public boolean isDownloaded() {
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
			if (isDownloaded()) {
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
			printDebug(mUrl, "Loading");
			return AppCache.readCacheForImage(mUrl, TYPE);
		}
	}

	private static final String BUNDLE_KEY_IS_PROCESSED = "BUNDLE_KEY_IS_PROCESSED";

	private static final int DIALOG_LOADING_ID = 0;

	private static final HashSet<String> DUMMY_PIC_CRC32 = new HashSet<String>();

	private SourceDownloader mSourceDownloader;

	private Manga mManga;
	private Chapter mChapter;

	private String[] mPageUrls;
	private HashMap<Integer, Page> mPages;
	private int mPageCurrent;

	private TextView mtvDebug;
	private ScrollView msvScroller;
	private ProgressDialog mLoadingDialog;
	private ImageView mPageView;

	private LinearLayout mvgTitleBar;
	private TextView mtvTitle;
	private LinearLayout mvgStatusBar;
	private TextView mtvDownloading;
	private TextView mtvDownloaded;
	private ProgressBar mpbDownload;

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
		// setTitle(getCustomTitle());

		// Debug controls
		mtvDebug = (TextView) findViewById(R.id.mtvDebug);
		mtvDebug.setClickable(true);
		msvScroller = (ScrollView) findViewById(R.id.msvScroller);
		// Page image
		mPageView = (ImageView) findViewById(R.id.mivPage);
		// Buttons
		((Button) findViewById(R.id.mbtnNext)).setOnClickListener(this);
		((Button) findViewById(R.id.mbtnPrev)).setOnClickListener(this);
		// Title bar
		mvgTitleBar = (LinearLayout) findViewById(R.id.mvgTitleBar);
		mvgTitleBar.setVisibility(View.GONE);
		mtvTitle = (TextView) findViewById(R.id.mtvTitle);
		mtvTitle.setText(getCustomTitle());
		// Status bar
		mvgStatusBar = (LinearLayout) findViewById(R.id.mvgStatusBar);
		mvgStatusBar.setVisibility(View.GONE);
		mtvDownloading = (TextView) findViewById(R.id.mtvDownloading);
		mtvDownloading.setText(String.format(getString(R.string.ui_downloading_page), 0));
		mtvDownloaded = (TextView) findViewById(R.id.mtvDownloaded);
		mpbDownload = (ProgressBar) findViewById(R.id.mpbDownload);
		mpbDownload.setProgress(0);

		// Listener
		mtvDebug.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// AppUtils.popupMessage(ActivityChapter.this, "onClick");
				msvScroller.setVisibility(View.GONE);
				mtvTitle.setClickable(true);
				mtvTitle.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// mtvTitle.setOnClickListener(null);
						// mtvTitle.setClickable(false);
						msvScroller.setVisibility(View.VISIBLE);
					}
				});
			}
		});

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

		stopTask();

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

		stopTask();

		outState.putBoolean(BUNDLE_KEY_IS_PROCESSED, mProcessed);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		AppUtils.logV(this, "onRestoreInstanceState()");
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_LOADING_ID:
			mLoadingDialog = createLoadingDialog();
			return mLoadingDialog;
		default:
			dialog = super.onCreateDialog(id);
		}
		return dialog;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		if (mSourceDownloader != null) {
			mSourceDownloader.cancelDownload();
		}
		finish();
	}

	@Override
	public void onClick(View v) {
		if (mPageCurrent < 1 || mPageCurrent > mPages.size()) {
			return;
		}

		switch (v.getId()) {
		case R.id.mbtnNext:
			AppUtils.popupMessage(this, "Goto Next page.");
			break;
		case R.id.mbtnPrev:
			AppUtils.popupMessage(this, "Goto Previous page.");
			break;
		}
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
		removeDialog(DIALOG_LOADING_ID);
	}

	private String getCustomTitle() {
		String title = String.format("%s - %s - %s", mManga.getSiteDisplayname(),
				mManga.displayname, mChapter.displayname);
		if (mPageCurrent > 0) {
			title = String.format("%s - " + getString(R.string.ui_page), title, mPageCurrent);
		}
		return title;
	}

	private void setMessage(String msg) {
		((TextView) findViewById(R.id.mtvMessage)).setText(msg);
	}

	private ProgressDialog createLoadingDialog() {
		ProgressDialog dialog = new ProgressDialog(this);
		dialog.setTitle(mChapter.displayname);
		dialog.setMessage(String.format(getString(R.string.dialog_loading_chapter_message_format),
				"0.000KB"));
		dialog.setCancelable(true);
		dialog.setOnCancelListener(this);
		return dialog;
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
				printDebug(urlImgServers, "Loading");
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
		printDebug(imgServer, "Get DynamicImgServer");

		for (int i = 0; i < mPageUrls.length; i++) {
			String url = (imgServer + mPageUrls[i]).replaceAll("(?<!http:)//", "/");
			Page page = new Page(i + 1, url);
			mPages.put(i + 1, page);
		}

		try {
			dismissDialog(DIALOG_LOADING_ID);
		} catch (Exception e) {
			AppUtils.logE(this, "dismissDialog(DIALOG_LOADING_ID)");
		}

		mProcessed = true;

		chagePage(1);
	}

	private void chagePage(int pageIndex) {
		if (pageIndex <= 0 || pageIndex > mPages.size()) {
			return;
		}

		AppUtils.logI(this, String.format("chagePage(%d)", pageIndex));
		mPageCurrent = pageIndex;
		mtvTitle.setText(getCustomTitle());
		mvgTitleBar.setVisibility(View.VISIBLE);
		Page page = mPages.get(mPageCurrent);
		page.download();
	}

	private void preloadPage(int pageIndex) {
		if (pageIndex - mPageCurrent <= AppConst.IMG_PRELOAD_MAX && pageIndex < mPages.size()) {
			mPages.get(pageIndex).download();
		} else {
			new Handler().postAtTime(new Runnable() {
				@Override
				public void run() {
					msvScroller.setVisibility(View.GONE);
				}
			}, SystemClock.uptimeMillis() + 2000);
		}
	}

	private void notifyPageDownloaded(Page page) {
		AppUtils.logI(this, String.format("Notify that Page %d downloaded.", page.mPageIndex));

		// current page
		if (page.mPageIndex == mPageCurrent) {
			Bitmap bitmap = page.getBitmap();
			if (bitmap != null) {
				mPageView.setImageBitmap(bitmap);
				new Handler().postAtTime(new Runnable() {
					@Override
					public void run() {
						mvgTitleBar.setVisibility(View.GONE);
					}
				}, SystemClock.uptimeMillis() + 2000);
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
