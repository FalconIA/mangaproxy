package org.falconia.mangaproxy.data;

import java.io.Serializable;
import java.util.HashMap;

import org.falconia.mangaproxy.plugin.IPlugin;
import org.falconia.mangaproxy.plugin.Plugins;

public final class Site implements Serializable {

	private static final long serialVersionUID = 1L;

	public final static int SITE_ID_FAVORITE = -1;

	private final static HashMap<Integer, Site> mSites;

	static {
		mSites = new HashMap<Integer, Site>();
		Integer[] ids = Plugins.getPluginIds();
		for (int i = 0; i < ids.length; i++)
			mSites.put(ids[i], new Site(Plugins.getPlugin(ids[i])));
	}

	public static boolean contains(int id) {
		return mSites.containsKey(id);
	}

	public static Site get(int id) {
		return mSites.get(id);
	}

	public static Integer[] getIds() {
		return mSites.keySet().toArray(new Integer[0]);
	}

	private final IPlugin mPlugin;
	private final int mSiteId;

	public Site(IPlugin plugin) {
		this.mPlugin = plugin;
		this.mSiteId = plugin.getSiteId();
	}

	public String getName() {
		return this.mPlugin.getName();
	}

	public String getDisplayname() {
		return this.mPlugin.getDisplayname();
	}

	public String getGenreListUrl() {
		return this.mPlugin.getGenreListUrl();
	}

	public GenreList getGenreList(String source) {
		GenreList list = new GenreList(this.mSiteId);
		GenreList listParsed = this.mPlugin.getGenreList(source);
		if (listParsed != null && listParsed.size() > 0) {
			list.add(Genre.getGenreAll(this.mSiteId));
			list.addAll(listParsed.toArray());
		}

		return list;
	}

	public int getSiteId() {
		return this.mSiteId;
	}

	public boolean hasGenreList() {
		return this.mPlugin.hasGenreList();
	}

	public boolean hasSearchEngine() {
		return this.mPlugin.hasSearchEngine();
	}

}
