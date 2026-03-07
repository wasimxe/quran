package com.tanxe.quran.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tanxe.quran.data.entity.WordByWord;

import java.util.List;

@Dao
public interface WordByWordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<WordByWord> words);

    @Query("SELECT * FROM word_by_word WHERE surahNumber = :surah AND ayahNumber = :ayah AND language = :language ORDER BY wordPosition")
    List<WordByWord> getWords(int surah, int ayah, String language);

    @Query("SELECT DISTINCT language FROM word_by_word")
    List<String> getAvailableLanguages();

    @Query("SELECT COUNT(*) FROM word_by_word WHERE language = :language")
    int getWordCount(String language);

    @Query("DELETE FROM word_by_word WHERE language = :language")
    void deleteLanguage(String language);

    @Query("SELECT arabicWord, COUNT(*) as frequency FROM word_by_word WHERE language = :language GROUP BY arabicWord ORDER BY frequency DESC")
    List<WordFrequency> getWordFrequencies(String language);

    @Query("SELECT arabicWord, COUNT(*) as frequency FROM word_by_word WHERE language = :language AND surahNumber = :surah GROUP BY arabicWord ORDER BY frequency DESC")
    List<WordFrequency> getWordFrequenciesBySurah(String language, int surah);

    @Query("SELECT arabicWord, translation, COUNT(*) as frequency FROM word_by_word WHERE language = :language GROUP BY arabicWord ORDER BY frequency DESC")
    List<WordWithTranslation> getWordsWithTranslations(String language);

    @Query("SELECT arabicWord, translation, COUNT(*) as frequency FROM word_by_word WHERE language = :language AND surahNumber = :surah GROUP BY arabicWord ORDER BY frequency DESC")
    List<WordWithTranslation> getWordsWithTranslationsBySurah(String language, int surah);

    /** Get all distinct translations for a specific Arabic word */
    @Query("SELECT translation, COUNT(*) as cnt FROM word_by_word WHERE language = :language AND arabicWord = :arabicWord GROUP BY translation ORDER BY cnt DESC")
    List<TranslationCount> getTranslationsForWord(String language, String arabicWord);

    static class TranslationCount {
        public String translation;
        public int cnt;
    }

    static class WordFrequency {
        public String arabicWord;
        public int frequency;
    }

    static class WordWithTranslation {
        public String arabicWord;
        public String translation;
        public int frequency;
    }
}
