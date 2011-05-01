package org.falconia.mangaproxy.plugin;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.falconia.mangaproxy.data.Chapter;
import org.falconia.mangaproxy.data.ChapterList;
import org.falconia.mangaproxy.data.Genre;
import org.falconia.mangaproxy.data.GenreList;
import org.falconia.mangaproxy.data.Manga;
import org.falconia.mangaproxy.data.MangaList;
import org.falconia.mangaproxy.helper.FormatHelper;
import org.falconia.mangaproxy.helper.Regex;

import android.text.TextUtils;

public class Plugin99770 extends PluginBase {
	protected static final String GENRE_URL_PREFIX_1 = "list/";
	protected static final String GENRE_URL_PREFIX_2 = "listabc/";
	protected static final int GENRE_NEW_ID = 0;
	protected static final String GENRE_NEW_ID_STRING = "new";
	protected static final String GENRE_NEW_URL = "more.htm";
	protected static final String GENRE_ALL_URL = "sitemap/";
	protected static final String GENRE_HIT_DISPLAYNAME = "点击排行";

	protected static final String MANGA_URL_PREFIX = "comic/";

	public Plugin99770(int siteId) {
		super(siteId);
	}

	@Override
	public String getName() {
		return "99770";
	}

	@Override
	public String getDisplayname() {
		return "99770漫画";
	}

	@Override
	public String getCharset() {
		return CHARSET_GBK;
	}

	@Override
	public TimeZone getTimeZone() {
		return TimeZone.getTimeZone("GMT+08:00");
	}

	@Override
	public String getUrlBase() {
		return "http://mh.99770.cc/";
	}

	@Override
	public boolean hasGenreList() {
		return true;
	}

	@Override
	public boolean hasSearchEngine() {
		return false;
	}

	@Override
	public String getGenreListUrl() {
		String url = getUrlBase() + "list/1/";
		logI(Get_URL_GenreList, url);
		return url;
	}

	@Override
	public String getGenreUrl(int genreId, int page) {
		String url;
		if (genreId == Genre.GENRE_ALL_ID)
			url = getGenreAllUrl(page);
		else {
			String genreIdString = decodeGenreId(genreId);
			url = getGenreUrl(genreIdString, page);
			logI(Get_URL_of_MangaList, genreIdString, url);
		}
		return url;
	}

	@Override
	protected String getGenreUrl(String genreId) {
		String url = getUrlBase();
		if (genreId == GENRE_NEW_ID_STRING)
			url += GENRE_NEW_URL;
		else
			url = super.getGenreUrl(genreId);
		return url;
	}

	protected String getGenreUrl(String genreId, int page) {
		String url = getGenreUrl(genreId);
		if (page > 1)
			url += String.format("%s.htm", page);
		return url;
	}

