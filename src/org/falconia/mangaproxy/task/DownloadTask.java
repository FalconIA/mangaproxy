package org.falconia.mangaproxy.task;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.falconia.mangaproxy.AppConst;

import android.os.AsyncTask;
import android.util.Log;

public class DownloadTask extends AsyncTask<String, Integer, byte[]> {

	protected static final int TIME_OUT_CONNECT = 10000;
	protected static final int TIME_OUT_READ = 10000;

	private static final int MAX_BUFFER_SIZE = 1024;

	private int mFileSize;
	private int mDownloaded;
	private OnDownloadListener mListener;

	public DownloadTask(OnDownloadListener listener) {
		this.mFileSize = 0;
		this.mDownloaded = 0;
		this.mListener = listener;
	}

	@Override
	protected byte[] doInBackground(String... params) {
		return download(params[0]);
	}

	@Override
	protected void onPreExecute() {
		logD("Download start.");
		this.mListener.onPreDownload();
	}

	@Override
	protected void onPostExecute(byte[] result) {
		logD("Download done.");
		this.mListener.onPostDownload(result);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		this.mListener.onDownloadProgressUpdate(values[0], this.mFileSize);
	}

	protected byte[] download(String url) {
		logD("Download: " + url);
		HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) (new URL(url)).openConnection();
			connection.setConnectTimeout(TIME_OUT_CONNECT);
			connection.setReadTimeout(TIME_OUT_READ);

			int statusCode = connection.getResponseCode();
			if (statusCode >= 400) {
				logE("Invalid Status Code: " + statusCode);
				return null;
			} else
				logD("Status Code: " + statusCode);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			logE("Invalid URL: " + e.getMessage());
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			logE("Fail to open the connection: " + e.getMessage());
			return null;
		}

		InputStream input;
		try {
			input = connection.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
			logE("IOException(InputStream): " + e.getMessage());
			return null;
		}

		this.mFileSize = connection.getContentLength();
		logD(String.format("Content-Length: %d", this.mFileSize));
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		publishProgress(0);

		try {
			int readed = 0;
			while (readed != -1 || this.mFileSize > this.mDownloaded) {
				byte[] buffer = new byte[MAX_BUFFER_SIZE];
				readed = input.read(buffer);
				if (readed == -1) {
					publishProgress(this.mDownloaded);
					break;
				}
				output.write(buffer, 0, readed);
				this.mDownloaded += readed;
				publishProgress(this.mDownloaded);
			}
			byte[] result = output.toByteArray();
			logD("Download length: " + result.length);
			output.flush();
			output.close();
			input.close();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			logE("IOException(InputStream): " + e.getMessage());
		}

		return null;
	}

	private String getTag() {
		return getClass().getSimpleName();
	}

	private void log(int priority, String msg) {
		Log.println(priority, AppConst.APP_NAME,
				String.format("[%s] %s", getTag(), msg));
	}

	private void logD(String msg) {
		log(Log.DEBUG, msg);
	}

	private void logE(String msg) {
		log(Log.ERROR, msg);
	}

}
