package org.falconia.mangaproxy.task;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.falconia.mangaproxy.ActivityInit;

import android.util.Log;

public class GetSourceTask extends BasePluginTask<String, Void, String> {

	private static final DefaultHttpClient mhClient;

	static {
		mhClient = new DefaultHttpClient();
	}

	private final ResponseHandler<String> mResponseHandler;
	private OnDownloadListener mListener;

	public GetSourceTask(int siteId, OnDownloadListener listener) {
		super(siteId);
		this.mListener = listener;
		this.mResponseHandler = new ResponseHandler<String>() {
			@Override
			public String handleResponse(final HttpResponse response)
					throws HttpResponseException, IOException {
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300)
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getReasonPhrase());

				HttpEntity entity = response.getEntity();
				return entity == null ? null : EntityUtils.toString(entity,
						GetSourceTask.this.mhPlugin.getCharset());
			}
		};
	}

	@Override
	protected String doInBackground(String... params) {
		return parseHtml(params[0]);
	}

	@Override
	protected void onPreExecute() {
		this.mListener.onPreDownload();
	}

	@Override
	protected void onPostExecute(String source) {
		this.mListener.onPostDownload(source);
	}

	private String parseHtml(String url) {
		String html = "";
		try {
			HttpGet request = new HttpGet(url);
			html = mhClient.execute(request, this.mResponseHandler);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			Log.e(ActivityInit.APP_NAME, String.format(
					"[%s] Fail to execute HTTP client.", getClass().getName()));
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(ActivityInit.APP_NAME, String.format(
					"[%s] Fail to execute HTTP client.", getClass().getName()));
		}
		return html;
	}

}
