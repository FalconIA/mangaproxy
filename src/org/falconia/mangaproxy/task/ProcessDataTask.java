package org.falconia.mangaproxy.task;

import org.falconia.mangaproxy.ActivityBase;

import android.os.AsyncTask;

public class ProcessDataTask extends AsyncTask<String, Void, Integer> {

	ActivityBase mhActivity;

	public ProcessDataTask(ActivityBase activity) {
		this.mhActivity = activity;
	}

	@Override
	protected Integer doInBackground(String... params) {
		return this.mhActivity.onProcess(params[0]);
	}

	@Override
	protected void onPreExecute() {
		this.mhActivity.onPreProcess();
	}

	@Override
	protected void onPostExecute(Integer result) {
		this.mhActivity.onPostProcess(result);
	}

}
