package ru.unit.usd_listener.ui.fragments

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import ru.unit.usd_listener.Config
import ru.unit.usd_listener.R
import ru.unit.usd_listener.databinding.DialogFragmentNotificationSettingsBinding


class NotificationSettingsDialogFragment(private val onDismissListener: DialogInterface.OnDismissListener) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DataBindingUtil.inflate<DialogFragmentNotificationSettingsBinding>(requireActivity().layoutInflater, R.layout.dialog_fragment_notification_settings, null, false)
        binding.cancalButton.setOnClickListener {
            onDismissListener.onDismiss(dialog)
            dialog?.dismiss()
        }

        binding.okButton.setOnClickListener {
            val sharedPref = requireContext().getSharedPreferences(Config.SHARED_PREFERENCES, Context.MODE_PRIVATE)
            val value = (binding.valueUsdNotificationView.text ?: "0").toString().toFloat()
            with (sharedPref.edit()) {
                putFloat(getString(R.string.pref_notification_value), value)
                apply()
            }

            onDismissListener.onDismiss(dialog)
            dialog?.dismiss()
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)

        return builder.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawableResource(R.drawable.background_dialog_fragment_notification_settings)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}