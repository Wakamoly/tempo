package com.cappielloantonio.tempo.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.media3.common.util.UnstableApi
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.databinding.DialogDeleteDownloadStorageBinding
import com.cappielloantonio.tempo.util.DownloadUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder

@OptIn(markerClass = [UnstableApi::class])
class DeleteDownloadStorageDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bind = DialogDeleteDownloadStorageBinding.inflate(getLayoutInflater())

        return MaterialAlertDialogBuilder(requireContext())
            .setView(bind.getRoot())
            .setTitle(R.string.delete_download_storage_dialog_title)
            .setPositiveButton(R.string.delete_download_storage_dialog_positive_button, null)
            .setNegativeButton(R.string.delete_download_storage_dialog_negative_button, null)
            .create()
    }

    override fun onResume() {
        super.onResume()
        setButtonAction()
    }

    private fun setButtonAction() {
        val dialog = dialog as AlertDialog?

        if (dialog != null) {
            val positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener { v: View? ->
                DownloadUtil.getDownloadTracker(requireContext())?.removeAll()
                dialog.dismiss()
            }

            val negativeButton = dialog.getButton(Dialog.BUTTON_NEGATIVE)
            negativeButton.setOnClickListener { v: View? ->
                dialog.dismiss()
            }
        }
    }
}
