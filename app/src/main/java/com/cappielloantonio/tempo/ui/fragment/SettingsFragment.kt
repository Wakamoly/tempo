package com.cappielloantonio.tempo.ui.fragment

import android.content.Intent
import android.media.audiofx.AudioEffect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.Preference.SummaryProvider
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.cappielloantonio.tempo.BuildConfig
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.helper.ThemeHelper
import com.cappielloantonio.tempo.interfaces.DialogClickCallback
import com.cappielloantonio.tempo.interfaces.ScanCallback
import com.cappielloantonio.tempo.subsonic.models.SubsonicResponse.scanStatus
import com.cappielloantonio.tempo.ui.activity.MainActivity
import com.cappielloantonio.tempo.ui.dialog.DeleteDownloadStorageDialog
import com.cappielloantonio.tempo.ui.dialog.DownloadStorageDialog
import com.cappielloantonio.tempo.ui.dialog.StarredAlbumSyncDialog
import com.cappielloantonio.tempo.ui.dialog.StarredSyncDialog
import com.cappielloantonio.tempo.ui.dialog.StreamingCacheStorageDialog
import com.cappielloantonio.tempo.util.DownloadUtil
import com.cappielloantonio.tempo.util.Preferences
import com.cappielloantonio.tempo.util.Preferences.getDownloadStoragePreference
import com.cappielloantonio.tempo.util.UIUtil
import com.cappielloantonio.tempo.viewmodel.SettingViewModel
import java.util.Locale

@OptIn(markerClass = UnstableApi::class)
class SettingsFragment : PreferenceFragmentCompat() {
    private var activity: MainActivity? = null
    private var settingViewModel: SettingViewModel? = null

    private var someActivityResultLauncher: ActivityResultLauncher<Intent?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        someActivityResultLauncher = registerForActivityResult<Intent?, ActivityResult?>(
            StartActivityForResult(),
            ActivityResultCallback { result: ActivityResult? -> })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity = activity as MainActivity?

        val view = super.onCreateView(inflater, container, savedInstanceState)
        settingViewModel =
            ViewModelProvider(requireActivity()).get<SettingViewModel>(SettingViewModel::class.java)

        if (view != null) {
            listView.setPadding(
                0,
                0,
                0,
                resources.getDimension(R.dimen.global_padding_bottom).toInt()
            )
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        activity!!.setBottomNavigationBarVisibility(false)
        activity!!.setBottomSheetVisibility(false)
    }

    override fun onResume() {
        super.onResume()

        checkEqualizer()
        checkCacheStorage()
        checkStorage()

        setStreamingCacheSize()
        setAppLanguage()
        setVersion()

        actionLogout()
        actionScan()
        actionSyncStarredAlbums()
        actionSyncStarredTracks()
        actionChangeStreamingCacheStorage()
        actionChangeDownloadStorage()
        actionDeleteDownloadStorage()
        actionKeepScreenOn()
    }

