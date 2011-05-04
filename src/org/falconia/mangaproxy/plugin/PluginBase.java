package org.falconia.mangaproxy.plugin;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.falconia.mangaproxy.AppConst;
import org.falconia.mangaproxy.ITag;
import org.falconia.mangaproxy.data.Genre;
import org.falconia.mangaproxy.data.Manga;
import org.falconia.mangaproxy.helper.FormatHelper;
import org.falconia.mangaproxy.helper.HttpHelper;
import org.falconia.mangaproxy.helper.MathHelper;
import org.falconia.mangaproxy.helper.Regex;

import android.text.TextUtils;
import android.util.Log;

public abstract class PluginBase implements ITag, IPlugin {

	public static final String SOURCE_SEPERATOR_STRING = "\n##########SOURCE_SEPERATOR_STRING##########\n";


	protected static final String Fail_to_process = "Fail to process data.";
	protected static final String Fail_to_parse_Int = "Fail to parse int: '%s'.";
	protected static final String Fail_to_parse_DateTime = "Fail to parse Date/Time: '%s'.";

	protected static final String Get_URL_of_GenreList = "Get URL of GenreList: %s";
	protected static final String Get_URL_of_MangaList = "Get URL of MangaList(GenreID:%s): %s";
	protected static final String Get_URL_of_AllMangaList = "Get URL of MangaList(All mangas): %s";
	protected static final String Get_URL_of_ChapterList = "Get URL of ChapterList(MangaID:%s): %s";
	protected static final String Get_URL_of_Chapter = "Get URL of Chapter(ChapterID:%s): %s";
	protected static final String Get_URL_of_DynamicImgServerSource = "Get URL of DynamicImgServerSource: %s";

	protected static final String Get_GenreList = "Get GenreList.";
	protected static final String Get_MangaList = "Get MangaList(GenreID:%s).";
	protected static final String Get_AllMangaList = "Get MangaList(All mangas).";
	protected static final String Get_ChapterList = "Get ChapterList(MangaID:%s).";
	protected static final String Get_Chapter = "Get Chapter(ChapterID:%s).";

	protected static final String Get_Source_Size_GenreList = "Get GenreList data of %s.";
	protected static final String Get_Source_Size_MangaList = "Get MangaList data of %s.";
	protected static final String Get_Source_Size_AllMangaList = "Get MangaList(All mangas) data of %s.";
	protected static final String Get_Source_Size_ChapterList = "Get ChapterList data of %s.";
	protected static final String Get_Source_Size_Chapter = "Get Chapter data of %s.";

	protected static final String Process_MangaList = "Process MangaList(GenreID:%s).";
	protected static final String Process_MangaList_New = "Process MangaList(New|GenreID:%s).";

	protected static final String Catched_sections = "Catched %d section(s) of list.";
	protected static final String Catched_count_in_section = "Catched %d in section '%s'.";
	protected static final String Catched_total_page = "Catched total page of %d.";
	protected static final String Catched_in_section = "Catched '%s' in section %d, { %s:%s }.";

	protected static final String Process_Time_MangaList = "Process MangaList in %dms.";
	protected static final String Process_Time_AllMangaList = "Process MangaList(All mangas) in %dms.";
	protected static final String Process_Time_ChapterList = "Process ChapterList in %dms.";

	protected static final String Source_is_empty = "Source is empty.";


	protected static final String CHARSET_GBK = HttpHelper.CHARSET_GBK;
	protected static final String CHARSET_UTF8 = HttpHelper.CHARSET_UTF8;

	protected static final String DEFAULT_MANGA_URL_PREFIX = "comic/";
	protected static final String DEFAULT_MANGA_URL_POSTFIX = "/";


	protected int miSiteId;

	public PluginBase(int siteId) {
		this.miSiteId = siteId;
	}

	@Override
	public int getSiteId() {
		return this.miSiteId;
	}

	@Override
	public String getGenreUrl(Genre genre) {
		return getUrlBase() + genre.genreId.replace('-', '/') + "/";
	}

	@Override
	public String getGenreAllUrl() {
		return getGenreAllUrl(1);
	}

