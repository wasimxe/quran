package com.tanxe.quran.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tanxe.quran.data.entity.Bookmark;

import java.util.List;

@Dao
public interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Bookmark bookmark);

    @Delete
    void delete(Bookmark bookmark);

    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    List<Bookmark> getAllBookmarks();

    @Query("SELECT * FROM bookmarks WHERE surahNumber = :surah AND ayahNumber = :ayah LIMIT 1")
    Bookmark getBookmark(int surah, int ayah);

    @Query("SELECT COUNT(*) FROM bookmarks WHERE surahNumber = :surah AND ayahNumber = :ayah")
    int isBookmarked(int surah, int ayah);

    @Query("DELETE FROM bookmarks WHERE surahNumber = :surah AND ayahNumber = :ayah")
    void deleteByAyah(int surah, int ayah);

    @Query("DELETE FROM bookmarks")
    void deleteAll();
}
