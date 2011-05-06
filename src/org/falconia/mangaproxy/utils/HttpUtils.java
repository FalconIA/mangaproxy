package org.falconia.mangaproxy.utils;

import java.net.MalformedURLException;
import java.net.URL;

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
}
