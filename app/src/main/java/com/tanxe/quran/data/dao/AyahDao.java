package com.tanxe.quran.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tanxe.quran.data.entity.Ayah;

import java.util.List;

@Dao
public interface AyahDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Ayah> ayahs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Ayah ayah);

    @Query("SELECT * FROM ayahs WHERE surahNumber = :surah AND ayahNumber = :ayah LIMIT 1")
    Ayah getAyah(int surah, int ayah);

    @Query("SELECT * FROM ayahs WHERE surahNumber = :surah ORDER BY ayahNumber")
    List<Ayah> getAyahsBySurah(int surah);

    @Query("SELECT * FROM ayahs WHERE juzNumber = :juz ORDER BY surahNumber, ayahNumber")
    List<Ayah> getAyahsByJuz(int juz);

    @Query("SELECT COUNT(*) FROM ayahs WHERE surahNumber = :surah")
    int getAyahCount(int surah);

    @Query("SELECT COUNT(*) FROM ayahs")
    int getTotalAyahCount();

    @Query("SELECT DISTINCT surahNumber, surahNameEn, surahNameAr FROM ayahs ORDER BY surahNumber")
    List<SurahInfo> getAllSurahs();

    @Query("SELECT * FROM ayahs WHERE arabicText LIKE '%' || :query || '%' OR defaultTranslation LIKE '%' || :query || '%' ORDER BY surahNumber, ayahNumber")
    List<Ayah> searchAyahs(String query);

    @Query("SELECT * FROM ayahs WHERE arabicText LIKE '%' || :query || '%' ORDER BY surahNumber, ayahNumber")
    List<Ayah> searchArabic(String query);

    @Query("SELECT * FROM ayahs WHERE defaultTranslation LIKE '%' || :query || '%' ORDER BY surahNumber, ayahNumber")
    List<Ayah> searchTranslation(String query);

    @Query("SELECT MAX(ayahNumber) FROM ayahs WHERE surahNumber = :surah")
    int getMaxAyahInSurah(int surah);

    @Query("SELECT * FROM ayahs ORDER BY RANDOM() LIMIT 1")
    Ayah getRandomAyah();

    static class SurahInfo {
        public int surahNumber;
        public String surahNameEn;
        public String surahNameAr;
    }
}
