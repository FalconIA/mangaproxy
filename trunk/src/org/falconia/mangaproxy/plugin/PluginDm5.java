package org.falconia.mangaproxy.plugin;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.falconia.mangaproxy.App;
import org.falconia.mangaproxy.data.Chapter;
import org.falconia.mangaproxy.data.ChapterList;
import org.falconia.mangaproxy.data.Genre;
import org.falconia.mangaproxy.data.GenreList;
import org.falconia.mangaproxy.data.Manga;
import org.falconia.mangaproxy.data.MangaList;
import org.falconia.mangaproxy.utils.FormatUtils;
import org.falconia.mangaproxy.utils.Regex;

import android.text.TextUtils;

public class PluginDm5 extends PluginBase {
	protected static final String GENRE_ALL_ID = "new";

	protected static final String MANGA_URL_PREFIX = "manhua-";

	protected static final String PAGE_REDIRECT_URL_PREFIX = "showimage.ashx";

	public PluginDm5(int siteId) {
		super(siteId);
	}

	@Override
	public String getName() {
		return "DM5";
	}

	@Override
	public String getDisplayname() {
		return "动漫屋";
	}

	@Override
	public String getCharset() {
		return CHARSET_UTF8;
	}

	@Override
	public TimeZone getTimeZone() {
		return TimeZone.getTimeZone("GMT+08:00");
	}

	@Override
	public String getUrlBase() {
		return "http://www.dm5.com/";
	}

	@Override
	public boolean hasSearchEngine() {
		return false;
	}

	@Override
	public boolean hasGenreList() {
		return true;
	}

	@Override
	public boolean usingImgRedirect() {
		return true;
	}

	@Override
	public boolean usingDynamicImgServer() {
		return false;
	}

	@Override
	public String getGenreListUrl() {
		String url = getUrlBase() + "manhua-new/";
		logI(Get_URL_of_GenreList, url);
		return url;
	}

	@Override
	public String getGenreUrl(Genre genre, int page) {
		String url = getUrlBase();
		String id = genre.isGenreAll() ? GENRE_ALL_ID : genre.genreId;
		if (page == 1) {
			url = String.format("%smanhua-%s/", url, id);
		} else {
			url = String.format("%smanhua-%s-p%d/", url, id, page);
		}
		if (genre.isGenreAll()) {
			logI(Get_URL_of_AllMangaList, url);
		} else {
			logI(Get_URL_of_MangaList, genre.genreId, url);
		}
		return url;
	}

	@Override
	public String getGenreUrl(Genre genre) {
		return getGenreUrl(genre, 1);
	}

	@Override
	public String getGenreAllUrl(int page) {
		return getGenreUrl(getGenreAll(), page);
	}

	@Override
	public String getMangaUrlPrefix() {
		return MANGA_URL_PREFIX;
	}

	@Override
	public String getMangaUrlPostfix() {
		return DEFAULT_MANGA_URL_POSTFIX;
	}

	@Override
	public String getChapterUrl(Chapter chapter, Manga manga) {
		String url = getUrlBase() + String.format("m%s/", chapter.chapterId);
		logI(Get_URL_of_Chapter, chapter.chapterId, url);
		return url;
	}

	protected GregorianCalendar parseDate(String string) {
		GregorianCalendar calendar = null;
		calendar = parseDateTime(string, "(\\d+)[年-](\\d+)[月-](\\d+)日?{'YY','M','D'}");
		return calendar;
	}

	protected GregorianCalendar parseDateTime(String string) {
		GregorianCalendar calendar = null;
		calendar = parseDateTime(string,
				"(\\d+)-(\\d+)-(\\d+)\\s+(\\d+)\\:(\\d+)\\:(\\d+){'YY','M','D','h','m','s'}");
		return calendar;
	}

	protected String parseAuthorName(String string) {
		string = string.replaceAll("(?si)</?a[^<>]*>", "");
		return parseName(string);
	}

	protected String parseChapterName(String string, String manga) {
		if (string.startsWith(manga)) {
			string = string.substring(manga.length());
		}
		return parseName(string);
	}

	@Override
	protected int parseChapterType(String string) {
		if (Regex.isMatch("(?i)卷$|^VOL\\.", string)) {
			return Chapter.TYPE_ID_VOLUME;
		}
		if (Regex.isMatch("(?i)话$|回$|^CH\\.", string)) {
			return Chapter.TYPE_ID_CHAPTER;
		}
		return Chapter.TYPE_ID_UNKNOW;
	}

	@Override
	public Genre getGenreAll() {
		return new Genre(Genre.GENRE_ALL_ID, String.format("%s (今日漫画)", App.UI_GENRE_ALL_TEXT),
				getSiteId());
	}

