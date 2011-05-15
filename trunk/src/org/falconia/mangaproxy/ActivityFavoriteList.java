package org.falconia.mangaproxy;

import org.falconia.mangaproxy.data.Manga;
import org.falconia.mangaproxy.data.Site;
import org.falconia.mangaproxy.plugin.Plugins;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;

public final class ActivityFavoriteList extends ActivityBase implements OnClickListener {

	private final class FavoriteListAdapter extends CursorAdapter {

		final class ViewHolder {
			public TextView tvDisplayname;
			public TextView tvDetails;
			public TextView tvCompleted;
			public CheckBox cbFavorite;
		}

		private LayoutInflater mInflater;

		public FavoriteListAdapter(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public Manga getItem(int position) {
			Cursor cursor = (Cursor) super.getItem(position);
			return App.DATABASE.getManga(cursor);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			Manga manga = App.DATABASE.getManga(cursor);
			ViewHolder holder = new ViewHolder();
			View view = mInflater.inflate(R.layout.list_item_manga, null);
			holder.tvDisplayname = (TextView) view.findViewById(R.id.mtvDisplayname);
			holder.tvDetails = (TextView) view.findViewById(R.id.mtvDetails);
			holder.tvCompleted = (TextView) view.findViewById(R.id.mtvCompleted);
			holder.cbFavorite = (CheckBox) view.findViewById(R.id.mcbFavorite);
			holder.cbFavorite.setOnClickListener(ActivityFavoriteList.this);
			view.setTag(holder);

			holder.tvDisplayname.setText(manga.displayname);
			if (TextUtils.isEmpty(manga.latestChapterDisplayname)) {
				holder.tvDetails.setText("-");
			} else {
				holder.tvDetails.setText(manga.latestChapterDisplayname);
			}
			if (manga.hasNewChapter) {
				holder.tvDetails.setTextColor(getResources().getColor(R.color.highlight));
			} else {
				holder.tvDetails.setTextColor(getResources().getColor(
						android.R.color.primary_text_dark));
			}
			holder.tvCompleted.setVisibility(manga.isCompleted ? View.VISIBLE : View.GONE);
			holder.cbFavorite.setChecked(manga.isFavorite);
			holder.cbFavorite.setTag(manga);

			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			Manga manga = App.DATABASE.getManga(cursor);
			ViewHolder holder = (ViewHolder) view.getTag();

			holder.tvDisplayname.setText(manga.displayname);
			if (TextUtils.isEmpty(manga.latestChapterDisplayname)) {
				holder.tvDetails.setText("-");
			} else {
				holder.tvDetails.setText(manga.latestChapterDisplayname);
			}
			if (manga.hasNewChapter) {
				holder.tvDetails.setTextColor(getResources().getColor(R.color.highlight));
			} else {
				holder.tvDetails.setTextColor(getResources().getColor(
						android.R.color.primary_text_dark));
			}
			holder.tvCompleted.setVisibility(manga.isCompleted ? View.VISIBLE : View.GONE);
			holder.cbFavorite.setChecked(manga.isFavorite);
			holder.cbFavorite.setTag(manga);
		}

		@Override
		public void notifyDataSetInvalidated() {
			Cursor cursor = mDB.getMangasCursor(null, null);
			changeCursor(cursor);
			super.notifyDataSetInvalidated();
		}

	}

	private AppSQLite mDB;

	private boolean mExit = false;

	@Override
	public int getSiteId() {
		return Site.SITE_ID_FAVORITE;
	}

	@Override
	public String getSiteName() {
		return "";
	}

	@Override
	String getSiteDisplayname() {
		return "";
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_favorite_list);
		setNoItemsMessage(R.string.ui_no_favorite_items);

		mDB = App.DATABASE.open();
		setupListView(new FavoriteListAdapter(this, null, true));
	}

	@Override
	protected void onResume() {
		super.onResume();

		mListAdapter.notifyDataSetInvalidated();
	}

	@Override
	protected void onStop() {
		super.onStop();

		mDB.close();
	}

	@Override
	protected void onRestart() {
		super.onRestart();

		if (!mDB.isOpen()) {
			mDB = App.DATABASE.open();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mExit) {
			System.exit(0);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_CLOSE_ID:
			dialog = createExitConfirmDialog();
			break;
		case DIALOG_LOADING_ID:
			dialog = createLoadingDialog(null);
			break;
		default:
			dialog = super.onCreateDialog(id);
		}
		return dialog;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_favorite_list, menu);
		MenuItem menuSource = menu.findItem(R.id.mmiSource);
		SubMenu submenuSource = menuSource.getSubMenu();
		for (int pluginId : Plugins.getPluginIds()) {
			submenuSource.add(R.id.mmgSourceGroup, pluginId, Menu.NONE, Plugins.getPlugin(pluginId)
					.getDisplayname());
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getGroupId()) {
		case R.id.mmgSourceGroup:
			onSourceSelected(item.getItemId());
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		showDialog(DIALOG_CLOSE_ID);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Manga manga = (Manga) mListAdapter.getItem(position);
		ActivityChapterList.IntentHandler.startActivityMangaList(this, manga);
	}

	@Override
	public void onClick(View view) {
		// For favorite CheckBox in Manga item
		if (view instanceof CheckBox) {
			final CheckBox button = (CheckBox) view;
			final Manga manga = (Manga) button.getTag();
			if (button.isChecked()) {
				modifiedFavorite(manga, button.isChecked());
			} else {
				button.toggle();
				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == DialogInterface.BUTTON_POSITIVE) {
							button.toggle();
							modifiedFavorite(manga, button.isChecked());
						}
					}
				};
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.dialog_confirm_to_remove_favorite).setCancelable(true)
						.setPositiveButton(R.string.dialog_ok, listener)
						.setNegativeButton(R.string.dialog_cancel, listener).show();
			}
		}
	}

	private void onSourceSelected(int siteId) {
		Site site = Site.get(siteId);
		if (site.hasGenreList()) {
			ActivityGenreList.IntentHandler.startActivityGenreList(this, siteId);
		} else {
			ActivityMangaList.IntentHandler.startActivityAllMangaList(this, siteId);
		}
	}

	private Dialog createExitConfirmDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.dialog_confirm_to_exit).setCancelable(false)
				.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						finish();
						mExit = true;
					}
				}).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		return builder.create();
	}

	private void modifiedFavorite(Manga manga, boolean add) {
		if (add) {
			AppUtils.logI(this, "Add to Favorite.");
			try {
				long id = mDB.insertManga(manga);
				AppUtils.logW(this, "Add as ID " + id + ".");
			} catch (SQLException e) {
				AppUtils.logE(this, e.getMessage());
			}
		} else {
			AppUtils.logI(this, "Remove from Favorite.");
			int deleted;
			if ((deleted = mDB.deleteManga(manga)) == 0) {
				AppUtils.logE(this, "Remove none.");
			} else {
				AppUtils.logW(this, "Remove " + deleted + " mangas.");
			}
		}
		mListAdapter.notifyDataSetInvalidated();
	}

}
