package ru.unit.usd_listener.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private val model: UsdListenerViewModel by activityViewModels()
    private lateinit var dialogNotificationSettings: NotificationSettingsDialogFragment
    private val dialogNoInternet = NoInternetDialogFragment()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentUsdListenerBinding>(inflater, R.layout.fragment_usd_listener, container, false)

        dialogNotificationSettings = NotificationSettingsDialogFragment {
            updateNotificationSettingsButtonText(binding)
        }

        refreshRepository(binding)

        updateNotificationSettingsButtonText(binding)

        binding.monthUsdValueView.layoutManager = LinearLayoutManager(context)

        binding.notificationButton.setOnClickListener {
            showNotificationSettingsDialog()
        }
        binding.refreshButton.setOnClickListener {
            hideRefreshButton(binding)
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

            println(max - min)
            println((max - min) / 4)
            val offset = ceil((max - min) / 4)
            for(i in 0 until 5) {
                val level = (offset * i) + min
                lines[i] = Chart.LevelLine(level.toInt().toString(), level)
            }

            binding.monthUsdValueView.adapter = Adapter(it.reversed())
            binding.usdValueView.text = String.format("%.2f $/₽", it.last().value)

            binding.usdChart.update(lines, elements, true) {
                showRefreshButton(binding)
            }
        }

        return binding.root
    }

    private fun showRefreshButton(binding: FragmentUsdListenerBinding) {
        binding.refreshButton.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }

    private fun hideRefreshButton(binding: FragmentUsdListenerBinding) {
        binding.refreshButton.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun showNotificationSettingsDialog() {
        dialogNotificationSettings.show(childFragmentManager, "notificationSettingsDialog")
    }

    private fun showNoInternetDialog() {
        dialogNoInternet.show(childFragmentManager, "noInternetDialog")
    }

    private fun getNotificationValue() = requireContext().getSharedPreferences(Config.SHARED_PREFERENCES, Context.MODE_PRIVATE).getFloat(getString(R.string.pref_notification_value), 0f)

    private fun updateNotificationSettingsButtonText(binding: FragmentUsdListenerBinding) {
        UsdNotificationWorker.createOrDisable(requireContext())

        val value = getNotificationValue()
        binding.notificationButton.text = if(value == 0f) "activate notification" else String.format("Notification if >%.2f", value)
    }

    private fun refreshRepository(binding: FragmentUsdListenerBinding) {
        if(AppStatus.isOnline(requireContext())) {
            model.refreshRepository()
        } else {
            showRefreshButton(binding)
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