	@Override
	public GenreList getGenreList(String source) {
		GenreList list = new GenreList(getSiteId());

		logI(Get_GenreList);
		logD(Get_Source_Size_GenreList, FormatUtils.getFileSize(source, getCharset()));

		if (TextUtils.isEmpty(source)) {
			logE(Source_is_empty);
			return list;
		}

		try {
			long time = System.currentTimeMillis();

			String genreId, section;
			String pattern;
			ArrayList<String> groups;
			ArrayList<ArrayList<String>> matches;

			pattern = "(?is)<ul class=\"dm_nav\">(.+?)</ul>.+?<div id=\"nav_fl2\">(.+?</div>).+?<div class=\"nav_zm[^<>]+>(.+?)</div>";
			groups = Regex.match(pattern, source);
			logD(Catched_sections, groups.size() - 1);

			// Section 1

			// Section 2
			section = "nav_fl2";
			pattern = "(?is)<a href=\"/manhua-([^\"]+)/\" title=\"([^\"]+)\"\\s*>(.+?)</a>";
			matches = Regex.matchAll(pattern, groups.get(2));
			logD(Catched_count_in_section, matches.size(), section);

			for (ArrayList<String> match : matches) {
				genreId = parseGenreId(match.get(1));
				list.add(genreId, parseGenreName(match.get(3)));
			}

			// Section 3
			section = "nav_zm";
			pattern = "(?is)<a href=\"/manhua-([^\"]+)/\">(.+?)</a>";
			matches = Regex.matchAll(pattern, groups.get(3));
			logD(Catched_count_in_section, matches.size(), section);

			for (ArrayList<String> match : matches) {
				genreId = parseGenreId(match.get(1));
				list.add(genreId, parseGenreName(match.get(2)));
			}

			time = System.currentTimeMillis() - time;
			logD(Process_Time_GenreList, time);

			logV(list.toString());

		} catch (Exception e) {
			e.printStackTrace();
			logE(Fail_to_process, "GenreList");
		}

		return list;
	}

	@Override
	public MangaList getMangaList(String source, Genre genre) {
		logI(Get_MangaList, genre.genreId);
		logD(Get_Source_Size_MangaList, FormatUtils.getFileSize(source, getCharset()));

		return getMangaListBase(source, genre);
	}

	@Override
	public MangaList getAllMangaList(String source) {
		logI(Get_AllMangaList);
		logD(Get_Source_Size_AllMangaList, FormatUtils.getFileSize(source, getCharset()));

		return getMangaListBase(source, getGenreAll());
	}

	private MangaList getMangaListBase(String source, Genre genre) {
		MangaList list = new MangaList(getSiteId());

		if (TextUtils.isEmpty(source)) {
			logE(Source_is_empty);
			return list;
		}

		try {
			long time = System.currentTimeMillis();

			String pattern;
			ArrayList<String> groups;
			ArrayList<ArrayList<String>> matches;

			pattern = "(?is)<div class=\"innr3\">(.+?)</div>.+?当前第\\s*(?:<[^<>]+>)?\\d+/(\\d+)(?:</[^<>]+>)?\\s*页";
			groups = Regex.match(pattern, source);
			logD(Catched_sections, groups.size() - 1);

			// Section 1
			if (genre.genreId.equalsIgnoreCase("updated")) {
				pattern = "(?is)<li [^<>]+>\\s*<a href=\"/manhua-([^\"]+)/\" title=\"([^\"]+?)\"[^<>]*>.+?<strong>.+?</strong></a>.+?<br />漫画人气：(\\d+).+?\\[\\s*<a [^<>]*title=\"[^\"]+\"[^<>]*>(.+?)</a>：<a [^<>]+>(.+?)</a>\\s*\\]\\s*</li>";
				matches = Regex.matchAll(pattern, groups.get(1));
				logD(Catched_count_in_section, matches.size(), "Mangas");

				for (ArrayList<String> match : matches) {
					Manga manga = new Manga(parseId(match.get(1)), parseName(match.get(2)), null,
							getSiteId());
					manga.details = "HIT: " + match.get(3);
					manga.chapterDisplayname = parseChapterName(match.get(4), manga.displayname);
					manga.latestChapterDisplayname = manga.chapterDisplayname;
					manga.author = parseAuthorName(match.get(5));
					manga.setDetailsTemplate("%author%\n%chapterDisplayname%, %details%");
					list.add(manga, true);
					// logV(manga.toLongString());
				}
			} else {
				pattern = "(?is)<li [^<>]+>\\s*<a href=\"/manhua-([^\"]+)/\" title=\"([^\"]+?)\"[^<>]*>.+?<strong>.+?</strong></a>.+?<br />漫画家：<a [^<>]+>(.+?)</a>.+?\\[\\s*([\\d年月日]+)：<a [^<>]*title=\"[^\"]+\"[^<>]*>(.+?)</a>\\s*\\]\\s*</li>";
				matches = Regex.matchAll(pattern, groups.get(1));
				logD(Catched_count_in_section, matches.size(), "Mangas");

				for (ArrayList<String> match : matches) {
					Manga manga = new Manga(parseId(match.get(1)), parseName(match.get(2)), null,
							getSiteId());
					manga.author = parseName(match.get(3));
					manga.updatedAt = parseDate(match.get(4));
					manga.chapterDisplayname = parseName(match.get(5));
					manga.setDetailsTemplate("%author%\n%chapterDisplayname%, %updatedAt%");
					list.add(manga, true);
					// logV(manga.toLongString());
				}
			}

			// Section 2
			list.pageIndexMax = parseInt(groups.get(2));
			logV(Catched_in_section, groups.get(2), 0, "PageIndexMax", list.pageIndexMax);

			time = System.currentTimeMillis() - time;
			logD(Process_Time_MangaList, time);

			logV(list.toString());

		} catch (Exception e) {
			e.printStackTrace();
			logE(Fail_to_process, "MangaList");
		}

		return list;
	}

