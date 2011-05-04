package org.falconia.mangaproxy.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import android.text.TextUtils;

public final class GenreList implements Serializable, ISiteId, Iterable<Genre> {

	private static final long serialVersionUID = 1L;

	private final ArrayList<Genre> mGenreList;
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

	public void add(String genreId, String displayname) {
		this.mGenreList.add(new Genre(genreId, displayname, this.mSiteId));
	}

	public void addAll(Collection<Genre> genres) {
		this.mGenreList.addAll(genres);
	}

	public void insert(int position, Genre genre) {
		this.mGenreList.add(position, genre);
	}

	public void insert(int position, String genreId, String displayname) {
		this.mGenreList.add(position, new Genre(genreId, displayname,
				this.mSiteId));
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

	@Override
	public String toString() {
		ArrayList<String> strings = new ArrayList<String>();
		for (Genre genre : this.mGenreList)
			strings.add(genre.toString());
		return String.format("{ %s }", TextUtils.join(", ", strings));
	}

}
