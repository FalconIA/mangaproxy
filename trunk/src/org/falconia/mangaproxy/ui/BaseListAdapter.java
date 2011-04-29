package org.falconia.mangaproxy.ui;

import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;

public abstract class BaseListAdapter extends BaseAdapter implements
		OnScrollListener {
	private boolean mDisplaySectionHeaders = true;

	public boolean getDisplaySectionHeadersEnabled() {
		return this.mDisplaySectionHeaders;
	}

}
