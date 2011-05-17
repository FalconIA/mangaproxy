package org.falconia.mangaproxy;

import org.falconia.mangaproxy.data.Genre;
import org.falconia.mangaproxy.data.GenreList;
import org.falconia.mangaproxy.data.Site;
import org.falconia.mangaproxy.ui.BaseHeadersAdapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;

public final class ActivityGenreList extends ActivityBase {

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

	private final class GenreListAdapter extends BaseHeadersAdapter {

		final class ViewHolder {
			public TextView tvDisplayname;
		}

		private LayoutInflater mInflater;

		public GenreListAdapter() {
			mInflater = LayoutInflater.from(ActivityGenreList.this);
		}

		@Override
		public int getCount() {
			if (mGenreList == null) {
				return 0;
			}
			return mGenreList.size();
		}

		@Override
		public Genre getItem(int position) {
			return mGenreList.getAt(position);
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).genreId.hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.list_item_genre, null);
				holder.tvDisplayname = (TextView) convertView.findViewById(R.id.mtvDisplayname);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.tvDisplayname.setText(mGenreList.getDisplayname(position));

			return convertView;
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
				int totalItemCount) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub

		}

	}

	private static final String BUNDLE_KEY_GENRE_LIST = "BUNDLE_KEY_GENRE_LIST";

	private int mSiteId;
	private Site mSite;
	private GenreList mGenreList;

	@Override
	public int getSiteId() {
		return mSiteId;
	}

	@Override
	public String getSiteName() {
		return mSite.getName();
	}

	@Override
	String getSiteDisplayname() {
		return mSite.getDisplayname();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSiteId = IntentHandler.getSiteId(this);
		mSite = Site.get(mSiteId);

		setContentView(R.layout.activity_genre_list);
		setCustomTitle(getString(R.string.genre));

		// this.mShowProcessDialog = false;

		setupListView(new GenreListAdapter());

		if (mSite.hasSearchEngine()) {
			findViewById(R.id.mvgSearch).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.mvgSearch).setVisibility(View.GONE);
		}

		if (!mProcessed) {
			loadGenreList();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable(BUNDLE_KEY_GENRE_LIST, mGenreList);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		mGenreList = (GenreList) savedInstanceState.getSerializable(BUNDLE_KEY_GENRE_LIST);
		mListAdapter.notifyDataSetChanged();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_DOWNLOAD_ID:
			dialog = mSourceDownloader.createDownloadDialog(R.string.source_of_genre_list);
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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_base_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mmiRefresh:
			mProcessed = false;
			mSourceDownloader.download(mSite.getGenreListUrl());
			return true;
		case R.id.mmiPreferences:
			startActivity(new Intent(this, ActivityPreference.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Genre genre = mGenreList.getAt(position);
		ActivityMangaList.IntentHandler.startActivityMangaList(this, genre);
	}

	@Override
	public int onSourceProcess(String source) {
		mGenreList = mSite.getGenreList(source);
		return mGenreList.size();
	}

	@Override
	public void onPostSourceProcess(int result) {
		mListAdapter.notifyDataSetChanged();
		getListView().requestFocus();

		super.onPostSourceProcess(result);
	}

	private void loadGenreList() {
		mSourceDownloader = new SourceDownloader();
		mSourceDownloader.download(mSite.getGenreListUrl());
	}

}
