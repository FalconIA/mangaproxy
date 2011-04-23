package org.falconia.mangaproxy.plugin;

import java.util.ArrayList;

import org.falconia.mangaproxy.ActivityInit;
import org.falconia.mangaproxy.data.Chapter;
import org.falconia.mangaproxy.data.Genre;
import org.falconia.mangaproxy.data.GenreList;
import org.falconia.mangaproxy.data.Manga;
import org.falconia.mangaproxy.data.MangaList;
import org.falconia.mangaproxy.helper.Regex;

public class Plugin99770 extends APlugin {
	protected static final String GENRE_URL_PREFIX_1 = "list/";
	protected static final String GENRE_URL_PREFIX_2 = "listabc/";
	protected static final String GENRE_NEW_ID = "new";
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
	public String getGenreUrl(int genreId) {
		return getGenreUrl(decodeGenreId(genreId));
	}

	protected String getGenreUrl(String genreId) {
		String url = getUrlBase();
		if (genreId == GENRE_NEW_ID)
			url += GENRE_NEW_URL;
		else
			url = super.getGenreUrl(genreId);
		return url;
	}

	@Override
	public String getGenreAllUrl() {
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
	public boolean hasGenreList() {
		return false;
	}

	@Override
	public boolean hasSearchEngine() {
		return false;
	}

	@Override
	protected String checkGenreName(String string) {
		string = super.checkName(string);
		string = string.replaceAll("^漫画$", "视界漫画");
		string = string.replaceAll("[1-9]$", "0");
		return string;
	}

	@Override
	public GenreList getGenreList() {
		GenreList list = new GenreList(getSiteId());
		String url = getUrlBase() + "list/1/";

		// ActivityInit.debugPrintLine("Start.");
		// ActivityInit.debugPrintLine(url);

		try {
			String html = parseHtml(url);
			String genreId;
			String pattern, html2;
			// ArrayList<String> groups;
			ArrayList<ArrayList<String>> matches;

			ActivityInit.debugPrintLine("Length: " + html.length());
			// ActivityInit.debugPrintLine(html);

			pattern = "(?s)(<div class=\"mm bg bd\">.*?)<div class=ncont ";
			// ActivityInit.debugPrintLine("Pattern: " + pattern);
			html2 = Regex.matchString(pattern, html);
			// ActivityInit.debugPrintLine(pattern2);

			pattern = "(?s)<a\\s+href=\"([^\"]+?)\"\\s+target=\"_top\"\\s*>(.+?)</a>";
			matches = Regex.matchAll(pattern, html2);
			// ActivityInit.debugPrintMatchAll(matches, 2, 1, ", ", "; ");
			for (ArrayList<String> groups : matches) {
				genreId = checkId(groups.get(1));
				if (genreId.equalsIgnoreCase(GENRE_NEW_ID))
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
			ActivityInit.debugPrintLine(e.toString());
			e.printStackTrace();
		}

		return list;
	}

	@Override
	public MangaList getMangaList(int genreId, int page) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MangaList getAllMangaList(int page) {
		// TODO Auto-generated method stub
		MangaList list = new MangaList(getSiteId());
		String url = getGenreAllUrl();

		try {
			long time = System.currentTimeMillis();

			String html = parseHtml(url);
			String pattern, html2;
			ArrayList<ArrayList<String>> matches;

			ActivityInit.debugPrintLine("Length: " + html.length());
			// ActivityInit.debugPrintLine(html);

			time = System.currentTimeMillis() - time;
			ActivityInit.debugPrintLine("Cost: " + time + "ms");
			time = System.currentTimeMillis();

			pattern = "(?s)(<div id='all'><div class='allf'>.*?<span class='redzi'>(.*?)</span>.*?)<div class='aa'></div>";
			// ActivityInit.debugPrintLine("Pattern: " + pattern);
			matches = Regex.matchAll(pattern, html);
			ActivityInit.debugPrintLine("Matches: " + matches.size());

			for (ArrayList<String> groups : matches) {
				html2 = groups.get(1);
				// ActivityInit.debugPrintLine(pattern2);
				String sGenre = checkGenreName(groups.get(2));
				ActivityInit.debugPrint(sGenre + ": ");
				pattern = "(?s)<a href=\"/" + getMangaUrlPrefix()
						+ "(\\d+)\">(.*?)</a>";
				ArrayList<ArrayList<String>> matches2 = Regex.matchAll(pattern,
						html2);
				int nMangaSize = matches2.size();
				ActivityInit.debugPrintLine("" + nMangaSize);
				for (ArrayList<String> groups2 : matches2) {
					list.add(Integer.valueOf(groups2.get(1)),
							checkName(groups2.get(2)), sGenre);
				}
			}

			time = System.currentTimeMillis() - time;
			ActivityInit.debugPrintLine("Cost: " + time + "ms");

		} catch (Exception e) {
			ActivityInit.debugPrintLine(e.toString());
			e.printStackTrace();
		}

		return null;
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
		int id = -2;
		if (Regex.isMatch("^" + GENRE_URL_PREFIX_1.replace('/', '-') + "\\d+$",
				string))
			id = Integer.valueOf(string.substring(GENRE_URL_PREFIX_1.length()));
		else if (Regex.isMatch("^" + GENRE_URL_PREFIX_2.replace('/', '-')
				+ "[\\da-zA-Z]$", string))
			id = bitsToInt(string.substring(GENRE_URL_PREFIX_2.length()));
		else if (string.equalsIgnoreCase(GENRE_NEW_ID))
			id = 0;
		return id;
	}

	public String decodeGenreId(int id) {
		String string = "";
		if (id == 0)
			string = GENRE_NEW_ID;
		else if (id > 0 && id < "0".charAt(0))
			string = GENRE_URL_PREFIX_1.replace('/', '-') + id;
		else if (id >= "0".charAt(0))
			string = GENRE_URL_PREFIX_2.replace('/', '-') + intToBits(id);
		return string;
	}

}
