package org.falconia.mangaproxy.data;

import java.io.Serializable;

import org.falconia.mangaproxy.plugin.IPlugin;
import org.falconia.mangaproxy.plugin.Plugins;

public final class Genre implements Serializable {

	private static final long serialVersionUID = 4844325344090017780L;

	public static final int GENRE_ID_ALL = -1;
	public static String GENRE_TEXT_ALL = "All";

	static {
	}

	public static Genre getGenreAll(int siteId) {
		return new Genre(GENRE_ID_ALL, GENRE_TEXT_ALL, siteId);
	}

	public final int iSiteId;
	public final int iGenreId;
	public final String sDisplayname;

	public Genre(int genreId, String displayname, int siteId) {
		this.iGenreId = genreId;
		this.sDisplayname = displayname;
		this.iSiteId = siteId;
	}

	private IPlugin getPlugin() {
		return Plugins.getPlugin(this.iSiteId);
	}

	public String getUrl() {
		return getPlugin().getGenreUrl(this.iGenreId);
	}

	public boolean isGenreAll() {
		return this.iGenreId == GENRE_ID_ALL;
	}

	public MangaList getMangaList(String source) {
		return getMangaList(source, 1);
	}

	public MangaList getMangaList(String source, int page) {
		if (isGenreAll())
			return getPlugin().getAllMangaList(source, page);
		else
			return getPlugin().getMangaList(source, this.iGenreId, page);
	}

	@Override
	public String toString() {
		return this.sDisplayname;
	}

}
