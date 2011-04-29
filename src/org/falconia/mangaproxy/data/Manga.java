package org.falconia.mangaproxy.data;

import java.io.Serializable;

import org.falconia.mangaproxy.plugin.IPlugin;
import org.falconia.mangaproxy.plugin.Plugins;

import android.graphics.Bitmap;

public class Manga implements Serializable {

	private static final long serialVersionUID = 1L;

	public final int siteId;
	public final int mangaId;
	public final String displayName;
	public final String section;

	public String chapterDisplayname;
	public String updatedAt;
	public boolean isCompleted = false;
	public boolean hasNewChapter = false;
	public boolean isFavorite = false;

	public transient Bitmap extraInfoCoverBitmap = null;
	public transient String extraInfoArtist = null;
	public transient String extraInfoAuthor = null;
	public transient String extraInfoGenre = null;
	public transient String extraInfoSummary = null;

	private Chapter mLastReadChapter = null;
	private Chapter mLatestChapter = null;

	public Manga(int mangaId, String displayname, String section, int siteId) {
		this.mangaId = mangaId;
		this.displayName = displayname;
		this.section = section;
		this.siteId = siteId;
	}

	private IPlugin getPlugin() {
		return Plugins.getPlugin(this.siteId);
	}

	public String getUrl() {
		return getPlugin().getMangaUrl(this.mangaId);
	}

	public int getIconDrawableId() {
		throw new RuntimeException("Invalid directory id.");
	}

	public void setLastReadChapter(Chapter chapter) {
		this.mLastReadChapter = chapter;
	}

	public Chapter getLastReadChapter() {
		return this.mLastReadChapter;
	}

	public String getLastReadChapterDisplayname() {
		if (this.mLastReadChapter != null)
			return this.mLastReadChapter.displayname;
		else
			return null;
	}

	public void setLatestChapter(Chapter chapter) {
		this.mLatestChapter = chapter;
	}

	public Chapter getLatestChapter() {
		return this.mLatestChapter;
	}

	public String getLatestChapterDisplayname() {
		if (this.mLatestChapter != null)
			return this.mLatestChapter.displayname;
		else
			return null;
	}

	public void resetExtraInfo() {
		this.extraInfoAuthor = null;
		this.extraInfoArtist = null;
		this.extraInfoGenre = null;
		this.extraInfoSummary = null;
		if (this.extraInfoCoverBitmap != null) {
			this.extraInfoCoverBitmap.recycle();
			this.extraInfoCoverBitmap = null;
		}
	}

}
