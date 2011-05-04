package org.falconia.mangaproxy.data;

import java.io.Serializable;

import org.falconia.mangaproxy.plugin.IPlugin;
import org.falconia.mangaproxy.plugin.Plugins;

public final class Chapter implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int TYPE_ID_VOLUME = 0;
	public static final int TYPE_ID_CHAPTER = 1;
	public static final int TYPE_ID_UNKNOW = 2;

	public static final int IMG_SERVER_ID_NONE = -1;

	public final int siteId;
	public final Manga manga;
	public final String chapterId;
	public final String displayname;

	public int typeId = TYPE_ID_UNKNOW;

	private String[] dynamicImgServers;
	private int dynamicImgServerId = IMG_SERVER_ID_NONE;

	public Chapter(String chapterId, String displayname, Manga manga) {
		this.chapterId = chapterId;
		this.displayname = displayname;
		this.manga = manga;
		this.siteId = manga.siteId;
	}

	private IPlugin getPlugin() {
		return Plugins.getPlugin(this.siteId);
	}

	public String getUrl() {
		return getPlugin().getChapterUrl(this, this.manga);
	}

	public void setDynamicImgServers(String[] imgServers) {
		this.dynamicImgServers = imgServers;
	}

	public void setDynamicImgServerId(int imgServerId) {
		this.dynamicImgServerId = imgServerId;
	}

	public boolean hasDynamicImgServers() {
		return this.dynamicImgServers != null
				&& this.dynamicImgServers.length > 0;
	}

	public boolean hasDynamicImgServerId() {
		return this.dynamicImgServerId != IMG_SERVER_ID_NONE;
	}

	public boolean hasDynamicImgServer() {
		return hasDynamicImgServerId() && hasDynamicImgServers();
	}

	public String getDynamicImgServer() {
		if (!hasDynamicImgServer())
			throw new NullPointerException("Dynamic Img Server is not set.");
		return this.dynamicImgServers[this.dynamicImgServerId];
	}

	@Override
	public String toString() {
		return String.format("{%s:%s}", this.chapterId, this.displayname);
	}

	public String toLongString() {
		return String
				.format("{ SiteID:%d, MangaID:'%s', ChapterId:'%s', Name:'%s', TypeId:%d, ImgServerId:%d, ImgServers:%d }",
						this.siteId, this.manga.mangaId, this.chapterId,
						this.displayname, this.typeId, this.dynamicImgServerId,
						(this.dynamicImgServers == null ? 0
								: this.dynamicImgServers.length));
	}

}
