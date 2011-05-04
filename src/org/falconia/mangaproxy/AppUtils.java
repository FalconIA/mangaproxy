package org.falconia.mangaproxy;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public final class AppUtils {

	public static void popupMessage(Context context, int resId) {
		Toast.makeText(context.getApplicationContext(), resId,
				Toast.LENGTH_SHORT).show();
	}

	public static void popupMessage(Context context, CharSequence text) {
		Toast.makeText(context.getApplicationContext(), text,
				Toast.LENGTH_SHORT).show();
	}

	public static void log(int priority, String tag, String msg) {
		Log.println(priority, AppConst.APP_NAME,
				String.format("[%s] %s", tag, msg));
	}

	public static void log(int priority, ITag tag, String msg) {
		log(priority, tag.getTag(), msg);
	}

	public static void logV(ITag tag, String msg) {
		// DEBUG = 2
		log(Log.VERBOSE, tag, msg);
	}

	public static void logD(ITag tag, String msg) {
		// DEBUG = 3
		log(Log.DEBUG, tag, msg);
	}

	public static void logI(ITag tag, String msg) {
		// INFO = 4
		log(Log.INFO, tag, msg);
	}

	public static void logW(ITag tag, String msg) {
		// WARN = 5
		log(Log.WARN, tag, msg);
	}

	public static void logE(ITag tag, String msg) {
		// ERROR = 6
		log(Log.ERROR, tag, msg);
	}

}
