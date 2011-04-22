package org.falconia.mangaproxy;

import org.falconia.mangaproxy.data.Genre;
import org.falconia.mangaproxy.data.MangaList;
import org.falconia.mangaproxy.data.MangaListAdapter;
import org.falconia.mangaproxy.helper.ProgressView;
import org.falconia.mangaproxy.menu.IOnMenuItemClickListener;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class ActivityMangaList extends AActivityBase {
	private static final int DIALOG_ID_CONTEXT_MENU = 0;
	private static final int DIALOG_ID_CONFIRM_REMOVE_FROM_FAVORITE = 1;
	private static final int DIALOG_ID_VIEW_SUMMARY = 2;

	public final static class IntentHandler {

		private static final String BUNDLE_KEY_GENRE_DATA = "BUNDLE_KEY_GENRE_DATA";

		// @Deprecated
		public static Intent getIntent(Context context, Genre genre) {
			Bundle bundle = new Bundle();
			bundle.putSerializable(BUNDLE_KEY_GENRE_DATA, genre);
			Intent i = new Intent(context, ActivityMangaList.class);
			i.putExtras(bundle);
			return i;
		}

		protected static Genre getGenre(ActivityMangaList activity) {
			return (Genre) activity.getIntent().getExtras()
					.getSerializable(BUNDLE_KEY_GENRE_DATA);
		}

	}

	private Genre mhGenre;
	private MangaList mhMangaList;
	private int miPageIndexCurrent;
	private int miPageIndexMax;

	private OnItemClickListener mhOnListItemClick;
	private OnItemLongClickListener mhOnListItemLongClick;
	private OnScrollListener mhOnScroll;
	// private FadeAnimation mhFadeAnim;
	// private final IOnFadeEndListener mhOnFadeInEnd;
	// private final IOnFadeEndListener mhOnFadeOutEnd;
	private TextWatcher mhTextWatcher;
	private OnClickListener mhOnClick;
	private final IOnMenuItemClickListener mhOnContextMenuItemClick;

	private MangaListAdapter mhListAdapter;
	private ListView mlvListView;
	private TextView mtvIndex;
	private EditText metSearch;
	private View mibtnReload;
	private View mvgPageIndex;
	private TextView mtvPageIndex;
	private View mbtnPageIndexPrev;
	private View mbtnPageIndexNext;

	public ActivityMangaList() {
		this.miPageIndexCurrent = 1;
		this.miPageIndexMax = 0;

		this.mhOnListItemClick = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub

			}
		};

		this.mhOnListItemLongClick = new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				return false;
			}
		};

		this.mhOnScroll = new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub

			}
		};

		this.mhTextWatcher = new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub

			}
		};

		this.mhOnClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		};

		this.mhOnContextMenuItemClick = new IOnMenuItemClickListener() {
			@Override
			public void onMenuItemClick(int menuId) {
				// TODO Auto-generated method stub

			}
		};

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manga_list);

		this.mhGenre = IntentHandler.getGenre(this);
		if (this.mhGenre == null)
			finish();

		setTitle(this.mhGenre.getDisplayname() + " - "
				+ getString(R.string.genre));
		setProgressView((ProgressView) findViewById(R.id.mvgProgress));

		this.mhListAdapter = new MangaListAdapter(this);

		this.mlvListView = (ListView) findViewById(R.id.mlvList);
		this.mlvListView.setOnItemClickListener(this.mhOnListItemClick);
		this.mlvListView.setOnItemLongClickListener(this.mhOnListItemLongClick);
		this.mlvListView.setOnScrollListener(this.mhOnScroll);
		this.mlvListView.setAdapter(mhListAdapter);
		this.mtvIndex = (TextView) findViewById(R.id.mtvIndex);
		this.metSearch = (EditText) findViewById(R.id.metSearch);
		this.metSearch.addTextChangedListener(this.mhTextWatcher);
		this.mibtnReload = findViewById(R.id.mibtnReload);
		this.mibtnReload.setOnClickListener(this.mhOnClick);
		this.mvgPageIndex = findViewById(R.id.mvgDirectoryPageIndex);
		this.mtvPageIndex = (TextView) findViewById(R.id.mtvDirectoryPageIndex);
		this.mbtnPageIndexPrev = findViewById(R.id.mbtnDirectoryPageIndexPrev);
		this.mbtnPageIndexPrev.setOnClickListener(this.mhOnClick);
		this.mbtnPageIndexNext = findViewById(R.id.mbtnDirectoryPageIndexNext);
		this.mbtnPageIndexNext.setOnClickListener(this.mhOnClick);

		loadMangaList();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.mhListAdapter.onDestroy();
	}

	private void loadMangaList() {
		showProgressView("Loading...");

		final Handler messageHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

			}
		};
		Runnable run = new Runnable() {
			@Override
			public void run() {
				mhMangaList = mhGenre.getMangaList(miPageIndexCurrent);

				if (miPageIndexMax == 0)
					miPageIndexMax = mhMangaList.getPageIndexMax();
			}
		};
		new Thread(run).start();
	}

}
