package org.falconia.mangaproxy.plugin;

import java.util.HashMap;

public class Plugins {
	private static HashMap<Integer, IPlugin> Plugins;

	static {
		int i = 1000;
		Plugins = new HashMap<Integer, IPlugin>();
		Plugins.put(i, new Plugin99770(i++));
	}

	public static Integer[] getPluginIds() {
		return Plugins.keySet().toArray(new Integer[0]);
	}

	public static IPlugin getPlugin(int id) {
		return Plugins.get(id);
	}
}
