package com.tanxe.quran.data.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.tanxe.quran.data.entity.Bookmark;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class BookmarkDao_Impl implements BookmarkDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Bookmark> __insertionAdapterOfBookmark;

  private final EntityDeletionOrUpdateAdapter<Bookmark> __deletionAdapterOfBookmark;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByAyah;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public BookmarkDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfBookmark = new EntityInsertionAdapter<Bookmark>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `bookmarks` (`id`,`surahNumber`,`ayahNumber`,`surahName`,`note`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Bookmark entity) {
        statement.bindLong(1, entity.id);
        statement.bindLong(2, entity.surahNumber);
        statement.bindLong(3, entity.ayahNumber);
        if (entity.surahName == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.surahName);
        }
        if (entity.note == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.note);
        }
        statement.bindLong(6, entity.timestamp);
      }
    };
    this.__deletionAdapterOfBookmark = new EntityDeletionOrUpdateAdapter<Bookmark>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `bookmarks` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Bookmark entity) {
        statement.bindLong(1, entity.id);
      }
    };
    this.__preparedStmtOfDeleteByAyah = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM bookmarks WHERE surahNumber = ? AND ayahNumber = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM bookmarks";
        return _query;
      }
    };
  }

  @Override
  public void insert(final Bookmark bookmark) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfBookmark.insert(bookmark);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final Bookmark bookmark) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfBookmark.handle(bookmark);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteByAyah(final int surah, final int ayah) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteByAyah.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, surah);
    _argIndex = 2;
    _stmt.bindLong(_argIndex, ayah);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteByAyah.release(_stmt);
    }
  }

  @Override
  public void deleteAll() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteAll.release(_stmt);
    }
  }

  @Override
  public List<Bookmark> getAllBookmarks() {
    final String _sql = "SELECT * FROM bookmarks ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfSurahNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "surahNumber");
      final int _cursorIndexOfAyahNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "ayahNumber");
      final int _cursorIndexOfSurahName = CursorUtil.getColumnIndexOrThrow(_cursor, "surahName");
      final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final List<Bookmark> _result = new ArrayList<Bookmark>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Bookmark _item;
        _item = new Bookmark();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.surahNumber = _cursor.getInt(_cursorIndexOfSurahNumber);
        _item.ayahNumber = _cursor.getInt(_cursorIndexOfAyahNumber);
        if (_cursor.isNull(_cursorIndexOfSurahName)) {
          _item.surahName = null;
        } else {
          _item.surahName = _cursor.getString(_cursorIndexOfSurahName);
        }
        if (_cursor.isNull(_cursorIndexOfNote)) {
          _item.note = null;
        } else {
          _item.note = _cursor.getString(_cursorIndexOfNote);
        }
        _item.timestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public Bookmark getBookmark(final int surah, final int ayah) {
    final String _sql = "SELECT * FROM bookmarks WHERE surahNumber = ? AND ayahNumber = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, surah);
    _argIndex = 2;
    _statement.bindLong(_argIndex, ayah);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfSurahNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "surahNumber");
      final int _cursorIndexOfAyahNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "ayahNumber");
      final int _cursorIndexOfSurahName = CursorUtil.getColumnIndexOrThrow(_cursor, "surahName");
      final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final Bookmark _result;
      if (_cursor.moveToFirst()) {
        _result = new Bookmark();
        _result.id = _cursor.getInt(_cursorIndexOfId);
        _result.surahNumber = _cursor.getInt(_cursorIndexOfSurahNumber);
        _result.ayahNumber = _cursor.getInt(_cursorIndexOfAyahNumber);
        if (_cursor.isNull(_cursorIndexOfSurahName)) {
          _result.surahName = null;
        } else {
          _result.surahName = _cursor.getString(_cursorIndexOfSurahName);
        }
        if (_cursor.isNull(_cursorIndexOfNote)) {
          _result.note = null;
        } else {
          _result.note = _cursor.getString(_cursorIndexOfNote);
        }
        _result.timestamp = _cursor.getLong(_cursorIndexOfTimestamp);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int isBookmarked(final int surah, final int ayah) {
    final String _sql = "SELECT COUNT(*) FROM bookmarks WHERE surahNumber = ? AND ayahNumber = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, surah);
    _argIndex = 2;
    _statement.bindLong(_argIndex, ayah);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
