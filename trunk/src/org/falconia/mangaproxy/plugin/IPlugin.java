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

	boolean hasSearchEngine();

	boolean hasGenreList();

	boolean isDynamicImgServer();

	String getGenreListUrl();

	String getGenreUrl(Genre genre, int page);

	String getGenreUrl(Genre genre);

	String getGenreAllUrl(int page);

	String getGenreAllUrl();

	String getMangaUrl(Manga manga);

	String getMangaUrlPrefix();

	String getMangaUrlPostfix();

	String getChapterUrl(Chapter chapter, Manga manga);

	String getDynamicImgServerSourceUrl(String source);

	GenreList getGenreList(String source);

	MangaList getMangaList(String source, Genre genre);

	MangaList getAllMangaList(String source);

	ChapterList getChapterList(String source, Manga manga);

	Chapter getChapter(Manga manga, Chapter chapter);

}
