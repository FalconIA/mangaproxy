package org.falconia.mangaproxy.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public final class ChapterList implements Serializable, ISiteId,
		Iterable<Chapter> {

	private static final long serialVersionUID = 1L;

	private final ArrayList<Chapter> mChapterList;
	private final Manga mManga;

	private int sizeVolume = 0, sizeChapter = 0, sizeUnknow = 0;
	private String[] dynamicImgServers;

	public ChapterList(Manga manga) {
		this.mChapterList = new ArrayList<Chapter>();
		this.mManga = manga;
	}

	@Override
	public int getSiteId() {
		return this.mManga.siteId;
	}

	public String getDisplayname(int position) {
		return getAt(position).displayname;
	}

	public boolean hasDynamicImgServers() {
		return this.dynamicImgServers != null
				&& this.dynamicImgServers.length > 0;
	}

	@Override
	public Iterator<Chapter> iterator() {
		return this.mChapterList.iterator();
	}

	public void add(Chapter chapter) {
		countType(chapter);
		setDynamicImgServers(chapter);
		this.mChapterList.add(chapter);
	}

	public void add(String chapterId, String displayname) {
		this.mChapterList.add(new Chapter(chapterId, displayname, this.mManga));
	}

	public void addAll(Collection<Chapter> chapters) {
		for (Chapter chapter : chapters)
			this.mChapterList.add(chapter);
	}

	public void insert(int position, Chapter chapter) {
		countType(chapter);
		setDynamicImgServers(chapter);
		this.mChapterList.add(position, chapter);
	}

	public void insert(int position, String chapterId, String displayname) {
		this.mChapterList.add(position, new Chapter(chapterId, displayname,
				this.mManga));
	}

	public Chapter getAt(int position) {
		Chapter chapter = this.mChapterList.get(position);
		setDynamicImgServers(chapter);
		return chapter;
	}

	public int size() {
		return this.mChapterList.size();
	}

	public ArrayList<Chapter> toArray() {
		return this.mChapterList;
	}

	private void countType(Chapter chapter) {
		switch (chapter.typeId) {
		case Chapter.TYPE_ID_VOLUME:
			this.sizeVolume++;
			break;
		case Chapter.TYPE_ID_CHAPTER:
			this.sizeChapter++;
			break;
		case Chapter.TYPE_ID_UNKNOW:
			this.sizeUnknow++;
			break;
		}
	}

	private void setDynamicImgServers(Chapter chapter) {
		if (!chapter.hasDynamicImgServers() && hasDynamicImgServers())
			chapter.setDynamicImgServers(this.dynamicImgServers);
	}

	@Override
	public String toString() {
		return String
				.format("{ SiteId:%d, MangaId:'%s', Size:%d, Volume:%d, Chapter:%d, Unknow:%d, HasDynamicImgServers:%b }",
						this.mManga.siteId, this.mManga.mangaId, size(),
						this.sizeVolume, this.sizeChapter, this.sizeUnknow,
						hasDynamicImgServers());
	}

}
