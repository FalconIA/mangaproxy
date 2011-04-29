package org.falconia.mangaproxy.plugin;

import java.util.ArrayList;

import org.falconia.mangaproxy.ActivityInit;
import org.falconia.mangaproxy.data.Chapter;
import org.falconia.mangaproxy.data.Genre;
import org.falconia.mangaproxy.data.GenreList;
import org.falconia.mangaproxy.data.Manga;
import org.falconia.mangaproxy.data.MangaList;
import org.falconia.mangaproxy.helper.Regex;

import android.text.TextUtils;
import android.util.Log;

public class Plugin99770 extends PluginBase {
	protected static final String GENRE_URL_PREFIX_1 = "list/";
	protected static final String GENRE_URL_PREFIX_2 = "listabc/";
	protected static final int GENRE_NEW_ID = 0;
	protected static final String GENRE_NEW_ID_STRING = "new";
	protected static final String GENRE_NEW_URL = "more.htm";
	protected static final String GENRE_ALL_URL = "sitemap/";

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
	public String getEncoding() {
		return CHARSET_GBK;
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
		logI("Get URL of GenreList: " + url);
		return url;
	}

	@Override
	public String getGenreUrl(int genreId, int page) {
		String url;
		if (genreId == Genre.GENRE_ALL_ID) {
			url = getGenreAllUrl(page);
			logI("Get URL of MangaList(All): " + url);
		} else {
			String genreIdString = decodeGenreId(genreId);
			url = getGenreUrl(genreIdString, page);
			logI("Get URL of MangaList(" + genreIdString + "): " + url);
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
		return getUrlBase() + GENRE_ALL_URL;
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
	protected String checkGenreName(String string) {
		string = super.checkName(string);
		string = string.replaceAll("^漫画$", "视界漫画");
		string = string.replaceAll("[1-9]$", "0");
		return string;
	}

	@Override
	public GenreList getGenreList(String source) {
		GenreList list = new GenreList(getSiteId());

		// ActivityInit.debugPrintLine("Start.");
		// ActivityInit.debugPrintLine(url);

		if (TextUtils.isEmpty(source))
			return list;

		try {
			String genreId;
			String pattern, html2;
			// ArrayList<String> groups;
			ArrayList<ArrayList<String>> matches;

			// ActivityInit.debugPrintLine("Length: " + html.length());
			// ActivityInit.debugPrintLine(source);

			pattern = "(?s)(<div class=\"mm bg bd\">.*?)<div class=ncont ";
			// ActivityInit.debugPrintLine("Pattern: " + pattern);
			html2 = Regex.matchString(pattern, source);
			// ActivityInit.debugPrintLine(pattern2);

			pattern = "(?s)<a\\s+href=\"([^\"]+?)\"\\s+target=\"_top\"\\s*>(.+?)</a>";
			matches = Regex.matchAll(pattern, html2);
			// ActivityInit.debugPrintMatchAll(matches, 2, 1, ", ", "; ");
			for (ArrayList<String> groups : matches) {
				genreId = checkId(groups.get(1));
				if (genreId.equalsIgnoreCase(GENRE_NEW_ID_STRING))
					list.add(0, encodeGenreId(genreId),
							checkGenreName(groups.get(2)));
				else
					list.add(encodeGenreId(genreId),
							checkGenreName(groups.get(2)));
			}

			pattern = "(?s)<a\\s+href=\"([^\"]+?)\"\\s+title=\"(.+?)\">(.+?)</a>";
			matches = Regex.matchAll(pattern, html2);
			// ActivityInit.debugPrintMatchAll(matches, 2, 1, ", ", "; ");
			for (ArrayList<String> groups : matches) {
				genreId = checkId(groups.get(1));
				list.add(encodeGenreId(genreId), checkGenreName(groups.get(2)));
			}

			for (Genre genre : list)
				ActivityInit.debugPrintLine(genre.toString());

		} catch (Exception e) {
			e.printStackTrace();
			log(Log.ERROR, e.toString() + ": " + e.getMessage());
		}

		return list;
	}

	@Override
	public MangaList getMangaList(String source, int genreId, int page) {
		MangaList list = new MangaList(getSiteId());

		logI("Get MangaList.");
		logD("Get MangaList source of " + source.length() + "B.");

		try {
			long time = System.currentTimeMillis();

			String pattern;
			ArrayList<String> groups;

			if (genreId == GENRE_NEW_ID) {
				logD("Process MangaList for Genre of NEW.");

				pattern = "(?s)<td width=\"50%\">\\s*((?:<table .*?</table>)+)\\s*</td>\\s*<td width=\"50%\">\\s*((?:<table .*?</table>)+)\\s*</td>";
				groups = Regex.match(pattern, source);
				logD("Matches: " + (groups.size() - 1) + "part(s).");
			} else
				logD("Process MangaList for normal Genre.");

			time = System.currentTimeMillis() - time;
			logD("Process MangaList in " + time + "ms.");

		} catch (Exception e) {
			e.printStackTrace();
			log(Log.ERROR, e.toString() + ": " + e.getMessage());
		}

		return list;
	}

	@Override
	public MangaList getAllMangaList(String source, int page) {
		MangaList list = new MangaList(getSiteId());

		try {
			long time = System.currentTimeMillis();

			String pattern, source2;
			ArrayList<ArrayList<String>> matches;

			ActivityInit.debugPrintLine("Length: " + source.length());
			// ActivityInit.debugPrintLine(source);

			pattern = "(?s)(<div id='all'><div class='allf'>.*?<span class='redzi'>(.*?)</span>.*?)<div class='aa'></div>";
			// ActivityInit.debugPrintLine("Pattern: " + pattern);
			matches = Regex.matchAll(pattern, source);
			ActivityInit.debugPrintLine("Matches: " + matches.size());

			for (ArrayList<String> groups : matches) {
				source2 = groups.get(1);
				// ActivityInit.debugPrintLine(pattern2);
				String sGenre = checkGenreName(groups.get(2));
				pattern = "(?s)<a href=\"/" + getMangaUrlPrefix()
						+ "(\\d+)\">(.*?)</a>";
				ArrayList<ArrayList<String>> matches2 = Regex.matchAll(pattern,
						source2);
				int nMangaSize = matches2.size();
				ActivityInit.debugPrintLine(sGenre + ": " + nMangaSize);
				for (ArrayList<String> groups2 : matches2)
					list.add(Integer.valueOf(groups2.get(1)),
							checkName(groups2.get(2)), sGenre);
			}

			time = System.currentTimeMillis() - time;
			ActivityInit.debugPrintLine("Cost: " + time + "ms");

		} catch (Exception e) {
			e.printStackTrace();
			log(Log.ERROR, e.toString() + ": " + e.getMessage());
		}

		return list;
	}

	@Override
	public Manga getComic(int comicId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Chapter getChapter(int comicId, int chapterId) {
		// TODO Auto-generated method stub
		return null;
	}

	public int encodeGenreId(String string) {
		int id = Genre.GENRE_UNKNOWN_ID;
		if (Regex.isMatch("^" + GENRE_URL_PREFIX_1.replace('/', '-') + "\\d+$",
				string))
			id = Integer.valueOf(string.substring(GENRE_URL_PREFIX_1.length()));
		else if (Regex.isMatch("^" + GENRE_URL_PREFIX_2.replace('/', '-')
				+ "[\\da-zA-Z]$", string))
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
