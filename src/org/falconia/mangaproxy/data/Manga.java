package org.falconia.mangaproxy.data;

import java.io.Serializable;
import java.util.GregorianCalendar;

import org.falconia.mangaproxy.App;
import org.falconia.mangaproxy.plugin.IPlugin;
import org.falconia.mangaproxy.plugin.Plugins;

import android.graphics.Bitmap;
import android.text.TextUtils;

public final class Manga implements Serializable {

	public static Manga getFavoriteManga(int siteId, String mangaId, String displayname,
			boolean isCompleted, int chapterCount, boolean hasNewChapter) {
		Manga manga = new Manga(mangaId, displayname, null, siteId);
		manga.isCompleted = isCompleted;
		manga.chapterCount = chapterCount;
		manga.hasNewChapter = hasNewChapter;
		manga.isFavorite = true;
		return manga;
	}

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

	// for Intent
	public ChapterList chapterList = null;

	// for Favorite
	public boolean isFavorite = false;
	public boolean hasNewChapter = false;

	private Chapter lastReadChapter = null;
	private Chapter latestChapter = null;

	// for Extra Info
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

	public int getId() {
		return String.format("%s - %s", getSiteName(), mangaId).hashCode();
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
			result = result.replaceAll("%chapterCount%",
					String.format(App.UI_CHAPTER_COUNT, chapterCount == 0 ? "??" : chapterCount));
			result = result.replaceAll("%updatedAt%", String.format(App.UI_LAST_UPDATE, updatedAt));
			result = result.replaceAll("%details%", details);
			return result;
		}
		return TextUtils.isEmpty(details) ? "-" : details;
	}

	// for Favorite

	public void setLastReadChapter(Chapter chapter) {
		// TODO Update database
		lastReadChapter = chapter;
	}

	public Chapter getLastReadChapter() {
		return lastReadChapter;
	}

	public String getLastReadChapterId() {
		if (lastReadChapter == null) {
			return null;
		}
		return lastReadChapter.chapterId;
	}

	public String getLastReadChapterDisplayname() {
		if (lastReadChapter == null) {
			return null;
		}
		return lastReadChapter.displayname;
	}

	public void setLatestChapter(Chapter chapter) {
		// TODO Update database
		latestChapter = chapter;
	}

	public Chapter getLatestChapter() {
		return latestChapter;
	}

	public String getLatestChapterId() {
		if (latestChapter == null) {
			return null;
		}
		return latestChapter.chapterId;
	}

	public String getLatestChapterDisplayname() {
		if (latestChapter == null) {
			return null;
		}
		return latestChapter.displayname;
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
