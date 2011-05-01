package org.falconia.mangaproxy;

import org.falconia.mangaproxy.task.GetSourceTask;
import org.falconia.mangaproxy.task.OnDownloadListener;
import org.falconia.mangaproxy.task.OnSourceProcessListener;
import org.falconia.mangaproxy.task.SourceProcessTask;
import org.falconia.mangaproxy.ui.BaseListAdapter;
import org.falconia.mangaproxy.ui.PinnedHeaderListView;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

public abstract class ActivityBase extends ListActivity implements
		OnFocusChangeListener, OnTouchListener, OnItemClickListener,
		OnItemLongClickListener, OnDownloadListener, OnSourceProcessListener {

	protected static final String BUNDLE_KEY_IS_PROCESSED = "BUNDLE_KEY_IS_PROCESSED";

	protected static final int DIALOG_CLOSE_ID = -1;
	protected static final int DIALOG_LOADING_ID = 0;
	protected static final int DIALOG_DOWNLOAD_ID = 1;
	protected static final int DIALOG_PROCESS_ID = 2;

	protected GetSourceTask mGetSourceTask;
	protected SourceProcessTask mSourceProcessTask;
	protected boolean mShowProcessDialog = true;
	protected boolean mProcessed = false;

	protected BaseListAdapter mListAdapter;

	abstract String getSiteName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.mProcessed = getProcessed(savedInstanceState);
		super.onCreate(savedInstanceState);
		logI("onCreate()");
	}

	@Override
	protected void onStart() {
		super.onStart();
		logI("onStart()");
	}

	@Override
	protected void onResume() {
		// System.gc();
		super.onResume();
		logI("onResume()");
	}

	@Override
	protected void onPause() {
		super.onPause();
		logI("onPause()");
	}

	@Override
	protected void onStop() {
		super.onStop();
		logI("onStop()");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		logI("onRestart()");
	}

	@Override
	protected void onDestroy() {
		// System.gc();
		super.onDestroy();
		logI("onDestroy()");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		removeDialog(DIALOG_DOWNLOAD_ID);
		removeDialog(DIALOG_PROCESS_ID);
		outState.putBoolean(BUNDLE_KEY_IS_PROCESSED, this.mProcessed);
		super.onSaveInstanceState(outState);
		Log.i(getTag(), "onSaveInstanceState()");
	}

	protected boolean getProcessed(Bundle savedInstanceState) {
		boolean processed = false;
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(BUNDLE_KEY_IS_PROCESSED))
			processed = savedInstanceState.getBoolean(BUNDLE_KEY_IS_PROCESSED);
		return processed;
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Log.i(getTag(), "onRestoreInstanceState()");
	}

	/**
	 * Dismisses the soft keyboard when the list takes focus.
	 */
	@Override
	public void onFocusChange(View view, boolean hasFocus) {
		if (view == getListView() && hasFocus)
			hideSoftKeyboard();
	}

	/**
	 * Dismisses the soft keyboard when the list takes focus.
	 */
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (view == getListView())
			hideSoftKeyboard();
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// TODO Auto-generated method stub

		return false;
	}

	protected void onCancelDownload() {
		if (this.mGetSourceTask != null
				&& this.mGetSourceTask.getStatus() == AsyncTask.Status.RUNNING)
			this.mGetSourceTask.cancel(true);
		if (!this.mProcessed)
			finish();
	}

	@Override
	public void onPreDownload() {
		if (this.mGetSourceTask == null) {
			logE("GetSourceTask is not initialized.");
			return;
		}
		showDialog(DIALOG_DOWNLOAD_ID);
	}

	@Override
	public void onPostDownload(String source) {
		if (this.mGetSourceTask == null) {
			logE("GetSourceTask is not initialized.");
			return;
		}
		if (TextUtils.isEmpty(source)) {
			setNoItemsMessage(String.format(
					getString(R.string.error_on_download), getSiteName()));
			return;
		}
		dismissDialog(DIALOG_DOWNLOAD_ID);

		this.mSourceProcessTask = new SourceProcessTask(this);
		this.mSourceProcessTask.execute(source);
	}

	@Override
	public int onSourceProcess(String source) {
		return 0;
	}

	@Override
	public void onPreSourceProcess() {
		if (this.mSourceProcessTask == null) {
			logE("ProcessDataTask is not initialized.");
			return;
		}
		this.mProcessed = false;
		if (this.mShowProcessDialog)
			showDialog(DIALOG_PROCESS_ID);
	}

	@Override
	public void onPostSourceProcess(int size) {
		if (this.mSourceProcessTask == null) {
			logE("ProcessDataTask is not initialized.");
			return;
		}
		this.mProcessed = true;
		if (size <= 0)
			setNoItemsMessage(String.format(
					getString(R.string.error_on_process), getSiteName()));
		if (this.mShowProcessDialog)
			dismissDialog(DIALOG_PROCESS_ID);
	}

	protected void setCustomTitle(String string) {
		String str = getString(R.string.app_name);
		if (!TextUtils.isEmpty(string))
			str += string;
		setTitle(str);
	}

	protected void setNoItemsMessage(String msg) {
		((TextView) findViewById(R.id.mtvNoItems)).setText(msg);
	}

	protected void setNoItemsMessage(int resId) {
		setNoItemsMessage(getString(resId));
	}

	protected void setupListView(BaseListAdapter adapter) {
		this.mListAdapter = adapter;

		setListAdapter(this.mListAdapter);

		final ListView list = getListView();
		final LayoutInflater inflater = getLayoutInflater();

		// mHighlightingAnimation = new NameHighlightingAnimation(list,
		// TEXT_HIGHLIGHTING_ANIMATION_DURATION);

		// Tell list view to not show dividers. We'll do it ourself so that we
		// can *not* show
		// them when an A-Z headers is visible.
		// list.setDividerHeight(0);
		// list.setOnCreateContextMenuListener(this);

		list.setEmptyView(findViewById(R.id.mvgEmpty));

		if (list instanceof PinnedHeaderListView
				&& this.mListAdapter.getDisplaySectionHeadersEnabled()) {
			// mPinnedHeaderBackgroundColor =
			// getResources().getColor(R.color.pinned_header_background);
			PinnedHeaderListView pinnedHeaderList = (PinnedHeaderListView) list;
			View pinnedHeader = inflater.inflate(R.layout.list_section, list,
					false);
			pinnedHeaderList.setPinnedHeaderView(pinnedHeader);
		}

		list.setOnScrollListener(this.mListAdapter);
		list.setOnFocusChangeListener(this);
		list.setOnTouchListener(this);
		list.setOnItemClickListener(this);
		list.setOnItemLongClickListener(this);

		// We manually save/restore the listview state
		// list.setSaveEnabled(false);
	}

	protected ProgressDialog createProgressDialog(CharSequence title,
			CharSequence message, boolean indeterminate, boolean cancelable) {
		ProgressDialog dialog = new ProgressDialog(this);
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setIndeterminate(indeterminate);
		dialog.setCancelable(cancelable);
		return dialog;
	}

	protected ProgressDialog createProgressDialog(CharSequence title,
			CharSequence message, boolean indeterminate) {
		return createProgressDialog(title, message, indeterminate, false);
	}

	protected ProgressDialog createLoadingDialog(CharSequence what) {
		CharSequence title, message;
		if (TextUtils.isEmpty(what)) {
			title = null;
			message = getString(R.string.dialog_loading_message);
		} else {
			title = getString(R.string.dialog_loading_title);
			message = String.format(
					getString(R.string.dialog_loading_message_format), what);
		}
		return createProgressDialog(title, message, true, false);
	}

	protected ProgressDialog createLoadingDialog(int whatResId) {
		return createLoadingDialog(getString(whatResId));
	}

	protected ProgressDialog createLoadingDialog() {
		return createLoadingDialog(null);
	}

	protected ProgressDialog createDownloadDialog(CharSequence what) {
		CharSequence title, message;
		if (TextUtils.isEmpty(what)) {
			title = null;
			message = getString(R.string.dialog_download_message);
		} else {
			title = getString(R.string.dialog_download_title);
			message = String.format(
					getString(R.string.dialog_download_message_format), what);
		}
		ProgressDialog dialog = createProgressDialog(title, message, true, true);
		dialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				onCancelDownload();
			}
		});
		return dialog;
	}

	protected ProgressDialog createDownloadDialog(int whatResId) {
		return createDownloadDialog(getString(whatResId));
	}

	protected ProgressDialog createProcessDialog(CharSequence what) {
		CharSequence title, message;
		if (TextUtils.isEmpty(what)) {
			title = null;
			message = getString(R.string.dialog_process_message);
		} else {
			title = getString(R.string.dialog_process_title);
			message = String.format(
					getString(R.string.dialog_process_message_format), what);
		}
		return createProgressDialog(title, message, true, false);
	}

	protected ProgressDialog createProcessDialog(int whatResId) {
		return createProcessDialog(getString(whatResId));
	}

	private void hideSoftKeyboard() {
		// Hide soft keyboard, if visible
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(getListView()
				.getWindowToken(), 0);
	}

	protected String getTag() {
		return getClass().getSimpleName();
	}

	protected void log(int priority, String msg) {
		Log.println(priority, getString(R.string.app_name),
				String.format("[%s] %s", getTag(), msg));
	}

	protected void logD(String msg) {
		// DEBUG = 3
		log(Log.DEBUG, msg);
	}

	protected void logI(String msg) {
		// INFO = 4
		log(Log.INFO, msg);
	}

	protected void logE(String msg) {
		// ERROR = 6
		log(Log.ERROR, msg);
	}

}
