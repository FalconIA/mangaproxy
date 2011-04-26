package org.falconia.mangaproxy;

import java.util.HashMap;

import org.falconia.mangaproxy.data.Genre;
import org.falconia.mangaproxy.data.Manga;
import org.falconia.mangaproxy.data.MangaListAdapter;
import org.falconia.mangaproxy.data.Site;
import org.falconia.mangaproxy.helper.ProgressView;
import org.falconia.mangaproxy.menu.DialogSites;
import org.falconia.mangaproxy.menu.IOnMenuItemClickListener;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

public final class ActivityFavoriteList extends AActivityBase {
	private static final int DIALOG_ID_CONFIRM_REMOVE_FROM_FAVORITE = 1;
	private static final int DIALOG_ID_CONTEXT_MENU = 0;
	private static final int DIALOG_ID_SELECT_SITE = 3;
	private static final int DIALOG_ID_VIEW_SUMMARY = 2;

	private final HashMap<Integer, Dialog> marrDialog;

	private IOnMenuItemClickListener mhOnContextMenuItemClick;
	private OnItemClickListener mhOnListItemClick;
	private OnItemLongClickListener mhOnListItemLongClick;
	private final OnClickListener mhOnClick;

	private int miSelectedItem;

	private ListView mlvListView;
	private MangaListAdapter mhListAdapter;

	public ActivityFavoriteList() {
		this.marrDialog = new HashMap<Integer, Dialog>();

		this.mhOnContextMenuItemClick = new IOnMenuItemClickListener() {
			@Override
			public void onMenuItemClick(int menuId) {
				if (Site.contains(menuId)) {
					Site site = Site.get(menuId);
					if (site.hasGenreList()) {
						Intent i = new Intent(ActivityFavoriteList.this,
								ActivityGenreList.class);
						i.putExtras(ActivityGenreList.BundleHandler
								.getBundle(menuId));
						startActivity(i);
					} else {
						Genre genreAll = Genre.getGenreAll(menuId);
						Intent i = ActivityMangaList.IntentHandler.getIntent(
								ActivityFavoriteList.this, genreAll);
						startActivity(i);
					}
				}
			}
		};

		this.mhOnListItemClick = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Manga manga = ActivityFavoriteList.this.mhListAdapter
						.getItem(position);
				startActivityManga(manga, false);
			}
		};

		this.mhOnListItemLongClick = new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				ActivityFavoriteList.this.miSelectedItem = position;
				getMyDialog(DIALOG_ID_CONTEXT_MENU);
				showMyDialog(DIALOG_ID_CONTEXT_MENU);
				return false;
			}
		};

		this.mhOnClick = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.mbtnBrowse:
					showMyDialog(DIALOG_ID_SELECT_SITE);
					break;
				case R.id.mbtnUpdate:
				case R.id.mbtnContinue:
				}
			}
		};

		this.miSelectedItem = -1;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_favorite_list);
		setProgressView((ProgressView) findViewById(R.id.mvgProgress));

		this.mhListAdapter = new MangaListAdapter(this, Site.SITE_ID_FAVORITE);

		this.mlvListView = (ListView) findViewById(R.id.mlvList);
		this.mlvListView.setOnItemClickListener(this.mhOnListItemClick);
		this.mlvListView.setOnItemLongClickListener(this.mhOnListItemLongClick);
		this.mlvListView.setAdapter(this.mhListAdapter);

		findViewById(R.id.mbtnBrowse).setOnClickListener(this.mhOnClick);
		findViewById(R.id.mbtnUpdate).setOnClickListener(this.mhOnClick);
		findViewById(R.id.mbtnContinue).setOnClickListener(this.mhOnClick);
	}

	private Dialog getMyDialog(int dialogId) {
		switch (dialogId) {
		case DIALOG_ID_CONTEXT_MENU:
			break;
		case DIALOG_ID_CONFIRM_REMOVE_FROM_FAVORITE:
			break;
		case DIALOG_ID_VIEW_SUMMARY:
			break;
		case DIALOG_ID_SELECT_SITE:
			DialogSites dialogDirectory = (DialogSites) this.marrDialog
					.get(dialogId);
			if (dialogDirectory == null) {
				dialogDirectory = new DialogSites(this,
						this.mhOnContextMenuItemClick);
				this.marrDialog.put(dialogId, dialogDirectory);
			}
			return dialogDirectory;
		}
		return null;
	}

	private void showMyDialog(int dialogId) {
		switch (dialogId) {
		case DIALOG_ID_CONTEXT_MENU:
			// Manga manga = this.mhListAdapter.getItem(this.miSelectedItem);
			break;
		case DIALOG_ID_CONFIRM_REMOVE_FROM_FAVORITE:
			break;
		case DIALOG_ID_VIEW_SUMMARY:
			break;
		case DIALOG_ID_SELECT_SITE:
			((DialogSites) getMyDialog(DIALOG_ID_SELECT_SITE)).show();
			break;
		}
	}

	private void startActivityManga(Manga manga, boolean bool) {
		// Intent i = ActivityChapter.IntentHandler.getIntent(this, manga,
		// bool);
		// startActivity(i);
	}

}
