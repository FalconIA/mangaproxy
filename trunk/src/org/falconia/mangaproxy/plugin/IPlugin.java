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

	String getGenreListUrl();

	String getGenreUrl(int genreId);

	String getGenreAllUrl();

	String getMangaUrl(int mangaId);

	String getMangaUrlPrefix();

	String getMangaUrlPostfix();

	boolean hasGenreList();

	boolean hasSearchEngine();

	GenreList getGenreList(String source);

	MangaList getMangaList(String source, int genreId);

	MangaList getMangaList(String source, int genreId, int page);

	String getAllMangaListUrl();

	MangaList getAllMangaList(String source);

	MangaList getAllMangaList(String source, int page);

	Manga getComic(int comicId);

	Chapter getChapter(int comicId, int chapterId);

}
