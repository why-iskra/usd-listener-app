package ru.unit.usd_listener.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import ru.unit.usd_listener.R
import ru.unit.usd_listener.databinding.DialogFragmentNoInternetBinding


class NoInternetDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DataBindingUtil.inflate<DialogFragmentNoInternetBinding>(requireActivity().layoutInflater, R.layout.dialog_fragment_no_internet, null, false)

        binding.okButton.setOnClickListener {
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