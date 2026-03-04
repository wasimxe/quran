package com.tanxe.quran.data.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.tanxe.quran.data.entity.Tafseer;
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
public final class TafseerDao_Impl implements TafseerDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Tafseer> __insertionAdapterOfTafseer;

  private final SharedSQLiteStatement __preparedStmtOfDeleteEdition;

  public TafseerDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTafseer = new EntityInsertionAdapter<Tafseer>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `tafseers` (`id`,`surahNumber`,`ayahNumber`,`text`,`edition`,`language`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Tafseer entity) {
        statement.bindLong(1, entity.id);
        statement.bindLong(2, entity.surahNumber);
        statement.bindLong(3, entity.ayahNumber);
        if (entity.text == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.text);
        }
        if (entity.edition == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.edition);
        }
        if (entity.language == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.language);
        }
      }
    };
    this.__preparedStmtOfDeleteEdition = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM tafseers WHERE edition = ?";
        return _query;
      }
    };
  }

  @Override
  public void insertAll(final List<Tafseer> tafseers) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfTafseer.insert(tafseers);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteEdition(final String edition) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteEdition.acquire();
    int _argIndex = 1;
    if (edition == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, edition);
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
      __preparedStmtOfDeleteEdition.release(_stmt);
    }
  }

  @Override
  public Tafseer getTafseer(final int surah, final int ayah, final String edition) {
    final String _sql = "SELECT * FROM tafseers WHERE surahNumber = ? AND ayahNumber = ? AND edition = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, surah);
    _argIndex = 2;
    _statement.bindLong(_argIndex, ayah);
    _argIndex = 3;
    if (edition == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, edition);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfSurahNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "surahNumber");
      final int _cursorIndexOfAyahNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "ayahNumber");
      final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
      final int _cursorIndexOfEdition = CursorUtil.getColumnIndexOrThrow(_cursor, "edition");
      final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
      final Tafseer _result;
      if (_cursor.moveToFirst()) {
        _result = new Tafseer();
        _result.id = _cursor.getInt(_cursorIndexOfId);
        _result.surahNumber = _cursor.getInt(_cursorIndexOfSurahNumber);
        _result.ayahNumber = _cursor.getInt(_cursorIndexOfAyahNumber);
        if (_cursor.isNull(_cursorIndexOfText)) {
          _result.text = null;
        } else {
          _result.text = _cursor.getString(_cursorIndexOfText);
        }
        if (_cursor.isNull(_cursorIndexOfEdition)) {
          _result.edition = null;
        } else {
          _result.edition = _cursor.getString(_cursorIndexOfEdition);
        }
        if (_cursor.isNull(_cursorIndexOfLanguage)) {
          _result.language = null;
        } else {
          _result.language = _cursor.getString(_cursorIndexOfLanguage);
        }
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
  public List<String> getAvailableEditions() {
    final String _sql = "SELECT DISTINCT edition FROM tafseers";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final List<String> _result = new ArrayList<String>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final String _item;
        if (_cursor.isNull(0)) {
          _item = null;
        } else {
          _item = _cursor.getString(0);
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
  public List<String> getEditionsByLanguage(final String language) {
    final String _sql = "SELECT DISTINCT edition FROM tafseers WHERE language = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (language == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, language);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final List<String> _result = new ArrayList<String>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final String _item;
        if (_cursor.isNull(0)) {
          _item = null;
        } else {
          _item = _cursor.getString(0);
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
  public List<Tafseer> searchInEdition(final String edition, final String query) {
    final String _sql = "SELECT * FROM tafseers WHERE edition = ? AND (text LIKE '%' || ? || '%') ORDER BY surahNumber, ayahNumber";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (edition == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, edition);
    }
    _argIndex = 2;
    if (query == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, query);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfSurahNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "surahNumber");
      final int _cursorIndexOfAyahNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "ayahNumber");
      final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
      final int _cursorIndexOfEdition = CursorUtil.getColumnIndexOrThrow(_cursor, "edition");
      final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
      final List<Tafseer> _result = new ArrayList<Tafseer>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Tafseer _item;
        _item = new Tafseer();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.surahNumber = _cursor.getInt(_cursorIndexOfSurahNumber);
        _item.ayahNumber = _cursor.getInt(_cursorIndexOfAyahNumber);
        if (_cursor.isNull(_cursorIndexOfText)) {
          _item.text = null;
        } else {
          _item.text = _cursor.getString(_cursorIndexOfText);
        }
        if (_cursor.isNull(_cursorIndexOfEdition)) {
          _item.edition = null;
        } else {
          _item.edition = _cursor.getString(_cursorIndexOfEdition);
        }
        if (_cursor.isNull(_cursorIndexOfLanguage)) {
          _item.language = null;
        } else {
          _item.language = _cursor.getString(_cursorIndexOfLanguage);
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
  public int getEditionCount(final String edition) {
    final String _sql = "SELECT COUNT(*) FROM tafseers WHERE edition = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (edition == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, edition);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
