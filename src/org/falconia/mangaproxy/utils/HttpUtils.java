package org.falconia.mangaproxy.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.falconia.mangaproxy.AppUtils;

import android.text.TextUtils;

public final class HttpUtils {

	public static final String TAG = "AppCache";

	public static final String CHARSET_GBK = "GBK";
	public static final String CHARSET_UTF8 = "UTF-8";

	public static String joinUrl(String base, String spec) {
		if (TextUtils.isEmpty(base)) {
			return null;
		}
		try {
			URL url;
			if (TextUtils.isEmpty(spec)) {
				url = new URL(base);
			} else if (spec.matches("^http://.+")) {
				url = new URL(spec);
			} else {
				url = new URL(new URL(base), spec);
			}
			return url.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			AppUtils.logE(TAG, "Invalid URL.");
		}
		return null;
	}

	public static String urlencode(String url) {
		try {

			Pattern p = Pattern.compile("^(?:https?|ftp)://|[^a-zA-Z0-9=?&/~`!@#$%^()+.*_-]+");
			Matcher m = p.matcher(url);

			String urlNew = "";
			int end = 0;

			m.find();
			while (m.find()) {
				urlNew += url.substring(end, m.start());
				urlNew += URLEncoder.encode(m.group(), CHARSET_UTF8);
				end = m.end();
			}
			urlNew += url.substring(end, url.length());
			url = urlNew;
			AppUtils.logV(TAG, "New URL: " + urlNew);

		} catch (Exception e) {
		}
		return url;
	}
}