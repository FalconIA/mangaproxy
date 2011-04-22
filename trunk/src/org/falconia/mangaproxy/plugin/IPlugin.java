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
	String getGenreUrl(String genreId);
	String getGenreAllUrl();

	boolean hasGenreList();
	boolean hasSearchEngine();

	GenreList getGenreList();
	MangaList getMangaList(int genreId);
	MangaList getMangaList(int genreId, int page);
	MangaList getAllMangaList();
	MangaList getAllMangaList(int page);
	Manga getComic(int comicId);
	Chapter getChapter(int comicId, int chapterId);
}
