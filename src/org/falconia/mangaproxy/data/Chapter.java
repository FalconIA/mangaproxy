package org.falconia.mangaproxy.data;

import java.io.Serializable;

public class Chapter implements Serializable {

	private static final long serialVersionUID = 1L;

	public final int siteId;
	public final int chapterId;
	public final String displayname;
	public final String url;

	public Chapter(int chapterId, String displayname, String url, int siteId) {
		this.chapterId = chapterId;
		this.displayname = displayname;
		this.url = url;
		this.siteId = siteId;
	}

}
