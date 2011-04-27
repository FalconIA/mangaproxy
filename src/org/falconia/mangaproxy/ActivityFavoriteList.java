package org.falconia.mangaproxy;

import org.falconia.mangaproxy.data.Genre;
import org.falconia.mangaproxy.data.Site;
import org.falconia.mangaproxy.plugin.Plugins;
import org.falconia.mangaproxy.ui.MangaListAdapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

public final class ActivityFavoriteList extends ActivityBase {
	private OnItemClickListener mhOnListItemClick;
	private OnItemLongClickListener mhOnListItemLongClick;

	private ListView mlvListView;
	private MangaListAdapter mhListAdapter;

	public ActivityFavoriteList() {
		this.mhOnListItemClick = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub

			}
		};
		this.mhOnListItemLongClick = new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub

				return false;
			}
		};
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			showDialog(DIALOG_CLOSE_ID);
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_favorite_list);

		this.mhListAdapter = new MangaListAdapter(this, Site.SITE_ID_FAVORITE);

		this.mlvListView = (ListView) findViewById(R.id.mlvListView);
		this.mlvListView.setEmptyView(findViewById(R.id.mtvNoItems));
		this.mlvListView.setOnItemClickListener(this.mhOnListItemClick);
		this.mlvListView.setOnItemLongClickListener(this.mhOnListItemLongClick);
		this.mlvListView.setAdapter(this.mhListAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_favorite_list, menu);
		MenuItem menuSource = menu.findItem(R.id.mmiSource);
		SubMenu submenuSource = menuSource.getSubMenu();
		for (int pluginId : Plugins.getPluginIds())
			submenuSource.add(R.id.mmgSourceGroup, pluginId, Menu.NONE, Plugins
					.getPlugin(pluginId).getDisplayname());
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

	private void onSourceSelected(int siteId) {
		Site site = Site.get(siteId);
		if (site.hasGenreList()) {
			Intent i = new Intent(ActivityFavoriteList.this,
					ActivityGenreList.class);
			i.putExtras(ActivityGenreList.BundleHandler.getBundle(siteId));
			startActivity(i);
		} else {
			Genre genreAll = Genre.getGenreAll(siteId);
			Intent i = ActivityMangaList.IntentHandler.getIntent(
					ActivityFavoriteList.this, genreAll);
			startActivity(i);
		}
	}

	private Dialog createExitConfirmDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Confirm to exit?")
				.setCancelable(false)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						ActivityFavoriteList.this.finish();
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		return builder.create();
	}

}
