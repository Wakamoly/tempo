package com.cappielloantonio.tempo.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cappielloantonio.tempo.model.RecentSearch

@Dao
interface RecentSearchDao {
    @get:Query("SELECT * FROM recent_search ORDER BY search DESC")
    val recent: MutableList<String?>?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insert(search: RecentSearch?)

    @Delete
    fun delete(search: RecentSearch?)
}
