package org.falconia.mangaproxy.plugin;

import java.util.TimeZone;

import org.falconia.mangaproxy.data.Chapter;
import org.falconia.mangaproxy.data.ChapterList;
import org.falconia.mangaproxy.data.Genre;
import org.falconia.mangaproxy.data.GenreList;
import org.falconia.mangaproxy.data.Manga;
import org.falconia.mangaproxy.data.MangaList;

public interface IPlugin {

	int getSiteId();

	String getName();

	String getDisplayname();

	String getCharset();

	TimeZone getTimeZone();

	String getUrlBase();

	boolean hasGenreList();

	boolean hasSearchEngine();

	String getGenreListUrl();

	String getGenreUrl(int genreId, int page);

	String getGenreUrl(int genreId);

	String getGenreAllUrl(int page);

	String getGenreAllUrl();

	String getMangaUrl(int mangaId);

	String getMangaUrlPrefix();

	String getMangaUrlPostfix();

	GenreList getGenreList(String source);

	MangaList getMangaList(String source, Genre genre);

	MangaList getAllMangaList(String source);

	ChapterList getChapterList(Manga manga);

	Chapter getChapter(Manga manga, Chapter chapter);

}