	@Override
	public String getMangaUrl(Manga manga) {
		String url = getUrlBase() + getMangaUrlPrefix() + manga.mangaId
				+ getMangaUrlPostfix();
		logI(Get_URL_of_ChapterList, manga.mangaId, url);
		return url;
	}

	@Override
	public String getDynamicImgServerSourceUrl(String source) {
		if (isDynamicImgServer())
			throw new RuntimeException("The method should to be overrode.");
		else
			throw new RuntimeException(
					"The site is unsupported of Dynamic Img Server.");
	}

	protected int parseInt(String string) {
		if (TextUtils.isEmpty(string) || TextUtils.isEmpty(string.trim()))
			return 0;
		try {
			return Integer.parseInt(string.trim());
		} catch (Exception e) {
			e.printStackTrace();
			logE(Fail_to_parse_Int, string);
			return 0;
		}
	}

	protected GregorianCalendar parseDateTime(String string, String format) {
		GregorianCalendar cal = new GregorianCalendar(getTimeZone());
		cal.clear();

		try {
			HashMap<String, String> groups = Regex.matchGroup(format, string);

			if (groups.containsKey("YY"))
				cal.set(Calendar.YEAR, parseInt(groups.get("YY")));
			if (groups.containsKey("Y"))
				cal.set(Calendar.YEAR,
						FormatHelper.year2to4(parseInt(groups.get("Y"))));
			if (groups.containsKey("M"))
				cal.set(Calendar.MONTH,
						parseInt(Calendar.JANUARY + groups.get("M")) - 1);
			if (groups.containsKey("D"))
				cal.set(Calendar.DAY_OF_MONTH, parseInt(groups.get("D")));
			if (groups.containsKey("h"))
				cal.set(Calendar.HOUR_OF_DAY, parseInt(groups.get("h")));
			if (groups.containsKey("m"))
				cal.set(Calendar.MINUTE, parseInt(groups.get("m")));
			if (groups.containsKey("s"))
				cal.set(Calendar.SECOND, parseInt(groups.get("s")));

		} catch (Exception e) {
			e.printStackTrace();
			logE(Fail_to_parse_DateTime, string);
		}
		return cal;
	}

	protected String parseId(String string) {
		return string.trim();
	}

	protected String parseGenreId(String string) {
		string = Regex.match("^[\\s/]*(.+?)[\\s/]*$", string).get(1);
		return string.trim().replace('/', '-');
	}

	protected String parseName(String string) {
		return string.trim();
	}

	protected String parseGenreName(String string) {
		return parseName(string);
	}

	protected boolean parseIsCompleted(String string) {
		if (string.matches("\\s*经典\\s*"))
			return true;
		return string.indexOf("完") >= 0;
	}

	protected abstract int parseChapterType(String string);

	protected int bitsToInt(String string) {
		int result = 0;
		byte[] bits = string.getBytes();
		for (int n = bits.length - 1; n >= 0; n--)
			result = (result << 8) + bits[n];
		return result;
	}

	protected String intToBits(int i) {
		int digit = (int) MathHelper.logBase(i, 256) + 1;
		byte[] bits = new byte[digit];
		for (int n = 0; n < digit; n++) {
			bits[n] = (byte) (i % 256);
			i /= 256;
		}
		return new String(bits);
	}

	@Override
	public String getTag() {
		return getClass().getSimpleName();
	}

	private void log(int priority, String msg) {
		Log.println(priority, AppConst.APP_NAME + " Plugin",
				String.format("[%s] %s", getTag(), msg));
	}

	protected void logV(String format, Object... args) {
		log(Log.VERBOSE, String.format(format, args));
	}

	protected void logD(String format, Object... args) {
		log(Log.DEBUG, String.format(format, args));
	}

	protected void logI(String format, Object... args) {
		log(Log.INFO, String.format(format, args));
	}

	protected void logW(String format, Object... args) {
		log(Log.WARN, String.format(format, args));
	}

	protected void logE(String format, Object... args) {
		log(Log.ERROR, String.format(format, args));
	}

}
