package org.falconia.mangaproxy;

import org.falconia.mangaproxy.data.Site;
import org.falconia.mangaproxy.plugin.Plugins;
import org.falconia.mangaproxy.ui.MangaListAdapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;

public final class ActivityFavoriteList extends ActivityBase {

	@Override
	public String getSiteName() {
		return "";
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && !event.isLongPress()) {
			showDialog(DIALOG_CLOSE_ID);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_favorite_list);

		setupListView(new MangaListAdapter(this, Site.SITE_ID_FAVORITE));
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
		if (site.hasGenreList())
			ActivityGenreList.IntentHandler
					.startActivityGenreList(this, siteId);
		else
			ActivityMangaList.IntentHandler.startActivityAllMangaList(this,
					siteId);
	}

	private Dialog createExitConfirmDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.dialog_confirm_to_exit))
				.setCancelable(false)
				.setPositiveButton(getString(R.string.dialog_ok),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								ActivityFavoriteList.this.finish();
								System.exit(0);
							}
						})
				.setNegativeButton(getString(R.string.dialog_cancel),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		return builder.create();
	}

}
