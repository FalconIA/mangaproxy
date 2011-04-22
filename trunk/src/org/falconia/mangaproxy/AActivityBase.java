package org.falconia.mangaproxy;

import org.falconia.mangaproxy.helper.ProgressView;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

public abstract class AActivityBase extends Activity {
	private boolean mbInProgress = false;
	private boolean mbIsDestroyed = false;
	private boolean mbIsVisble = false;
	private ProgressView mvProgress;

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		switch (event.getKeyCode()) {
		case 19:
		case 20:
		case 21:
		case 22:
		case 23:
		case 82:
			return false;
		default:
			return super.dispatchKeyEvent(event);
		}
	}

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
	protected void onResume() {
		this.mbIsVisble = true;
		System.gc();
		super.onResume();
	}

	@Override
	protected void onPause() {
		this.mbIsVisble = false;
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		this.mbIsDestroyed = true;
		System.gc();
		super.onDestroy();
	}

	protected void setCustomTitle(String string) {
		String str = getString(R.string.app_name);
		if (!TextUtils.isEmpty(string))
			str += string;
		setTitle(str);
	}

	protected void setProgressView(ProgressView progressView) {
		progressView.setVisibility(View.GONE);
		this.mvProgress = progressView;
	}

	protected void showProgressView() {
		showProgressView(null);
	}

	protected void showProgressView(String string) {
		this.mbInProgress = true;
		if (this.mvProgress != null)
			this.mvProgress.startProgress(string);
		setProgressBarIndeterminateVisibility(true);
	}

	protected void hideProgressView() {
		this.mbInProgress = false;
		if (this.mvProgress != null)
			this.mvProgress.stopProgress();
		setProgressBarIndeterminateVisibility(false);
	}

}
