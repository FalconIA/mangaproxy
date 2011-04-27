package org.falconia.mangaproxy.task;

import org.falconia.mangaproxy.ActivityBase;

import android.os.AsyncTask;

public class ProcessDataTask extends AsyncTask<String, Void, Void> {

	ActivityBase mhActivity;

	public ProcessDataTask(ActivityBase activity) {
		this.mhActivity = activity;
	}

	@Override
	protected Void doInBackground(String... params) {
		this.mhActivity.onProcess(params[0]);
		return null;
	}

	@Override
	protected void onPreExecute() {
		this.mhActivity.onPreProcess();
	}

	@Override
	protected void onPostExecute(Void result) {
		this.mhActivity.onPostProcess();
	}

}
