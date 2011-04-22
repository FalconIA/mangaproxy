package org.falconia.mangaproxy.data;

import java.util.ArrayList;
import java.util.Collection;

public class MangaList implements ISiteId {

	private ArrayList<Manga> marrManga;
	private final int miSiteId;

	private int miPageIndexCurrent;
	private int miPageIndexMax;

	public MangaList(int siteId) {
		this(siteId, 1, 1);
	}

	public MangaList(int siteId, int pageCurrent, int pageMax) {
		this.marrManga = new ArrayList<Manga>();
		this.miSiteId = siteId;

		this.miPageIndexCurrent = pageCurrent;
		this.miPageIndexMax = pageMax;
	}

	@Override
	public int getSiteId() {
		return this.miSiteId;
	}

	public int getPageIndexCurrent() {
		return miPageIndexCurrent;
	}

	public int getPageIndexMax() {
		return miPageIndexMax;
	}

	public void add(Manga manga) {
		this.marrManga.add(manga);
	}

	public void add(int mangaId, String displayname, String url) {
		this.marrManga.add(new Manga(mangaId, displayname, url, this.miSiteId));
	}

	public void addAll(Collection<Manga> genres) {
		this.marrManga.addAll(genres);
	}

	public void add(int index, Manga manga) {
		this.marrManga.add(index, manga);
	}

	public void add(int index, int mangaId, String displayname, String url) {
		this.marrManga.add(index, new Manga(mangaId, displayname, url,
				this.miSiteId));
	}

	public Manga get(int position) {
		return this.marrManga.get(position);
	}

	public int size() {
		return this.marrManga.size();
	}

	public ArrayList<Manga> toArray() {
		return marrManga;
	}

}
