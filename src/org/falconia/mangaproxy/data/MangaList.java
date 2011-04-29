package org.falconia.mangaproxy.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class MangaList implements Serializable, ISiteId, Collection<Manga> {

	private static final long serialVersionUID = 1L;

	private static final int DEFAULT_PAGE_CURRENT = 1;
	private static final int DEFAULT_PAGE_MAX = 1;

	private ArrayList<Integer> mMangaKeyList;
	private HashMap<Integer, Manga> mMangaList;
	private final int mSiteId;

	private int mPageIndexCurrent;
	private int mPageIndexMax;

	public MangaList(int siteId) {
		this(siteId, DEFAULT_PAGE_CURRENT, DEFAULT_PAGE_MAX);
	}

	public MangaList(int siteId, int pageCurrent, int pageMax) {
		this.mMangaKeyList = new ArrayList<Integer>();
		this.mMangaList = new HashMap<Integer, Manga>();
		this.mSiteId = siteId;

		this.mPageIndexCurrent = pageCurrent;
		this.mPageIndexMax = pageMax;
	}

	@Override
	public int getSiteId() {
		return this.mSiteId;
	}

	public int getPageIndexCurrent() {
		return this.mPageIndexCurrent;
	}

	public int getPageIndexMax() {
		return this.mPageIndexMax;
	}

	public int getMangaId(int position) {
		return this.mMangaKeyList.get(position);
	}

	public Manga get(int mangaId) {
		return this.mMangaList.get(mangaId);
	}

	public Manga getAt(int position) {
		return get(getMangaId(position));
	}

	@Override
	public boolean add(Manga manga) {
		int key = manga.mangaId;
		if (contains(key))
			return false;
		this.mMangaList.put(key, manga);
		this.mMangaKeyList.add(key);
		return true;
	}

	public boolean add(int mangaId, String displayname, String inital) {
		return add(new Manga(mangaId, displayname, inital, this.mSiteId));
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
		int key = manga.mangaId;
		if (contains(key))
			return this.mMangaList.put(key, manga);
		else {
			add(manga);
			return null;
		}
	}

	public Manga update(int mangaId, String displayname, String inital) {
		return update(new Manga(mangaId, displayname, inital, this.mSiteId));
	}

	public ArrayList<Manga> updateAll(Collection<? extends Manga> mangas) {
		ArrayList<Manga> previous = new ArrayList<Manga>();
		for (Manga manga : mangas)
			previous.add(update(manga));
		return previous;
	}

	@Override
	public void clear() {
		this.mMangaKeyList.clear();
		this.mMangaList.clear();
		this.mPageIndexCurrent = DEFAULT_PAGE_CURRENT;
		this.mPageIndexMax = DEFAULT_PAGE_MAX;
	}

	@Override
	public boolean contains(Object object) {
		return contains(((Manga) object).mangaId);
	}

	public boolean contains(int mangaId) {
		return this.mMangaList.containsKey(mangaId);
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
		return this.mMangaKeyList.isEmpty();
	}

	@Override
	public Iterator<Manga> iterator() {
		return new Iterator<Manga>() {

			Iterator<Integer> keys = MangaList.this.mMangaKeyList.iterator();

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
		int key = ((Manga) object).mangaId;
		return remove(key);
	}

	public boolean remove(int mangaId) {
		if (!contains(mangaId))
			return false;
		this.mMangaList.remove(mangaId);
		this.mMangaKeyList.remove((Object) mangaId);
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
			mangaIds.add(((Manga) object).mangaId);
		for (Integer mangaId : this.mMangaKeyList)
			if (mangaIds.contains(mangaId))
				remove(mangaId);
		return false;
	}

	@Override
	public int size() {
		return this.mMangaKeyList.size();
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
		for (int mangaId : this.mMangaKeyList)
			mangas.add(this.mMangaList.get(mangaId));
		return mangas;
	}

}
