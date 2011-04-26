package org.falconia.mangaproxy.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class MangaList implements ISiteId, Collection<Manga> {

	private static final int DEFAULT_PAGE_CURRENT = 1;
	private static final int DEFAULT_PAGE_MAX = 1;

	private ArrayList<Integer> marrMangaKey;
	private HashMap<Integer, Manga> marrManga;
	private final int miSiteId;

	private int miPageIndexCurrent;
	private int miPageIndexMax;

	public MangaList(int siteId) {
		this(siteId, DEFAULT_PAGE_CURRENT, DEFAULT_PAGE_MAX);
	}

	public MangaList(int siteId, int pageCurrent, int pageMax) {
		this.marrMangaKey = new ArrayList<Integer>();
		this.marrManga = new HashMap<Integer, Manga>();
		this.miSiteId = siteId;

		this.miPageIndexCurrent = pageCurrent;
		this.miPageIndexMax = pageMax;
	}

	@Override
	public int getSiteId() {
		return this.miSiteId;
	}

	public int getPageIndexCurrent() {
		return this.miPageIndexCurrent;
	}

	public int getPageIndexMax() {
		return this.miPageIndexMax;
	}

	public int getMangaId(int position) {
		return this.marrMangaKey.get(position);
	}

	public Manga get(int mangaId) {
		return this.marrManga.get(mangaId);
	}

	public Manga getAt(int position) {
		return get(getMangaId(position));
	}

	@Override
	public boolean add(Manga manga) {
		int key = manga.iMangaId;
		if (contains(key))
			return false;
		this.marrManga.put(key, manga);
		this.marrMangaKey.add(key);
		return true;
	}

	public boolean add(int mangaId, String displayname, String inital) {
		return add(new Manga(mangaId, displayname, inital, this.miSiteId));
	}

	@Override
	public boolean addAll(Collection<? extends Manga> mangas) {
		boolean modified = false;
		for (Manga manga : mangas)
			if (add(manga) && !modified)
				modified = true;
		return modified;
	}

	public Manga update(Manga manga) {
		int key = manga.iMangaId;
		if (contains(key))
			return this.marrManga.put(key, manga);
		else {
			add(manga);
			return null;
		}
	}

	public Manga update(int mangaId, String displayname, String inital) {
		return update(new Manga(mangaId, displayname, inital, this.miSiteId));
	}

	public ArrayList<Manga> updateAll(Collection<? extends Manga> mangas) {
		ArrayList<Manga> previous = new ArrayList<Manga>();
		for (Manga manga : mangas)
			previous.add(update(manga));
		return previous;
	}

	@Override
	public void clear() {
		this.marrMangaKey.clear();
		this.marrManga.clear();
		this.miPageIndexCurrent = DEFAULT_PAGE_CURRENT;
		this.miPageIndexMax = DEFAULT_PAGE_MAX;
	}

	@Override
	public boolean contains(Object object) {
		return contains(((Manga) object).iMangaId);
	}

	public boolean contains(int mangaId) {
		return this.marrManga.containsKey(mangaId);
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		for (Object object : collection)
			if (!contains(object))
				return false;
		return true;
	}

	@Override
	public boolean isEmpty() {
		return this.marrMangaKey.isEmpty();
	}

	@Override
	public Iterator<Manga> iterator() {
		return new Iterator<Manga>() {

			Iterator<Integer> keys = MangaList.this.marrMangaKey.iterator();

			@Override
			public boolean hasNext() {
				return this.keys.hasNext();
			}

			@Override
			public Manga next() {
				return get(this.keys.next());
			}

			@Override
			public void remove() {
				this.keys.remove();
			}

		};
	}

	@Override
	public boolean remove(Object object) {
		int key = ((Manga) object).iMangaId;
		return remove(key);
	}

	public boolean remove(int mangaId) {
		if (!contains(mangaId))
			return false;
		this.marrManga.remove(mangaId);
		this.marrMangaKey.remove((Object) mangaId);
		return true;
	}

	public void removeAt(int position) {
		remove(getAt(position));
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		boolean modified = false;
		for (Object object : collection)
			if (remove(object) && !modified)
				modified = true;
		return modified;
	}

	@Override
	public boolean retainAll(Collection<?> collection) {
		ArrayList<Integer> mangaIds = new ArrayList<Integer>();
		for (Object object : collection)
			mangaIds.add(((Manga) object).iMangaId);
		for (Integer mangaId : this.marrMangaKey)
			if (mangaIds.contains(mangaId))
				remove(mangaId);
		return false;
	}

	@Override
	public int size() {
		return this.marrMangaKey.size();
	}

	@Override
	public Object[] toArray() {
		return toArrayList().toArray();
	}

	@Override
	public <T> T[] toArray(T[] array) {
		return toArrayList().toArray(array);
	}

	public ArrayList<Manga> toArrayList() {
		ArrayList<Manga> mangas = new ArrayList<Manga>();
		for (int mangaId : this.marrMangaKey)
			mangas.add(this.marrManga.get(mangaId));
		return mangas;
	}

}