	@Override
	public String getGenreAllUrl(int page) {
		String url = getUrlBase() + GENRE_ALL_URL;
		logI(Get_URL_of_AllMangaList, url);
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

	protected GregorianCalendar parseDate(String string) {
		GregorianCalendar calendar = null;
		calendar = parseDateTime(string, "(\\d+)/(\\d+)/(\\d+){'YY','M','D'}");
		return calendar;
	}

	@Override
	protected String parseGenreName(String string) {
		string = super.parseName(string);
		string = string.replaceAll("^漫画$", "视界漫画");
		string = string.replaceAll("[1-9]$", "0");
		return string;
	}

	@Override
	public GenreList getGenreList(String source) {
		GenreList list = new GenreList(getSiteId());

		logI(Get_GenreList);
		logD(Get_Source_Size_GenreList,
				FormatHelper.getFileSize(source, getCharset()));

		if (TextUtils.isEmpty(source))
			return list;

		try {
			String genreId;
			String pattern, html2;
			ArrayList<ArrayList<String>> matches;

			pattern = "(?s)(<div class=\"mm bg bd\">.*?)<div class=ncont ";
			html2 = Regex.matchString(pattern, source);

			pattern = "(?s)<a\\s+href=\"([^\"]+?)\"\\s+target=\"_top\"\\s*>(.+?)</a>";
			matches = Regex.matchAll(pattern, html2);

			for (ArrayList<String> groups : matches) {
				genreId = parseGenreId(groups.get(1));
				if (genreId.equalsIgnoreCase(GENRE_NEW_ID_STRING)) {
					list.add(0, encodeGenreId(genreId),
							parseGenreName(groups.get(2)));
					list.add(1, encodeGenreId(genreId), GENRE_HIT_DISPLAYNAME);
				} else
					list.add(encodeGenreId(genreId),
							parseGenreName(groups.get(2)));
			}

			pattern = "(?s)<a\\s+href=\"([^\"]+?)\"\\s+title=\"(.+?)\">(.+?)</a>";
			matches = Regex.matchAll(pattern, html2);

			for (ArrayList<String> groups : matches) {
				genreId = parseGenreId(groups.get(1));
				list.add(encodeGenreId(genreId), parseGenreName(groups.get(2)));
			}

			logV(list.toString());

		} catch (Exception e) {
			e.printStackTrace();
			logE(Fail_to_process);
		}

		return list;
	}

	@Override
	public MangaList getMangaList(String source, Genre genre) {
		MangaList list = new MangaList(getSiteId());

		logI(Get_MangaList, genre.genreId);
		logD(Get_Source_Size_MangaList,
				FormatHelper.getFileSize(source, getCharset()));

		try {
			long time = System.currentTimeMillis();

			String pattern;
			ArrayList<String> groups;
			ArrayList<ArrayList<String>> matches;

			if (genre.genreId == GENRE_NEW_ID) {
				logD(Process_MangaList_New, genre.genreId);

				pattern = "(?s)<td width=\"50%\">\\s*((?:<table .*?</table>)+)\\s*</td>\\s*<td width=\"50%\">\\s*((?:<table .*?</table>)+)\\s*</td>";
				groups = Regex.match(pattern, source);
				logD(Catched_sections, groups.size() - 1);

				if (!genre.displayname.equals(GENRE_HIT_DISPLAYNAME)) {
					// Section 1 (Genre New)
					String section = "Last Updates";
					pattern = "(?s)<table .+?>[\\s\\d·]+<a href=\"/comic/(\\d+)/\" .+?>\\s*(.+?)\\s*</a>.+?<b>(\\d*)</b><.+?>集\\(卷\\)<.+?>〖(.+?)〗<.+?>\\s*(?:<img [^<>]+>\\s*)?<.+?>([\\d/]+)<.+?</table>";
					matches = Regex.matchAll(pattern, groups.get(1));
					logD(Catched_count_in_section, matches.size(), section);

					for (ArrayList<String> match : matches) {
						// logV(match.get(0));
						Manga manga = new Manga(parseInt(match.get(1)),
								match.get(2), section, getSiteId());
						manga.chapterCount = parseInt(match.get(3));
						manga.isCompleted = parseIsCompleted(match.get(4));
						manga.updatedAt = parseDate(match.get(5));
						manga.setDetailsTemplate("%chapterCount%, %updatedAt%");
						list.add(manga);
						// logV(manga.toLongString());
					}
				} else {
					// Section 2 (Genre Hit)
					String section = "Most Hits";
					pattern = "(?s)<table .+?<a href=\"/comic/(\\d+)/\" .+?>\\s*(.+?)\\s*</a>.+?<b>(\\d*)</b><.+?>集\\(卷\\)<.+?>〖(.+?)〗<.+?>(\\d+)<.+?</table>";
					matches = Regex.matchAll(pattern, groups.get(2));
					logD(Catched_count_in_section, matches.size(), section);

					for (ArrayList<String> match : matches) {
						// logV(match.get(0));
						Manga manga = new Manga(parseInt(match.get(1)),
								match.get(2), section, getSiteId());
						manga.chapterCount = parseInt(match.get(3));
						manga.isCompleted = parseIsCompleted(match.get(4));
						manga.details = "Hit: " + parseInt(match.get(5));
						manga.setDetailsTemplate("%chapterCount%, %details%");
						list.add(manga, true);
						// logV(manga.toLongString());
					}
				}
			} else {
				logD(Process_MangaList, genre.genreId);

				pattern = "(?s).+(<table .*?</table>)\\s*<ul .*?>\\s*(.*?)\\s*</ul>";
				groups = Regex.match(pattern, source);
				logD(Catched_sections, groups.size() - 1);

				// Section 1 (Max Page)
				pattern = "<a href=\"(\\d+)\\.htm\">末页</a>";
				String group = Regex.match(pattern, groups.get(1)).get(1);
				list.pageIndexMax = parseInt(group);
				logD(Catched_total_page, list.pageIndexMax);

				// Section 2 (List)
				pattern = "(?s)<li .*?<a href=\"/Comic/(\\d+)/\" .*?<img src=\"?([^\"]+?)\"? alt=\"提示:\\[(.+?)\\]\\s+?(\\d*?)集\\(卷\\)\".*?>\\s*<h3><a .*?>(.*?)</a></h3>.*?</li>";
				matches = Regex.matchAll(pattern, groups.get(2));
				logD(Catched_count_in_section, matches.size(), "ul");

				for (ArrayList<String> match : matches) {
					Manga manga = new Manga(parseInt(match.get(1)),
							match.get(5), null, getSiteId());
					manga.isCompleted = parseIsCompleted(match.get(3));
					manga.chapterCount = parseInt(match.get(4));
					manga.setDetailsTemplate("%chapterCount%");
					list.add(manga);
					// logV(manga.toLongString());
				}

			}

			time = System.currentTimeMillis() - time;
			logD(Process_Time_MangaList, time);

		} catch (Exception e) {
			e.printStackTrace();
			logE(Fail_to_process);
		}

		return list;
	}

	@Override
	public MangaList getAllMangaList(String source) {
		MangaList list = new MangaList(getSiteId());

		logI(Get_AllMangaList);
		logD(Get_Source_Size_AllMangaList,
				FormatHelper.getFileSize(source, getCharset()));

		try {
			long time = System.currentTimeMillis();

			String pattern, source2;
			ArrayList<ArrayList<String>> matches;

			pattern = "(?s)(<div id='all'><div class='allf'>.*?<span class='redzi'>(.*?)</span>.*?)<div class='aa'></div>";
			matches = Regex.matchAll(pattern, source);
			logD(Catched_sections, matches.size());

			for (ArrayList<String> groups : matches) {
				source2 = groups.get(1);
				String sGenre = parseGenreName(groups.get(2));
				pattern = "(?s)<a href=\"/" + getMangaUrlPrefix()
						+ "(\\d+)\">(.*?)</a>";
				ArrayList<ArrayList<String>> matches2 = Regex.matchAll(pattern,
						source2);
				int nMangaSize = matches2.size();
				logV(Catched_count_in_section, nMangaSize, sGenre);

				for (ArrayList<String> groups2 : matches2)
					list.add(Integer.valueOf(groups2.get(1)),
							parseName(groups2.get(2)), sGenre);
			}

			time = System.currentTimeMillis() - time;
			logD(Process_Time_AllMangaList, time);

		} catch (Exception e) {
			e.printStackTrace();
			logE(Fail_to_process);
		}

		return list;
	}

	@Override
	public ChapterList getChapterList(Manga manga) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Chapter getChapter(Manga manga, Chapter chapter) {
		// TODO Auto-generated method stub
		return null;
	}

	public int encodeGenreId(String string) {
		int id = Genre.GENRE_UNKNOWN_ID;
		if (string
				.matches("^" + GENRE_URL_PREFIX_1.replace('/', '-') + "\\d+$"))
			id = Integer.valueOf(string.substring(GENRE_URL_PREFIX_1.length()));
		else if (string.matches("^" + GENRE_URL_PREFIX_2.replace('/', '-')
				+ "[\\da-zA-Z]$"))
			id = bitsToInt(string.substring(GENRE_URL_PREFIX_2.length()));
		else if (string.equalsIgnoreCase(GENRE_NEW_ID_STRING))
			id = GENRE_NEW_ID;
		return id;
	}

	public String decodeGenreId(int id) {
		String string = "";
		if (id == GENRE_NEW_ID)
			string = GENRE_NEW_ID_STRING;
		else if (id > GENRE_NEW_ID && id < "0".charAt(0))
			string = GENRE_URL_PREFIX_1.replace('/', '-') + id;
		else if (id >= "0".charAt(0))
			string = GENRE_URL_PREFIX_2.replace('/', '-') + intToBits(id);
		return string;
	}

}
