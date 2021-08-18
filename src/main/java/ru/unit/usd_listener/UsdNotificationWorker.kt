package ru.unit.usd_listener

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.unit.usd_listener.ui.MainActivity

class UsdNotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val value = getNotificationValue()

        CoroutineScope(Dispatchers.IO).launch {
            val list = Repository.getValuesWithin30Days()
            withContext(Dispatchers.Main) {
                val newValue = list.last().value
//				if (newValue > value) {
                val builder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext, MainActivity.CHANNEL_ID)
                    .setContentTitle("Value per USD exceeded %.2f RUB".format(value))
                    .setContentText("%.2f USD/RUB".format(newValue))
                    .setSmallIcon(R.drawable.ic_round_dollar_24)

                val notification: Notification = builder.build()

                val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(1, notification)
//				}
            }
        }

        return Result.success()
    }

    private fun getNotificationValue() = applicationContext.getSharedPreferences(Config.SHARED_PREFERENCES, Context.MODE_PRIVATE).getFloat(applicationContext.getString(R.string.pref_notification_value), 0f)
}