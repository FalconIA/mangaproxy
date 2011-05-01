package org.falconia.mangaproxy;

import org.falconia.mangaproxy.data.Genre;
import org.falconia.mangaproxy.data.GenreList;
import org.falconia.mangaproxy.data.Site;
import org.falconia.mangaproxy.task.GetSourceTask;
import org.falconia.mangaproxy.ui.BaseListAdapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;

public class ActivityGenreList extends ActivityBase {

	public final static class IntentHandler {

		private static final String BUNDLE_KEY_SITE_ID = "BUNDLE_KEY_SITE_ID";

		private static Intent getIntent(Context context, int siteId) {
			Bundle bundle = new Bundle();
			bundle.putInt(BUNDLE_KEY_SITE_ID, siteId);
			Intent i = new Intent(context, ActivityGenreList.class);
			i.putExtras(bundle);
			return i;
		}

		protected static int getSiteId(ActivityGenreList activity) {
			return activity.getIntent().getExtras().getInt(BUNDLE_KEY_SITE_ID);
		}

		public static void startActivityGenreList(Context context, int siteId) {
			context.startActivity(getIntent(context, siteId));
		}

	}

	final class GenreListAdapter extends BaseListAdapter {
		final class ViewHolder {
			public TextView tvDisplayname;
		}

		private GenreList mGenreList;
		private LayoutInflater mInflater;

		public GenreListAdapter() {
			// this.mGenreList = new GenreList(ActivityGenreList.this.mSiteId);
			this.mInflater = LayoutInflater.from(ActivityGenreList.this);
		}

		@Override
		public int getCount() {
			if (this.mGenreList == null)
				return 0;
			return this.mGenreList.size();
		}

		@Override
		public Genre getItem(int position) {
			return this.mGenreList.getAt(position);
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).genreId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = this.mInflater.inflate(R.layout.list_item_genre,
						null);
				holder.tvDisplayname = (TextView) convertView
						.findViewById(R.id.mtvDisplayname);
				holder.tvDisplayname.setText(this.mGenreList
						.getDisplayname(position));
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
				holder.tvDisplayname.setText(this.mGenreList
						.getDisplayname(position));
			}
			return convertView;
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

		public void setGenreList(GenreList genreList) {
			this.mGenreList = genreList;
			notifyDataSetChanged();
		}

	}

	protected static final String BUNDLE_KEY_GENRE_LIST = "BUNDLE_KEY_GENRE_LIST";

	private int mSiteId;
	private Site mSite;
	private GenreList mGenreList;

	@Override
	public String getSiteName() {
		return this.mSite.getName();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.mSiteId = IntentHandler.getSiteId(this);
		this.mSite = Site.get(this.mSiteId);

		setContentView(R.layout.activity_genre_list);
		setTitle(String.format("%s - %s", this.mSite.getDisplayname(),
				getString(R.string.genre)));

		// this.mShowProcessDialog = false;

		setupListView(new GenreListAdapter());

		if (this.mSite.hasSearchEngine())
			findViewById(R.id.mvgSearch).setVisibility(View.VISIBLE);
		else
			findViewById(R.id.mvgSearch).setVisibility(View.GONE);

		if (!this.mProcessed)
			loadGenreList();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(BUNDLE_KEY_GENRE_LIST, this.mGenreList);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		this.mGenreList = (GenreList) savedInstanceState
				.getSerializable(BUNDLE_KEY_GENRE_LIST);
		((GenreListAdapter) this.mListAdapter).setGenreList(this.mGenreList);
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_DOWNLOAD_ID:
			dialog = createDownloadDialog(R.string.source_of_genre_list);
			break;
		case DIALOG_PROCESS_ID:
			dialog = createProcessDialog(R.string.source_of_genre_list);
			break;
		default:
			dialog = super.onCreateDialog(id);
		}
		return dialog;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Genre genre = this.mGenreList.getAt(position);
		ActivityMangaList.IntentHandler.startActivityMangaList(this, genre);
	}

	@Override
	public int onSourceProcess(String source) {
		this.mGenreList = this.mSite.getGenreList(source);
		return this.mGenreList.size();
	}

	@Override
	public void onPostSourceProcess(int result) {
		((GenreListAdapter) this.mListAdapter).setGenreList(this.mGenreList);
		getListView().requestFocus();

		super.onPostSourceProcess(result);
	}

	private void loadGenreList() {
		this.mGetSourceTask = new GetSourceTask(this.mSiteId, this);
		this.mSite.getGenreListSource(this.mGetSourceTask);
	}

}
