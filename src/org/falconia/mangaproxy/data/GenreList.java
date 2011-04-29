package org.falconia.mangaproxy.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class GenreList implements Serializable, ISiteId, Iterable<Genre> {

	private static final long serialVersionUID = 1L;

	private ArrayList<Genre> mGenreList;
	private final int mSiteId;

	public GenreList(int siteId) {
		this.mGenreList = new ArrayList<Genre>();
		this.mSiteId = siteId;
	}

	@Override
	public int getSiteId() {
		return this.mSiteId;
	}

	public String getDisplayname(int position) {
		return getAt(position).displayname;
	}

	@Override
	public Iterator<Genre> iterator() {
		return this.mGenreList.iterator();
	}

	public void add(Genre genre) {
		this.mGenreList.add(genre);
	}

	public void add(int genreId, String displayname) {
		this.mGenreList.add(new Genre(genreId, displayname, this.mSiteId));
	}

	public void addAll(Collection<Genre> genres) {
		this.mGenreList.addAll(genres);
	}

	public void add(int index, Genre genre) {
		this.mGenreList.add(index, genre);
	}

	public void add(int index, int genreId, String displayname) {
		this.mGenreList.add(index,
				new Genre(genreId, displayname, this.mSiteId));
	}

	public Genre getAt(int position) {
		return this.mGenreList.get(position);
	}

	public int size() {
		return this.mGenreList.size();
	}

	public ArrayList<Genre> toArray() {
		return this.mGenreList;
	}

}
