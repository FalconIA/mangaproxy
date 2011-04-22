package org.falconia.mangaproxy.data;

import java.io.Serializable;

import org.falconia.mangaproxy.plugin.Plugin;


public final class Genre implements Serializable, ISiteId {

	private static final long serialVersionUID = 4844325344090017780L;

	public static final int GENRE_ID_ALL = -1;
	public static String GENRE_TEXT_ALL = "All";

	static {
	}

	public static Genre getGenreAll(int siteId) {
		return new Genre(GENRE_ID_ALL, GENRE_TEXT_ALL, Plugin.getPlugin(siteId)
				.getGenreAllUrl(), siteId);
	}

	public final int iSiteId;
	public final int iGenreId;
	public final String sDisplayName;
	public final String sUrl;

	public Genre(int genreId, String displayname, String url, int siteId) {
		this.iGenreId = genreId;
		this.sDisplayName = displayname;
		this.sUrl = url;
		this.iSiteId = siteId;
	}

	@Override
	public int getSiteId() {
		return this.iSiteId;
	}

	public int getGenreId() {
		return this.iGenreId;
	}

	public String getDisplayname() {
		return this.sDisplayName;
	}

	public String getUrl() {
		return this.sUrl;
	}

	public boolean isGenreAll() {
		return this.iGenreId == GENRE_ID_ALL;
	}

	public MangaList getMangaList() {
		return getMangaList(1);
	}

	public MangaList getMangaList(int page) {
		if (isGenreAll())
			return Plugin.getPlugin(this.iSiteId).getAllMangaList(page);
		else
			return Plugin.getPlugin(this.iSiteId).getMangaList(this.iGenreId, page);
	}

	@Override
	public String toString() {
		return String.format("[%d-%04d] %s: %s", getSiteId(), getGenreId(),
				getDisplayname(), getUrl());
	}

}
