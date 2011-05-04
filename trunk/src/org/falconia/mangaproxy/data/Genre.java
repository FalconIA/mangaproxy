package org.falconia.mangaproxy.data;

import java.io.Serializable;

import org.falconia.mangaproxy.AppConst;
import org.falconia.mangaproxy.plugin.IPlugin;
import org.falconia.mangaproxy.plugin.Plugins;

public final class Genre implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String GENRE_UNKNOWN_ID = "GENRE_UNKNOWN";
	public static final String GENRE_ALL_ID = "GENRE_UNKNOWN";

	public static Genre getGenreAll(int siteId) {
		return new Genre(GENRE_ALL_ID, AppConst.GENRE_ALL_TEXT, siteId);
	}

	public final int siteId;
	public final String genreId;
	public final String displayname;

	public Genre(String genreId, String displayname, int siteId) {
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
			return getPlugin().getGenreUrl(this, page);
	}

	public String getUrl() {
		return getUrl(1);
	}

	public boolean isGenreAll() {
		return this.genreId.equals(GENRE_ALL_ID);
	}

	public MangaList getMangaList(String source) {
		if (isGenreAll())
			return getPlugin().getAllMangaList(source);
		else
			return getPlugin().getMangaList(source, this);
	}

	@Override
	public String toString() {
		return String.format("{%s:%s}", this.genreId, this.displayname);
	}

}
