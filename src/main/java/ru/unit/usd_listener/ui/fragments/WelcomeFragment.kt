package ru.unit.usd_listener.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.unit.usd_listener.R

class WelcomeFragment : Fragment(R.layout.fragment_welcome) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Handler(Looper.getMainLooper()).postDelayed({
            findNavController().navigate(R.id.action_welcomeFragment_to_usdListenerFragment)
        }, 2000)

        return super.onCreateView(inflater, container, savedInstanceState)
    }
}