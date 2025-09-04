package com.cappielloantonio.tempo.ui.activity

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.NavController.OnDestinationChangedListener
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.cappielloantonio.tempo.App.Companion.refreshSubsonicClient
import com.cappielloantonio.tempo.BuildConfig
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.broadcast.receiver.ConnectivityStatusBroadcastReceiver
import com.cappielloantonio.tempo.databinding.ActivityMainBinding
import com.cappielloantonio.tempo.github.models.LatestRelease
import com.cappielloantonio.tempo.github.utils.UpdateUtil
import com.cappielloantonio.tempo.service.MediaManager
import com.cappielloantonio.tempo.subsonic.models.OpenSubsonicExtension
import com.cappielloantonio.tempo.subsonic.models.SubsonicResponse
import com.cappielloantonio.tempo.ui.activity.base.BaseActivity
import com.cappielloantonio.tempo.ui.dialog.ConnectionAlertDialog
import com.cappielloantonio.tempo.ui.dialog.GithubTempoUpdateDialog
import com.cappielloantonio.tempo.ui.dialog.ServerUnreachableDialog
import com.cappielloantonio.tempo.ui.fragment.PlayerBottomSheetFragment
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.util.Preferences
import com.cappielloantonio.tempo.util.Preferences.getPassword
import com.cappielloantonio.tempo.util.Preferences.getSalt
import com.cappielloantonio.tempo.util.Preferences.getToken
import com.cappielloantonio.tempo.util.Preferences.isInUseServerAddressLocal
import com.cappielloantonio.tempo.util.Preferences.isServerSwitchable
import com.cappielloantonio.tempo.util.Preferences.isWifiOnly
import com.cappielloantonio.tempo.util.Preferences.setDataSavingMode
import com.cappielloantonio.tempo.util.Preferences.setLocalAddress
import com.cappielloantonio.tempo.util.Preferences.setOpenSubsonic
import com.cappielloantonio.tempo.util.Preferences.setPassword
import com.cappielloantonio.tempo.util.Preferences.setPlaybackSpeed
import com.cappielloantonio.tempo.util.Preferences.setSalt
import com.cappielloantonio.tempo.util.Preferences.setServer
import com.cappielloantonio.tempo.util.Preferences.setServerId
import com.cappielloantonio.tempo.util.Preferences.setServerSwitchableTimer
import com.cappielloantonio.tempo.util.Preferences.setSkipSilenceMode
import com.cappielloantonio.tempo.util.Preferences.setStarredAlbumsSyncEnabled
import com.cappielloantonio.tempo.util.Preferences.setStarredSyncEnabled
import com.cappielloantonio.tempo.util.Preferences.setToken
import com.cappielloantonio.tempo.util.Preferences.setUser
import com.cappielloantonio.tempo.util.Preferences.showServerUnreachableDialog
import com.cappielloantonio.tempo.util.Preferences.showTempoUpdateDialog
import com.cappielloantonio.tempo.util.Preferences.switchInUseServerAddress
import com.cappielloantonio.tempo.viewmodel.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.color.DynamicColors
import com.google.common.util.concurrent.MoreExecutors
import java.util.Objects
import java.util.concurrent.ExecutionException
import kotlin.math.max
import kotlin.math.min

@UnstableApi
class MainActivity : BaseActivity() {
    private var _binding: ActivityMainBinding? = null
    val binding: ActivityMainBinding
        get() = _binding!!
    private var mainViewModel: MainViewModel? = null

    private var fragmentManager: FragmentManager? = null
    private var navHostFragment: NavHostFragment? = null
    private var bottomNavigationView: BottomNavigationView? = null

    @JvmField
    var navController: NavController? = null
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null

    var connectivityStatusBroadcastReceiver: ConnectivityStatusBroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        DynamicColors.applyToActivityIfAvailable(this)

        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View = _binding!!.getRoot()
        setContentView(view)

        mainViewModel = ViewModelProvider(this).get<MainViewModel>(MainViewModel::class.java)

