package com.tanxe.quran.data.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.tanxe.quran.data.entity.ReadingProgress;
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
public final class ReadingProgressDao_Impl implements ReadingProgressDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ReadingProgress> __insertionAdapterOfReadingProgress;

  public ReadingProgressDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfReadingProgress = new EntityInsertionAdapter<ReadingProgress>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `reading_progress` (`id`,`surahNumber`,`ayahNumber`,`timestamp`,`sessionDurationSeconds`,`type`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final ReadingProgress entity) {
        statement.bindLong(1, entity.id);
        statement.bindLong(2, entity.surahNumber);
        statement.bindLong(3, entity.ayahNumber);
        statement.bindLong(4, entity.timestamp);
        statement.bindLong(5, entity.sessionDurationSeconds);
        if (entity.type == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.type);
        }
      }
    };
  }

  @Override
  public void insert(final ReadingProgress progress) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfReadingProgress.insert(progress);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int getTotalReadTimeSeconds() {
    final String _sql = "SELECT COALESCE(SUM(sessionDurationSeconds), 0) FROM reading_progress";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
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

  @Override
  public int getTodayReadTimeSeconds(final long startOfDay) {
    final String _sql = "SELECT COALESCE(SUM(sessionDurationSeconds), 0) FROM reading_progress WHERE timestamp >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startOfDay);
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

  @Override
  public int getUniqueAyahsRead() {
    final String _sql = "SELECT COUNT(DISTINCT (surahNumber * 1000 + ayahNumber)) FROM reading_progress";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
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

  @Override
  public List<ReadingProgress> getRecentProgress(final int limit) {
    final String _sql = "SELECT * FROM reading_progress ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfSurahNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "surahNumber");
      final int _cursorIndexOfAyahNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "ayahNumber");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfSessionDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionDurationSeconds");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final List<ReadingProgress> _result = new ArrayList<ReadingProgress>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ReadingProgress _item;
        _item = new ReadingProgress();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.surahNumber = _cursor.getInt(_cursorIndexOfSurahNumber);
        _item.ayahNumber = _cursor.getInt(_cursorIndexOfAyahNumber);
        _item.timestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _item.sessionDurationSeconds = _cursor.getInt(_cursorIndexOfSessionDurationSeconds);
        if (_cursor.isNull(_cursorIndexOfType)) {
          _item.type = null;
        } else {
          _item.type = _cursor.getString(_cursorIndexOfType);
        }
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int getReadingDaysCount(final long since) {
    final String _sql = "SELECT COUNT(DISTINCT strftime('%Y-%m-%d', timestamp / 1000, 'unixepoch')) FROM reading_progress WHERE timestamp >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
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
