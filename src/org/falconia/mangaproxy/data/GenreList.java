package org.falconia.mangaproxy.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class GenreList implements ISiteId, Iterable<Genre> {

	private ArrayList<Genre> marrGenre;
	private final int miSiteId;

	public GenreList(int siteId) {
		this.marrGenre = new ArrayList<Genre>();
		this.miSiteId = siteId;
	}

	@Override
	public int getSiteId() {
		return this.miSiteId;
	}

	public String getDisplayname(int position) {
		return getAt(position).sDisplayname;
	}

	@Override
	public Iterator<Genre> iterator() {
		return this.marrGenre.iterator();
	}

	public void add(Genre genre) {
		this.marrGenre.add(genre);
	}

	public void add(int genreId, String displayname) {
		this.marrGenre.add(new Genre(genreId, displayname, this.miSiteId));
	}

	public void addAll(Collection<Genre> genres) {
		this.marrGenre.addAll(genres);
	}

	public void add(int index, Genre genre) {
		this.marrGenre.add(index, genre);
	}

	public void add(int index, int genreId, String displayname) {
		this.marrGenre.add(index,
				new Genre(genreId, displayname, this.miSiteId));
	}

	public Genre getAt(int position) {
		return this.marrGenre.get(position);
	}

	public int size() {
		return this.marrGenre.size();
	}

	public ArrayList<Genre> toArray() {
		return this.marrGenre;
	}

}
