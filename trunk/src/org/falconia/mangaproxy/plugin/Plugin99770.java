package org.falconia.mangaproxy.plugin;


import java.util.ArrayList;

import org.falconia.mangaproxy.ActivityInit;
import org.falconia.mangaproxy.data.Chapter;
import org.falconia.mangaproxy.data.GenreList;
import org.falconia.mangaproxy.data.Manga;
import org.falconia.mangaproxy.data.MangaList;
import org.falconia.mangaproxy.helper.Regex;


public class Plugin99770 extends APlugin {
	private static final String GENRE_ID_LINK_0 = "new";
	private static final String GENRE_ID_LINK_1 = "list";
	private static final String GENRE_ID_LINK_2 = "listabc";
	private static final String GENRE_URL_0 = "more.htm";
	private static final String GENRE_URL_ALL = "sitemap/";

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
	public String getGenreUrl(String genreId) {
		String url = getUrlBase();
		if (genreId == GENRE_ID_LINK_0)
			url += GENRE_URL_0;
		else
			url = super.getGenreUrl(genreId);
		return url;
	}

	@Override
	public String getGenreAllUrl() {
		return getUrlBase() + GENRE_URL_ALL;
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
		return string;
	}

	@Override
	public GenreList getGenreList() {
		GenreList list = new GenreList(this.getSiteId());
		String url = getUrlBase() + "list/1/";

		// ActivityInit.debugPrintLine("Start.");
		// ActivityInit.debugPrintLine(url);

		try {
			String html = parseHtml(url);
			String genreId;
			String pattern, pattern2;
			ArrayList<String> groups;
			ArrayList<ArrayList<String>> matches;

			ActivityInit.debugPrintLine("Length: " + html.length());
			// ActivityInit.debugPrintLine(html);

			pattern = "(?s)<div class=\"mm bg bd\">(.*?)<div class=ncont ";
			// ActivityInit.debugPrintLine("Pattern: " + pattern);
			groups = Regex.match(pattern, html);
			// ActivityInit.debugPrintLine("Matches: " + groups.size());
			pattern2 = groups.get(1);
			// ActivityInit.debugPrintLine(pattern2);

			pattern = "(?s)<a\\s+href=\"([^\"]+?)\"\\s+target=\"_top\"\\s*>(.+?)</a>";
			matches = Regex.matchAll(pattern, pattern2);
			// ActivityInit.debugPrintMatchAll(matches, 2, 1, ", ", "; ");
			for (int i = 0; i < matches.size(); i++) {
				groups = matches.get(i);
				genreId = checkId(groups.get(1));
				if (genreId.equalsIgnoreCase(GENRE_ID_LINK_0))
					list.add(0, encodeGenreId(genreId),
							checkGenreName(groups.get(2)), getGenreUrl(genreId));
				else
					list.add(encodeGenreId(genreId),
							checkGenreName(groups.get(2)), getGenreUrl(genreId));
			}

			pattern = "(?s)<a\\s+href=\"([^\"]+?)\"\\s+title=\"(.+?)\">(.+?)</a>";
			matches = Regex.matchAll(pattern, pattern2);
			// ActivityInit.debugPrintMatchAll(matches, 2, 1, ", ", "; ");
			for (int i = 0; i < matches.size(); i++) {
				groups = matches.get(i);
				genreId = checkId(groups.get(1));
				list.add(encodeGenreId(genreId), checkGenreName(groups.get(2)),
						getGenreUrl(genreId));
			}

			for (int i = 0; i < list.size(); i++)
				ActivityInit.debugPrintLine(list.get(i).toString());

		} catch (Exception e) {
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
		MangaList list = new MangaList(this.getSiteId());
		String url = getUrlBase() + GENRE_URL_0;

		try {
			String html = parseHtml(url);

			ActivityInit.debugPrintLine("Length: " + html.length());
			ActivityInit.debugPrintLine(html);

		} catch (Exception e) {
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
		if (Regex.isMatch("^" + GENRE_ID_LINK_1 + "-\\d+$", string)) {
			id = Integer.valueOf(string.replace(GENRE_ID_LINK_1 + "-", ""));
		} else if (Regex.isMatch("^" + GENRE_ID_LINK_2 + "-[\\da-zA-Z]$",
				string)) {
			id = bitsToInt(string.replace(GENRE_ID_LINK_2 + "-", ""));
		} else if (string.equalsIgnoreCase(GENRE_ID_LINK_0)) {
			id = 0;
		}
		return id;
	}

	public String decodeGenreId(int id) {
		String string = "";
		if (id == 0) {
			string = GENRE_ID_LINK_0;
		} else if (id > 0 && id < "0".charAt(0)) {
			string = GENRE_ID_LINK_1 + "-" + id;
		} else if (id >= "0".charAt(0)) {
			string = GENRE_ID_LINK_2 + "-" + intToBits(id);
		}
		return string;
	}

}
