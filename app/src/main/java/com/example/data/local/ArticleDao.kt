package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles WHERE isBookmarked = 1 ORDER BY savedAt DESC")
    fun getBookmarkedArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE isBookmarked = 1 ORDER BY savedAt DESC")
    suspend fun getBookmarkedArticlesSync(): List<ArticleEntity>

    @Query("SELECT id FROM articles WHERE isBookmarked = 1")
    fun getBookmarkedIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(article: ArticleEntity)

    @Query("UPDATE articles SET isBookmarked = :bookmarked, savedAt = :savedAt WHERE id = :id")
    suspend fun setBookmark(id: String, bookmarked: Boolean, savedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM articles WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM articles WHERE id = :id AND isBookmarked = 1)")
    suspend fun isBookmarked(id: String): Boolean
}
