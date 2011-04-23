package org.falconia.mangaproxy.plugin;

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
import org.falconia.mangaproxy.data.MangaList;
import org.falconia.mangaproxy.helper.HttpHelper;
import org.falconia.mangaproxy.helper.Regex;

public abstract class APlugin implements IPlugin {
	protected static final String CHARSET_GBK = HttpHelper.CHARSET_GBK;
	protected static final String CHARSET_UTF8 = HttpHelper.CHARSET_UTF8;

	protected static final String DEFAULT_MANGA_URL_PREFIX = "comic/";
	protected static final String DEFAULT_MANGA_URL_POSTFIX = "/";

	private static final DefaultHttpClient client;

	static {
		client = new DefaultHttpClient();
	}

	protected int miSiteId;

	private final HttpHelper mhHttpHelper;

	private final ResponseHandler<String> mhResponseHandler;

	public APlugin(int siteId) {
		this.miSiteId = siteId;
		this.mhHttpHelper = new HttpHelper(getEncoding());

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
						getEncoding());
			}
		};
	}

	@Override
	public int getSiteId() {
		return this.miSiteId;
	}

	@Override
	public String getMangaUrl(int mangaId) {
		return getUrlBase() + getMangaUrlPrefix() + mangaId
				+ getMangaUrlPostfix();
	}

	protected String getGenreUrl(String genreId) {
		return getUrlBase() + genreId.replace('-', '/') + "/";
	}

	@Override
	public MangaList getMangaList(int genreId) {
		return getMangaList(genreId, 1);
	}

	@Override
	public MangaList getAllMangaList() {
		return getAllMangaList(1);
	}

	protected String checkId(String string) {
		string = Regex.match("^[\\s/]*(.+?)[\\s/]*$", string).get(1);
		return string.trim().replace('/', '-');
	}

	protected String checkName(String string) {
		return string.trim();
	}

	protected String checkGenreName(String string) {
		return checkName(string);
	}

	@Deprecated
	protected String parseHtml2(String url) {
		return this.mhHttpHelper.performGet(url);
	}

	protected String parseHtml(String url) {
		String html = "";
		HttpGet request = new HttpGet(url);
		try {
			html = client.execute(request, this.mhResponseHandler);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return html;
	}

	protected int bitsToInt(String string) {
		int result = 0;
		byte[] bits = string.getBytes();
		for (int n = bits.length - 1; n >= 0; n--)
			result = (result << 8) + bits[n];
		return result;
	}

	protected String intToBits(int i) {
		byte[] bits = new byte[4];
		for (int n = 0; n < 4; n++) {
			bits[n] = (byte) (i % 256);
			i /= 256;
		}
		return new String(bits);
	}

}
