package com.cappielloantonio.tempo.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.view.View
import android.view.View.OnLongClickListener
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.databinding.DialogServerSignupBinding
import com.cappielloantonio.tempo.model.Server
import com.cappielloantonio.tempo.util.MusicUtil
import com.cappielloantonio.tempo.viewmodel.LoginViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Objects
import java.util.UUID

class ServerSignupDialog : DialogFragment() {
    private var bind: DialogServerSignupBinding? = null
    private var loginViewModel: LoginViewModel? = null

    private var serverName: String? = null
    private var username: String? = null
    private var password: String? = null
    private var server: String? = null
    private var localAddress: String? = null
    private var lowSecurity = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bind = DialogServerSignupBinding.inflate(getLayoutInflater())

        loginViewModel =
            ViewModelProvider(requireActivity()).get<LoginViewModel>(LoginViewModel::class.java)

        return MaterialAlertDialogBuilder(activity!!)
            .setView(bind!!.getRoot())
            .setTitle(R.string.server_signup_dialog_title)
            .setNeutralButton(
                R.string.server_signup_dialog_neutral_button,
                DialogInterface.OnClickListener { dialog: DialogInterface?, id: Int -> },
            ).setPositiveButton(
                R.string.server_signup_dialog_positive_button,
                DialogInterface.OnClickListener { dialog: DialogInterface?, id: Int -> },
            ).setNegativeButton(
                R.string.server_signup_dialog_negative_button,
                DialogInterface.OnClickListener { dialog: DialogInterface?, id: Int -> dialog!!.cancel() },
            ).create()
    }

    override fun onStart() {
        super.onStart()

        setServerInfo()
        setButtonAction()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bind = null
    }

    private fun setServerInfo() {
        if (arguments != null) {
            loginViewModel!!.setServerToEdit(requireArguments().getParcelable<Server?>("server_object"))

            if (loginViewModel!!.getServerToEdit() != null) {
                bind!!.serverNameTextView.setText(loginViewModel!!.getServerToEdit().serverName)
                bind!!.usernameTextView.setText(loginViewModel!!.getServerToEdit().username)
                bind!!.passwordTextView.setText("")
                bind!!.serverTextView.setText(loginViewModel!!.getServerToEdit().address)
                bind!!.localAddressTextView.setText(loginViewModel!!.getServerToEdit().localAddress)
                bind!!.lowSecurityCheckbox.setChecked(loginViewModel!!.getServerToEdit().isLowSecurity)
            }
        } else {
            loginViewModel!!.setServerToEdit(null)
        }
    }

    private fun setButtonAction() {
        val alertDialog = Objects.requireNonNull<Dialog?>(dialog) as AlertDialog

        alertDialog
            .getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener(
                View.OnClickListener { v: View? ->
                    if (validateInput()) {
                        saveServerPreference()
                        Objects.requireNonNull<Dialog?>(dialog).dismiss()
                    }
                },
            )

        alertDialog
            .getButton(AlertDialog.BUTTON_NEUTRAL)
            .setOnClickListener(
                View.OnClickListener { v: View? ->
                    Toast
                        .makeText(
                            requireContext(),
                            R.string.server_signup_dialog_action_delete_toast,
                            Toast.LENGTH_SHORT,
                        ).show()
                },
            )

        alertDialog
            .getButton(AlertDialog.BUTTON_NEUTRAL)
            .setOnLongClickListener(
                OnLongClickListener { v: View? ->
                    loginViewModel!!.deleteServer(null)
                    Objects.requireNonNull<Dialog?>(dialog).dismiss()
                    true
                },
            )
    }

    private fun validateInput(): Boolean {
        serverName =
            Objects
                .requireNonNull<Editable?>(bind!!.serverNameTextView.getText())
                .toString()
                .trim { it <= ' ' }
        username =
            Objects
                .requireNonNull<Editable?>(bind!!.usernameTextView.getText())
                .toString()
                .trim { it <= ' ' }
        password =
            if (bind!!.lowSecurityCheckbox.isChecked) {
                MusicUtil.passwordHexEncoding(
                    Objects
                        .requireNonNull<Editable?>(
                            bind!!.passwordTextView.getText(),
                        ).toString(),
                )
            } else {
                Objects
                    .requireNonNull<Editable?>(
                        bind!!.passwordTextView.getText(),
                    ).toString()
            }
        server =
            if (bind!!.serverTextView.getText() != null &&
                !bind!!
                    .serverTextView
                    .getText()
                    .toString()
                    .trim { it <= ' ' }
                    .isBlank()
            ) {
                bind!!
                    .serverTextView
                    .getText()
                    .toString()
                    .trim { it <= ' ' }
            } else {
                null
            }
        localAddress =
            if (bind!!.localAddressTextView.getText() != null &&
                !bind!!
                    .localAddressTextView
                    .getText()
                    .toString()
                    .trim { it <= ' ' }
                    .isBlank()
            ) {
                bind!!
                    .localAddressTextView
                    .getText()
                    .toString()
                    .trim { it <= ' ' }
            } else {
                null
            }
        lowSecurity = bind!!.lowSecurityCheckbox.isChecked

        if (TextUtils.isEmpty(serverName)) {
            bind!!.serverNameTextView.error = getString(R.string.error_required)
            return false
        }

        if (TextUtils.isEmpty(username)) {
            bind!!.usernameTextView.error = getString(R.string.error_required)
            return false
        }

        if (TextUtils.isEmpty(server)) {
            bind!!.serverTextView.error = getString(R.string.error_required)
            return false
        }

        if (!TextUtils.isEmpty(localAddress) && !localAddress!!.matches("^https?://(.*)".toRegex())) {
            bind!!.localAddressTextView.error = getString(R.string.error_server_prefix)
            return false
        }

        if (!server!!.matches("^https?://(.*)".toRegex())) {
            bind!!.serverTextView.error = getString(R.string.error_server_prefix)
            return false
        }

        return true
    }

    private fun saveServerPreference() {
        val serverID =
            if (loginViewModel!!.getServerToEdit() != null) {
                loginViewModel!!.getServerToEdit().serverId
            } else {
                UUID
                    .randomUUID()
                    .toString()
            }
        loginViewModel!!.addServer(
            Server(
                serverID,
                this.serverName!!,
                this.username!!,
                this.password!!,
                this.server!!,
                this.localAddress,
                System.currentTimeMillis(),
                this.lowSecurity,
            ),
        )
    }

    companion object {
        private const val TAG = "ServerSignupDialog"
    }
}
