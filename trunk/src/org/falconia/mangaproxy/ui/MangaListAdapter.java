package org.falconia.mangaproxy.ui;

import org.falconia.mangaproxy.R;
import org.falconia.mangaproxy.data.Manga;
import org.falconia.mangaproxy.data.MangaList;
import org.falconia.mangaproxy.data.Site;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MangaListAdapter extends BaseAdapter {

	class ViewHolderMangaList {
		public TextView tvDisplayname;
		public TextView tvChapterDisplayname;
		public TextView tvCompleted;

		private ViewHolderMangaList() {
		}
	}

	private MangaList mhMangaList;

	private final Context mhContext;
	private LayoutInflater mhInflater;

	public MangaListAdapter(Context context, int siteId) {
		this.mhMangaList = new MangaList(siteId);

		this.mhContext = context;
		this.mhInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return this.mhMangaList.size();
	}

	@Override
	public Manga getItem(int position) {
		return this.mhMangaList.getAt(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		switch (this.mhMangaList.getSiteId()) {
		case (Site.SITE_ID_FAVORITE):
			return getFavoriteListView(position, convertView, parent);
		default:
			return getMangaListView(position, convertView, parent);
		}
	}

	public View getFavoriteListView(int position, View convertView,
			ViewGroup parent) {
		// TODO Auto-generated method stub

		return null;
	}

	public View getMangaListView(int position, View convertView,
			ViewGroup parent) {
		Manga manga = getItem(position);
		ViewHolderMangaList holder;
		if (convertView == null) {
			holder = new ViewHolderMangaList();
			convertView = this.mhInflater.inflate(R.layout.list_item_manga,
					null);
			holder.tvDisplayname = (TextView) convertView
					.findViewById(R.id.mtvDisplayname);
			holder.tvChapterDisplayname = (TextView) convertView
					.findViewById(R.id.mtvChapterDisplayname);
			holder.tvCompleted = (TextView) convertView
					.findViewById(R.id.mtvCompleted);
			holder.tvDisplayname.setText(manga.sDisplayName);
			holder.tvChapterDisplayname.setText(manga.sChapterDisplayname);
			holder.tvCompleted.setVisibility(manga.bIsCompleted ? View.VISIBLE
					: View.GONE);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolderMangaList) convertView.getTag();
			holder.tvDisplayname.setText(manga.sDisplayName);
			holder.tvChapterDisplayname.setText(manga.sChapterDisplayname);
			holder.tvCompleted.setVisibility(manga.bIsCompleted ? View.VISIBLE
					: View.GONE);
		}

		return convertView;
	}

	public void onDestroy() {
		// TODO Auto-generated method stub

	}

	public void setMangaList(MangaList mangaList) {
		this.mhMangaList = mangaList;
		notifyDataSetChanged();
	}

}
