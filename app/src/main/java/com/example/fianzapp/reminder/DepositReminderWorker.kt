package com.example.fianzapp.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.fianzapp.MainActivity
import com.example.fianzapp.R
import com.example.fianzapp.data.SettingsRepository
import com.example.fianzapp.data.db.FianzappDatabase
import com.example.fianzapp.domain.Reminder
import com.example.fianzapp.domain.depositDeadline
import com.example.fianzapp.domain.parseDate
import com.example.fianzapp.domain.reminderToSend
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.concurrent.TimeUnit

private const val CHANNEL_ID = "deposit_reminder"
private const val NOTIFICATION_ID = 36
/** The unique name of the daily work, and of the one-off run used to test it. */
private const val PERIODIC_WORK = "deposit_reminder_daily"
private const val TEST_WORK = "deposit_reminder_test"

/**
 * A7: once a day, reads the day the keys went back (Room), works out the deadline with the same
 * depositDeadline() as the screens, and notifies on the reminder day (A3) and on the deadline.
 * It runs with no screen, so it uses the application context.
 */
class DepositReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val lease = FianzappDatabase.getInstance(context).leaseDao().observeLease().first()
        // No lease or no day of handing back the keys yet is not an error: nothing to remind.
        val keys = lease?.keysReturnedDate?.let(::parseDate) ?: return Result.success()
        val deadline = depositDeadline(keys)

        val settings = SettingsRepository(context)
        if (!settings.reminderOn.first()) return Result.success()

        val reminder = reminderToSend(
            deadline = deadline,
            today = LocalDate.now(),
            warningDays = settings.warningDays.first(),
            warningSentFor = settings.warningSentFor(),
            deadlineSentFor = settings.deadlineSentFor()
        )
        if (reminder == Reminder.NONE || !canNotify(context)) return Result.success()

        val date = deadline.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
        val text = if (reminder == Reminder.WARNING) {
            context.getString(R.string.reminder_warning_text, date)
        } else {
            context.getString(R.string.reminder_deadline_text, date)
        }
        showNotification(context, text)
        if (reminder == Reminder.WARNING) {
            settings.markWarningSent(deadline.toString())
        } else {
            settings.markDeadlineSent(deadline.toString())
        }
        return Result.success()
    }
}

/** On Android 13 or later the app may only notify with the permission. */
fun canNotify(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

private fun showNotification(context: Context, text: String) {
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(
        NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.reminder_channel),
            NotificationManager.IMPORTANCE_DEFAULT
        )
    )
    val openApp = PendingIntent.getActivity(
        context, 0,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE
    )
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(context.getString(R.string.reminder_title))
        .setContentText(text)
        .setStyle(NotificationCompat.BigTextStyle().bigText(text))
        .setContentIntent(openApp)
        .setAutoCancel(true)
        .build()
    try {
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    } catch (e: SecurityException) {
        // The permission was taken away between the check and now: no notification, no crash.
    }
}

/** Turns the daily reminder on (KEEP: turning it on twice does not restart it) or off. */
object ReminderScheduler {
    fun enable(context: Context) {
        val request = PeriodicWorkRequestBuilder<DepositReminderWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(PERIODIC_WORK, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun disable(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK)
    }

    /** One run now, under its own name, so it never cancels the daily work. For testing. */
    fun runNow(context: Context) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            TEST_WORK,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<DepositReminderWorker>().build()
        )
    }
}
