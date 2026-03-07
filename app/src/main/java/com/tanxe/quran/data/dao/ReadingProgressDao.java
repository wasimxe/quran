package com.tanxe.quran.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.tanxe.quran.data.entity.ReadingProgress;

import java.util.List;

@Dao
public interface ReadingProgressDao {
    @Insert
    void insert(ReadingProgress progress);

    @Query("SELECT COALESCE(SUM(sessionDurationSeconds), 0) FROM reading_progress")
    int getTotalReadTimeSeconds();

    @Query("SELECT COALESCE(SUM(sessionDurationSeconds), 0) FROM reading_progress WHERE timestamp >= :startOfDay")
    int getTodayReadTimeSeconds(long startOfDay);

    @Query("SELECT COUNT(DISTINCT (surahNumber * 1000 + ayahNumber)) FROM reading_progress")
    int getUniqueAyahsRead();

    @Query("SELECT * FROM reading_progress ORDER BY timestamp DESC LIMIT :limit")
    List<ReadingProgress> getRecentProgress(int limit);

    @Query("SELECT COUNT(DISTINCT strftime('%Y-%m-%d', timestamp / 1000, 'unixepoch')) FROM reading_progress WHERE timestamp >= :since")
    int getReadingDaysCount(long since);
}
