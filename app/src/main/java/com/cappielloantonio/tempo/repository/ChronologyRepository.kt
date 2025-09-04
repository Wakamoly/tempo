package com.cappielloantonio.tempo.repository

import androidx.lifecycle.LiveData
import androidx.media3.common.util.UnstableApi
import com.cappielloantonio.tempo.database.AppDatabase
import com.cappielloantonio.tempo.database.dao.ChronologyDao
import com.cappielloantonio.tempo.model.Chronology

@UnstableApi
class ChronologyRepository {
    private val chronologyDao: ChronologyDao = AppDatabase.Companion.instance.chronologyDao()

    fun getChronology(
        server: String?,
        start: Long,
        end: Long,
    ): LiveData<MutableList<Chronology>> = chronologyDao.getAllFrom(start, end, server)

    fun insert(item: Chronology) {
        val insert = InsertThreadSafe(chronologyDao, item)
        val thread = Thread(insert)
        thread.start()
    }

    private class InsertThreadSafe(
        private val chronologyDao: ChronologyDao,
        private val item: Chronology,
    ) : Runnable {
        override fun run() {
            chronologyDao.insert(item)
        }
    }
}
