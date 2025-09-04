package com.cappielloantonio.tempo.repository

import androidx.lifecycle.LiveData
import androidx.media3.common.util.UnstableApi
import com.cappielloantonio.tempo.database.AppDatabase
import com.cappielloantonio.tempo.database.dao.ServerDao
import com.cappielloantonio.tempo.model.Server

@UnstableApi
class ServerRepository {
    private val serverDao: ServerDao = AppDatabase.Companion.instance.serverDao()

    val liveServer: LiveData<MutableList<Server>>?
        get() = serverDao.all

    fun insert(server: Server?) {
        val insert = InsertThreadSafe(serverDao, server)
        val thread = Thread(insert)
        thread.start()
    }

    fun delete(server: Server?) {
        val delete = DeleteThreadSafe(serverDao, server)
        val thread = Thread(delete)
        thread.start()
    }

    private class InsertThreadSafe(
        private val serverDao: ServerDao,
        private val server: Server?,
    ) : Runnable {
        override fun run() {
            serverDao.insert(server)
        }
    }

    private class DeleteThreadSafe(
        private val serverDao: ServerDao,
        private val server: Server?,
    ) : Runnable {
        override fun run() {
            serverDao.delete(server)
        }
    }

    companion object {
        private const val TAG = "QueueRepository"
    }
}
