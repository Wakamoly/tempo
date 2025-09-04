package com.cappielloantonio.tempo.startup

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import androidx.work.WorkManager
import com.cappielloantonio.tempo.BuildConfig
import com.cappielloantonio.tempo.koin.tempoUtilModule
import com.cappielloantonio.tempo.koin.tempoViewModelModule
import com.cappielloantonio.tempo.work.TempoWorkConfigProvider
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

/**
 * Initializes [WorkManager] using `androidx.startup`.
 */
class TempoWorkManagerInitializer : Initializer<WorkManager> {
    override fun create(context: Context): WorkManager {
        Log.d(TAG, "Initializing WorkManager with Tempo configuration.")
        WorkManager.initialize(
            context,
            TempoWorkConfigProvider(isDebug = BuildConfig.DEBUG).workManagerConfiguration,
        )
        return WorkManager.getInstance(context)
    }

    /**
     * Ensure [KoinInitializer] is initialized before [WorkManager] is initialized.
     */
    override fun dependencies(): List<Class<out Initializer<*>>> =
        listOf(
            KoinInitializer::class.java,
        )

    companion object {
        private const val TAG = "WrkMgrInitializer"
    }
}

class KoinInitializer : Initializer<KoinApplication> {
    override fun create(context: Context): KoinApplication =
        startKoin {
            androidContext(context)
            modules(
                tempoUtilModule,
                tempoViewModelModule,
            )
        }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf()
}