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
import com.tanxe.quran.data.entity.KnownWord;
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
public final class KnownWordDao_Impl implements KnownWordDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<KnownWord> __insertionAdapterOfKnownWord;

  private final EntityDeletionOrUpdateAdapter<KnownWord> __deletionAdapterOfKnownWord;

  private final SharedSQLiteStatement __preparedStmtOfDeleteWord;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public KnownWordDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfKnownWord = new EntityInsertionAdapter<KnownWord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `known_words` (`arabicWord`,`frequency`,`learnedAt`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final KnownWord entity) {
        if (entity.arabicWord == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.arabicWord);
        }
        statement.bindLong(2, entity.frequency);
        statement.bindLong(3, entity.learnedAt);
      }
    };
    this.__deletionAdapterOfKnownWord = new EntityDeletionOrUpdateAdapter<KnownWord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `known_words` WHERE `arabicWord` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final KnownWord entity) {
        if (entity.arabicWord == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.arabicWord);
        }
      }
    };
    this.__preparedStmtOfDeleteWord = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM known_words WHERE arabicWord = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM known_words";
        return _query;
      }
    };
  }

  @Override
  public void insert(final KnownWord word) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfKnownWord.insert(word);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insertAll(final List<KnownWord> words) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfKnownWord.insert(words);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final KnownWord word) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfKnownWord.handle(word);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteWord(final String word) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteWord.acquire();
    int _argIndex = 1;
    if (word == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, word);
    }
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteWord.release(_stmt);
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
  public List<KnownWord> getAllKnownWords() {
    final String _sql = "SELECT * FROM known_words ORDER BY learnedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfArabicWord = CursorUtil.getColumnIndexOrThrow(_cursor, "arabicWord");
      final int _cursorIndexOfFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "frequency");
      final int _cursorIndexOfLearnedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "learnedAt");
      final List<KnownWord> _result = new ArrayList<KnownWord>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final KnownWord _item;
        _item = new KnownWord();
        if (_cursor.isNull(_cursorIndexOfArabicWord)) {
          _item.arabicWord = null;
        } else {
          _item.arabicWord = _cursor.getString(_cursorIndexOfArabicWord);
        }
        _item.frequency = _cursor.getInt(_cursorIndexOfFrequency);
        _item.learnedAt = _cursor.getLong(_cursorIndexOfLearnedAt);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int isKnown(final String word) {
    final String _sql = "SELECT COUNT(*) FROM known_words WHERE arabicWord = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (word == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, word);
    }
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
  public int getKnownCount() {
    final String _sql = "SELECT COUNT(*) FROM known_words";
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
  public int getTotalKnownFrequency() {
    final String _sql = "SELECT SUM(frequency) FROM known_words";
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
