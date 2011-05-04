package org.falconia.mangaproxy.ui;

import org.falconia.mangaproxy.R;
import org.falconia.mangaproxy.data.Manga;
import org.falconia.mangaproxy.data.MangaList;
import org.falconia.mangaproxy.data.Site;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.TextView;

public final class MangaListAdapter extends BaseListAdapter {

	final class ViewHolder {
		public TextView tvDisplayname;
		public TextView tvDetails;
		public TextView tvCompleted;
		public CheckBox cbFavorite;
	}

	private MangaList mMangaList;
	private LayoutInflater mInflater;

	public MangaListAdapter(Context context) {
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
		return getItem(position).mangaId.hashCode();
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
		ViewHolder holder;

		if (convertView == null) {
			holder = new ViewHolder();
			convertView = this.mInflater
					.inflate(R.layout.list_item_manga, null);
			holder.tvDisplayname = (TextView) convertView
					.findViewById(R.id.mtvDisplayname);
			holder.tvDetails = (TextView) convertView
					.findViewById(R.id.mtvDetails);
			holder.tvCompleted = (TextView) convertView
					.findViewById(R.id.mtvCompleted);
			holder.cbFavorite = (CheckBox) convertView
					.findViewById(R.id.mcbFavorite);
		} else
			holder = (ViewHolder) convertView.getTag();

		holder.tvDisplayname.setText(manga.displayname);
		holder.tvDetails.setText(manga.getDetails());
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
