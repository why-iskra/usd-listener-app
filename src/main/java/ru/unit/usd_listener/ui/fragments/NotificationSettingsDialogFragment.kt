package ru.unit.usd_listener.ui.fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import ru.unit.usd_listener.R
import ru.unit.usd_listener.databinding.DialogFragmentNotificationSettingsBinding
import ru.unit.usd_listener.utils.Config


class NotificationSettingsDialogFragment() : DialogFragment() {

    private var onDismissListener: () -> Unit = {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DataBindingUtil.inflate<DialogFragmentNotificationSettingsBinding>(requireActivity().layoutInflater, R.layout.dialog_fragment_notification_settings, null, false)
        binding.cancalButton.setOnClickListener {
            onDismissListener()
            dialog?.dismiss()
        }

        binding.okButton.setOnClickListener {
            val sharedPref = requireContext().getSharedPreferences(Config.SHARED_PREFERENCES, Context.MODE_PRIVATE)
            val value = (binding.valueUsdNotificationView.text ?: "0").toString().toFloatOrNull()
            if(value == null) {
                DrawableCompat.setTint(binding.valueUsdNotificationView.background, requireContext().getColor(R.color.amaranth))
                return@setOnClickListener
            }

            with (sharedPref.edit()) {
                putFloat(getString(R.string.pref_notification_value), value)
                apply()
            }

            onDismissListener()
            dialog?.dismiss()
        }

        binding.disableButton.setOnClickListener {
            val sharedPref = requireContext().getSharedPreferences(Config.SHARED_PREFERENCES, Context.MODE_PRIVATE)
            with (sharedPref.edit()) {
                putFloat(getString(R.string.pref_notification_value), 0f)
                apply()
            }

            onDismissListener()
            dialog?.dismiss()
        }

        val builder = AlertDialog.Builder(requireContext(), R.style.Theme_Usdlistener_Dialog_Common)
        builder.setView(binding.root)

        return builder.create()
    }

    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }

    override fun dismiss() {
        if(isAdded) super.dismiss()
    }

    override fun show(manager: FragmentManager, tag: String?) {
        dismiss() // if add, then dismiss
        super.show(manager, tag)
    }
}