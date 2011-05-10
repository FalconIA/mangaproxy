package org.falconia.mangaproxy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.zip.CRC32;

import org.apache.http.util.EncodingUtils;
import org.falconia.mangaproxy.AppConst.ZoomMode;
import org.falconia.mangaproxy.data.Chapter;
import org.falconia.mangaproxy.data.Manga;
import org.falconia.mangaproxy.plugin.Plugins;
import org.falconia.mangaproxy.task.DownloadTask;
import org.falconia.mangaproxy.task.OnDownloadListener;
import org.falconia.mangaproxy.ui.ZoomViewOnTouchListener;
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
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.sonyericsson.zoom.DynamicZoomControl;
import com.sonyericsson.zoom.ImageZoomView;
import com.sonyericsson.zoom.ZoomState.AlignX;
import com.sonyericsson.zoom.ZoomState.AlignY;

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
			AppCache.wipeCacheForImage(TYPE);
		}

	}

	private final class PageViewOnTouchListener extends ZoomViewOnTouchListener {

		public PageViewOnTouchListener() {
			super(getApplicationContext());
		}

		@Override
		public boolean onSingleTap() {
			showTitleBar();
			return true;
		}

		@Override
		public void onNextPage() {
		}

		@Override
		public void onPrevPage() {
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
				break;
			case MODE_IMG_SERVERS:
				mLoadingDialog.setMessage(String.format(
						getString(R.string.dialog_loading_imgsvrs_message_format), "0.000KB"));
				break;
			}

			showDialog(DIALOG_LOADING_ID);
		}

		@Override
		public void onPostDownload(byte[] result) {
			AppUtils.logV(this, "onPostDownload()");

			try {
				dismissDialog(DIALOG_LOADING_ID);
			} catch (Exception e) {
				AppUtils.logE(this, "dismissDialog(DIALOG_LOADING_ID)");
			}

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

			// TODO Debug
			printDebug(mUrl, "Downloading");

			if (mPageIndex == mPageIndexLoading) {
				showStatusBar();
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

			if (mPageIndex == mPageIndexLoading) {
				mvgStatusBar.setVisibility(View.GONE);
			}

			// TODO Debug
			printDebug(mUrl, "Downloaded");

			notifyPageDownloaded(this);
		}

		@Override
		public void onDownloadProgressUpdate(int value, int total) {
			if (mPageIndex == mPageIndexLoading) {
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
				if (mPageIndex == mPageIndexLoading) {
					showStatusBar();
				}
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
				mIsDownloading = false;
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

		private void showStatusBar() {
			mpbDownload.setProgress(0);
			mtvDownloading.setText(String.format(getString(R.string.ui_downloading_page),
					mPageIndexLoading));
			mvgStatusBar.setVisibility(View.VISIBLE);
		}
	}

	private final class Configuration {
		private boolean mProcessed;

		private String[] mPageUrls;
		private HashMap<Integer, Page> mPages;
		private int mPageIndexCurrent;
		private int mPageIndexLoading;

		private CharSequence mtvDebugText;
		private int msvScrollerVisibility;

		private ZoomMode mZoomMode;
		private Bitmap mPageViewImage;

		private int mvgTitleBarVisibility;
	}

	private static final int DIALOG_LOADING_ID = 0;

	private static final HashSet<String> DUMMY_PIC_CRC32 = new HashSet<String>();

	private static final String TYPE = "page";

	private SourceDownloader mSourceDownloader;

	private Manga mManga;
	private Chapter mChapter;

	private boolean mProcessed;

	private String[] mPageUrls;
	private HashMap<Integer, Page> mPages;
	private int mPageIndexCurrent;
	private int mPageIndexLoading;
	private LinkedList<Integer> mPreloadPageIndexQueue;

	private TextView mtvDebug;
	private ScrollView msvScroller;

	private ProgressDialog mLoadingDialog;

	private ZoomMode mZoomMode;
	private ImageZoomView mPageView;
	private DynamicZoomControl mZoomControl;
	private PageViewOnTouchListener mZoomListener;

	private LinearLayout mvgTitleBar;
	private TextView mtvTitle;
	private LinearLayout mvgStatusBar;
	private TextView mtvDownloading;
	private TextView mtvDownloaded;
	private ProgressBar mpbDownload;

	private MenuItem mmiZoomFitWidthAutoSplit;
	private MenuItem mmiZoomFitWidth;
	private MenuItem mmiZoomFitHeight;
	private MenuItem mmiZoomFitScreen;

	private final Handler mHideScrollerHandler;
	private final Handler mHideTitleBarHandler;
	private final Runnable mHideScrollerRunnable;
	private final Runnable mHideTitleBarRunnable;

	public ActivityChapter() {
		mProcessed = false;
		mPageIndexCurrent = 0;
		mPageIndexLoading = 0;
		mZoomMode = ZoomMode.FIT_WIDTH_AUTO_SPLIT;
		mPreloadPageIndexQueue = new LinkedList<Integer>();
		mHideScrollerHandler = new Handler();
		mHideTitleBarHandler = new Handler();
		mHideScrollerRunnable = new Runnable() {
			@Override
			public void run() {
				if (msvScroller != null) {
					msvScroller.setVisibility(View.GONE);
					mHideScrollerHandler.removeCallbacks(this);
				}
			}
		};
		mHideTitleBarRunnable = new Runnable() {
			@Override
			public void run() {
				if (mvgTitleBar != null) {
					mvgTitleBar.setVisibility(View.GONE);
					mHideTitleBarHandler.removeCallbacks(this);
				}
			}
		};
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

		mManga = IntentHandler.getManga(this);
		mChapter = IntentHandler.getChapter(this);
		if (mManga == null || mChapter == null) {
			finish();
		}

		setContentView(R.layout.activity_chapter);
		// setTitle(getCustomTitle());

		final Configuration conf = (Configuration) getLastNonConfigurationInstance();
		if (conf != null) {
			mProcessed = conf.mProcessed;
			mPageIndexCurrent = conf.mPageIndexCurrent;
			mPageIndexLoading = conf.mPageIndexLoading;
			mZoomMode = conf.mZoomMode;
		}

		// Debug controls
		mtvDebug = (TextView) findViewById(R.id.mtvDebug);
		msvScroller = (ScrollView) findViewById(R.id.msvScroller);
		msvScroller.setVisibility(View.GONE);
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
		// Page image
		mZoomControl = new DynamicZoomControl();
		mZoomListener = new PageViewOnTouchListener();
		mZoomListener.setZoomControl(mZoomControl);
		mPageView = (ImageZoomView) findViewById(R.id.mivPage);
		mPageView.setZoomState(mZoomControl.getZoomState());
		mPageView.setOnTouchListener(mZoomListener);
		mZoomControl.setAspectQuotient(mPageView.getAspectQuotient());
		setupZoomState();

		if (AppConst.DEBUG > 0) {
			msvScroller.setVisibility(View.VISIBLE);

			// Listener
			mtvDebug.setClickable(true);
			mtvDebug.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					msvScroller.setVisibility(View.GONE);
				}
			});
			mtvTitle.setClickable(true);
			mtvTitle.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// mtvTitle.setOnClickListener(null);
					// mtvTitle.setClickable(false);
					msvScroller.setVisibility(View.VISIBLE);
					msvScroller.fullScroll(View.FOCUS_DOWN);
				}
			});
		}

		if (!mProcessed) {
			loadChapter();
		} else {
			mPageUrls = conf.mPageUrls;
			mPages = new HashMap<Integer, Page>();
			for (int key : conf.mPages.keySet()) {
				mPages.put(key, new Page(key, conf.mPages.get(key).mUrl));
			}

			if (AppConst.DEBUG > 0) {
				mtvDebug.setText(conf.mtvDebugText);
				msvScroller.post(new Runnable() {
					@Override
					public void run() {
						msvScroller.fullScroll(View.FOCUS_DOWN);
					}
				});
				msvScroller.setVisibility(conf.msvScrollerVisibility);
			}

			mvgTitleBar.setVisibility(conf.mvgTitleBarVisibility);

			// Set image
			mPageView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					AppUtils.logV(this, "onGlobalLayout()");
					setImage(conf.mPageViewImage);
				}
			});

			mHideScrollerHandler.postAtTime(mHideScrollerRunnable, 2000);
			mHideTitleBarHandler.postAtTime(mHideTitleBarRunnable, 2000);

			if (mPageIndexCurrent != mPageIndexLoading) {
				AppUtils.logW(this, "mPageIndexCurrent != mPageIndexLoading");
				changePage(mPageIndexLoading);
			} else {
				AppUtils.logW(this, "mPageIndexCurrent == mPageIndexLoading");
				changePage(mPageIndexLoading);
			}
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

		removeDialog(DIALOG_LOADING_ID);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		AppUtils.logV(this, "onRestoreInstanceState()");

		mpbDownload.setProgress(0);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		AppUtils.logV(this, "onRetainNonConfigurationInstance()");

		Configuration conf = new Configuration();

		conf.mProcessed = mProcessed;

		conf.mPageUrls = mPageUrls;
		conf.mPages = mPages;
		conf.mPageIndexCurrent = mPageIndexCurrent;
		conf.mPageIndexLoading = mPageIndexLoading;

		conf.mtvDebugText = mtvDebug.getText();
		conf.msvScrollerVisibility = msvScroller.getVisibility();

		conf.mZoomMode = mZoomMode;
		conf.mPageViewImage = mPageView.getImage();

		conf.mvgTitleBarVisibility = mvgTitleBar.getVisibility();

		return conf;
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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_chapter, menu);
		mmiZoomFitWidthAutoSplit = menu.findItem(R.id.mmiZoomFitWidthAutoSplit);
		mmiZoomFitWidthAutoSplit.setChecked(mZoomMode == ZoomMode.FIT_WIDTH_AUTO_SPLIT);
		mmiZoomFitWidth = menu.findItem(R.id.mmiZoomFitWidth);
		mmiZoomFitWidth.setChecked(mZoomMode == ZoomMode.FIT_WIDTH);
		mmiZoomFitHeight = menu.findItem(R.id.mmiZoomFitHeight);
		mmiZoomFitHeight.setChecked(mZoomMode == ZoomMode.FIT_HEIGHT);
		mmiZoomFitScreen = menu.findItem(R.id.mmiZoomFitScreen);
		mmiZoomFitScreen.setChecked(mZoomMode == ZoomMode.FIT_SCREEN);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getGroupId() == R.id.mmgZoomGroup) {
			switch (item.getItemId()) {
			case R.id.mmiZoomFitWidthAutoSplit:
				mZoomMode = ZoomMode.FIT_WIDTH_AUTO_SPLIT;
				break;
			case R.id.mmiZoomFitWidth:
				mZoomMode = ZoomMode.FIT_WIDTH;
				break;
			case R.id.mmiZoomFitHeight:
				mZoomMode = ZoomMode.FIT_HEIGHT;
				break;
			case R.id.mmiZoomFitScreen:
				mZoomMode = ZoomMode.FIT_SCREEN;
				break;
			}
			mmiZoomFitWidthAutoSplit.setChecked(mZoomMode == ZoomMode.FIT_WIDTH_AUTO_SPLIT);
			mmiZoomFitWidth.setChecked(mZoomMode == ZoomMode.FIT_WIDTH);
			mmiZoomFitHeight.setChecked(mZoomMode == ZoomMode.FIT_HEIGHT);
			mmiZoomFitScreen.setChecked(mZoomMode == ZoomMode.FIT_SCREEN);
			mZoomControl.getZoomState().setDefaultZoom(computeDefaultZoom(mZoomMode, mPageView));
			mZoomControl.getZoomState().notifyObservers();
			mZoomControl.startFling(0, 0);
			return true;
		}
		return super.onOptionsItemSelected(item);
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
		switch (v.getId()) {
		case R.id.mbtnNext:
			changePage(true);
			break;
		case R.id.mbtnPrev:
			changePage(false);
			break;
		}
	}

	private void stopTask() {
		mtvDebug.setOnClickListener(null);
		mtvTitle.setOnClickListener(null);

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
		String title = String.format("%s - %s", mManga.displayname, mChapter.displayname);
		if (mPageIndexCurrent > 0) {
			title = String.format("%s - " + getString(R.string.ui_page), title, mPageIndexCurrent);
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

		mProcessed = true;

		changePage(1);
	}

	private void changePage(int pageIndex) {
		if (pageIndex <= 0 || pageIndex > mPages.size()) {
			return;
		}

		AppUtils.logI(this, String.format("chagePage(%d)", pageIndex));

		mPageIndexLoading = pageIndex;
		mPreloadPageIndexQueue.clear();
		for (int i = 1; i <= AppConst.IMG_PRELOAD_MAX; i++) {
			mPreloadPageIndexQueue.add(mPageIndexLoading + i);
		}

		Page page = mPages.get(mPageIndexLoading);
		page.download();
	}

	private void changePage(boolean nextpage) {
		// Loading
		if (mPageIndexLoading != mPageIndexCurrent) {
			return;
		}

		int mPageIndexGoto = mPageIndexCurrent + (nextpage ? 1 : -1);

		// Prev chapter
		if (mPageIndexGoto < 1) {
			AppUtils.popupMessage(this, "First Page");
		}
		// Next chapter
		else if (mPageIndexGoto > mPages.size()) {
			AppUtils.popupMessage(this, "Last Page");
		} else {
			changePage(mPageIndexGoto);
		}
	}

	private void preloadPage(int pageIndex) {
		if (pageIndex - mPageIndexLoading <= AppConst.IMG_PRELOAD_MAX && pageIndex <= mPages.size()) {
			AppUtils.logI(this, String.format("preloadPage(%d)", pageIndex));
			mPages.get(pageIndex).download();
		} else {
			hideScroller();
		}
	}

	private void notifyPageDownloaded(Page page) {
		AppUtils.logI(this, String.format("Notify that Page %d downloaded.", page.mPageIndex));

		// current page
		if (page.mPageIndex == mPageIndexLoading) {
			Bitmap bitmap = page.getBitmap();
			if (bitmap != null) {
				mPageIndexCurrent = mPageIndexLoading;
				setImage(bitmap);
				mtvTitle.setText(getCustomTitle());
				showTitleBar();
			} else {
				AppUtils.logE(this, "Invalid bitmap.");
			}
		}

		// preload page
		if (mPreloadPageIndexQueue.isEmpty()) {
			hideScroller();
		} else {
			if (AppConst.DEBUG > 0) {
				mHideScrollerHandler.removeCallbacks(mHideScrollerRunnable);
				msvScroller.setVisibility(View.VISIBLE);
			}
			int pageIndexPreload = mPreloadPageIndexQueue.poll();
			preloadPage(pageIndexPreload);
		}
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

	private void setupZoomState() {
		mZoomControl.getZoomState().setAlignX(AlignX.Right);
		mZoomControl.getZoomState().setAlignY(AlignY.Top);
		mZoomControl.getZoomState().setPanX(0.0f);
		mZoomControl.getZoomState().setPanY(0.0f);
	}

	private void setImage(Bitmap bitmap) {
		AppUtils.logD(this, "ZoomView Width: " + mPageView.getWidth());
		AppUtils.logD(this, "ZoomView Height: " + mPageView.getHeight());
		AppUtils.logD(this, "AspectQuotient: " + mPageView.getAspectQuotient().get());

		mZoomControl.stopFling();
		mZoomControl.getZoomState().setPanX(0.0f);
		mZoomControl.getZoomState().setPanY(0.0f);
		mPageView.setImage(bitmap);
		mZoomControl.getZoomState().setDefaultZoom(computeDefaultZoom(mZoomMode, mPageView));
		mZoomControl.getZoomState().notifyObservers();
	}

	private float computeDefaultZoom(ZoomMode mode, ImageZoomView view) {
		final Bitmap bitmap = view.getImage();

		if (view.getAspectQuotient() == null || view.getAspectQuotient().get() == Float.NaN) {
			return 1f;
		}
		if (view == null || view.getWidth() == 0 || view.getHeight() == 0) {
			return 1f;
		}
		if (bitmap == null || bitmap.getWidth() == 0 || bitmap.getHeight() == 0) {
			return 1f;
		}

		if (mode == ZoomMode.FIT_SCREEN) {
			return 1f;
		}

		// aq = (bW / bH) / (vW / vH)
		final float aq = view.getAspectQuotient().get();
		float zoom = 1f;

		if (mode == ZoomMode.FIT_WIDTH || mode == ZoomMode.FIT_WIDTH_AUTO_SPLIT) {
			// Over height
			if (aq < 1f) {
				zoom = 1f / aq;
			} else {
				zoom = 1f;
			}

			if (mode == ZoomMode.FIT_WIDTH_AUTO_SPLIT) {
				if (1f * bitmap.getWidth() / view.getWidth() > 1.5f
						&& bitmap.getWidth() > bitmap.getHeight()) {
					zoom *= (2f + AppConst.WIDTH_AUTO_SPLIT_MARGIN)
							/ (1f + AppConst.WIDTH_AUTO_SPLIT_MARGIN);
				}
			}
		} else if (mode == ZoomMode.FIT_HEIGHT) {
			// Over width
			if (aq > 1f) {
				zoom = aq;
			} else {
				zoom = 1f;
			}
		}

		return zoom;
	}

	private void printDebug(String msg, String tag) {
		if (AppConst.DEBUG == 0) {
			return;
		}

		mtvDebug.append("\n");
		SpannableString text;
		if (TextUtils.isEmpty(tag)) {
			text = new SpannableString(msg);
		} else {
			text = new SpannableString(tag + ": " + msg);
			text.setSpan(new StyleSpan(Typeface.BOLD), 0, tag.length() + 1, 0);
		}
		mtvDebug.append(text);
		msvScroller.post(new Runnable() {
			@Override
			public void run() {
				if (msvScroller != null) {
					msvScroller.smoothScrollBy(0, 100);
				}
			}
		});
	}

	private void hideScroller() {
		mHideScrollerHandler.removeCallbacks(mHideScrollerRunnable);
		mHideScrollerHandler.postDelayed(mHideScrollerRunnable, 2000);
	}

	private void showTitleBar() {
		mvgTitleBar.setVisibility(View.VISIBLE);
		mtvTitle.requestFocus();
		hideTitleBar();
	}

	private void hideTitleBar() {
		mHideTitleBarHandler.removeCallbacks(mHideTitleBarRunnable);
		mHideTitleBarHandler.postDelayed(mHideTitleBarRunnable, 5000);
	}

	// private void printDebug(String msg) {
	// printDebug(msg, null);
	// }
}
