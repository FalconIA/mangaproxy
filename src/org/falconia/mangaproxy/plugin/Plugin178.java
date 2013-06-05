package org.falconia.mangaproxy.plugin;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.falconia.mangaproxy.App;
import org.falconia.mangaproxy.data.Chapter;
import org.falconia.mangaproxy.data.ChapterList;
import org.falconia.mangaproxy.data.Genre;
import org.falconia.mangaproxy.data.GenreList;
import org.falconia.mangaproxy.data.GenreSearch;
import org.falconia.mangaproxy.data.Manga;
import org.falconia.mangaproxy.data.MangaList;
import org.falconia.mangaproxy.utils.FormatUtils;
import org.falconia.mangaproxy.utils.Regex;
import org.json.JSONArray;
import org.json.JSONObject;

import android.text.TextUtils;

public class Plugin178 extends PluginBase {

	protected static final String GENRE_ALL_REAL_ID = "0-0-0-all-0-0-0";

	protected static final String URL_BASE_PAGE = "http://imgfast.dmzj.com/";

	protected static final String PAGE_POSTFIX = ".shtml";
	protected static final String GENRE_LIST_URL = "tags/category_search/" + GENRE_ALL_REAL_ID + "-1" + PAGE_POSTFIX;
	protected static final String GENRE_URL_CALLBACK = "search.renderResult";
	protected static final String GENRE_URL_FORMAT = "http://s.acg.178.com/mh/index.php?c=category&m=doSearch&status=%s&reader_group=%s&zone=%s&initial=%s&type=%s&_order=%s&p=%d&callback=" + GENRE_URL_CALLBACK;
	protected static final String SEARCH_URL_FORMAT = "http://s.acg.178.com/comicsum/search.php?s=%s";
	protected static final String MANGA_URL_PREFIX = "";

	public Plugin178(int siteId) {
		super(siteId);
	}

	@Override
	public String getName() {
		return "178";
	}

	@Override
	public String getDisplayname() {
		return "178(动漫之家)";
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
		return "http://www.dmzj.com/";
	}

	@Override
	public boolean hasSearchEngine() {
		return true;
	}

	@Override
	public boolean hasGenreList() {
		return true;
	}

	@Override
	public boolean usingImgRedirect() {
		return false;
	}

	@Override
	public boolean usingDynamicImgServer() {
		return false;
	}

	@Override
	public String getGenreListUrl() {
		return getUrlBase() + GENRE_LIST_URL;
	}

	@Override
	public String getGenreUrl(Genre genre, int page) {
		String genreId = GENRE_ALL_REAL_ID;
		if (!genre.isGenreAll()) {
			genreId = genre.genreId;
		}
		String[] ids = genreId.split("-");
		String order = "";
		if (ids[5].equals("1")) {
			order = "t";
		} else if (ids[6].equals("1")) {
			order = "h";
		}
		String url = String.format(GENRE_URL_FORMAT, ids[0], ids[1], ids[2], ids[3], ids[4], order, page);
		return url;
	}

	@Override
	public String getGenreAllUrl(int page) {
		return getGenreUrl(getGenreAll(), page);
	}

	@Override
	public String getSearchUrl(GenreSearch genreSearch, int page) {
		String search = genreSearch.search;
		try {
			search = URLEncoder.encode(genreSearch.search, getCharset());
		} catch (Exception e) {
		}
		String url = String.format(SEARCH_URL_FORMAT, search, page);
		logI(Get_URL_of_SearchMangaList, url);
		return url;
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
		return getMangaUrl(manga) + chapter.chapterId + PAGE_POSTFIX;
	}

	@Override
	protected String parseGenreName(String string) {
		string = super.parseGenreName(string);
		string = string.replaceAll("漫画$", "");
		return string;
	}

	protected String parseChapterName(String string, String manga) {
		string = parseName(string);
		if (Regex.isMatch("(?i)^VOL[_\\.]\\d+", string)) {
			string = string.replaceFirst("^v", "V");
		}
		string = string.replaceAll("卷完$", "卷");
		return string;
	}

