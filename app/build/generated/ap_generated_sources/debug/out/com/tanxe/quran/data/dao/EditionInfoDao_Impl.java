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
import com.tanxe.quran.data.entity.EditionInfo;
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
public final class EditionInfoDao_Impl implements EditionInfoDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<EditionInfo> __insertionAdapterOfEditionInfo;

  private final SharedSQLiteStatement __preparedStmtOfUpdateDownloadState;

  public EditionInfoDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfEditionInfo = new EntityInsertionAdapter<EditionInfo>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `edition_info` (`identifier`,`name`,`language`,`languageName`,`type`,`direction`,`isDownloaded`,`downloadProgress`,`downloadedAt`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final EditionInfo entity) {
        if (entity.identifier == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.identifier);
        }
        if (entity.name == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.name);
        }
        if (entity.language == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.language);
        }
        if (entity.languageName == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.languageName);
        }
        if (entity.type == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.type);
        }
        if (entity.direction == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.direction);
        }
        final int _tmp = entity.isDownloaded ? 1 : 0;
        statement.bindLong(7, _tmp);
        statement.bindLong(8, entity.downloadProgress);
        statement.bindLong(9, entity.downloadedAt);
      }
    };
    this.__preparedStmtOfUpdateDownloadState = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE edition_info SET isDownloaded = ?, downloadProgress = ?, downloadedAt = ? WHERE identifier = ?";
        return _query;
      }
    };
  }

  @Override
  public void insertAll(final List<EditionInfo> editions) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfEditionInfo.insert(editions);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void updateDownloadState(final String identifier, final boolean downloaded,
      final int progress, final long downloadedAt) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateDownloadState.acquire();
    int _argIndex = 1;
    final int _tmp = downloaded ? 1 : 0;
    _stmt.bindLong(_argIndex, _tmp);
    _argIndex = 2;
    _stmt.bindLong(_argIndex, progress);
    _argIndex = 3;
    _stmt.bindLong(_argIndex, downloadedAt);
    _argIndex = 4;
    if (identifier == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, identifier);
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
      __preparedStmtOfUpdateDownloadState.release(_stmt);
    }
  }

  @Override
  public List<EditionInfo> getByLanguage(final String language) {
    final String _sql = "SELECT * FROM edition_info WHERE language = ? ORDER BY name";
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
      final int _cursorIndexOfIdentifier = CursorUtil.getColumnIndexOrThrow(_cursor, "identifier");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
      final int _cursorIndexOfLanguageName = CursorUtil.getColumnIndexOrThrow(_cursor, "languageName");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "direction");
      final int _cursorIndexOfIsDownloaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isDownloaded");
      final int _cursorIndexOfDownloadProgress = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadProgress");
      final int _cursorIndexOfDownloadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadedAt");
      final List<EditionInfo> _result = new ArrayList<EditionInfo>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final EditionInfo _item;
        _item = new EditionInfo();
        if (_cursor.isNull(_cursorIndexOfIdentifier)) {
          _item.identifier = null;
        } else {
          _item.identifier = _cursor.getString(_cursorIndexOfIdentifier);
        }
        if (_cursor.isNull(_cursorIndexOfName)) {
          _item.name = null;
        } else {
          _item.name = _cursor.getString(_cursorIndexOfName);
        }
        if (_cursor.isNull(_cursorIndexOfLanguage)) {
          _item.language = null;
        } else {
          _item.language = _cursor.getString(_cursorIndexOfLanguage);
        }
        if (_cursor.isNull(_cursorIndexOfLanguageName)) {
          _item.languageName = null;
        } else {
          _item.languageName = _cursor.getString(_cursorIndexOfLanguageName);
        }
        if (_cursor.isNull(_cursorIndexOfType)) {
          _item.type = null;
        } else {
          _item.type = _cursor.getString(_cursorIndexOfType);
        }
        if (_cursor.isNull(_cursorIndexOfDirection)) {
          _item.direction = null;
        } else {
          _item.direction = _cursor.getString(_cursorIndexOfDirection);
        }
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsDownloaded);
        _item.isDownloaded = _tmp != 0;
        _item.downloadProgress = _cursor.getInt(_cursorIndexOfDownloadProgress);
        _item.downloadedAt = _cursor.getLong(_cursorIndexOfDownloadedAt);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<EditionInfo> getByType(final String type) {
    final String _sql = "SELECT * FROM edition_info WHERE type = ? ORDER BY language, name";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (type == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, type);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfIdentifier = CursorUtil.getColumnIndexOrThrow(_cursor, "identifier");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
      final int _cursorIndexOfLanguageName = CursorUtil.getColumnIndexOrThrow(_cursor, "languageName");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "direction");
      final int _cursorIndexOfIsDownloaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isDownloaded");
      final int _cursorIndexOfDownloadProgress = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadProgress");
      final int _cursorIndexOfDownloadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadedAt");
      final List<EditionInfo> _result = new ArrayList<EditionInfo>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final EditionInfo _item;
        _item = new EditionInfo();
        if (_cursor.isNull(_cursorIndexOfIdentifier)) {
          _item.identifier = null;
        } else {
          _item.identifier = _cursor.getString(_cursorIndexOfIdentifier);
        }
        if (_cursor.isNull(_cursorIndexOfName)) {
          _item.name = null;
        } else {
          _item.name = _cursor.getString(_cursorIndexOfName);
        }
        if (_cursor.isNull(_cursorIndexOfLanguage)) {
          _item.language = null;
        } else {
          _item.language = _cursor.getString(_cursorIndexOfLanguage);
        }
        if (_cursor.isNull(_cursorIndexOfLanguageName)) {
          _item.languageName = null;
        } else {
          _item.languageName = _cursor.getString(_cursorIndexOfLanguageName);
        }
        if (_cursor.isNull(_cursorIndexOfType)) {
          _item.type = null;
        } else {
          _item.type = _cursor.getString(_cursorIndexOfType);
        }
        if (_cursor.isNull(_cursorIndexOfDirection)) {
          _item.direction = null;
        } else {
          _item.direction = _cursor.getString(_cursorIndexOfDirection);
        }
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsDownloaded);
        _item.isDownloaded = _tmp != 0;
        _item.downloadProgress = _cursor.getInt(_cursorIndexOfDownloadProgress);
        _item.downloadedAt = _cursor.getLong(_cursorIndexOfDownloadedAt);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<EditionInfo> getDownloaded() {
    final String _sql = "SELECT * FROM edition_info WHERE isDownloaded = 1 ORDER BY name";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfIdentifier = CursorUtil.getColumnIndexOrThrow(_cursor, "identifier");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
      final int _cursorIndexOfLanguageName = CursorUtil.getColumnIndexOrThrow(_cursor, "languageName");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "direction");
      final int _cursorIndexOfIsDownloaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isDownloaded");
      final int _cursorIndexOfDownloadProgress = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadProgress");
      final int _cursorIndexOfDownloadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadedAt");
      final List<EditionInfo> _result = new ArrayList<EditionInfo>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final EditionInfo _item;
        _item = new EditionInfo();
        if (_cursor.isNull(_cursorIndexOfIdentifier)) {
          _item.identifier = null;
        } else {
          _item.identifier = _cursor.getString(_cursorIndexOfIdentifier);
        }
        if (_cursor.isNull(_cursorIndexOfName)) {
          _item.name = null;
        } else {
          _item.name = _cursor.getString(_cursorIndexOfName);
        }
        if (_cursor.isNull(_cursorIndexOfLanguage)) {
          _item.language = null;
        } else {
          _item.language = _cursor.getString(_cursorIndexOfLanguage);
        }
        if (_cursor.isNull(_cursorIndexOfLanguageName)) {
          _item.languageName = null;
        } else {
          _item.languageName = _cursor.getString(_cursorIndexOfLanguageName);
        }
        if (_cursor.isNull(_cursorIndexOfType)) {
          _item.type = null;
        } else {
          _item.type = _cursor.getString(_cursorIndexOfType);
        }
        if (_cursor.isNull(_cursorIndexOfDirection)) {
          _item.direction = null;
        } else {
          _item.direction = _cursor.getString(_cursorIndexOfDirection);
        }
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsDownloaded);
        _item.isDownloaded = _tmp != 0;
        _item.downloadProgress = _cursor.getInt(_cursorIndexOfDownloadProgress);
        _item.downloadedAt = _cursor.getLong(_cursorIndexOfDownloadedAt);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<EditionInfo> getDownloadedByType(final String type) {
    final String _sql = "SELECT * FROM edition_info WHERE isDownloaded = 1 AND type = ? ORDER BY name";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (type == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, type);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfIdentifier = CursorUtil.getColumnIndexOrThrow(_cursor, "identifier");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
      final int _cursorIndexOfLanguageName = CursorUtil.getColumnIndexOrThrow(_cursor, "languageName");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "direction");
      final int _cursorIndexOfIsDownloaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isDownloaded");
      final int _cursorIndexOfDownloadProgress = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadProgress");
      final int _cursorIndexOfDownloadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadedAt");
      final List<EditionInfo> _result = new ArrayList<EditionInfo>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final EditionInfo _item;
        _item = new EditionInfo();
        if (_cursor.isNull(_cursorIndexOfIdentifier)) {
          _item.identifier = null;
        } else {
          _item.identifier = _cursor.getString(_cursorIndexOfIdentifier);
        }
        if (_cursor.isNull(_cursorIndexOfName)) {
          _item.name = null;
        } else {
          _item.name = _cursor.getString(_cursorIndexOfName);
        }
        if (_cursor.isNull(_cursorIndexOfLanguage)) {
          _item.language = null;
        } else {
          _item.language = _cursor.getString(_cursorIndexOfLanguage);
        }
        if (_cursor.isNull(_cursorIndexOfLanguageName)) {
          _item.languageName = null;
        } else {
          _item.languageName = _cursor.getString(_cursorIndexOfLanguageName);
        }
        if (_cursor.isNull(_cursorIndexOfType)) {
          _item.type = null;
        } else {
          _item.type = _cursor.getString(_cursorIndexOfType);
        }
        if (_cursor.isNull(_cursorIndexOfDirection)) {
          _item.direction = null;
        } else {
          _item.direction = _cursor.getString(_cursorIndexOfDirection);
        }
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsDownloaded);
        _item.isDownloaded = _tmp != 0;
        _item.downloadProgress = _cursor.getInt(_cursorIndexOfDownloadProgress);
        _item.downloadedAt = _cursor.getLong(_cursorIndexOfDownloadedAt);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<String> getAllLanguages() {
    final String _sql = "SELECT DISTINCT language FROM edition_info ORDER BY language";
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
  public List<EditionInfoDao.LanguageInfo> getAllLanguagesWithNames() {
    final String _sql = "SELECT DISTINCT language, languageName FROM edition_info ORDER BY languageName";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfLanguage = 0;
      final int _cursorIndexOfLanguageName = 1;
      final List<EditionInfoDao.LanguageInfo> _result = new ArrayList<EditionInfoDao.LanguageInfo>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final EditionInfoDao.LanguageInfo _item;
        _item = new EditionInfoDao.LanguageInfo();
        if (_cursor.isNull(_cursorIndexOfLanguage)) {
          _item.language = null;
        } else {
          _item.language = _cursor.getString(_cursorIndexOfLanguage);
        }
        if (_cursor.isNull(_cursorIndexOfLanguageName)) {
          _item.languageName = null;
        } else {
          _item.languageName = _cursor.getString(_cursorIndexOfLanguageName);
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
  public EditionInfo getByIdentifier(final String identifier) {
    final String _sql = "SELECT * FROM edition_info WHERE identifier = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (identifier == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, identifier);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfIdentifier = CursorUtil.getColumnIndexOrThrow(_cursor, "identifier");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
      final int _cursorIndexOfLanguageName = CursorUtil.getColumnIndexOrThrow(_cursor, "languageName");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "direction");
      final int _cursorIndexOfIsDownloaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isDownloaded");
      final int _cursorIndexOfDownloadProgress = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadProgress");
      final int _cursorIndexOfDownloadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadedAt");
      final EditionInfo _result;
      if (_cursor.moveToFirst()) {
        _result = new EditionInfo();
        if (_cursor.isNull(_cursorIndexOfIdentifier)) {
          _result.identifier = null;
        } else {
          _result.identifier = _cursor.getString(_cursorIndexOfIdentifier);
        }
        if (_cursor.isNull(_cursorIndexOfName)) {
          _result.name = null;
        } else {
          _result.name = _cursor.getString(_cursorIndexOfName);
        }
        if (_cursor.isNull(_cursorIndexOfLanguage)) {
          _result.language = null;
        } else {
          _result.language = _cursor.getString(_cursorIndexOfLanguage);
        }
        if (_cursor.isNull(_cursorIndexOfLanguageName)) {
          _result.languageName = null;
        } else {
          _result.languageName = _cursor.getString(_cursorIndexOfLanguageName);
        }
        if (_cursor.isNull(_cursorIndexOfType)) {
          _result.type = null;
        } else {
          _result.type = _cursor.getString(_cursorIndexOfType);
        }
        if (_cursor.isNull(_cursorIndexOfDirection)) {
          _result.direction = null;
        } else {
          _result.direction = _cursor.getString(_cursorIndexOfDirection);
        }
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsDownloaded);
        _result.isDownloaded = _tmp != 0;
        _result.downloadProgress = _cursor.getInt(_cursorIndexOfDownloadProgress);
        _result.downloadedAt = _cursor.getLong(_cursorIndexOfDownloadedAt);
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
  public List<EditionInfo> getAll() {
    final String _sql = "SELECT * FROM edition_info ORDER BY language, name";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfIdentifier = CursorUtil.getColumnIndexOrThrow(_cursor, "identifier");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
      final int _cursorIndexOfLanguageName = CursorUtil.getColumnIndexOrThrow(_cursor, "languageName");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "direction");
      final int _cursorIndexOfIsDownloaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isDownloaded");
      final int _cursorIndexOfDownloadProgress = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadProgress");
      final int _cursorIndexOfDownloadedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "downloadedAt");
      final List<EditionInfo> _result = new ArrayList<EditionInfo>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final EditionInfo _item;
        _item = new EditionInfo();
        if (_cursor.isNull(_cursorIndexOfIdentifier)) {
          _item.identifier = null;
        } else {
          _item.identifier = _cursor.getString(_cursorIndexOfIdentifier);
        }
        if (_cursor.isNull(_cursorIndexOfName)) {
          _item.name = null;
        } else {
          _item.name = _cursor.getString(_cursorIndexOfName);
        }
        if (_cursor.isNull(_cursorIndexOfLanguage)) {
          _item.language = null;
        } else {
          _item.language = _cursor.getString(_cursorIndexOfLanguage);
        }
        if (_cursor.isNull(_cursorIndexOfLanguageName)) {
          _item.languageName = null;
        } else {
          _item.languageName = _cursor.getString(_cursorIndexOfLanguageName);
        }
        if (_cursor.isNull(_cursorIndexOfType)) {
          _item.type = null;
        } else {
          _item.type = _cursor.getString(_cursorIndexOfType);
        }
        if (_cursor.isNull(_cursorIndexOfDirection)) {
          _item.direction = null;
        } else {
          _item.direction = _cursor.getString(_cursorIndexOfDirection);
        }
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsDownloaded);
        _item.isDownloaded = _tmp != 0;
        _item.downloadProgress = _cursor.getInt(_cursorIndexOfDownloadProgress);
        _item.downloadedAt = _cursor.getLong(_cursorIndexOfDownloadedAt);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int getCount() {
    final String _sql = "SELECT COUNT(*) FROM edition_info";
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
