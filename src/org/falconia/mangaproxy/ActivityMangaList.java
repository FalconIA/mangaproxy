package org.falconia.mangaproxy;

import org.falconia.mangaproxy.data.Genre;
import org.falconia.mangaproxy.data.MangaList;
import org.falconia.mangaproxy.task.GetSourceTask;
import org.falconia.mangaproxy.task.OnDownloadListener;
import org.falconia.mangaproxy.task.OnSourceProcessListener;
import org.falconia.mangaproxy.task.SourceProcessTask;
import org.falconia.mangaproxy.ui.MangaListAdapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ActivityMangaList extends ActivityBase {

	public final static class IntentHandler {

		private static final String BUNDLE_KEY_GENRE_DATA = "BUNDLE_KEY_GENRE_DATA";

		private static Intent getIntent(Context context, Genre genre) {
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

		public static void startActivityMangaList(Context context, Genre genre) {
			context.startActivity(getIntent(context, genre));
		}

		public static void startActivityAllMangaList(Context context, int siteId) {
			context.startActivity(getIntent(context, Genre.getGenreAll(siteId)));
		}

	}

	private final class LoadNextPageItem implements OnDownloadListener,
			OnSourceProcessListener {

		public static final int MODE_DEFAULT = 0;
		public static final int MODE_DOWNLOAD = 1;
		public static final int MODE_PROCESS = 2;
		public static final int MODE_DOWNLOAD_ERROR = 3;
		public static final int MODE_PROCESS_ERROR = 4;

		private View mListItem;
		private ProgressBar mProgress;
		private TextView mMessage;
		private TextView mDescribe;

		private int mMode;

		public LoadNextPageItem() {
			this.mListItem = getLayoutInflater().inflate(
					R.layout.list_item_load, null);
			this.mProgress = (ProgressBar) this.mListItem
					.findViewById(R.id.mpbProgress);
			this.mMessage = (TextView) this.mListItem
					.findViewById(R.id.mtvMessage);
			this.mDescribe = (TextView) this.mListItem
					.findViewById(R.id.mtvDescribe);
			setMode(MODE_DEFAULT);
		}

		public View getView() {
			return this.mListItem;
		}

		@Override
		public void onPreDownload() {
			setMode(MODE_DOWNLOAD);
		}

		@Override
		public void onPostDownload(String source) {
			if (TextUtils.isEmpty(source)) {
				setMode(MODE_DOWNLOAD_ERROR);
				return;
			}

			setMode(MODE_PROCESS);
			ActivityMangaList.this.mSourceProcessTask = new SourceProcessTask(
					this);
			ActivityMangaList.this.mSourceProcessTask.execute(source);
		}

		@Override
		public int onSourceProcess(String source) {
			MangaList mangaList = ActivityMangaList.this.mGenre
					.getMangaList(source);
			int size = mangaList.size();
			if (size > 0)
				ActivityMangaList.this.mMangaList.addAll(mangaList);
			return size;
		}

		@Override
		public void onPreSourceProcess() {

		}

		@Override
		public void onPostSourceProcess(int size) {
			if (size > 0) {
				ActivityMangaList.this.mPageIndexLoaded++;
				((MangaListAdapter) ActivityMangaList.this.mListAdapter)
						.setMangaList(ActivityMangaList.this.mMangaList);
				setMode(MODE_DEFAULT);
			} else {
				setMode(MODE_PROCESS_ERROR);
				return;
			}
			if (ActivityMangaList.this.mPageIndexLoaded == ActivityMangaList.this.mPageIndexMax)
				getListView().removeFooterView(
						ActivityMangaList.this.mFooter.getView());
		}

		public void setMode(int mode) {
			this.mMode = mode;
			switch (this.mMode) {
			case MODE_DEFAULT:
				this.mProgress.setVisibility(View.GONE);
				this.mMessage.setText(R.string.ui_load_next_page);
				this.mDescribe.setText(String.format("(%d/%d)",
						ActivityMangaList.this.mPageIndexLoaded,
						ActivityMangaList.this.mPageIndexMax));
				break;
			case MODE_DOWNLOAD:
				this.mProgress.setVisibility(View.VISIBLE);
				this.mMessage.setText(R.string.ui_download_data);
				this.mDescribe.setText(String.format("(%s)",
						getString(R.string.ui_click_to_cancel)));
				break;
			case MODE_PROCESS:
				this.mProgress.setVisibility(View.VISIBLE);
				this.mMessage.setText(R.string.ui_process_data);
				this.mDescribe.setText(String.format("(%d/%d)",
						ActivityMangaList.this.mPageIndexLoaded + 1,
						ActivityMangaList.this.mPageIndexMax));
				break;
			case MODE_DOWNLOAD_ERROR:
				this.mProgress.setVisibility(View.GONE);
				this.mMessage.setText(R.string.ui_error);
				this.mDescribe.setText(String.format("(%s)", String.format(
						getString(R.string.ui_fail_to_download),
						ActivityMangaList.this.mPageIndexLoaded + 1)));
				break;
			case MODE_PROCESS_ERROR:
				this.mProgress.setVisibility(View.GONE);
				this.mMessage.setText(R.string.ui_error);
				this.mDescribe.setText(String.format("(%s)", String.format(
						getString(R.string.ui_fail_to_process),
						ActivityMangaList.this.mPageIndexLoaded + 1)));
				break;
			}
		}

		public void click() {
			switch (this.mMode) {
			case MODE_DEFAULT:
			case MODE_DOWNLOAD_ERROR:
			case MODE_PROCESS_ERROR:
				ActivityMangaList.this.mGetSourceTask = new GetSourceTask(
						ActivityMangaList.this.mGenre.siteId, this);
				ActivityMangaList.this.mGenre.getMangaListSource(
						ActivityMangaList.this.mGetSourceTask,
						ActivityMangaList.this.mPageIndexLoaded + 1);
				break;
			case MODE_DOWNLOAD:
				ActivityMangaList.this.mGetSourceTask.cancel(true);
				setMode(MODE_DEFAULT);
				break;
			}
		}

	}

	protected static final String BUNDLE_KEY_MANGA_LIST = "BUNDLE_KEY_MANGA_LIST";

	private Genre mGenre;
	private MangaList mMangaList;
	private int mPageIndexMax;
	private int mPageIndexLoaded;

	private LoadNextPageItem mFooter;

	// private FadeAnimation mhFadeAnim;
	// private final IOnFadeEndListener mhOnFadeInEnd;
	// private final IOnFadeEndListener mhOnFadeOutEnd;

	public ActivityMangaList() {
		this.mPageIndexLoaded = 0;
		this.mPageIndexMax = 0;
	}

	@Override
	public String getSiteName() {
		return this.mGenre.getSiteName();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.mGenre = IntentHandler.getGenre(this);
		if (this.mGenre == null)
			finish();

		setContentView(R.layout.activity_manga_list);
		setTitle(String.format("%s - %s", this.mGenre.getSiteDisplayname(),
				this.mGenre.displayname));

		// this.mbShowProcessDialog = false;

		setupListView(new MangaListAdapter(this, this.mGenre.siteId));

		findViewById(R.id.mvgSearch).setVisibility(View.GONE);

		if (!this.mProcessed)
			loadMangaList();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(BUNDLE_KEY_MANGA_LIST, this.mMangaList);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		this.mMangaList = (MangaList) savedInstanceState
				.getSerializable(BUNDLE_KEY_MANGA_LIST);
		((MangaListAdapter) this.mListAdapter).setMangaList(this.mMangaList);
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
		if (view == this.mFooter.getView())
			this.mFooter.click();
	}

	@Override
	public int onSourceProcess(String source) {
		this.mMangaList = this.mGenre.getMangaList(source);
		if (this.mPageIndexMax == 0)
			this.mPageIndexMax = this.mMangaList.pageIndexMax;
		return this.mMangaList.size();
	}

	@Override
	public void onPostSourceProcess(int size) {
		if (size > 0)
			this.mPageIndexLoaded++;
		if (this.mPageIndexLoaded < this.mPageIndexMax)
			showFooter();
		((MangaListAdapter) this.mListAdapter).setMangaList(this.mMangaList);
		getListView().requestFocus();

		super.onPostSourceProcess(size);
	}

	private void loadMangaList() {
		this.mGetSourceTask = new GetSourceTask(this.mGenre.siteId, this);
		this.mGenre.getMangaListSource(this.mGetSourceTask,
				this.mPageIndexLoaded + 1);
	}

	private void showFooter() {
		this.mFooter = new LoadNextPageItem();
		getListView().addFooterView(this.mFooter.getView(), null, true);
		setListAdapter(this.mListAdapter);
	}

}
