package org.falconia.mangaproxy;

import org.falconia.mangaproxy.data.Chapter;
import org.falconia.mangaproxy.data.ChapterList;
import org.falconia.mangaproxy.data.Manga;
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

public final class ActivityChapterList extends ActivityBase {

	public final static class IntentHandler {

		private static final String BUNDLE_KEY_MANGA_DATA = "BUNDLE_KEY_MANGA_DATA";

		private static Intent getIntent(Context context, Manga manga) {
			Bundle bundle = new Bundle();
			bundle.putSerializable(BUNDLE_KEY_MANGA_DATA, manga);
			Intent i = new Intent(context, ActivityChapterList.class);
			i.putExtras(bundle);
			return i;
		}

		protected static Manga getManga(ActivityChapterList activity) {
			return (Manga) activity.getIntent().getExtras()
					.getSerializable(BUNDLE_KEY_MANGA_DATA);
		}

		public static void startActivityMangaList(Context context, Manga manga) {
			context.startActivity(getIntent(context, manga));
		}

	}

	private final class ChapterListAdapter extends BaseListAdapter {

		final class ViewHolder {
			public TextView tvDisplayname;
		}

		private ChapterList mChapterList;
		private LayoutInflater mInflater;

		public ChapterListAdapter() {
			mInflater = LayoutInflater.from(ActivityChapterList.this);
		}

		@Override
		public int getCount() {
			if (mChapterList == null) {
				return 0;
			}
			return mChapterList.size();
		}

		@Override
		public Chapter getItem(int position) {
			return mChapterList.getAt(position);
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).chapterId.hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.list_item_genre, null);
				holder.tvDisplayname = (TextView) convertView
						.findViewById(R.id.mtvDisplayname);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			Chapter chapter = mChapterList.getAt(position);
			holder.tvDisplayname.setText(chapter.displayname);
			if (chapter.typeId == Chapter.TYPE_ID_VOLUME) {
				holder.tvDisplayname.setTextColor(getResources().getColor(
						R.color.highlight));
			} else {
				holder.tvDisplayname.setTextColor(getResources().getColor(
						android.R.color.primary_text_dark));
			}

			if (convertView.getTag() == null) {
				convertView.setTag(holder);
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

		public void setChapterList(ChapterList chapterList) {
			mChapterList = chapterList;
			notifyDataSetChanged();
		}

	}

	private static final String BUNDLE_KEY_CHAPTER_LIST = "BUNDLE_KEY_CHAPTER_LIST";

	private Manga mManga;
	private ChapterList mChapterList;

	@Override
	public int getSiteId() {
		return mManga.siteId;
	}

	@Override
	String getSiteName() {
		return mManga.getSiteName();
	}

	@Override
	String getSiteDisplayname() {
		return mManga.getSiteDisplayname();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mManga = IntentHandler.getManga(this);
		if (mManga == null) {
			finish();
		}

		setContentView(R.layout.activity_chapter_list);
		setCustomTitle(mManga.displayname);

		// this.mbShowProcessDialog = false;

		setupListView(new ChapterListAdapter());

		if (!mProcessed) {
			loadChapterList();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(BUNDLE_KEY_CHAPTER_LIST, mChapterList);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		mChapterList = (ChapterList) savedInstanceState
				.getSerializable(BUNDLE_KEY_CHAPTER_LIST);
		((ChapterListAdapter) mListAdapter).setChapterList(mChapterList);
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_DOWNLOAD_ID:
			dialog = mSourceDownloader
					.createDownloadDialog(R.string.source_of_chapter_list);
			break;
		case DIALOG_PROCESS_ID:
			dialog = createProcessDialog(R.string.source_of_chapter_list);
			break;
		default:
			dialog = super.onCreateDialog(id);
		}
		return dialog;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ActivityChapter.IntentHandler.startActivityChapter(this, mManga,
				mChapterList.getAt(position));
	}

	@Override
	public int onSourceProcess(String source) {
		mChapterList = mManga.getChapterList(source);
		return mChapterList.size();
	}

	@Override
	public void onPostSourceProcess(int result) {
		((ChapterListAdapter) mListAdapter).setChapterList(mChapterList);
		getListView().requestFocus();

		super.onPostSourceProcess(result);
	}

	private void loadChapterList() {
		mSourceDownloader = new SourceDownloader();
		mSourceDownloader.download(mManga.getUrl());
	}

}
