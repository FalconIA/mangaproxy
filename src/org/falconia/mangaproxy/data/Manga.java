package org.falconia.mangaproxy.data;

import java.io.Serializable;
import java.util.GregorianCalendar;

import org.falconia.mangaproxy.AppConst;
import org.falconia.mangaproxy.plugin.IPlugin;
import org.falconia.mangaproxy.plugin.Plugins;

import android.graphics.Bitmap;
import android.text.TextUtils;

public final class Manga implements Serializable {

	private static final long serialVersionUID = 1L;

	public final int siteId;
	public final String mangaId;
	public final String displayname;

	public String section;
	public boolean isCompleted = false;
	public GregorianCalendar updatedAt;
	public int chapterCount;
	public String chapterDisplayname;
	public String details;

	private String mDetailsTemplate;

	// for Favorite
	public boolean isFavorite = false;
	public boolean hasNewChapter = false;

	private Chapter mLastReadChapter = null;
	private Chapter mLatestChapter = null;

	// for extra info
	public transient Bitmap extraInfoCoverBitmap = null;
	public transient String extraInfoArtist = null;
	public transient String extraInfoAuthor = null;
	public transient String extraInfoGenre = null;
	public transient String extraInfoSummary = null;

	public Manga(String mangaId, String displayname, String section, int siteId) {
		this.mangaId = mangaId;
		this.displayname = displayname;
		this.section = section;
		this.siteId = siteId;
	}

	private IPlugin getPlugin() {
		return Plugins.getPlugin(this.siteId);
	}

	public String getSiteName() {
		return getPlugin().getName();
	}

	public String getSiteDisplayname() {
		return getPlugin().getDisplayname();
	}

	public String getUrl() {
		return getPlugin().getMangaUrl(this);
	}

	public boolean isDynamicImgServer() {
		return getPlugin().isDynamicImgServer();
	}

	public String setDetailsTemplate(String template) {
		return this.mDetailsTemplate = template;
	}

	public String getDetails() {
		if (!TextUtils.isEmpty(this.mDetailsTemplate)) {
			String result = this.mDetailsTemplate;
			result = result.replaceAll("%chapterDisplayname%",
					this.chapterDisplayname);
			result = result.replaceAll("%chapterCount%", String.format(
					AppConst.UI_CHAPTER_COUNT, this.chapterCount == 0 ? "??"
							: this.chapterCount));
			result = result.replaceAll("%updatedAt%",
					String.format(AppConst.UI_LAST_UPDATE, this.updatedAt));
			result = result.replaceAll("%details%", this.details);
			return result;
		}
		return TextUtils.isEmpty(this.details) ? "-" : this.details;
	}

	// for Favorite

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

	// for extra info

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

	public ChapterList getChapterList(String source) {
		return getPlugin().getChapterList(source, this);
	}

	@Override
	public String toString() {
		return String.format("{%s:%s}", this.mangaId, this.displayname);
	}

	public String toLongString() {
		return String
				.format("{ SiteID:%d, MangaID:'%s', Name:'%s', Section:'%s', UpdatedAt:'%tF', Chapter:'%s', ChapterCount:%d, IsCompleted:%b, HasNewChapter:%b }",
						this.siteId, this.mangaId, this.displayname,
						this.section, this.updatedAt, this.chapterDisplayname,
						this.chapterCount, this.isCompleted, this.hasNewChapter);
	}

}
