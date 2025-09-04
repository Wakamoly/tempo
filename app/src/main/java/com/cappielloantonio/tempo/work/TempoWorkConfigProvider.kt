package com.cappielloantonio.tempo.work

import android.util.Log
import androidx.work.Configuration
import androidx.work.CoroutineWorker
import androidx.work.DelegatingWorkerFactory
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import org.koin.androidx.workmanager.factory.KoinWorkerFactory

/**
 * Provides configuration to be used for the [WorkManager] instance at app-start. Note that this applies to all work
 * instances spawned from this app including those created by other third-parties.
 *
 * [WorkerFactory] implementation here helps resolve an issue (since WM uses reflection) where a change to a
 * [CoroutineWorker] (for instance, class name or package change) may break execution of that enqueued work. Example:
 * ```
 * Caused by kotlin.lang.NoSuchMethodException: <init> [class android.content.Context,
 * class androidx.work.WorkerParameters]
 * ```
 *
 * @param [isDebug] Boolean used for determining logging level.
 */
class TempoWorkConfigProvider(
    private val isDebug: Boolean,
) : Configuration.Provider {
    override val workManagerConfiguration: Configuration
        get() {
            val delegatingWorkerFactory =
                DelegatingWorkerFactory().apply {
                    // Apply factories here, mitigates issues with migration of Worker-related classes
                    addFactory(KoinWorkerFactory())
                }
            return Configuration
                .Builder()
                .setMinimumLoggingLevel(if (isDebug) Log.DEBUG else Log.ERROR)
                .setWorkerFactory(delegatingWorkerFactory)
                .build()
        }
}