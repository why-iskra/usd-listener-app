package ru.unit.usd_listener.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.unit.usd_listener.Repository

class UsdListenerViewModel : ViewModel() {

    val usdPriceWithin30Days = MutableLiveData<List<Repository.DayUsdValue>>()

    fun refreshRepository() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                usdPriceWithin30Days.postValue(Repository.getValuesWithin30Days())
            } catch (e: Exception) {
                usdPriceWithin30Days.postValue(emptyList())
            }
        }
    }

}