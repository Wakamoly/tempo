package com.cappielloantonio.tempo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.cappielloantonio.tempo.model.Download
import com.cappielloantonio.tempo.model.DownloadStack
import com.cappielloantonio.tempo.repository.DownloadRepository
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.util.Preferences.getDefaultDownloadViewType
import java.util.stream.Collectors

class DownloadViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val downloadRepository: DownloadRepository

    private val downloadedTrackSample = MutableLiveData<MutableList<Child>>(null)
    private val viewStack = MutableLiveData<ArrayList<DownloadStack>>(null)

    init {
        downloadRepository = DownloadRepository()

        initViewStack(DownloadStack(getDefaultDownloadViewType(), null))
    }

    fun getDownloadedTracks(owner: LifecycleOwner): LiveData<MutableList<Child>> {
        downloadRepository
            .getLiveDownload()
            .observe(
                owner,
                Observer { downloads: MutableList<Download?>? ->
                    downloadedTrackSample.postValue(
                        downloads!!
                            .stream()
                            .map<Child?> { download: Download? -> download as Child? }
                            .collect(
                                Collectors.toList(),
                            ),
                    )
                },
            )
        return downloadedTrackSample
    }

    fun getViewStack(): LiveData<ArrayList<DownloadStack>> = viewStack

    fun initViewStack(level: DownloadStack?) {
        val stack = ArrayList<DownloadStack?>()
        stack.add(level)
        viewStack.value = stack
    }

    fun pushViewStack(level: DownloadStack?) {
        val stack = viewStack.getValue()
        stack!!.add(level)
        viewStack.value = stack
    }

    fun popViewStack() {
        val stack = viewStack.getValue()
        stack!!.removeAt(stack.size - 1)
        viewStack.value = stack
    }

    companion object {
        private const val TAG = "DownloadViewModel"
    }
}
