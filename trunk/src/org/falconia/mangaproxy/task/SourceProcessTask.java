package org.falconia.mangaproxy.task;

import android.os.AsyncTask;

public class SourceProcessTask extends AsyncTask<String, Void, Integer> {

	private OnSourceProcessListener mListener;

	public SourceProcessTask(OnSourceProcessListener listener) {
		mListener = listener;
	}

	@Override
	protected Integer doInBackground(String... params) {
		return mListener.onSourceProcess(params[0], params[1]);
	}

	@Override
	protected void onPreExecute() {
		mListener.onPreSourceProcess();
	}

	@Override
	protected void onPostExecute(Integer result) {
		mListener.onPostSourceProcess(result);
	}

}
