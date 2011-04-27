package org.falconia.mangaproxy;

import org.falconia.mangaproxy.data.Genre;
import org.falconia.mangaproxy.data.GenreList;
import org.falconia.mangaproxy.data.Site;
import org.falconia.mangaproxy.task.GetSourceTask;
import org.falconia.mangaproxy.task.ProcessDataTask;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class ActivityGenreList extends ActivityBase {

	private static final int WHAT_COMPLETED = -1;

	public final static class BundleHandler {
		private static final String BUNDLE_KEY_SITE_ID = "BUNDLE_KEY_SITE_ID";

		public static Bundle getBundle(int siteId) {
			Bundle localBundle = new Bundle();
			localBundle.putInt(BUNDLE_KEY_SITE_ID, siteId);
			return localBundle;
		}

		public static int getSiteId(Bundle bundle) {
			return bundle.getInt(BUNDLE_KEY_SITE_ID);
		}
	}

	final class GenreListAdapter extends BaseAdapter {
		final class ViewHolder {
			public TextView tvDisplayname;
		}

		private GenreList mhGenreList;
		private LayoutInflater mhInflater;

		public GenreListAdapter() {
			this.mhGenreList = new GenreList(ActivityGenreList.this.miSiteId);
			this.mhInflater = LayoutInflater.from(ActivityGenreList.this);
		}

		@Override
		public int getCount() {
			return this.mhGenreList.size();
		}

		@Override
		public Genre getItem(int position) {
			return this.mhGenreList.getAt(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = this.mhInflater.inflate(R.layout.list_item_genre,
						null);
				holder.tvDisplayname = (TextView) convertView
						.findViewById(R.id.mtvDisplayname);
				holder.tvDisplayname.setText(this.mhGenreList
						.getDisplayname(position));
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
				holder.tvDisplayname.setText(this.mhGenreList
						.getDisplayname(position));
			}
			return convertView;
		}

		public void setGenreList(GenreList genreList) {
			this.mhGenreList = genreList;
			notifyDataSetChanged();
		}

	}

	private int miSiteId;
	private Site mhSite;
	private GenreList mhGenreList;

	private OnClickListener mhOnClick;
	private final OnItemClickListener mhOnListItemClick;

	private GenreListAdapter mhListAdapter;
	private ListView mlvListView;
	private EditText metSearch;

	public ActivityGenreList() {

		this.mhOnClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		};

		this.mhOnListItemClick = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub

			}
		};

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.miSiteId = BundleHandler.getSiteId(getIntent().getExtras());
		this.mhSite = Site.get(this.miSiteId);

		this.mhGetSourceTask = new GetSourceTask(this.miSiteId, this);
		this.mhProcessDataTask = new ProcessDataTask(this);
		// this.mbShowProcessDialog = false;

		setContentView(R.layout.activity_genre_list);
		setTitle(this.mhSite.getDisplayname() + " - "
				+ getString(R.string.genre));

		this.mhListAdapter = new GenreListAdapter();

		this.mlvListView = (ListView) findViewById(R.id.mlvListView);
		this.mlvListView.setEmptyView(findViewById(R.id.mtvNoItems));
		this.mlvListView.setOnItemClickListener(this.mhOnListItemClick);
		this.mlvListView.setAdapter(this.mhListAdapter);

		if (this.mhSite.hasSearchEngine()) {
			findViewById(R.id.mvgSearch).setVisibility(View.VISIBLE);
			this.metSearch = (EditText) findViewById(R.id.metSearch);
			findViewById(R.id.mbtnSearch).setOnClickListener(this.mhOnClick);
		} else
			findViewById(R.id.mvgSearch).setVisibility(View.GONE);

		loadGenreList();

		// showProgressView("Loading...");
		// mhListAdapter.setGenreList(mdSite.getGenreList());
		// hideProgressView();
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
	public void onPostProcess() {
		super.onPostProcess();
		this.mhListAdapter.setGenreList(this.mhGenreList);
	}

	@Override
	public void onProcess(String source) {
		this.mhGenreList = this.mhSite.getGenreList(source);
	}

	private void loadGenreList() {
		this.mhSite.getGenreListSource(this.mhGetSourceTask);
	}

}
