package org.falconia.mangaproxy.task;

import android.os.AsyncTask;

public class SourceProcessTask extends AsyncTask<String, Void, Integer> {

	private OnSourceProcessListener mListener;

	public SourceProcessTask(OnSourceProcessListener listener) {
		this.mListener = listener;
	}

	@Override
	protected Integer doInBackground(String... params) {
		return this.mListener.onSourceProcess(params[0]);
	}

	@Override
	protected void onPreExecute() {
		this.mListener.onPreSourceProcess();
	}

	@Override
	protected void onPostExecute(Integer result) {
		this.mListener.onPostSourceProcess(result);
	}

}