	@Override
	public ChapterList getChapterList(String source, Manga manga) {
		ChapterList list = new ChapterList(manga);

		logI(Get_ChapterList, manga.mangaId);
		logD(Get_Source_Size_ChapterList, FormatUtils.getFileSize(source, getCharset()));

		if (TextUtils.isEmpty(source)) {
			logE(Source_is_empty);
			return list;
		}
		// logV(source);
		logV(manga.toLongString());

		try {
			long time = System.currentTimeMillis();

			String pattern, section;
			ArrayList<String> groups;
			ArrayList<ArrayList<String>> matches;

			pattern = "(?is)更新时间：([\\d-]+\\s+[\\d:]+)<.+?漫画状态：([^<]+)<.+?<ul [^<>]*id=\"cbc_1\">(.+?)</ul>";
			groups = Regex.match(pattern, source);
			logD(Catched_sections, groups.size() - 1);

			section = "UpdatedAt";
			manga.updatedAt = parseDateTime(groups.get(1));
			logV(Catched_in_section, groups.get(1), 3, section, manga.updatedAt.getTime());

			section = "IsCompleted";
			manga.isCompleted = parseIsCompleted(groups.get(2));
			logV(Catched_in_section, groups.get(2), 2, section, manga.isCompleted);

			section = "ul";
			// logV(groups.get(3));
			pattern = "(?is)<li><a [^<>]*href=\"/m(\\d+)/\"[^<>]*>(.+?)</a>.+?</li>";
			matches = Regex.matchAll(pattern, groups.get(3));
			logD(Catched_count_in_section, matches.size(), section);

			for (ArrayList<String> groups2 : matches) {
				Chapter chapter = new Chapter(parseId(groups2.get(1)), parseChapterName(
						groups2.get(2), manga.displayname), manga);
				chapter.typeId = parseChapterType(chapter.displayname);
				list.add(chapter);
				// logV(chapter.toLongString());
			}

			time = System.currentTimeMillis() - time;
			logD(Process_Time_ChapterList, time);

			logV(list.toString());

		} catch (Exception e) {
			e.printStackTrace();
			logE(Fail_to_process, "ChapterList");
		}

		return list;
	}

	@Override
	public String[] getChapterPages(String source, Chapter chapter) {
		String[] pageUrls = null;

		logI(Get_Chapter, chapter.chapterId);
		logD(Get_Source_Size_Chapter, FormatUtils.getFileSize(source, getCharset()));

		if (TextUtils.isEmpty(source)) {
			logE(Source_is_empty);
			return pageUrls;
		}

		try {
			long time = System.currentTimeMillis();

			String pattern;
			ArrayList<String> groups;

			pattern = "(?is)var DM5_CID=(\\d+);\\s+var DM5_IMAGE_COUNT=(\\d+);";
			groups = Regex.match(pattern, source);
			logD(Catched_sections, groups.size() - 1);

			// Section 1
			int cid = parseInt(groups.get(1));
			logV(Catched_in_section, groups.get(1), 1, "DM5_CID", cid);

			// Section 2
			int count = parseInt(groups.get(2));
			logV(Catched_in_section, groups.get(2), 1, "DM5_IMAGE_COUNT", count);

			pageUrls = new String[count];
			for (int i = 0; i < count; i++) {
				pageUrls[i] = String.format("%s%s?cid=%d&page=%d", getUrlBase(),
						PAGE_REDIRECT_URL_PREFIX, cid, i + 1);
			}

			time = System.currentTimeMillis() - time;
			logD(Process_Time_ChapterPages, time);

			// logV(chapter.toString());

		} catch (Exception e) {
			e.printStackTrace();
			logE(Fail_to_process, "ChapterPages");
		}

		return pageUrls;
	}

	@Override
	public String getPageRedirectUrl(String source) {
		String url = null;
		try {
			url = source.split(",")[0];
		} catch (Exception e) {
			e.printStackTrace();
			logE(Fail_to_process, "RedirectPageUrl");
		}
		return url;
	}

}
