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
import com.tanxe.quran.data.entity.ReciterInfo;
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
public final class ReciterInfoDao_Impl implements ReciterInfoDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ReciterInfo> __insertionAdapterOfReciterInfo;

  private final SharedSQLiteStatement __preparedStmtOfUpdateDownloadState;

  public ReciterInfoDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfReciterInfo = new EntityInsertionAdapter<ReciterInfo>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `reciter_info` (`identifier`,`name`,`style`,`subfolder`,`bitrate`,`isDownloaded`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final ReciterInfo entity) {
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
        if (entity.style == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.style);
        }
        if (entity.subfolder == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.subfolder);
        }
        statement.bindLong(5, entity.bitrate);
        final int _tmp = entity.isDownloaded ? 1 : 0;
        statement.bindLong(6, _tmp);
      }
    };
    this.__preparedStmtOfUpdateDownloadState = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE reciter_info SET isDownloaded = ? WHERE identifier = ?";
        return _query;
      }
    };
  }

  @Override
  public void insertAll(final List<ReciterInfo> reciters) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfReciterInfo.insert(reciters);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void updateDownloadState(final String identifier, final boolean downloaded) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateDownloadState.acquire();
    int _argIndex = 1;
    final int _tmp = downloaded ? 1 : 0;
    _stmt.bindLong(_argIndex, _tmp);
    _argIndex = 2;
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
  public List<ReciterInfo> getAll() {
    final String _sql = "SELECT * FROM reciter_info ORDER BY name";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfIdentifier = CursorUtil.getColumnIndexOrThrow(_cursor, "identifier");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfStyle = CursorUtil.getColumnIndexOrThrow(_cursor, "style");
      final int _cursorIndexOfSubfolder = CursorUtil.getColumnIndexOrThrow(_cursor, "subfolder");
      final int _cursorIndexOfBitrate = CursorUtil.getColumnIndexOrThrow(_cursor, "bitrate");
      final int _cursorIndexOfIsDownloaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isDownloaded");
      final List<ReciterInfo> _result = new ArrayList<ReciterInfo>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ReciterInfo _item;
        _item = new ReciterInfo();
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
        if (_cursor.isNull(_cursorIndexOfStyle)) {
          _item.style = null;
        } else {
          _item.style = _cursor.getString(_cursorIndexOfStyle);
        }
        if (_cursor.isNull(_cursorIndexOfSubfolder)) {
          _item.subfolder = null;
        } else {
          _item.subfolder = _cursor.getString(_cursorIndexOfSubfolder);
        }
        _item.bitrate = _cursor.getInt(_cursorIndexOfBitrate);
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsDownloaded);
        _item.isDownloaded = _tmp != 0;
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<ReciterInfo> getDownloaded() {
    final String _sql = "SELECT * FROM reciter_info WHERE isDownloaded = 1 ORDER BY name";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfIdentifier = CursorUtil.getColumnIndexOrThrow(_cursor, "identifier");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfStyle = CursorUtil.getColumnIndexOrThrow(_cursor, "style");
      final int _cursorIndexOfSubfolder = CursorUtil.getColumnIndexOrThrow(_cursor, "subfolder");
      final int _cursorIndexOfBitrate = CursorUtil.getColumnIndexOrThrow(_cursor, "bitrate");
      final int _cursorIndexOfIsDownloaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isDownloaded");
      final List<ReciterInfo> _result = new ArrayList<ReciterInfo>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ReciterInfo _item;
        _item = new ReciterInfo();
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
        if (_cursor.isNull(_cursorIndexOfStyle)) {
          _item.style = null;
        } else {
          _item.style = _cursor.getString(_cursorIndexOfStyle);
        }
        if (_cursor.isNull(_cursorIndexOfSubfolder)) {
          _item.subfolder = null;
        } else {
          _item.subfolder = _cursor.getString(_cursorIndexOfSubfolder);
        }
        _item.bitrate = _cursor.getInt(_cursorIndexOfBitrate);
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsDownloaded);
        _item.isDownloaded = _tmp != 0;
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public ReciterInfo getByIdentifier(final String identifier) {
    final String _sql = "SELECT * FROM reciter_info WHERE identifier = ?";
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
      final int _cursorIndexOfStyle = CursorUtil.getColumnIndexOrThrow(_cursor, "style");
      final int _cursorIndexOfSubfolder = CursorUtil.getColumnIndexOrThrow(_cursor, "subfolder");
      final int _cursorIndexOfBitrate = CursorUtil.getColumnIndexOrThrow(_cursor, "bitrate");
      final int _cursorIndexOfIsDownloaded = CursorUtil.getColumnIndexOrThrow(_cursor, "isDownloaded");
      final ReciterInfo _result;
      if (_cursor.moveToFirst()) {
        _result = new ReciterInfo();
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
        if (_cursor.isNull(_cursorIndexOfStyle)) {
          _result.style = null;
        } else {
          _result.style = _cursor.getString(_cursorIndexOfStyle);
        }
        if (_cursor.isNull(_cursorIndexOfSubfolder)) {
          _result.subfolder = null;
        } else {
          _result.subfolder = _cursor.getString(_cursorIndexOfSubfolder);
        }
        _result.bitrate = _cursor.getInt(_cursorIndexOfBitrate);
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsDownloaded);
        _result.isDownloaded = _tmp != 0;
      } else {
        _result = null;
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
