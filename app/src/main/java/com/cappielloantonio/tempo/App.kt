package com.cappielloantonio.tempo

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.work.Configuration
import com.cappielloantonio.tempo.github.Github
import com.cappielloantonio.tempo.helper.ThemeHelper
import com.cappielloantonio.tempo.helper.ThemeHelper.DEFAULT_MODE
import com.cappielloantonio.tempo.subsonic.Subsonic
import com.cappielloantonio.tempo.subsonic.SubsonicPreferences
import com.cappielloantonio.tempo.util.Preferences.getInUseServerAddress
import com.cappielloantonio.tempo.util.Preferences.getIsLowSecurity
import com.cappielloantonio.tempo.util.Preferences.getPassword
import com.cappielloantonio.tempo.util.Preferences.getSalt
import com.cappielloantonio.tempo.util.Preferences.getToken
import com.cappielloantonio.tempo.util.Preferences.getUser
import com.cappielloantonio.tempo.util.Preferences.setPassword
import com.cappielloantonio.tempo.util.Preferences.setSalt
import com.cappielloantonio.tempo.util.Preferences.setToken
import com.cappielloantonio.tempo.work.TempoWorkConfigProvider

class App :
    Application(),
    Configuration.Provider {
    override fun onCreate() {
        super.onCreate()

        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val themePref =
            sharedPreferences.getString(
                com.cappielloantonio.tempo.util.Preferences.THEME,
                DEFAULT_MODE,
            )
        ThemeHelper.applyTheme(themePref ?: DEFAULT_MODE)

        instance = App()
        context = applicationContext
        Companion.preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    val preferences: SharedPreferences
        get() =
            Companion.preferences ?: run {
                val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                Companion.preferences = prefs
                prefs
            }

    override val workManagerConfiguration: Configuration
        get() = TempoWorkConfigProvider(isDebug = BuildConfig.DEBUG).workManagerConfiguration

    companion object {
        private var instance: App? = null
        private var context: Context? = null
        private var subsonic: Subsonic? = null
        private var github: Github? = null
        private var preferences: SharedPreferences? = null

        fun getInstance(): App =
            instance ?: run {
                val app = App()
                instance = app
                app
            }

        @JvmStatic
        fun getContext(): Context =
            context ?: run {
                val instance = getInstance()
                context = instance
                instance
            }

        @JvmStatic
        fun getSubsonicClientInstance(override: Boolean): Subsonic {
            var subclient = subsonic
            if (subclient == null || override) {
                subsonic = subsonicClient
                subclient = subsonicClient
            }
            return subclient
        }

        @JvmStatic
        val githubClientInstance: Github
            get() =
                github ?: run {
                    val gh = Github()
                    github = gh
                    gh
                }

        @JvmStatic
        fun refreshSubsonicClient() {
            subsonic =
                subsonicClient
        }

        private val subsonicClient: Subsonic
            get() =
                subsonicPreferences.run {
                    authentication?.let { authentication ->
                        if (authentication.password != null) {
                            setPassword(authentication.password)
                        }
                        if (authentication.token != null) {
                            setToken(authentication.token)
                        }
                        if (authentication.salt != null) {
                            setSalt(authentication.salt)
                        }
                    }
                    return Subsonic(this)
                }

        private val subsonicPreferences: SubsonicPreferences
            get() {
                val server = getInUseServerAddress()
                val username = getUser()
                val password = getPassword()
                val token = getToken()
                val salt = getSalt()
                val isLowSecurity = getIsLowSecurity()

                val preferences = SubsonicPreferences()
                preferences.serverUrl = server
                preferences.username = username
                preferences.setAuthentication(password, token, salt, isLowSecurity)

                return preferences
            }
    }
}
