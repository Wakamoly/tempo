package com.cappielloantonio.tempo

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.cappielloantonio.tempo.github.Github
import com.cappielloantonio.tempo.helper.ThemeHelper
import com.cappielloantonio.tempo.subsonic.Subsonic
import com.cappielloantonio.tempo.subsonic.SubsonicPreferences
import com.cappielloantonio.tempo.util.Preferences.getInUseServerAddress
import com.cappielloantonio.tempo.util.Preferences.getPassword
import com.cappielloantonio.tempo.util.Preferences.getSalt
import com.cappielloantonio.tempo.util.Preferences.getToken
import com.cappielloantonio.tempo.util.Preferences.getUser
import com.cappielloantonio.tempo.util.Preferences.isLowSecurity
import com.cappielloantonio.tempo.util.Preferences.setPassword
import com.cappielloantonio.tempo.util.Preferences.setSalt
import com.cappielloantonio.tempo.util.Preferences.setToken

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val themePref: String = sharedPreferences.getString(
            com.cappielloantonio.tempo.util.Preferences.THEME,
            ThemeHelper.DEFAULT_MODE
        )!!
        ThemeHelper.applyTheme(themePref)

        instance = App()
        context = applicationContext
        Companion.preferences = PreferenceManager.getDefaultSharedPreferences(context!!)
    }

    val preferences: SharedPreferences
        get() =
            Companion.preferences ?: run {
                val prefs = PreferenceManager.getDefaultSharedPreferences(context!!)
                Companion.preferences = prefs
                prefs
            }


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
            if (subsonic == null || override) {
                subsonic =
                    subsonicClient
            }
            return subsonic!!
        }

        @JvmStatic
        val githubClientInstance: Github
            get() {
                if (github == null) {
                    github = Github()
                }
                return github!!
            }

        @JvmStatic
        fun refreshSubsonicClient() {
            subsonic =
                subsonicClient
        }

        private val subsonicClient: Subsonic
            get() {
                val preferences: SubsonicPreferences =
                    subsonicPreferences

                if (preferences.authentication != null) {
                    if (preferences.authentication
                            .password != null
                    ) setPassword(
                        preferences.authentication.password
                    )
                    if (preferences.authentication
                            .token != null
                    ) setToken(
                        preferences.authentication.token
                    )
                    if (preferences.authentication
                            .salt != null
                    ) setSalt(
                        preferences.authentication.salt
                    )
                }

                return Subsonic(preferences)
            }

        private val subsonicPreferences: SubsonicPreferences
            get() {
                val server = getInUseServerAddress()
                val username = getUser()
                val password = getPassword()
                val token = getToken()
                val salt = getSalt()
                val isLowSecurity = isLowSecurity()

                val preferences = SubsonicPreferences()
                preferences.serverUrl = server
                preferences.username = username
                preferences.setAuthentication(password, token, salt, isLowSecurity)

                return preferences
            }
    }
}
