package org.falconia.mangaproxy.helper;

import org.apache.http.impl.client.DefaultHttpClient;

public final class HttpHelper {
	public static final String CHARSET_GBK = "GBK";
	public static final String CHARSET_UTF8 = "UTF-8";

	@Deprecated
	private static final DefaultHttpClient mhClient;

	static {
		mhClient = new DefaultHttpClient();
	}

}
