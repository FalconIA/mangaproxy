package org.falconia.mangaproxy.data;

import java.io.Serializable;

import org.falconia.mangaproxy.plugin.IPlugin;
import org.falconia.mangaproxy.plugin.Plugins;
import org.falconia.mangaproxy.task.GetSourceTask;

public final class Genre implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int GENRE_UNKNOWN_ID = -2;
	public static final int GENRE_ALL_ID = -1;
	public static String GENRE_ALL_TEXT = "All";

	static {
	}

	public static Genre getGenreAll(int siteId) {
		return new Genre(GENRE_ALL_ID, GENRE_ALL_TEXT, siteId);
	}

	public final int siteId;
	public final int genreId;
	public final String displayname;

	public Genre(int genreId, String displayname, int siteId) {
		this.genreId = genreId;
		this.displayname = displayname;
		this.siteId = siteId;
	}

	private IPlugin getPlugin() {
		return Plugins.getPlugin(this.siteId);
	}

	public String getSiteName() {
		return getPlugin().getName();
	}

	public String getSiteDisplayname() {
		return getPlugin().getDisplayname();
	}

	public String getUrl(int page) {
		if (isGenreAll())
			return getPlugin().getGenreAllUrl(page);
		else
			return getPlugin().getGenreUrl(this.genreId, page);
	}

	public String getUrl() {
		return getUrl(1);
	}

	public boolean isGenreAll() {
		return this.genreId == GENRE_ALL_ID;
	}

	public void getMangaListSource(GetSourceTask task, int page) {
		task.execute(getUrl(page));
	}

	public MangaList getMangaList(String source) {
		if (isGenreAll())
			return getPlugin().getAllMangaList(source);
		else
			return getPlugin().getMangaList(source, this);
	}

	@Override
	public String toString() {
		return String.format("{%d:%s}", this.genreId, this.displayname);
	}

}
