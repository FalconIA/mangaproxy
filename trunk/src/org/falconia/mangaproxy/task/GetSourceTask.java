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
import org.falconia.mangaproxy.ActivityBase;

public class GetSourceTask extends BasePluginTask<String, Void, String> {

	private static final DefaultHttpClient mhClient;

	static {
		mhClient = new DefaultHttpClient();
	}

	private final ResponseHandler<String> mhResponseHandler;
	private final ActivityBase mhActivity;

	public GetSourceTask(int siteId, ActivityBase activity) {
		super(siteId);
		this.mhActivity = activity;
		this.mhResponseHandler = new ResponseHandler<String>() {
			@Override
			public String handleResponse(final HttpResponse response)
					throws HttpResponseException, IOException {
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() >= 300)
					throw new HttpResponseException(statusLine.getStatusCode(),
							statusLine.getReasonPhrase());

				HttpEntity entity = response.getEntity();
				return entity == null ? null : EntityUtils.toString(entity,
						GetSourceTask.this.mhPlugin.getEncoding());
			}
		};
	}

	@Override
	protected String doInBackground(String... params) {
		return parseHtml(params[0]);
	}

	@Override
	protected void onPreExecute() {
		this.mhActivity.onPreDownload();
	}

	@Override
	protected void onPostExecute(String result) {
		this.mhActivity.onPostDownload(result);
	}

	private String parseHtml(String url) {
		String html = "";
		HttpGet request = new HttpGet(url);
		try {
			html = mhClient.execute(request, this.mhResponseHandler);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return html;
	}

}
