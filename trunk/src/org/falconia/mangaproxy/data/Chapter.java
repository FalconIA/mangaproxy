package org.falconia.mangaproxy.data;

public class Chapter {

	public final int iSiteId;
	public final int iChapterId;
	public final String sDisplayName;
	public final String sUrl;

	public Chapter(int chapterId, String displayname, String url, int siteId) {
		this.iChapterId = chapterId;
		this.sDisplayName = displayname;
		this.sUrl = url;
		this.iSiteId = siteId;
	}

}
