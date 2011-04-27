package org.falconia.mangaproxy;

import org.falconia.mangaproxy.task.GetSourceTask;
import org.falconia.mangaproxy.task.ProcessDataTask;
import org.falconia.mangaproxy.ui.ProgressView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

@SuppressWarnings("deprecation")
public abstract class ActivityBase extends Activity {
	public static final int DIALOG_CLOSE_ID = -1;
	public static final int DIALOG_LOADING_ID = 0;
	public static final int DIALOG_DOWNLOAD_ID = 1;
	public static final int DIALOG_PROCESS_ID = 2;

	protected GetSourceTask mhGetSourceTask;
	protected ProcessDataTask mhProcessDataTask;
	protected boolean mbShowProcessDialog = true;

	private boolean mbInProgress = false;
	private boolean mbIsDestroyed = false;
	private boolean mbIsVisble = false;

	@Deprecated
	private ProgressView mvProgress;

	protected String getTag() {
		return getClass().getSimpleName();
	}

	@Deprecated
	protected boolean inProgress() {
		try {
			return this.mbInProgress;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Deprecated
	protected boolean isDestroyed() {
		try {
			return this.mbIsDestroyed;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Deprecated
	protected boolean isVisble() {
		try {
			return this.mbIsVisble;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onPause() {
		this.mbIsVisble = false;
		super.onPause();
	}

	@Override
	protected void onResume() {
		this.mbIsVisble = true;
		System.gc();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		this.mbIsDestroyed = true;
		System.gc();
		super.onDestroy();
	}

	protected void onCancelDownload() {
		if (this.mhGetSourceTask != null
				&& this.mhGetSourceTask.getStatus() == AsyncTask.Status.RUNNING)
			this.mhGetSourceTask.cancel(true);
		finish();
	}

	public void onPreDownload() {
		if (this.mhGetSourceTask == null)
			return;
		showDialog(DIALOG_DOWNLOAD_ID);
	}

	public void onPostDownload(String source) {
		if (this.mhGetSourceTask == null)
			return;
		dismissDialog(DIALOG_DOWNLOAD_ID);
		if (this.mhProcessDataTask == null)
			return;
		this.mhProcessDataTask.execute(source);
	}

	public void onProcess(String source) {

	}

	public void onPreProcess() {
		if (!this.mbShowProcessDialog || this.mhProcessDataTask == null)
			return;
		showDialog(DIALOG_PROCESS_ID);
	}

	public void onPostProcess() {
		if (!this.mbShowProcessDialog || this.mhProcessDataTask == null)
			return;
		dismissDialog(DIALOG_PROCESS_ID);
	}

	protected void setCustomTitle(String string) {
		String str = getString(R.string.app_name);
		if (!TextUtils.isEmpty(string))
			str += string;
		setTitle(str);
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
		ProgressDialog dialog = createProgressDialog(title, message, true,
				false);
		dialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				onCancelDownload();
			}
		});
		return dialog;
	}

	protected ProgressDialog createProcessDialog(int whatResId) {
		return createProcessDialog(getString(whatResId));
	}

	@Deprecated
	protected void setProgressView(ProgressView progressView) {
		progressView.setVisibility(View.GONE);
		this.mvProgress = progressView;
	}

	@Deprecated
	protected void showProgressView() {
		showProgressView(null);
	}

	@Deprecated
	protected void showProgressView(String string) {
		this.mbInProgress = true;
		if (this.mvProgress != null)
			this.mvProgress.startProgress(string);
		setProgressBarIndeterminateVisibility(true);
	}

	@Deprecated
	protected void hideProgressView() {
		this.mbInProgress = false;
		if (this.mvProgress != null)
			this.mvProgress.stopProgress();
		setProgressBarIndeterminateVisibility(false);
	}

}