        connectivityStatusBroadcastReceiver = ConnectivityStatusBroadcastReceiver(this)
        connectivityStatusReceiverManager(true)

        init()
        checkConnectionType()
        this.openSubsonicExtensions
        checkTempoUpdate()
    }

    override fun onStart() {
        super.onStart()
        initService()
    }

    override fun onResume() {
        super.onResume()
        pingServer()
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityStatusReceiverManager(false)
        _binding = null
    }

    override fun onBackPressed() {
        if (bottomSheetBehavior!!.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            collapseBottomSheetDelayed()
        } else {
            super.onBackPressed()
        }
    }

    fun init() {
        fragmentManager = supportFragmentManager

        initBottomSheet()
        initNavigation()

        if (getPassword() != null || (getToken() != null && getSalt() != null)) {
            goFromLogin()
        } else {
            goToLogin()
        }
    }

    // BOTTOM SHEET/NAVIGATION
    private fun initBottomSheet() {
        bottomSheetBehavior =
            BottomSheetBehavior.from<View?>(findViewById<View?>(R.id.player_bottom_sheet))
        bottomSheetBehavior!!.addBottomSheetCallback(bottomSheetCallback)
        fragmentManager!!
            .beginTransaction()
            .replace(R.id.player_bottom_sheet, PlayerBottomSheetFragment(), "PlayerBottomSheet")
            .commit()

        checkBottomSheetAfterStateChanged()
    }

    fun setBottomSheetInPeek(isVisible: Boolean) {
        if (isVisible) {
            bottomSheetBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED)
        } else {
            bottomSheetBehavior!!.setState(BottomSheetBehavior.STATE_HIDDEN)
        }
    }

    fun setBottomSheetVisibility(visibility: Boolean) {
        if (visibility) {
            findViewById<View?>(R.id.player_bottom_sheet).visibility = View.VISIBLE
        } else {
            findViewById<View?>(R.id.player_bottom_sheet).visibility = View.GONE
        }
    }

    private fun checkBottomSheetAfterStateChanged() {
        val handler = Handler()
        val runnable = Runnable { setBottomSheetInPeek(mainViewModel!!.isQueueLoaded()) }
        handler.postDelayed(runnable, 100)
    }

    fun collapseBottomSheetDelayed() {
        val handler = Handler()
        val runnable =
            Runnable { bottomSheetBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED) }
        handler.postDelayed(runnable, 100)
    }

    fun expandBottomSheet() {
        bottomSheetBehavior!!.setState(BottomSheetBehavior.STATE_EXPANDED)
    }

    fun setBottomSheetDraggableState(isDraggable: Boolean) {
        bottomSheetBehavior!!.isDraggable = isDraggable
    }

    private val bottomSheetCallback: BottomSheetCallback =
        object : BottomSheetCallback() {
            var navigationHeight: Int = 0

            override fun onStateChanged(
                view: View,
                state: Int,
            ) {
                val playerBottomSheetFragment =
                    supportFragmentManager.findFragmentByTag("PlayerBottomSheet") as PlayerBottomSheetFragment?

                when (state) {
                    BottomSheetBehavior.STATE_HIDDEN ->
                        resetMusicSession()
                    BottomSheetBehavior.STATE_COLLAPSED ->
                        playerBottomSheetFragment?.goBackToFirstPage()
                    BottomSheetBehavior.STATE_SETTLING,
                    BottomSheetBehavior.STATE_EXPANDED,
                    BottomSheetBehavior.STATE_DRAGGING,
                    BottomSheetBehavior.STATE_HALF_EXPANDED,
                    -> {
                    }
                }
            }

            override fun onSlide(
                view: View,
                slideOffset: Float,
            ) {
                animateBottomSheet(slideOffset)
                animateBottomNavigation(slideOffset, navigationHeight)
            }
        }

    private fun animateBottomSheet(slideOffset: Float) {
        val playerBottomSheetFragment =
            supportFragmentManager.findFragmentByTag("PlayerBottomSheet") as PlayerBottomSheetFragment?
        if (playerBottomSheetFragment != null) {
            val condensedSlideOffset = max(0.0f, min(0.2f, slideOffset - 0.2f)) / 0.2f
            playerBottomSheetFragment.getPlayerHeader().setAlpha(1 - condensedSlideOffset)
            playerBottomSheetFragment
                .getPlayerHeader()
                .setVisibility(if (condensedSlideOffset > 0.99) View.GONE else View.VISIBLE)
        }
    }

    private fun animateBottomNavigation(
        slideOffset: Float,
        navigationHeight: Int,
    ) {
        var navigationHeight = navigationHeight
        if (slideOffset < 0) return

        if (navigationHeight == 0) {
            navigationHeight = _binding!!.bottomNavigation.height
        }

        val slideY = navigationHeight - navigationHeight * (1 - slideOffset)

        _binding!!.bottomNavigation.translationY = slideY
    }

    private fun initNavigation() {
        bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        navHostFragment =
            fragmentManager!!.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        navController = Objects.requireNonNull<NavHostFragment?>(navHostFragment).navController

        /*
         * In questo modo intercetto il cambio schermata tramite navbar e se il bottom sheet è aperto,
         * lo chiudo
         */
        navController!!.addOnDestinationChangedListener(
            OnDestinationChangedListener { controller: NavController?, destination: NavDestination?, arguments: Bundle? ->
                if (bottomSheetBehavior!!.getState() == BottomSheetBehavior.STATE_EXPANDED &&
                    (
                        destination!!.id == R.id.homeFragment || destination.id == R.id.libraryFragment ||
                            destination.id == R.id.downloadFragment
                    )
                ) {
                    bottomSheetBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED)
                }
            },
        )

        setupWithNavController(bottomNavigationView!!, navController!!)
    }

    fun setBottomNavigationBarVisibility(visibility: Boolean) {
        if (visibility) {
            bottomNavigationView!!.visibility = View.VISIBLE
        } else {
            bottomNavigationView!!.visibility = View.GONE
        }
    }

    private fun initService() {
        MediaManager.check(getMediaBrowserListenableFuture())

        getMediaBrowserListenableFuture().addListener(
            Runnable {
                try {
                    getMediaBrowserListenableFuture().get().addListener(
                        object : Player.Listener {
                            override fun onIsPlayingChanged(isPlaying: Boolean) {
                                if (isPlaying && bottomSheetBehavior!!.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                                    setBottomSheetInPeek(true)
                                }
                            }
                        },
                    )
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            },
            MoreExecutors.directExecutor(),
        )
    }

    private fun goToLogin() {
        bottomSheetBehavior!!.setState(BottomSheetBehavior.STATE_HIDDEN)
        setBottomNavigationBarVisibility(false)
        setBottomSheetVisibility(false)

        if (Objects.requireNonNull<NavDestination?>(navController!!.currentDestination).id == R.id.landingFragment) {
            navController!!.navigate(R.id.action_landingFragment_to_loginFragment)
        } else if (Objects.requireNonNull<NavDestination?>(navController!!.currentDestination).id == R.id.settingsFragment) {
            navController!!.navigate(R.id.action_settingsFragment_to_loginFragment)
        } else if (Objects.requireNonNull<NavDestination?>(navController!!.currentDestination).id == R.id.homeFragment) {
            navController!!.navigate(R.id.action_homeFragment_to_loginFragment)
        }
    }

    private fun goToHome() {
        bottomNavigationView!!.visibility = View.VISIBLE

        if (Objects.requireNonNull<NavDestination?>(navController!!.currentDestination).id == R.id.landingFragment) {
            navController!!.navigate(R.id.action_landingFragment_to_homeFragment)
        } else if (Objects.requireNonNull<NavDestination?>(navController!!.currentDestination).id == R.id.loginFragment) {
            navController!!.navigate(R.id.action_loginFragment_to_homeFragment)
        }
    }

    fun goFromLogin() {
        setBottomSheetInPeek(mainViewModel!!.isQueueLoaded())
        goToHome()
    }

    fun quit() {
        resetUserSession()
        resetMusicSession()
        resetViewModel()
        goToLogin()
    }

    private fun resetUserSession() {
        setServerId(null)
        setSalt(null)
        setToken(null)
        setPassword(null)
        setServer(null)
        setLocalAddress(null)
        setUser(null)

        // TODO Enter all settings to be reset
        setOpenSubsonic(false)
        setPlaybackSpeed(Constants.MEDIA_PLAYBACK_SPEED_100)
        setSkipSilenceMode(false)
        setDataSavingMode(false)
        setStarredSyncEnabled(false)
        setStarredAlbumsSyncEnabled(false)
    }

    private fun resetMusicSession() {
        MediaManager.reset(getMediaBrowserListenableFuture())
    }

    private fun hideMusicSession() {
        MediaManager.hide(getMediaBrowserListenableFuture())
    }

    private fun resetViewModel() {
        this.getViewModelStore().clear()
    }

    // CONNECTION
    private fun connectivityStatusReceiverManager(isActive: Boolean) {
        if (isActive) {
            val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            registerReceiver(connectivityStatusBroadcastReceiver, filter)
        } else {
            unregisterReceiver(connectivityStatusBroadcastReceiver)
        }
    }

    private fun pingServer() {
        if (getToken() == null) return

        if (isInUseServerAddressLocal()) {
            mainViewModel!!.ping().observe(
                this,
                Observer { subsonicResponse: SubsonicResponse? ->
                    if (subsonicResponse == null) {
                        setServerSwitchableTimer()
                        switchInUseServerAddress()
                        refreshSubsonicClient()
                        pingServer()
                    } else {
                        setOpenSubsonic(subsonicResponse.openSubsonic != null && subsonicResponse.openSubsonic)
                    }
                },
            )
        } else {
            if (isServerSwitchable()) {
                setServerSwitchableTimer()
                switchInUseServerAddress()
                refreshSubsonicClient()
                pingServer()
            } else {
                mainViewModel!!
                    .ping()
                    .observe(
                        this,
                        Observer { subsonicResponse: SubsonicResponse? ->
                            if (subsonicResponse == null) {
                                if (showServerUnreachableDialog()) {
                                    val dialog = ServerUnreachableDialog()
                                    dialog.show(supportFragmentManager, null)
                                }
                            } else {
                                setOpenSubsonic(subsonicResponse.openSubsonic != null && subsonicResponse.openSubsonic)
                            }
                        },
                    )
            }
        }
    }

    private val openSubsonicExtensions: Unit
        get() {
            if (getToken() != null) {
                mainViewModel!!.getOpenSubsonicExtensions().observe(
                    this,
                    Observer { openSubsonicExtensions: MutableList<OpenSubsonicExtension?>? ->
                        if (openSubsonicExtensions != null) {
                            Preferences.setOpenSubsonicExtensions(
                                openSubsonicExtensions,
                            )
                        }
                    },
                )
            }
        }

    private fun checkTempoUpdate() {
        if (BuildConfig.FLAVOR == "tempo" && showTempoUpdateDialog()) {
            mainViewModel!!
                .checkTempoUpdate()
                .observe(
                    this,
                    Observer { latestRelease: LatestRelease? ->
                        if (latestRelease != null && UpdateUtil.showUpdateDialog(latestRelease)) {
                            val dialog = GithubTempoUpdateDialog(latestRelease)
                            dialog.show(supportFragmentManager, null)
                        }
                    },
                )
        }
    }

    private fun checkConnectionType() {
        if (isWifiOnly()) {
            val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo

            if (networkInfo != null && networkInfo.type != ConnectivityManager.TYPE_WIFI) {
                val dialog = ConnectionAlertDialog()
                dialog.show(supportFragmentManager, null)
            }
        }
    }

    companion object {
        private const val TAG = "MainActivityLogs"
    }
}
