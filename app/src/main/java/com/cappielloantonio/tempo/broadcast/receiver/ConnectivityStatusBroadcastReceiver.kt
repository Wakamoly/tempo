package com.cappielloantonio.tempo.broadcast.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.annotation.OptIn
import androidx.core.view.isVisible
import androidx.media3.common.util.UnstableApi
import com.cappielloantonio.tempo.ui.activity.MainActivity

class ConnectivityStatusBroadcastReceiver
@OptIn(UnstableApi::class)
constructor
    (private val activity: MainActivity) :
    BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
            val noConnectivity =
                intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)

            activity.binding.offlineModeTextView.isVisible = noConnectivity
        }
    }
}
