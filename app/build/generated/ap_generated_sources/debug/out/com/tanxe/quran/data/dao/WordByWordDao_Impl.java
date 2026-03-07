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
import com.tanxe.quran.data.entity.WordByWord;
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
public final class WordByWordDao_Impl implements WordByWordDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<WordByWord> __insertionAdapterOfWordByWord;

  private final SharedSQLiteStatement __preparedStmtOfDeleteLanguage;

  public WordByWordDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfWordByWord = new EntityInsertionAdapter<WordByWord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `word_by_word` (`id`,`surahNumber`,`ayahNumber`,`wordPosition`,`arabicWord`,`translation`,`transliteration`,`language`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final WordByWord entity) {
        statement.bindLong(1, entity.id);
        statement.bindLong(2, entity.surahNumber);
        statement.bindLong(3, entity.ayahNumber);
        statement.bindLong(4, entity.wordPosition);
        if (entity.arabicWord == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.arabicWord);
        }
        if (entity.translation == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.translation);
        }
        if (entity.transliteration == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.transliteration);
        }
        if (entity.language == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.language);
        }
      }
    };
    this.__preparedStmtOfDeleteLanguage = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM word_by_word WHERE language = ?";
        return _query;
      }
    };
  }

  @Override
  public void insertAll(final List<WordByWord> words) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfWordByWord.insert(words);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteLanguage(final String language) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteLanguage.acquire();
    int _argIndex = 1;
    if (language == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, language);
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
      __preparedStmtOfDeleteLanguage.release(_stmt);
    }
  }

  @Override
  public List<WordByWord> getWords(final int surah, final int ayah, final String language) {
    final String _sql = "SELECT * FROM word_by_word WHERE surahNumber = ? AND ayahNumber = ? AND language = ? ORDER BY wordPosition";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, surah);
    _argIndex = 2;
    _statement.bindLong(_argIndex, ayah);
    _argIndex = 3;
    if (language == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, language);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfSurahNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "surahNumber");
      final int _cursorIndexOfAyahNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "ayahNumber");
      final int _cursorIndexOfWordPosition = CursorUtil.getColumnIndexOrThrow(_cursor, "wordPosition");
      final int _cursorIndexOfArabicWord = CursorUtil.getColumnIndexOrThrow(_cursor, "arabicWord");
      final int _cursorIndexOfTranslation = CursorUtil.getColumnIndexOrThrow(_cursor, "translation");
      final int _cursorIndexOfTransliteration = CursorUtil.getColumnIndexOrThrow(_cursor, "transliteration");
      final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
      final List<WordByWord> _result = new ArrayList<WordByWord>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final WordByWord _item;
        _item = new WordByWord();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.surahNumber = _cursor.getInt(_cursorIndexOfSurahNumber);
        _item.ayahNumber = _cursor.getInt(_cursorIndexOfAyahNumber);
        _item.wordPosition = _cursor.getInt(_cursorIndexOfWordPosition);
        if (_cursor.isNull(_cursorIndexOfArabicWord)) {
          _item.arabicWord = null;
        } else {
          _item.arabicWord = _cursor.getString(_cursorIndexOfArabicWord);
        }
        if (_cursor.isNull(_cursorIndexOfTranslation)) {
          _item.translation = null;
        } else {
          _item.translation = _cursor.getString(_cursorIndexOfTranslation);
        }
        if (_cursor.isNull(_cursorIndexOfTransliteration)) {
          _item.transliteration = null;
        } else {
          _item.transliteration = _cursor.getString(_cursorIndexOfTransliteration);
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
  public List<String> getAvailableLanguages() {
    final String _sql = "SELECT DISTINCT language FROM word_by_word";
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
  public int getWordCount(final String language) {
    final String _sql = "SELECT COUNT(*) FROM word_by_word WHERE language = ?";
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
  public List<WordByWordDao.WordFrequency> getWordFrequencies(final String language) {
    final String _sql = "SELECT arabicWord, COUNT(*) as frequency FROM word_by_word WHERE language = ? GROUP BY arabicWord ORDER BY frequency DESC";
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
      final int _cursorIndexOfArabicWord = 0;
      final int _cursorIndexOfFrequency = 1;
      final List<WordByWordDao.WordFrequency> _result = new ArrayList<WordByWordDao.WordFrequency>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final WordByWordDao.WordFrequency _item;
        _item = new WordByWordDao.WordFrequency();
        if (_cursor.isNull(_cursorIndexOfArabicWord)) {
          _item.arabicWord = null;
        } else {
          _item.arabicWord = _cursor.getString(_cursorIndexOfArabicWord);
        }
        _item.frequency = _cursor.getInt(_cursorIndexOfFrequency);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<WordByWordDao.WordFrequency> getWordFrequenciesBySurah(final String language,
      final int surah) {
    final String _sql = "SELECT arabicWord, COUNT(*) as frequency FROM word_by_word WHERE language = ? AND surahNumber = ? GROUP BY arabicWord ORDER BY frequency DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (language == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, language);
    }
    _argIndex = 2;
    _statement.bindLong(_argIndex, surah);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfArabicWord = 0;
      final int _cursorIndexOfFrequency = 1;
      final List<WordByWordDao.WordFrequency> _result = new ArrayList<WordByWordDao.WordFrequency>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final WordByWordDao.WordFrequency _item;
        _item = new WordByWordDao.WordFrequency();
        if (_cursor.isNull(_cursorIndexOfArabicWord)) {
          _item.arabicWord = null;
        } else {
          _item.arabicWord = _cursor.getString(_cursorIndexOfArabicWord);
        }
        _item.frequency = _cursor.getInt(_cursorIndexOfFrequency);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<WordByWordDao.WordWithTranslation> getWordsWithTranslations(final String language) {
    final String _sql = "SELECT arabicWord, translation, COUNT(*) as frequency FROM word_by_word WHERE language = ? GROUP BY arabicWord ORDER BY frequency DESC";
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
      final int _cursorIndexOfArabicWord = 0;
      final int _cursorIndexOfTranslation = 1;
      final int _cursorIndexOfFrequency = 2;
      final List<WordByWordDao.WordWithTranslation> _result = new ArrayList<WordByWordDao.WordWithTranslation>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final WordByWordDao.WordWithTranslation _item;
        _item = new WordByWordDao.WordWithTranslation();
        if (_cursor.isNull(_cursorIndexOfArabicWord)) {
          _item.arabicWord = null;
        } else {
          _item.arabicWord = _cursor.getString(_cursorIndexOfArabicWord);
        }
        if (_cursor.isNull(_cursorIndexOfTranslation)) {
          _item.translation = null;
        } else {
          _item.translation = _cursor.getString(_cursorIndexOfTranslation);
        }
        _item.frequency = _cursor.getInt(_cursorIndexOfFrequency);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<WordByWordDao.WordWithTranslation> getWordsWithTranslationsBySurah(
      final String language, final int surah) {
    final String _sql = "SELECT arabicWord, translation, COUNT(*) as frequency FROM word_by_word WHERE language = ? AND surahNumber = ? GROUP BY arabicWord ORDER BY frequency DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (language == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, language);
    }
    _argIndex = 2;
    _statement.bindLong(_argIndex, surah);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfArabicWord = 0;
      final int _cursorIndexOfTranslation = 1;
      final int _cursorIndexOfFrequency = 2;
      final List<WordByWordDao.WordWithTranslation> _result = new ArrayList<WordByWordDao.WordWithTranslation>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final WordByWordDao.WordWithTranslation _item;
        _item = new WordByWordDao.WordWithTranslation();
        if (_cursor.isNull(_cursorIndexOfArabicWord)) {
          _item.arabicWord = null;
        } else {
          _item.arabicWord = _cursor.getString(_cursorIndexOfArabicWord);
        }
        if (_cursor.isNull(_cursorIndexOfTranslation)) {
          _item.translation = null;
        } else {
          _item.translation = _cursor.getString(_cursorIndexOfTranslation);
        }
        _item.frequency = _cursor.getInt(_cursorIndexOfFrequency);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<WordByWordDao.TranslationCount> getTranslationsForWord(final String language,
      final String arabicWord) {
    final String _sql = "SELECT translation, COUNT(*) as cnt FROM word_by_word WHERE language = ? AND arabicWord = ? GROUP BY translation ORDER BY cnt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (language == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, language);
    }
    _argIndex = 2;
    if (arabicWord == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, arabicWord);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfTranslation = 0;
      final int _cursorIndexOfCnt = 1;
      final List<WordByWordDao.TranslationCount> _result = new ArrayList<WordByWordDao.TranslationCount>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final WordByWordDao.TranslationCount _item;
        _item = new WordByWordDao.TranslationCount();
        if (_cursor.isNull(_cursorIndexOfTranslation)) {
          _item.translation = null;
        } else {
          _item.translation = _cursor.getString(_cursorIndexOfTranslation);
        }
        _item.cnt = _cursor.getInt(_cursorIndexOfCnt);
        _result.add(_item);
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
