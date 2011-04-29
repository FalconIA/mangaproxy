package org.falconia.mangaproxy.plugin;

import org.falconia.mangaproxy.data.Chapter;
import org.falconia.mangaproxy.data.GenreList;
import org.falconia.mangaproxy.data.Manga;
import org.falconia.mangaproxy.data.MangaList;

public interface IPlugin {

	int getSiteId();

	String getName();

	String getDisplayname();

	String getEncoding();

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

	MangaList getMangaList(String source, int genreId, int page);

	MangaList getMangaList(String source, int genreId);

	MangaList getAllMangaList(String source, int page);

	MangaList getAllMangaList(String source);

	Manga getComic(int comicId);

	Chapter getChapter(int comicId, int chapterId);

}
