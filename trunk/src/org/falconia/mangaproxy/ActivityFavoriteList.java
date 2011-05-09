package org.falconia.mangaproxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.falconia.mangaproxy.data.Site;
import org.falconia.mangaproxy.plugin.Plugins;
import org.falconia.mangaproxy.ui.MangaListAdapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;

public final class ActivityFavoriteList extends ActivityBase {

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

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open(
					"alpha.txt")));
			StringBuilder builder = new StringBuilder();
			String text;
			while ((text = reader.readLine()) != null) {
				builder.append(text + AppCache.NEW_LINE);
			}
			reader.close();
			text = builder.toString().trim();
			if (!TextUtils.isEmpty(text)) {
				setNoItemsMessage(text);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		setupListView(new MangaListAdapter(this));
	}

	@Override
	public void onBackPressed() {
		showDialog(DIALOG_CLOSE_ID);
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
						ActivityFavoriteList.this.finish();
						System.exit(0);
					}
				}).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		return builder.create();
	}

}
