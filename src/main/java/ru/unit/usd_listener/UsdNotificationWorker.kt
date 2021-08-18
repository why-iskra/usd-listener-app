package ru.unit.usd_listener

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.unit.usd_listener.ui.MainActivity
import java.util.concurrent.TimeUnit

class UsdNotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    companion object {
        fun createOrDisable(context: Context) {
            val workManager = WorkManager.getInstance()
            if(getNotificationValue(context) == 0f) {
                workManager.cancelAllWorkByTag(MainActivity.WORKER_TAG)
            } else {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val worker =
                    PeriodicWorkRequest.Builder(UsdNotificationWorker::class.java, 1, TimeUnit.DAYS)
                        .setConstraints(constraints)
                        .addTag(MainActivity.WORKER_TAG)
                        .build()

                workManager.enqueueUniquePeriodicWork(MainActivity.WORKER_TAG, ExistingPeriodicWorkPolicy.KEEP, worker)
            }
        }

        private fun getNotificationValue(context: Context) = context.getSharedPreferences(Config.SHARED_PREFERENCES, Context.MODE_PRIVATE).getFloat(context.getString(R.string.pref_notification_value), 0f)
    }

    override fun doWork(): Result {
        val value = getNotificationValue(applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            val list = Repository.getValuesWithin30Days()
            withContext(Dispatchers.Main) {
                val newValue = list.last().value
				if (newValue > value) {
                    val builder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext, MainActivity.CHANNEL_ID)
                        .setContentTitle("Value per USD exceeded %.2f RUB".format(value))
                        .setContentText("%.2f USD/RUB".format(newValue))
                        .setSmallIcon(R.drawable.ic_round_dollar_24)

                    val notification: Notification = builder.build()

                    val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(1, notification)
				}
            }
        }

        return Result.success()
    }

}