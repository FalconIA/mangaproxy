package org.falconia.mangaproxy.ui;

import org.falconia.mangaproxy.R;
import org.falconia.mangaproxy.data.Manga;
import org.falconia.mangaproxy.data.MangaList;
import org.falconia.mangaproxy.data.Site;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.TextView;

public class MangaListAdapter extends BaseListAdapter {

	class ViewHolderMangaList {
		public TextView tvDisplayname;
		public TextView tvChapterDisplayname;
		public TextView tvCompleted;
		public CheckBox cbFavorite;

		private ViewHolderMangaList() {
		}
	}

	private MangaList mMangaList;
	private LayoutInflater mInflater;

	public MangaListAdapter(Context context, int siteId) {
		// this.mMangaList = new MangaList(siteId);
		this.mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		if (this.mMangaList == null)
			return 0;
		return this.mMangaList.size();
	}

	@Override
	public Manga getItem(int position) {
		return this.mMangaList.getAt(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		switch (this.mMangaList.getSiteId()) {
		case (Site.SITE_ID_FAVORITE):
			return getFavoriteListView(position, convertView, parent);
		default:
			return getMangaListView(position, convertView, parent);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

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
			convertView = this.mInflater
					.inflate(R.layout.list_item_manga, null);
			holder.tvDisplayname = (TextView) convertView
					.findViewById(R.id.mtvDisplayname);
			holder.tvChapterDisplayname = (TextView) convertView
					.findViewById(R.id.mtvChapterDisplayname);
			holder.tvCompleted = (TextView) convertView
					.findViewById(R.id.mtvCompleted);
			holder.cbFavorite = (CheckBox) convertView
					.findViewById(R.id.mcbFavorite);
		} else
			holder = (ViewHolderMangaList) convertView.getTag();

		holder.tvDisplayname.setText(manga.displayName);
		holder.tvChapterDisplayname.setText(TextUtils
				.isEmpty(manga.chapterDisplayname) ? "-"
				: manga.chapterDisplayname);
		holder.tvCompleted.setVisibility(manga.isCompleted ? View.VISIBLE
				: View.GONE);
		// holder.cbFavorite.setChecked(manga.bIsFavorite);

		if (convertView.getTag() == null)
			convertView.setTag(holder);

		return convertView;
	}

	public void setMangaList(MangaList mangaList) {
		this.mMangaList = mangaList;
		notifyDataSetChanged();
	}

}
