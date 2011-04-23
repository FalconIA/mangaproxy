package org.falconia.mangaproxy.menu;

import org.falconia.mangaproxy.data.Site;

import android.app.Activity;

public final class DialogSites extends ADialogMenuBase {

	public DialogSites(Activity activity,
			IOnMenuItemClickListener onMenuItemClickListener) {
		super(activity, onMenuItemClickListener);
		setMenuTitle("Sites");
	}

	@Override
	protected void generateMenuItems() {
		Integer[] siteIds = Site.getIds();
		for (int i = 0; i < siteIds.length; i++)
			addMenuItem(Site.get(siteIds[i]).getDisplayname(), 2130837520,
					siteIds[i]);
	}

}
