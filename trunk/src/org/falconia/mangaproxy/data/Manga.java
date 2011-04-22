package org.falconia.mangaproxy.data;

import java.io.Serializable;

import android.graphics.Bitmap;

public class Manga implements Serializable, ISiteId {

	private static final long serialVersionUID = 3889024929248955233L;

	public final int iSiteId;
	public final int iMangaId;
	public final String sDisplayName;
	public final String sUrl;

	public String sChapterDisplayname;
	public String sUpdatedAt;
	public boolean bCompleted = false;
	public boolean bHasNewChapter = false;
	public boolean bIsFavorite = false;

	public transient Bitmap hExtraInfoCoverBitmap = null;
	public transient String sExtraInfoArtist = null;
	public transient String sExtraInfoAuthor = null;
	public transient String sExtraInfoGenre = null;
	public transient String sExtraInfoSummary = null;

	private Chapter mhLastReadChapter = null;
	private Chapter mhLatestChapter = null;

	public Manga(int mangaId, String displayname, String url, int siteId) {
		this.iMangaId = mangaId;
		this.sDisplayName = displayname;
		this.sUrl = url;
		this.iSiteId = siteId;
	}

	@Override
	public int getSiteId() {
		return this.iSiteId;
	}

	public int getIconDrawableId() {
		throw new RuntimeException("Invalid directory id.");
	}

	public void setLastReadChapter(Chapter chapter) {
		this.mhLastReadChapter = chapter;
	}

	public Chapter getLastReadChapter() {
		return this.mhLastReadChapter;
	}

	public String getLastReadChapterDisplayname() {
		if (this.mhLastReadChapter != null)
			return this.mhLastReadChapter.sDisplayName;
		else
			return null;
	}

	public void setLatestChapter(Chapter chapter) {
		this.mhLatestChapter = chapter;
	}

	public Chapter getLatestChapter() {
		return this.mhLatestChapter;
	}

	public String getLatestChapterDisplayname() {
		if (this.mhLatestChapter != null)
			return this.mhLatestChapter.sDisplayName;
		else
			return null;
	}

	public void resetExtraInfo() {
		this.sExtraInfoAuthor = null;
		this.sExtraInfoArtist = null;
		this.sExtraInfoGenre = null;
		this.sExtraInfoSummary = null;
		if (this.hExtraInfoCoverBitmap != null) {
			this.hExtraInfoCoverBitmap.recycle();
			this.hExtraInfoCoverBitmap = null;
		}
	}

}
