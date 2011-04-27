package org.falconia.mangaproxy.data;

import java.util.HashMap;

import org.falconia.mangaproxy.plugin.IPlugin;
import org.falconia.mangaproxy.plugin.Plugins;
import org.falconia.mangaproxy.task.GetSourceTask;

public class Site {

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

	private IPlugin mhPlugin;
	private int miSiteId;

	public Site(IPlugin plugin) {
		this.mhPlugin = plugin;
		this.miSiteId = plugin.getSiteId();
	}

	public String getDisplayname() {
		return this.mhPlugin.getDisplayname();
	}

	public void getGenreListSource(GetSourceTask task) {
		task.execute(this.mhPlugin.getGenreListUrl());
	}

	public GenreList getGenreList(String source) {
		GenreList list = new GenreList(this.miSiteId);
		list.add(Genre.getGenreAll(this.miSiteId));
		list.addAll(this.mhPlugin.getGenreList(source).toArray());

		return list;
	}

	public int getSiteId() {
		return this.miSiteId;
	}

	public boolean hasGenreList() {
		return this.mhPlugin.hasGenreList();
	}

	public boolean hasSearchEngine() {
		return this.mhPlugin.hasSearchEngine();
	}

}
