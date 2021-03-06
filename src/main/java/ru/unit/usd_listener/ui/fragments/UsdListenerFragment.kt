package ru.unit.usd_listener.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.unit.usd_listener.*
import ru.unit.usd_listener.databinding.FragmentUsdListenerBinding
import ru.unit.usd_listener.repository.Repository
import ru.unit.usd_listener.service.UsdNotificationWorker
import ru.unit.usd_listener.ui.viewmodels.UsdListenerViewModel
import ru.unit.usd_listener.utils.AppStatus
import ru.unit.usd_listener.utils.Config
import ru.unit.usd_listener.views.Chart
import kotlin.math.ceil


class UsdListenerFragment : Fragment() {

    companion object {
        private const val NOTIFICATION_SETTINGS_DIALOG_FRAGMENT_TAG = "notificationSettingsDialogFragment"
        private const val NO_INTERNET_DIALOG_FRAGMENT_TAG = "noInternetDialogFragment"
    }

    private val model: UsdListenerViewModel by activityViewModels()
    private lateinit var dialogNotificationSettings: NotificationSettingsDialogFragment
    private lateinit var dialogNoInternet: NoInternetDialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        dialogNotificationSettings = parentFragmentManager
            .findFragmentByTag(NOTIFICATION_SETTINGS_DIALOG_FRAGMENT_TAG) as? NotificationSettingsDialogFragment ?: NotificationSettingsDialogFragment()
        dialogNoInternet = parentFragmentManager
            .findFragmentByTag(NO_INTERNET_DIALOG_FRAGMENT_TAG) as? NoInternetDialogFragment ?: NoInternetDialogFragment()

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentUsdListenerBinding>(inflater, R.layout.fragment_usd_listener, container, false)

        dialogNotificationSettings.setOnDismissListener {
            updateNotificationSettingsButtonText(binding)
        }

        refreshRepository(binding)

        updateNotificationSettingsButtonText(binding)

        binding.monthUsdValueView.layoutManager = LinearLayoutManager(context)

        binding.notificationButton.setOnClickListener {
            showNotificationSettingsDialog()
        }
        binding.refreshButton.setOnClickListener {
            refreshRepository(binding)
        }

        model.usdPriceWithin30Days.observe(viewLifecycleOwner) {
            if(it.isEmpty()) {
                showRefreshButton(binding)
                showNoInternetDialog()
                return@observe
            }

            val lines = Array(5) { Chart.LevelLine(0f) }

            var max = 0f
            var min = 0f
            val elements = it.map { usd ->
                max = max.coerceAtLeast(usd.value)
                min = if(min == 0f) usd.value else min.coerceAtMost(usd.value)
                Chart.Element(usd.simpleDate, usd.value)
            }.toTypedArray()

            val offset = ceil((max - min) / 4)
            for(i in 0 until 5) {
                val level = (offset * i) + min
                lines[i] = Chart.LevelLine(level.toInt().toString(), level)
            }


            val animFadeIn: Animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
            val animFadeOut: Animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)

            binding.monthUsdValueView.startAnimation(animFadeOut)
            binding.usdValueView.startAnimation(animFadeOut)

            animFadeOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) { }

                override fun onAnimationEnd(animation: Animation?) {
                    binding.monthUsdValueView.visibility = View.INVISIBLE
                    binding.usdValueView.visibility = View.INVISIBLE
                }

                override fun onAnimationRepeat(animation: Animation?) { }
            })

            binding.usdChart.update(lines, elements, true) {
                showRefreshButton(binding)

                binding.monthUsdValueView.adapter = Adapter(it.reversed())
                binding.usdValueView.text = String.format("%.2f $/???", it.last().value)

                binding.monthUsdValueView.startAnimation(animFadeIn)
                binding.usdValueView.startAnimation(animFadeIn)
                binding.monthUsdValueView.visibility = View.VISIBLE
                binding.usdValueView.visibility = View.VISIBLE
            }
        }

        return binding.root
    }

    private fun showRefreshButton(binding: FragmentUsdListenerBinding) {
        val animFadeIn: Animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
        val animFadeOut: Animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)

        binding.progressBar.startAnimation(animFadeOut)
        animFadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) { }

            override fun onAnimationEnd(animation: Animation?) {
                binding.progressBar.visibility = View.GONE

                binding.refreshButton.startAnimation(animFadeIn)
                binding.refreshButton.visibility = View.VISIBLE
            }

            override fun onAnimationRepeat(animation: Animation?) { }
        })
    }

    private fun hideRefreshButton(binding: FragmentUsdListenerBinding) {
        val animFadeIn: Animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
        val animFadeOut: Animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)

        binding.refreshButton.startAnimation(animFadeOut)
        animFadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) { }

            override fun onAnimationEnd(animation: Animation?) {
                binding.refreshButton.visibility = View.GONE

                binding.progressBar.startAnimation(animFadeIn)
                binding.progressBar.visibility = View.VISIBLE
            }

            override fun onAnimationRepeat(animation: Animation?) { }
        })
    }

    private fun refreshButtonIsHide(binding: FragmentUsdListenerBinding) = (binding.progressBar.visibility == View.VISIBLE)

    private fun showNotificationSettingsDialog() {
        if(!dialogNotificationSettings.isAdded) {
            dialogNotificationSettings.show(parentFragmentManager, NOTIFICATION_SETTINGS_DIALOG_FRAGMENT_TAG)
        }
    }

    private fun showNoInternetDialog() {
        if(!dialogNoInternet.isAdded) {
            dialogNoInternet.show(parentFragmentManager, NO_INTERNET_DIALOG_FRAGMENT_TAG)
        }
    }

    private fun getNotificationValue() = requireContext().getSharedPreferences(Config.SHARED_PREFERENCES, Context.MODE_PRIVATE).getFloat(getString(R.string.pref_notification_value), 0f)

    private fun updateNotificationSettingsButtonText(binding: FragmentUsdListenerBinding) {
        UsdNotificationWorker.createOrDisable(requireContext())

        val value = getNotificationValue()
        binding.notificationButton.text = if(value == 0f) "activate notification" else String.format("Notification if >%.2f", value)
    }

    private fun refreshRepository(binding: FragmentUsdListenerBinding) {
        if(AppStatus.isOnline(requireContext())) {
            if(!refreshButtonIsHide(binding)) {
                hideRefreshButton(binding)
            }
            model.refreshRepository()
        } else {
            if(refreshButtonIsHide(binding)) {
                showRefreshButton(binding)
            }
            showNoInternetDialog()
        }
    }

    inner class Adapter(private val list: List<Repository.DayUsdValue>) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var valueView: TextView? = null
            var dateView: TextView? = null
            var changeView: TextView? = null

            init {
                valueView = itemView.findViewById(R.id.dayUsdValueView)
                dateView = itemView.findViewById(R.id.dayUsdDateView)
                changeView = itemView.findViewById(R.id.dayUsdChangeView)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_day_usd, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.valueView?.text = String.format("%.2f USD/RUB", list[position].value)
            holder.dateView?.text = list[position].date

            val current = list[position].value
            val last = list[position + 1].value
            val change = String.format("(%.3f%%)", (if(current > last) current / last else last / current) - 1)
            holder.changeView?.text = change
            holder.changeView?.setCompoundDrawablesWithIntrinsicBounds(if(current > last) R.drawable.ic_round_north_24 else R.drawable.ic_round_south_24, 0, 0, 0)
            holder.changeView?.setTextColor(if(current > last) ResourcesCompat.getColor(resources, R.color.jade, null) else ResourcesCompat.getColor(resources, R.color.amaranth, null))
        }

        override fun getItemCount(): Int = list.size - 1
    }
}