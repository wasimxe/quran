package com.tanxe.quran.data.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.tanxe.quran.data.entity.Ayah;
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
public final class AyahDao_Impl implements AyahDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Ayah> __insertionAdapterOfAyah;

  public AyahDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAyah = new EntityInsertionAdapter<Ayah>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `ayahs` (`id`,`surahNumber`,`ayahNumber`,`surahNameEn`,`surahNameAr`,`arabicText`,`defaultTranslation`,`juzNumber`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Ayah entity) {
        statement.bindLong(1, entity.id);
        statement.bindLong(2, entity.surahNumber);
        statement.bindLong(3, entity.ayahNumber);
        if (entity.surahNameEn == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.surahNameEn);
        }
        if (entity.surahNameAr == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.surahNameAr);
        }
        if (entity.arabicText == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.arabicText);
        }
        if (entity.defaultTranslation == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.defaultTranslation);
        }
        statement.bindLong(8, entity.juzNumber);
      }
    };
  }

  @Override
  public void insertAll(final List<Ayah> ayahs) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfAyah.insert(ayahs);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insert(final Ayah ayah) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfAyah.insert(ayah);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public Ayah getAyah(final int surah, final int ayah) {
    final String _sql = "SELECT * FROM ayahs WHERE surahNumber = ? AND ayahNumber = ? LIMIT 1";
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
      final int _cursorIndexOfSurahNameEn = CursorUtil.getColumnIndexOrThrow(_cursor, "surahNameEn");
      final int _cursorIndexOfSurahNameAr = CursorUtil.getColumnIndexOrThrow(_cursor, "surahNameAr");
      final int _cursorIndexOfArabicText = CursorUtil.getColumnIndexOrThrow(_cursor, "arabicText");
      final int _cursorIndexOfDefaultTranslation = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultTranslation");
      final int _cursorIndexOfJuzNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "juzNumber");
      final Ayah _result;
      if (_cursor.moveToFirst()) {
        _result = new Ayah();
        _result.id = _cursor.getInt(_cursorIndexOfId);
        _result.surahNumber = _cursor.getInt(_cursorIndexOfSurahNumber);
        _result.ayahNumber = _cursor.getInt(_cursorIndexOfAyahNumber);
        if (_cursor.isNull(_cursorIndexOfSurahNameEn)) {
          _result.surahNameEn = null;
        } else {
          _result.surahNameEn = _cursor.getString(_cursorIndexOfSurahNameEn);
        }
        if (_cursor.isNull(_cursorIndexOfSurahNameAr)) {
          _result.surahNameAr = null;
        } else {
          _result.surahNameAr = _cursor.getString(_cursorIndexOfSurahNameAr);
        }
        if (_cursor.isNull(_cursorIndexOfArabicText)) {
          _result.arabicText = null;
        } else {
          _result.arabicText = _cursor.getString(_cursorIndexOfArabicText);
        }
        if (_cursor.isNull(_cursorIndexOfDefaultTranslation)) {
          _result.defaultTranslation = null;
        } else {
          _result.defaultTranslation = _cursor.getString(_cursorIndexOfDefaultTranslation);
        }
        _result.juzNumber = _cursor.getInt(_cursorIndexOfJuzNumber);
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
  public List<Ayah> getAyahsBySurah(final int surah) {
    final String _sql = "SELECT * FROM ayahs WHERE surahNumber = ? ORDER BY ayahNumber";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, surah);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfSurahNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "surahNumber");
      final int _cursorIndexOfAyahNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "ayahNumber");
      final int _cursorIndexOfSurahNameEn = CursorUtil.getColumnIndexOrThrow(_cursor, "surahNameEn");
      final int _cursorIndexOfSurahNameAr = CursorUtil.getColumnIndexOrThrow(_cursor, "surahNameAr");
      final int _cursorIndexOfArabicText = CursorUtil.getColumnIndexOrThrow(_cursor, "arabicText");
      final int _cursorIndexOfDefaultTranslation = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultTranslation");
      final int _cursorIndexOfJuzNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "juzNumber");
      final List<Ayah> _result = new ArrayList<Ayah>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Ayah _item;
        _item = new Ayah();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.surahNumber = _cursor.getInt(_cursorIndexOfSurahNumber);
        _item.ayahNumber = _cursor.getInt(_cursorIndexOfAyahNumber);
        if (_cursor.isNull(_cursorIndexOfSurahNameEn)) {
          _item.surahNameEn = null;
        } else {
          _item.surahNameEn = _cursor.getString(_cursorIndexOfSurahNameEn);
        }
        if (_cursor.isNull(_cursorIndexOfSurahNameAr)) {
          _item.surahNameAr = null;
        } else {
          _item.surahNameAr = _cursor.getString(_cursorIndexOfSurahNameAr);
        }
        if (_cursor.isNull(_cursorIndexOfArabicText)) {
          _item.arabicText = null;
        } else {
          _item.arabicText = _cursor.getString(_cursorIndexOfArabicText);
        }
        if (_cursor.isNull(_cursorIndexOfDefaultTranslation)) {
          _item.defaultTranslation = null;
        } else {
          _item.defaultTranslation = _cursor.getString(_cursorIndexOfDefaultTranslation);
        }
        _item.juzNumber = _cursor.getInt(_cursorIndexOfJuzNumber);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<Ayah> getAyahsByJuz(final int juz) {
    final String _sql = "SELECT * FROM ayahs WHERE juzNumber = ? ORDER BY surahNumber, ayahNumber";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, juz);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfSurahNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "surahNumber");
      final int _cursorIndexOfAyahNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "ayahNumber");
      final int _cursorIndexOfSurahNameEn = CursorUtil.getColumnIndexOrThrow(_cursor, "surahNameEn");
      final int _cursorIndexOfSurahNameAr = CursorUtil.getColumnIndexOrThrow(_cursor, "surahNameAr");
      final int _cursorIndexOfArabicText = CursorUtil.getColumnIndexOrThrow(_cursor, "arabicText");
      final int _cursorIndexOfDefaultTranslation = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultTranslation");
      final int _cursorIndexOfJuzNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "juzNumber");
      final List<Ayah> _result = new ArrayList<Ayah>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Ayah _item;
        _item = new Ayah();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.surahNumber = _cursor.getInt(_cursorIndexOfSurahNumber);
        _item.ayahNumber = _cursor.getInt(_cursorIndexOfAyahNumber);
        if (_cursor.isNull(_cursorIndexOfSurahNameEn)) {
          _item.surahNameEn = null;
        } else {
          _item.surahNameEn = _cursor.getString(_cursorIndexOfSurahNameEn);
        }
        if (_cursor.isNull(_cursorIndexOfSurahNameAr)) {
          _item.surahNameAr = null;
        } else {
          _item.surahNameAr = _cursor.getString(_cursorIndexOfSurahNameAr);
        }
        if (_cursor.isNull(_cursorIndexOfArabicText)) {
          _item.arabicText = null;
        } else {
          _item.arabicText = _cursor.getString(_cursorIndexOfArabicText);
        }
        if (_cursor.isNull(_cursorIndexOfDefaultTranslation)) {
          _item.defaultTranslation = null;
        } else {
          _item.defaultTranslation = _cursor.getString(_cursorIndexOfDefaultTranslation);
        }
        _item.juzNumber = _cursor.getInt(_cursorIndexOfJuzNumber);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int getAyahCount(final int surah) {
    final String _sql = "SELECT COUNT(*) FROM ayahs WHERE surahNumber = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, surah);
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
  public int getTotalAyahCount() {
    final String _sql = "SELECT COUNT(*) FROM ayahs";
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
  public List<AyahDao.SurahInfo> getAllSurahs() {
    final String _sql = "SELECT DISTINCT surahNumber, surahNameEn, surahNameAr FROM ayahs ORDER BY surahNumber";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfSurahNumber = 0;
      final int _cursorIndexOfSurahNameEn = 1;
      final int _cursorIndexOfSurahNameAr = 2;
      final List<AyahDao.SurahInfo> _result = new ArrayList<AyahDao.SurahInfo>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final AyahDao.SurahInfo _item;
        _item = new AyahDao.SurahInfo();
        _item.surahNumber = _cursor.getInt(_cursorIndexOfSurahNumber);
        if (_cursor.isNull(_cursorIndexOfSurahNameEn)) {
          _item.surahNameEn = null;
        } else {
          _item.surahNameEn = _cursor.getString(_cursorIndexOfSurahNameEn);
        }
        if (_cursor.isNull(_cursorIndexOfSurahNameAr)) {
          _item.surahNameAr = null;
        } else {
          _item.surahNameAr = _cursor.getString(_cursorIndexOfSurahNameAr);
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
  public List<Ayah> searchAyahs(final String query) {
    final String _sql = "SELECT * FROM ayahs WHERE arabicText LIKE '%' || ? || '%' OR defaultTranslation LIKE '%' || ? || '%' ORDER BY surahNumber, ayahNumber";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (query == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, query);
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
      final int _cursorIndexOfSurahNameEn = CursorUtil.getColumnIndexOrThrow(_cursor, "surahNameEn");
      final int _cursorIndexOfSurahNameAr = CursorUtil.getColumnIndexOrThrow(_cursor, "surahNameAr");
      final int _cursorIndexOfArabicText = CursorUtil.getColumnIndexOrThrow(_cursor, "arabicText");
      final int _cursorIndexOfDefaultTranslation = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultTranslation");
      final int _cursorIndexOfJuzNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "juzNumber");
      final List<Ayah> _result = new ArrayList<Ayah>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Ayah _item;
        _item = new Ayah();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.surahNumber = _cursor.getInt(_cursorIndexOfSurahNumber);
        _item.ayahNumber = _cursor.getInt(_cursorIndexOfAyahNumber);
        if (_cursor.isNull(_cursorIndexOfSurahNameEn)) {
          _item.surahNameEn = null;
        } else {
          _item.surahNameEn = _cursor.getString(_cursorIndexOfSurahNameEn);
        }
        if (_cursor.isNull(_cursorIndexOfSurahNameAr)) {
          _item.surahNameAr = null;
        } else {
          _item.surahNameAr = _cursor.getString(_cursorIndexOfSurahNameAr);
        }
        if (_cursor.isNull(_cursorIndexOfArabicText)) {
          _item.arabicText = null;
        } else {
          _item.arabicText = _cursor.getString(_cursorIndexOfArabicText);
        }
        if (_cursor.isNull(_cursorIndexOfDefaultTranslation)) {
          _item.defaultTranslation = null;
        } else {
          _item.defaultTranslation = _cursor.getString(_cursorIndexOfDefaultTranslation);
        }
        _item.juzNumber = _cursor.getInt(_cursorIndexOfJuzNumber);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<Ayah> searchArabic(final String query) {
    final String _sql = "SELECT * FROM ayahs WHERE arabicText LIKE '%' || ? || '%' ORDER BY surahNumber, ayahNumber";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
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
      final int _cursorIndexOfSurahNameEn = CursorUtil.getColumnIndexOrThrow(_cursor, "surahNameEn");
      final int _cursorIndexOfSurahNameAr = CursorUtil.getColumnIndexOrThrow(_cursor, "surahNameAr");
      final int _cursorIndexOfArabicText = CursorUtil.getColumnIndexOrThrow(_cursor, "arabicText");
      final int _cursorIndexOfDefaultTranslation = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultTranslation");
      final int _cursorIndexOfJuzNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "juzNumber");
      final List<Ayah> _result = new ArrayList<Ayah>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Ayah _item;
        _item = new Ayah();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.surahNumber = _cursor.getInt(_cursorIndexOfSurahNumber);
        _item.ayahNumber = _cursor.getInt(_cursorIndexOfAyahNumber);
        if (_cursor.isNull(_cursorIndexOfSurahNameEn)) {
          _item.surahNameEn = null;
        } else {
          _item.surahNameEn = _cursor.getString(_cursorIndexOfSurahNameEn);
        }
        if (_cursor.isNull(_cursorIndexOfSurahNameAr)) {
          _item.surahNameAr = null;
        } else {
          _item.surahNameAr = _cursor.getString(_cursorIndexOfSurahNameAr);
        }
        if (_cursor.isNull(_cursorIndexOfArabicText)) {
          _item.arabicText = null;
        } else {
          _item.arabicText = _cursor.getString(_cursorIndexOfArabicText);
        }
        if (_cursor.isNull(_cursorIndexOfDefaultTranslation)) {
          _item.defaultTranslation = null;
        } else {
          _item.defaultTranslation = _cursor.getString(_cursorIndexOfDefaultTranslation);
        }
        _item.juzNumber = _cursor.getInt(_cursorIndexOfJuzNumber);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<Ayah> searchTranslation(final String query) {
    final String _sql = "SELECT * FROM ayahs WHERE defaultTranslation LIKE '%' || ? || '%' ORDER BY surahNumber, ayahNumber";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
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
      final int _cursorIndexOfSurahNameEn = CursorUtil.getColumnIndexOrThrow(_cursor, "surahNameEn");
      final int _cursorIndexOfSurahNameAr = CursorUtil.getColumnIndexOrThrow(_cursor, "surahNameAr");
      final int _cursorIndexOfArabicText = CursorUtil.getColumnIndexOrThrow(_cursor, "arabicText");
      final int _cursorIndexOfDefaultTranslation = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultTranslation");
      final int _cursorIndexOfJuzNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "juzNumber");
      final List<Ayah> _result = new ArrayList<Ayah>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Ayah _item;
        _item = new Ayah();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.surahNumber = _cursor.getInt(_cursorIndexOfSurahNumber);
        _item.ayahNumber = _cursor.getInt(_cursorIndexOfAyahNumber);
        if (_cursor.isNull(_cursorIndexOfSurahNameEn)) {
          _item.surahNameEn = null;
        } else {
          _item.surahNameEn = _cursor.getString(_cursorIndexOfSurahNameEn);
        }
        if (_cursor.isNull(_cursorIndexOfSurahNameAr)) {
          _item.surahNameAr = null;
        } else {
          _item.surahNameAr = _cursor.getString(_cursorIndexOfSurahNameAr);
        }
        if (_cursor.isNull(_cursorIndexOfArabicText)) {
          _item.arabicText = null;
        } else {
          _item.arabicText = _cursor.getString(_cursorIndexOfArabicText);
        }
        if (_cursor.isNull(_cursorIndexOfDefaultTranslation)) {
          _item.defaultTranslation = null;
        } else {
          _item.defaultTranslation = _cursor.getString(_cursorIndexOfDefaultTranslation);
        }
        _item.juzNumber = _cursor.getInt(_cursorIndexOfJuzNumber);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int getMaxAyahInSurah(final int surah) {
    final String _sql = "SELECT MAX(ayahNumber) FROM ayahs WHERE surahNumber = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, surah);
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
  public Ayah getRandomAyah() {
    final String _sql = "SELECT * FROM ayahs ORDER BY RANDOM() LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfSurahNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "surahNumber");
      final int _cursorIndexOfAyahNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "ayahNumber");
      final int _cursorIndexOfSurahNameEn = CursorUtil.getColumnIndexOrThrow(_cursor, "surahNameEn");
      final int _cursorIndexOfSurahNameAr = CursorUtil.getColumnIndexOrThrow(_cursor, "surahNameAr");
      final int _cursorIndexOfArabicText = CursorUtil.getColumnIndexOrThrow(_cursor, "arabicText");
      final int _cursorIndexOfDefaultTranslation = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultTranslation");
      final int _cursorIndexOfJuzNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "juzNumber");
      final Ayah _result;
      if (_cursor.moveToFirst()) {
        _result = new Ayah();
        _result.id = _cursor.getInt(_cursorIndexOfId);
        _result.surahNumber = _cursor.getInt(_cursorIndexOfSurahNumber);
        _result.ayahNumber = _cursor.getInt(_cursorIndexOfAyahNumber);
        if (_cursor.isNull(_cursorIndexOfSurahNameEn)) {
          _result.surahNameEn = null;
        } else {
          _result.surahNameEn = _cursor.getString(_cursorIndexOfSurahNameEn);
        }
        if (_cursor.isNull(_cursorIndexOfSurahNameAr)) {
          _result.surahNameAr = null;
        } else {
          _result.surahNameAr = _cursor.getString(_cursorIndexOfSurahNameAr);
        }
        if (_cursor.isNull(_cursorIndexOfArabicText)) {
          _result.arabicText = null;
        } else {
          _result.arabicText = _cursor.getString(_cursorIndexOfArabicText);
        }
        if (_cursor.isNull(_cursorIndexOfDefaultTranslation)) {
          _result.defaultTranslation = null;
        } else {
          _result.defaultTranslation = _cursor.getString(_cursorIndexOfDefaultTranslation);
        }
        _result.juzNumber = _cursor.getInt(_cursorIndexOfJuzNumber);
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
