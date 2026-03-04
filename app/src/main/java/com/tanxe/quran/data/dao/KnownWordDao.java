package com.tanxe.quran.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Delete;

import com.tanxe.quran.data.entity.KnownWord;

import java.util.List;

@Dao
public interface KnownWordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(KnownWord word);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<KnownWord> words);

    @Delete
    void delete(KnownWord word);

    @Query("SELECT * FROM known_words ORDER BY learnedAt DESC")
    List<KnownWord> getAllKnownWords();

    @Query("SELECT COUNT(*) FROM known_words WHERE arabicWord = :word")
    int isKnown(String word);

    @Query("SELECT COUNT(*) FROM known_words")
    int getKnownCount();

    @Query("SELECT SUM(frequency) FROM known_words")
    int getTotalKnownFrequency();

    @Query("DELETE FROM known_words WHERE arabicWord = :word")
    void deleteWord(String word);

    @Query("DELETE FROM known_words")
    void deleteAll();
}