	@Override
	protected int parseChapterType(String string) {
		if (Regex.isMatch("(?i)卷(（[^（）]+）)?$|^VOL[_\\.]\\d+|^高清版本\\d+", string)) {
			return Chapter.TYPE_ID_VOLUME;
		}
		if (Regex.isMatch("(?i)话$|^番外篇|^特别篇|^CH\\d+", string)) {
			return Chapter.TYPE_ID_CHAPTER;
		}
		return Chapter.TYPE_ID_UNKNOW;
	}

	protected String parseGenreId(String cate, String value) {
		String status = cate.equals("status") ? value : "0";
		String reader_group = cate.equals("reader_group") ? value : "0";
		String zone = cate.equals("zone") ? value : "0";
		String initial = cate.equals("initial") ? value : "all";
		String type = cate.equals("type") ? value : "0";
		String order_by_time = cate.equals("order_by") && value.equals("time") ? "1" : "0";
		String order_by_hot = cate.equals("order_by") && value.equals("hot") ? "1" : "0";

		return status + "-" + reader_group + "-" + zone + "-" + initial + "-" + type + "-" + order_by_time + "-" + order_by_hot;
	}

	protected String parseMangaIdByUrl(String string) {
		return Regex.matchString("/" + MANGA_URL_PREFIX + "([^/]+)/?$", string);
	}

	protected GregorianCalendar parseDate(String string) {
		GregorianCalendar calendar = null;
		calendar = parseDateTime(string, "(\\d+)-(\\d+)-(\\d+){'YY','M','D'}");
		return calendar;
	}

	@Override
	public Genre getGenreAll() {
		return new Genre(Genre.GENRE_ALL_ID, App.UI_GENRE_ALL_TEXT_ZH, getSiteId());
	}

	@Override
	public GenreList getGenreList(String source, String url) {
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
			ArrayList<ArrayList<String>>[] groupedMatches = (ArrayList<ArrayList<String>>[]) new ArrayList[8];

			pattern = "(?is)<div class=\"anim_search_list\">(.+?)</div>\\s*<div class=\"anim_search_ending\">";
			groups = Regex.match(pattern, source);
			logD(Catched_sections, groups.size() - 1);

			// Section 1
			section = "search_list_m";
			pattern = "(?is)<a[^<>]* onclick=\"changeType\\('([^'\"]+)',\\s*(\\d+|'[^'\"]+')\\)\\s*;\\s*return false;\\s*\"[^<>]*>(.+?)</a>";
			matches = Regex.matchAll(pattern, groups.get(1));
			logD(Catched_count_in_section, matches.size(), section);

			// Sort by category
			for (ArrayList<String> match : matches) {
				match.set(2, match.get(2).replaceAll("^'|'$", ""));

				String cate = match.get(1);
				int cateGroupId = 7;
				if (cate.equals("status")) {
					cateGroupId = 2;
				} else if (cate.equals("reader_group")) {
					cateGroupId = 3;
				} else if (cate.equals("zone")) {
					cateGroupId = 6;
				} else if (cate.equals("initial")) {
					cateGroupId = 5;
				} else if (cate.equals("type")) {
					cateGroupId = 4;
				} else if (cate.equals("order_by")) {
					if (match.get(2).equals("time")) {
						cateGroupId = 0;
						match.set(3, String.format("最新更新(%s)", match.get(3)));
					} else if (match.get(2).equals("hot")) {
						cateGroupId = 1;
						match.set(3, String.format("漫画排行(%s)", match.get(3)));
					} else {
						cateGroupId = 1;
					}
				}
				if (groupedMatches[cateGroupId] == null) {
					groupedMatches[cateGroupId] = new ArrayList<ArrayList<String>>();
					// Except first one (GenreAll)
					if (cateGroupId != 0) {
						continue;
					}
				}
				groupedMatches[cateGroupId].add(match);
			}

			// Fill list
			for (ArrayList<ArrayList<String>> groupMatches : groupedMatches) {
				if (groupMatches == null || groupMatches.size() == 0) {
					continue;
				}
				for (ArrayList<String> match : groupMatches) {
					genreId = parseGenreId(match.get(1), match.get(2));
					list.add(genreId, parseGenreName(match.get(3)));
				}
			}

			time = System.currentTimeMillis() - time;
			logD(Process_Time_GenreList, time);

			logV(list.toString());

		} catch (Exception e) {
			e.printStackTrace();
			logE(Fail_to_process, "GenreList", url);
		}

