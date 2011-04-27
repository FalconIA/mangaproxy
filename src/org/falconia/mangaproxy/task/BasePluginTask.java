package org.falconia.mangaproxy.task;

import org.falconia.mangaproxy.plugin.IPlugin;
import org.falconia.mangaproxy.plugin.Plugins;

import android.os.AsyncTask;

public abstract class BasePluginTask<T1, T2, T3> extends AsyncTask<T1, T2, T3> {

	protected final IPlugin mhPlugin;

	public BasePluginTask(IPlugin plugin) {
		this.mhPlugin = plugin;
	}

	public BasePluginTask(int pluginId) {
		this(Plugins.getPlugin(pluginId));
	}

}
