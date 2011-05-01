package org.falconia.mangaproxy.data;

import java.io.Serializable;
import java.util.GregorianCalendar;

import org.falconia.mangaproxy.plugin.IPlugin;
import org.falconia.mangaproxy.plugin.Plugins;

import android.graphics.Bitmap;
import android.text.TextUtils;

public class Manga implements Serializable {

	public static String UI_CHAPTER_COUNT = "Chapters: %s";
	public static String UI_LAST_UPDATE = "Update: %tF";

	private static final long serialVersionUID = 1L;

	public final int siteId;
	public final int mangaId;
	public final String displayname;

	public String section;

	public boolean isCompleted = false;
	public GregorianCalendar updatedAt;
	public int chapterCount;
	public String details;

	public String chapterDisplayname;
	public boolean isFavorite = false;
	public boolean hasNewChapter = false;

	public transient Bitmap extraInfoCoverBitmap = null;
	public transient String extraInfoArtist = null;
	public transient String extraInfoAuthor = null;
	public transient String extraInfoGenre = null;
	public transient String extraInfoSummary = null;

	private String detailsTemplate;
	private Chapter mLastReadChapter = null;
	private Chapter mLatestChapter = null;

	public Manga(int mangaId, String displayname, String section, int siteId) {
		this.mangaId = mangaId;
		this.displayname = displayname;
		this.section = section;
		this.siteId = siteId;
	}

	private IPlugin getPlugin() {
		return Plugins.getPlugin(this.siteId);
	}

	public String getUrl() {
		return getPlugin().getMangaUrl(this.mangaId);
	}

	public String getDetails() {
		if (!TextUtils.isEmpty(this.detailsTemplate)) {
			String result = this.detailsTemplate;
			result = result.replaceAll("%chapterDisplayname%",
					this.chapterDisplayname);
			result = result.replaceAll("%chapterCount%", String.format(
					UI_CHAPTER_COUNT, this.chapterCount == 0 ? "??"
							: this.chapterCount));
			result = result.replaceAll("%updatedAt%",
					String.format(UI_LAST_UPDATE, this.updatedAt));
			result = result.replaceAll("%details%", this.details);
			return result;
		}
		return TextUtils.isEmpty(this.details) ? "-" : this.details;
	}

	public String setDetailsTemplate(String template) {
		return this.detailsTemplate = template;
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

	@Override
	public String toString() {
		return String.format("{%d:%s}", this.mangaId, this.displayname);
	}

	public String toLongString() {
		return String
				.format("{ SiteID:%d, MangaID:%d, Name:'%s', Section:'%s', UpdatedAt:'%tF', Chapter:'%s', ChapterCount:%d, IsCompleted:%b, HasNewChapter:%b }",
						this.siteId, this.mangaId, this.displayname,
						this.section, this.updatedAt, this.chapterDisplayname,
						this.chapterCount, this.isCompleted, this.hasNewChapter);
	}
}
