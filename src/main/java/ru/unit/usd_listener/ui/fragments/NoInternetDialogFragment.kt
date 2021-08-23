package ru.unit.usd_listener.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import ru.unit.usd_listener.R
import ru.unit.usd_listener.databinding.DialogFragmentNoInternetBinding


class NoInternetDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DataBindingUtil.inflate<DialogFragmentNoInternetBinding>(requireActivity().layoutInflater, R.layout.dialog_fragment_no_internet, null, false)

        binding.okButton.setOnClickListener {
            dialog?.dismiss()
        }

        val builder = AlertDialog.Builder(requireContext(), R.style.Theme_Usdlistener_Dialog_Common)
        builder.setView(binding.root)

        return builder.create()
    }

    override fun dismiss() {
        if(isAdded) super.dismiss()
    }

    override fun show(manager: FragmentManager, tag: String?) {
        dismiss() // if add, then dismiss
        super.show(manager, tag)
    }
}