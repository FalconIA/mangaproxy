package org.falconia.mangaproxy;

import java.util.ArrayList;
import java.util.HashMap;

import org.falconia.mangaproxy.data.Manga;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public final class AppSQLite {

	public static final String DATABASE_NAME = "mangaproxy";
	public static final String DATABASE_TABLE_MANGA = "tManga";
	public static final int DATABASE_VERSION = 1;

	public static final String KEY_SITE_ID = "iSiteId";
	public static final String KEY_MANGA_ID = "sMangaId";
	public static final String KEY_DISPLAYNAME = "sDisplayname";

	public static final String KEY_IS_COMPLETED = "bIsCompleted";
	public static final String KEY_CHAPTER_COUNT = "iChapterCount";
	public static final String KEY_HAS_NEW_CHAPTER = "bHasNewChapter";

	public static final String KEY_UPDATED_AT = "iUpdatedAt";
	public static final String KEY_UPDATED_AT_TIMEZONE = "iUpdatedAtTimeZone";

	public static final String KEY_LAST_READ_CHAPTER_ID = "sLastReadChapterId";
	public static final String KEY_LATEST_CHAPTER_ID = "sLatestChapterId";

	public static final String KEY_ROW_ID = BaseColumns._ID;
	public static final String DATABASE_CREATE = ""
			+ String.format("CREATE TABLE %s (", DATABASE_TABLE_MANGA)
			+ String.format("%s INTEGER PRIMARY KEY AUTOINCREMENT, ", KEY_ROW_ID)
			+ String.format("%s INT NOT NULL, ", KEY_SITE_ID)
			+ String.format("%s TEXT NOT NULL, ", KEY_MANGA_ID)
			+ String.format("%s TEXT, ", KEY_DISPLAYNAME)
			+ String.format("%s INT1 DEFAULT 0, ", KEY_IS_COMPLETED)
			+ String.format("%s NUM DEFAULT 0, ", KEY_CHAPTER_COUNT)
			+ String.format("%s INT1 DEFAULT 0, ", KEY_HAS_NEW_CHAPTER)
			+ String.format("%s DATETIME, ", KEY_UPDATED_AT)
			+ String.format("%s TEXT, ", KEY_UPDATED_AT_TIMEZONE)
			+ String.format("%s TEXT, ", KEY_LAST_READ_CHAPTER_ID)
			+ String.format("%s TEXT)", KEY_LATEST_CHAPTER_ID);

	public class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper() {
			super(mContext, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				db.execSQL(DATABASE_CREATE);
				AppUtils.logI(this, "Created initial database structure.");
			} catch (SQLException e) {
				AppUtils.logE(this, e.getMessage());
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub

		}
	}

	public DatabaseHelper dbHelper;
	public SQLiteDatabase db;

	private final Context mContext;

	public AppSQLite(Context context) {
		mContext = context;
		dbHelper = new DatabaseHelper();
	}

	public AppSQLite open() throws SQLException {
		db = dbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		dbHelper.close();
	}

	public boolean isOpen() {
		return db != null && db.isOpen();
	}

	// For manga

	public Cursor getAllMangaRows(String selection, String orderBy) throws SQLException {
		// String[] columns = new String[] { KEY_ROW_ID };
		Cursor cursor = db.query(true, DATABASE_TABLE_MANGA, null, selection, null, null, null,
				orderBy, null);

		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	public ArrayList<Manga> getAllMangas(String orderBy) throws SQLException {
		Cursor cursor = getAllMangaRows(null, orderBy);

		if (cursor == null || cursor.getCount() == 0) {
			return null;
		}

		ArrayList<Manga> mangaList = new ArrayList<Manga>();
		int count = cursor.getCount();
		for (int i = 0; i < count; i++) {
			Manga manga = getManga(cursor);
			mangaList.add(manga);
			cursor.moveToNext();
		}
		cursor.close();
		return mangaList;
	}

	public ArrayList<Manga> getAllMangas() throws SQLException {
		return getAllMangas(null);
	}

	public HashMap<String, Manga> getAllMangasBySite(int siteId) throws SQLException {
		String selection = String.format("%s=%d", KEY_SITE_ID, siteId);
		Cursor cursor = getAllMangaRows(selection, null);

		if (cursor == null || cursor.getCount() == 0) {
			return null;
		}

		HashMap<String, Manga> mangaList = new HashMap<String, Manga>();
		int count = cursor.getCount();
		for (int i = 0; i < count; i++) {
			Manga manga = getManga(cursor);
			mangaList.put(manga.mangaId, manga);
			cursor.moveToNext();
		}
		cursor.close();
		return mangaList;
	}

	public Manga getManga(Cursor cursor) throws SQLException {
		if (cursor == null || cursor.getCount() == 0) {
			return null;
		}

		Manga manga = Manga
				.getFavoriteManga(cursor.getInt(1), cursor.getString(2), cursor.getString(3),
						cursor.getInt(4) != 0, cursor.getInt(7), cursor.getInt(8) != 0);
		return manga;
	}

	public Manga getManga(long id) throws SQLException {
		String selection = String.format("%s=%d", KEY_ROW_ID, id);
		Cursor cursor = db.query(true, DATABASE_TABLE_MANGA, null, selection, null, null, null,
				null, null);
		cursor.close();

		return getManga(cursor);
	}

	public long containsManga(Manga manga) throws SQLException {
		String[] columns = new String[] { KEY_ROW_ID };
		String selection = String.format("%s=%d AND %s=%s", KEY_SITE_ID, manga.siteId,
				KEY_MANGA_ID, manga.mangaId);
		Cursor cursor = db.query(true, DATABASE_TABLE_MANGA, columns, selection, null, null, null,
				null, null);

		if (cursor == null || cursor.getCount() == 0) {
			return -1;
		}

		long id = cursor.getLong(0);
		cursor.close();
		return id;
	}

	public long insertManga(Manga manga) throws SQLException {
		final long id = containsManga(manga);
		if (id > -1) {
			return id;
		}

		ContentValues values = new ContentValues();
		values.put(KEY_SITE_ID, manga.siteId);
		values.put(KEY_MANGA_ID, manga.mangaId);
		values.put(KEY_DISPLAYNAME, manga.displayname);
		values.put(KEY_IS_COMPLETED, manga.isCompleted);
		if (manga.updatedAt != null) {
			values.put(KEY_UPDATED_AT, manga.updatedAt.getTimeInMillis());
			values.put(KEY_UPDATED_AT_TIMEZONE, manga.updatedAt.getTimeZone().getID());
		}
		values.put(KEY_CHAPTER_COUNT, manga.chapterCount);
		values.put(KEY_HAS_NEW_CHAPTER, manga.hasNewChapter);
		return db.insertOrThrow(DATABASE_TABLE_MANGA, null, values);
	}

	public int deleteManga(Manga manga) throws SQLException {
		String selection = String.format("%s=%d AND %s=%s", KEY_SITE_ID, manga.siteId,
				KEY_MANGA_ID, manga.mangaId);
		return db.delete(DATABASE_TABLE_MANGA, selection, null);
	}
}
