package org.falconia.mangaproxy.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public final class ChapterList implements Serializable, ISiteId, Iterable<Chapter> {

	private static final long serialVersionUID = 1L;

	private final ArrayList<Chapter> mChapterList;
	private final Manga mManga;

	private int sizeVolume = 0, sizeChapter = 0, sizeUnknow = 0;
	private String[] dynamicImgServers;

	public ChapterList(Manga manga) {
		mChapterList = new ArrayList<Chapter>();
		mManga = manga;
	}

	@Override
	public int getSiteId() {
		return mManga.siteId;
	}

	public String getDisplayname(int position) {
		return getAt(position).displayname;
	}

	public boolean hasDynamicImgServers() {
		return dynamicImgServers != null && dynamicImgServers.length > 0;
	}

	@Override
	public Iterator<Chapter> iterator() {
		return mChapterList.iterator();
	}

	public void add(Chapter chapter) {
		countType(chapter);
		setDynamicImgServers(chapter);
		mChapterList.add(chapter);
	}

	public void add(String chapterId, String displayname) {
		mChapterList.add(new Chapter(chapterId, displayname, mManga));
	}

	public void addAll(Collection<Chapter> chapters) {
		for (Chapter chapter : chapters) {
			mChapterList.add(chapter);
		}
	}

	public void insert(int position, Chapter chapter) {
		countType(chapter);
		setDynamicImgServers(chapter);
		mChapterList.add(position, chapter);
	}

	public void insert(int position, String chapterId, String displayname) {
		mChapterList.add(position, new Chapter(chapterId, displayname, mManga));
	}

	public Chapter getAt(int position) {
		Chapter chapter = mChapterList.get(position);
		setDynamicImgServers(chapter);
		return chapter;
	}

	public int size() {
		return mChapterList.size();
	}

	public ArrayList<Chapter> toArray() {
		return mChapterList;
	}

	private void countType(Chapter chapter) {
		switch (chapter.typeId) {
		case Chapter.TYPE_ID_VOLUME:
			sizeVolume++;
			break;
		case Chapter.TYPE_ID_CHAPTER:
			sizeChapter++;
			break;
		case Chapter.TYPE_ID_UNKNOW:
			sizeUnknow++;
			break;
		}
	}

	private void setDynamicImgServers(Chapter chapter) {
		if (!chapter.hasDynamicImgServers() && hasDynamicImgServers()) {
			chapter.setDynamicImgServers(dynamicImgServers);
		}
	}

	@Override
	public String toString() {
		return String
				.format("{ SiteId:%d, MangaId:'%s', Size:%d, Volume:%d, Chapter:%d, Unknow:%d, HasDynamicImgServers:%b }",
						mManga.siteId, mManga.mangaId, size(), sizeVolume, sizeChapter, sizeUnknow,
						hasDynamicImgServers());
	}

}