		return list;
	}

	@Override
	public MangaList getMangaList(String source, String url, Genre genre) {
		MangaList list = new MangaList(getSiteId());

		logI(Get_MangaList, genre.genreId);
		logD(Get_Source_Size_MangaList, FormatUtils.getFileSize(source, getCharset()));

		if (TextUtils.isEmpty(source)) {
			logE(Source_is_empty);
			return list;
		}

		try {
			long time = System.currentTimeMillis();

			String pattern;
			ArrayList<String> groups;
			JSONObject json;
			JSONArray mangaObjs;

			logD(Process_MangaList, genre.genreId);

			pattern = "(?is)" + GENRE_URL_CALLBACK + "\\((.+)?\\);";
			groups = Regex.match(pattern, source);
			logD(Catched_sections, groups.size() - 1);

			json = new JSONObject(groups.get(1));
			mangaObjs = json.getJSONArray("result");

			for (int i = 0; i < mangaObjs.length(); i++) {
				JSONObject mangaObj = mangaObjs.getJSONObject(i);
				Manga manga = new Manga(parseMangaIdByUrl(mangaObj.getString("comic_url")), mangaObj.getString("name"), null, getSiteId());
				manga.details = mangaObj.getString("type");
				manga.updatedAt = parseDate(mangaObj.getString("last_update_date"));
				manga.author = mangaObj.getString("author");
				manga.chapterDisplayname = parseName(mangaObj.getString("last_chapter"));
				manga.isCompleted = parseIsCompleted(mangaObj.getString("status"));
				manga.setDetailsTemplate("%author%\n%details%\n%chapterDisplayname%\n%updatedAt%");
				list.add(manga);
				// logV(manga.toLongString());
			}

			list.pageIndexMax = parseInt(json.getString("page_count"));

			time = System.currentTimeMillis() - time;
			logD(Process_Time_MangaList, time);

			logV(list.toString());

		} catch (Exception e) {
			e.printStackTrace();
			logE(Fail_to_process, "MangaList", url);
		}

		return list;
	}

	@Override
	public MangaList getAllMangaList(String source, String url) {
		return getMangaList(source, url, getGenreAll());
	}

	@Override
	public MangaList getSearchMangaList(String source, String url) {
		MangaList list = new MangaList(getSiteId());

		logI(Get_SearchMangaList);
		logD(Get_Source_Size_SearchMangaList, FormatUtils.getFileSize(source, getCharset()));

		if (TextUtils.isEmpty(source)) {
			logE(Source_is_empty);
			return list;
		}

		try {
			long time = System.currentTimeMillis();

			String pattern;
			ArrayList<String> groups;
			ArrayList<ArrayList<String>> matches;
			JSONArray mangaObjs;

			pattern = "(?is)^var g_search_data\\s*=\\s*(\\[.+\\]);$";
			groups = Regex.match(pattern, source);
			logD(Catched_sections, groups.size() - 1);

			mangaObjs = new JSONArray(groups.get(1));

			for (int i = 0; i < mangaObjs.length(); i++) {
				JSONObject mangaObj = mangaObjs.getJSONObject(i);
				Manga manga = new Manga(parseMangaIdByUrl(mangaObj.getString("comic_url")), mangaObj.getString("comic_name"), null, getSiteId());
				manga.details = mangaObj.getString("types");
				manga.author = mangaObj.getString("comic_author");
				manga.chapterDisplayname = parseName(mangaObj.getString("last_update_chapter_name"));
				manga.isCompleted = parseIsCompleted(mangaObj.getString("status"));
				manga.setDetailsTemplate("%author%\n%details%\n%chapterDisplayname%");
				list.add(manga);
				// logV(manga.toLongString());
			}

			time = System.currentTimeMillis() - time;
			logD(Process_Time_MangaList, time);

			logV(list.toString());

		} catch (Exception e) {
			e.printStackTrace();
			logE(Fail_to_process, "SearchMangaList", url);
		}

		return list;
	}

	@Override
	public ChapterList getChapterList(String source, String url, Manga manga) {
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

			int n;
			String pattern, section;
			ArrayList<String> groups;
			ArrayList<ArrayList<String>> matches;

			pattern = "(?is)((?:<div class=\"cartoon_online_border\"[^<>]*>\\s*<ul>.+</ul><div class=\"clearfix\"></div></div>\\s*)+).*?<div class=\"anim-main_list\">.*?<th>状态：</th>\\s*<td><a [^<>]+>([^<>]+)</a></td>.*?<th>最新收录：</th>\\s*<td\\s*><a [^<>]+>(.+)</a>[^<>]*<br /><span[^<>]*>([0-9-]+)</span></td>";
			groups = Regex.match(pattern, source);
			logD(Catched_sections, groups.size() - 1);

			n = 2;
			section = "IsCompleted";
			manga.isCompleted = parseIsCompleted(groups.get(n));
			logV(Catched_in_section, groups.get(n), n, section, manga.isCompleted);

			n = 4;
			section = "UpdatedAt";
			manga.updatedAt = parseDate(groups.get(n));
			logV(Catched_in_section, groups.get(n), n, section, manga.updatedAt.getTime());

			n = 1;
			section = "ul";
			// logV(groups.get(3));
			pattern = "(?is)<li[^<>]*><a [^<>]*href=\"/[^\"/]+/(\\d+)\\.shtml\"[^<>]*>(.+?)</a></li>";
			matches = Regex.matchAll(pattern, groups.get(n));
			logD(Catched_count_in_section, matches.size(), section);

			for (ArrayList<String> groups2 : matches) {
				Chapter chapter = new Chapter(parseId(groups2.get(1)), parseChapterName(groups2.get(2),
						manga.displayname), manga);
				chapter.typeId = parseChapterType(chapter.displayname);
				list.add(chapter);
				// logV(chapter.toLongString());
			}
			list.reverse();

			time = System.currentTimeMillis() - time;
			logD(Process_Time_ChapterList, time);

			logV(list.toString());

		} catch (Exception e) {
			e.printStackTrace();
			logE(Fail_to_process, "ChapterList", url);
		}

		return list;
	}

	@Override
	public String[] getChapterPages(String source, String url, Chapter chapter) {
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
			ArrayList<ArrayList<String>> matches;

			pattern = "(?is)<script .*?>.*?\\s+(eval.+?)\\s*;\\s*var .*?</script>";
			groups = Regex.match(pattern, source);
			logD(Catched_sections, groups.size() - 1);

			String json = decodePackedJs(groups.get(1));
			json = json.replace("\\\\/", "/");

			time = System.currentTimeMillis() - time;
			logD(Process_Time_ChapterPages, time);

			pattern = "(?is)\"([^\"]+)\"";
			matches = Regex.matchAll(pattern, json);
			logD(Catched_count_in_section, matches.size(), "json");

			if (matches.size() > 0) {
				pageUrls = new String[matches.size()];
				for (int i = 0; i < matches.size(); i++) {
					ArrayList<String> match = matches.get(i);
					pageUrls[i] = URL_BASE_PAGE + match.get(1);
				}
			}

			// logV(chapter.toString());

		} catch (Exception e) {
			e.printStackTrace();
			logE(Fail_to_process, "ChapterPages", url);
		}

		return pageUrls;
	}
}
