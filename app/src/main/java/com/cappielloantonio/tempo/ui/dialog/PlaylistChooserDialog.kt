package com.cappielloantonio.tempo.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.databinding.DialogPlaylistChooserBinding
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.subsonic.models.Playlist
import com.cappielloantonio.tempo.ui.adapter.PlaylistDialogHorizontalAdapter
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.viewmodel.PlaylistChooserViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Objects

class PlaylistChooserDialog :
    DialogFragment(),
    ClickCallback {
    private var bind: DialogPlaylistChooserBinding? = null
    private var playlistChooserViewModel: PlaylistChooserViewModel? = null

    private var playlistDialogHorizontalAdapter: PlaylistDialogHorizontalAdapter? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bind = DialogPlaylistChooserBinding.inflate(getLayoutInflater())

        playlistChooserViewModel =
            ViewModelProvider(requireActivity()).get<PlaylistChooserViewModel>(
                PlaylistChooserViewModel::class.java,
            )

        return MaterialAlertDialogBuilder(activity!!)
            .setView(bind!!.getRoot())
            .setTitle(R.string.playlist_chooser_dialog_title)
            .setNeutralButton(
                R.string.playlist_chooser_dialog_neutral_button,
                DialogInterface.OnClickListener { dialog: DialogInterface?, id: Int -> },
            ).setNegativeButton(
                R.string.playlist_chooser_dialog_negative_button,
                DialogInterface.OnClickListener { dialog: DialogInterface?, id: Int -> dialog!!.cancel() },
            ).create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bind = null
    }

    override fun onStart() {
        super.onStart()

        initPlaylistView()
        setSongInfo()
        setButtonAction()
    }

    private fun setSongInfo() {
        playlistChooserViewModel!!.setSongsToAdd(
            requireArguments().getParcelableArrayList<Child?>(
                Constants.TRACKS_OBJECT,
            ),
        )
    }

    private fun setButtonAction() {
        val alertDialog = Objects.requireNonNull<Dialog?>(dialog) as AlertDialog
        alertDialog
            .getButton(AlertDialog.BUTTON_NEUTRAL)
            .setOnClickListener(
                View.OnClickListener { v: View? ->
                    val bundle = Bundle()
                    bundle.putParcelableArrayList(
                        Constants.TRACKS_OBJECT,
                        playlistChooserViewModel!!.getSongsToAdd(),
                    )

                    val dialog = PlaylistEditorDialog(null)
                    dialog.setArguments(bundle)
                    dialog.show(requireActivity().supportFragmentManager, null)
                    Objects.requireNonNull<Dialog?>(getDialog()).dismiss()
                },
            )
    }

    private fun initPlaylistView() {
        bind!!.playlistDialogRecyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        bind!!.playlistDialogRecyclerView.setHasFixedSize(true)

        playlistDialogHorizontalAdapter = PlaylistDialogHorizontalAdapter(this)
        bind!!.playlistDialogRecyclerView.setAdapter(playlistDialogHorizontalAdapter)

        playlistChooserViewModel!!
            .getPlaylistList(requireActivity())
            .observe(
                requireActivity(),
                Observer { playlists: MutableList<Playlist?>? ->
                    if (playlists != null) {
                        if (!playlists.isEmpty()) {
                            if (bind != null) bind!!.noPlaylistsCreatedTextView.visibility = View.GONE
                            if (bind != null) bind!!.playlistDialogRecyclerView.visibility = View.VISIBLE
                            playlistDialogHorizontalAdapter!!.setItems(playlists)
                        } else {
                            if (bind != null) bind!!.noPlaylistsCreatedTextView.visibility = View.VISIBLE
                            if (bind != null) bind!!.playlistDialogRecyclerView.visibility = View.GONE
                        }
                    }
                },
            )
    }

    override fun onPlaylistClick(bundle: Bundle) {
        if (playlistChooserViewModel!!.getSongsToAdd() != null &&
            !playlistChooserViewModel!!
                .getSongsToAdd()
                .isEmpty()
        ) {
            val playlist = bundle.getParcelable<Playlist?>(Constants.PLAYLIST_OBJECT)
            playlistChooserViewModel!!.addSongsToPlaylist(playlist!!.id)
            dismiss()
        } else {
            Toast
                .makeText(
                    requireContext(),
                    R.string.playlist_chooser_dialog_toast_add_failure,
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }
}
