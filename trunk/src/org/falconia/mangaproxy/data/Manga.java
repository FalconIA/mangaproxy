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
		return Plugins.getPlugin(siteId);
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
		return mDetailsTemplate = template;
	}

	public String getDetails() {
		if (!TextUtils.isEmpty(mDetailsTemplate)) {
			String result = mDetailsTemplate;
			result = result.replaceAll("%chapterDisplayname%", chapterDisplayname);
			result = result.replaceAll("%chapterCount%", String.format(AppConst.UI_CHAPTER_COUNT,
					chapterCount == 0 ? "??" : chapterCount));
			result = result.replaceAll("%updatedAt%",
					String.format(AppConst.UI_LAST_UPDATE, updatedAt));
			result = result.replaceAll("%details%", details);
			return result;
		}
		return TextUtils.isEmpty(details) ? "-" : details;
	}

	// for Favorite

	public void setLastReadChapter(Chapter chapter) {
		mLastReadChapter = chapter;
	}

	public Chapter getLastReadChapter() {
		return mLastReadChapter;
	}

	public String getLastReadChapterDisplayname() {
		if (mLastReadChapter != null) {
			return mLastReadChapter.displayname;
		} else {
			return null;
		}
	}

	public void setLatestChapter(Chapter chapter) {
		mLatestChapter = chapter;
	}

	public Chapter getLatestChapter() {
		return mLatestChapter;
	}

	public String getLatestChapterDisplayname() {
		if (mLatestChapter != null) {
			return mLatestChapter.displayname;
		} else {
			return null;
		}
	}

	// for extra info

	public void resetExtraInfo() {
		extraInfoAuthor = null;
		extraInfoArtist = null;
		extraInfoGenre = null;
		extraInfoSummary = null;
		if (extraInfoCoverBitmap != null) {
			extraInfoCoverBitmap.recycle();
			extraInfoCoverBitmap = null;
		}
	}

	public ChapterList getChapterList(String source) {
		return getPlugin().getChapterList(source, this);
	}

	@Override
	public String toString() {
		return String.format("{%s:%s}", mangaId, displayname);
	}

	public String toLongString() {
		return String
				.format("{ SiteID:%d, MangaID:'%s', Name:'%s', Section:'%s', UpdatedAt:'%tF', Chapter:'%s', ChapterCount:%d, IsCompleted:%b, HasNewChapter:%b }",
						siteId, mangaId, displayname, section, updatedAt, chapterDisplayname,
						chapterCount, isCompleted, hasNewChapter);
	}

}