    override fun onStop() {
        super.onStop()
        activity!!.setBottomSheetVisibility(true)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.global_preferences, rootKey)
        val themePreference = findPreference<ListPreference?>(Preferences.THEME)
        if (themePreference != null) {
            themePreference.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any? ->
                    val themeOption = newValue as String
                    ThemeHelper.applyTheme(themeOption)
                    true
                }
        }
    }

    private fun checkEqualizer() {
        val equalizer = findPreference<Preference?>("equalizer")

        if (equalizer == null) return

        val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)

        if ((intent.resolveActivity(requireActivity().packageManager) != null)) {
            equalizer.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference: Preference? ->
                someActivityResultLauncher!!.launch(intent)
                true
            }
        } else {
            equalizer.isVisible = false
        }
    }

    private fun checkCacheStorage() {
        val storage = findPreference<Preference?>("streaming_cache_storage")

        if (storage == null) return

        try {
            if (requireContext().getExternalFilesDirs(null)[1] == null) {
                storage.isVisible = false
            } else {
                storage.setSummary(if (getDownloadStoragePreference() == 0) R.string.download_storage_internal_dialog_negative_button else R.string.download_storage_external_dialog_positive_button)
            }
        } catch (exception: Exception) {
            storage.isVisible = false
        }
    }

    private fun checkStorage() {
        val storage = findPreference<Preference?>("download_storage")

        if (storage == null) return

        try {
            if (requireContext().getExternalFilesDirs(null)[1] == null) {
                storage.isVisible = false
            } else {
                storage.setSummary(if (getDownloadStoragePreference() == 0) R.string.download_storage_internal_dialog_negative_button else R.string.download_storage_external_dialog_positive_button)
            }
        } catch (exception: Exception) {
            storage.isVisible = false
        }
    }

    private fun setStreamingCacheSize() {
        val streamingCachePreference = findPreference<ListPreference?>("streaming_cache_size")

        if (streamingCachePreference != null) {
            streamingCachePreference.setSummaryProvider(object : SummaryProvider<ListPreference?> {
                override fun provideSummary(preference: ListPreference): CharSequence? {
                    val entry = preference.getEntry()

                    if (entry == null) return null

                    val currentSizeMb =
                        DownloadUtil.getStreamingCacheSize(requireActivity()) / (1024 * 1024)

                    return getString(
                        R.string.settings_summary_streaming_cache_size,
                        entry,
                        currentSizeMb.toString()
                    )
                }
            })
        }
    }

    private fun setAppLanguage() {
        val localePref = findPreference<ListPreference?>("language")

        val locales = UIUtil.getLangPreferenceDropdownEntries(requireContext())

        val entries = locales.keys.toTypedArray<CharSequence?>()
        val entryValues = locales.values.toTypedArray<CharSequence?>()

        localePref!!.entries = entries
        localePref.entryValues = entryValues

        val value = localePref.value
        if ("default" == value) {
            localePref.setSummary(requireContext().getString(R.string.settings_system_language))
        } else {
            localePref.setSummary(Locale.forLanguageTag(value).displayName)
        }

        localePref.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any? ->
                if ("default" == newValue) {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                    preference!!.setSummary(requireContext().getString(R.string.settings_system_language))
                } else {
                    val appLocale = LocaleListCompat.forLanguageTags(newValue as String?)
                    AppCompatDelegate.setApplicationLocales(appLocale)
                    preference!!.setSummary(Locale.forLanguageTag(newValue).displayName)
                }
                true
            }
    }

    private fun setVersion() {
        findPreference<Preference?>("version")!!.setSummary(BuildConfig.VERSION_NAME)
    }

    private fun actionLogout() {
        findPreference<Preference?>("logout")!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { preference: Preference? ->
                activity!!.quit()
                true
            }
    }

    private fun actionScan() {
        findPreference<Preference?>("scan_library")!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { preference: Preference? ->
                settingViewModel!!.launchScan(object : ScanCallback {
                    override fun onError(exception: Exception) {
                        findPreference<Preference?>("scan_library")!!.setSummary(exception.message)
                    }

                    override fun onSuccess(isScanning: Boolean, count: Long) {
                        findPreference<Preference?>("scan_library")!!.setSummary("Scanning: counting " + count + " tracks")
                        if (isScanning) this.scanStatus
                    }
                })
                true
            }
    }

    private fun actionSyncStarredTracks() {
        findPreference<Preference?>("sync_starred_tracks_for_offline_use")!!.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any? ->
                if (newValue is Boolean) {
                    if (newValue) {
                        val dialog = StarredSyncDialog(Runnable {
                            (preference as SwitchPreference).setChecked(false)
                        })
                        dialog.show(activity!!.supportFragmentManager, null)
                    }
                }
                true
            }
    }

    private fun actionSyncStarredAlbums() {
        findPreference<Preference?>("sync_starred_albums_for_offline_use")!!.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any? ->
                if (newValue is Boolean) {
                    if (newValue) {
                        val dialog = StarredAlbumSyncDialog(Runnable {
                            (preference as SwitchPreference).setChecked(false)
                        })
                        dialog.show(activity!!.supportFragmentManager, null)
                    }
                }
                true
            }
    }

    private fun actionChangeStreamingCacheStorage() {
        findPreference<Preference?>("streaming_cache_storage")!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { preference: Preference? ->
                val dialog = StreamingCacheStorageDialog(object : DialogClickCallback {
                    override fun onPositiveClick() {
                        findPreference<Preference?>("streaming_cache_storage")!!.setSummary(R.string.streaming_cache_storage_external_dialog_positive_button)
                    }

                    override fun onNegativeClick() {
                        findPreference<Preference?>("streaming_cache_storage")!!.setSummary(R.string.streaming_cache_storage_internal_dialog_negative_button)
                    }
                })
                dialog.show(activity!!.supportFragmentManager, null)
                true
            }
    }

    private fun actionChangeDownloadStorage() {
        findPreference<Preference?>("download_storage")!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { preference: Preference? ->
                val dialog = DownloadStorageDialog(object : DialogClickCallback {
                    override fun onPositiveClick() {
                        findPreference<Preference?>("download_storage")!!.setSummary(R.string.download_storage_external_dialog_positive_button)
                    }

                    override fun onNegativeClick() {
                        findPreference<Preference?>("download_storage")!!.setSummary(R.string.download_storage_internal_dialog_negative_button)
                    }
                })
                dialog.show(activity!!.supportFragmentManager, null)
                true
            }
    }

    private fun actionDeleteDownloadStorage() {
        findPreference<Preference?>("delete_download_storage")!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { preference: Preference? ->
                val dialog = DeleteDownloadStorageDialog()
                dialog.show(activity!!.supportFragmentManager, null)
                true
            }
    }

    private val scanStatus: Unit
        get() {
            settingViewModel!!.getScanStatus(object :
                ScanCallback {
                override fun onError(exception: Exception) {
                    findPreference<Preference?>("scan_library")!!.setSummary(
                        exception.message
                    )
                }

                override fun onSuccess(isScanning: Boolean, count: Long) {
                    findPreference<Preference?>("scan_library")!!.setSummary("Scanning: counting " + count + " tracks")
                    if (isScanning) getScanStatus()
                }
            })
        }

    private fun actionKeepScreenOn() {
        findPreference<Preference?>("always_on_display")!!.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any? ->
                if (newValue is Boolean) {
                    if (newValue) {
                        activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        activity!!.window
                            .clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }
                true
            }
    }

    companion object {
        private const val TAG = "SettingsFragment"
    }
}
