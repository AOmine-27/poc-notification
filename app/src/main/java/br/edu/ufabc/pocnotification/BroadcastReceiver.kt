package br.edu.ufabc.pocnotification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.*

class BroadcastReceiver: BroadcastReceiver() {

    fun getIntent(context: Context, requestCode: Int): PendingIntent? {
        val intent = Intent(context, BroadcastReceiver::class.java)
        // https://developer.android.com/reference/android/app/PendingIntent.html#getBroadcast(android.content.Context,%20int,%20android.content.Intent,%20int)
        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    fun generateUniqueId ():Int = System.currentTimeMillis().toInt()

    fun setAlarm (context: Context,
                  notification: Notification,
                  weekday: Int,
                  pendingIntent: PendingIntent) {
        val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val scheduleData = calculateNextTriggerDateInMillis(weekday, notification.hours, notification.minutes)

        Log.d("BROAD | SCHEDULE", scheduleData.toString())
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
            scheduleData+10000,60000, pendingIntent)
    }

    fun cancelAlarm (context: Context, pendingIntent: PendingIntent) {
        val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    fun calculateNextTriggerDateInMillis (weekday: Int, hour: Int, minutes: Int ): Long {
        val scheduledDate = Calendar.getInstance() //Today
        if (scheduledDate.get(Calendar.DAY_OF_WEEK) != weekday) {
            scheduledDate.add(Calendar.DAY_OF_MONTH, (weekday + 7 - scheduledDate.get(Calendar.DAY_OF_WEEK))% 7)
        } else {
            val minOfDay = scheduledDate.get(Calendar.HOUR_OF_DAY) * 60 + scheduledDate.get(Calendar.MINUTE)
            if (minOfDay >= (hour) * 60 + minutes) scheduledDate.add(Calendar.DAY_OF_MONTH, 7) //Next week
        }
        scheduledDate.set(Calendar.HOUR_OF_DAY, hour) //To local time
        scheduledDate.set(Calendar.MINUTE, minutes)

        Log.d("BROAD", "Calculated: ${scheduledDate.timeInMillis}")
        return scheduledDate.timeInMillis
    }

    fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "POCNotification_createNot_name"
            val descriptionText = "POC_notification_descript"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("POCNotification_create_id", name, importance).apply {
                description = descriptionText
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onReceive(p0: Context?, p1: Intent?) {
        val garbageType = p1?.getStringExtra("garbageCategory").toString()
        val addressId = p1?.getIntExtra("addressId",0)
        Log.d("DEBUG", addressId.toString())

        var builder = NotificationCompat.Builder(p0!!, "POCNotification_create_id")
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle("Seu Lembrete de retirar o lixo!")
            .setContentText(p0.getString(R.string.notification_content,garbageType))
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        Log.d("BROAD", "Notification Time: ${System.currentTimeMillis().toString()}, id: $addressId")
        var notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(p0)

        notificationManager.notify(addressId!!, builder.build())
    }

    companion object {
         fun calculateNextTriggerDateInMillis(weekday: Int, hour: Int, minutes: Int): Long {
             val scheduledDate = Calendar.getInstance(TimeZone.getTimeZone("GMT-3")) //Today, GMT-3 (Brasilia)
             val scheduledDate2 = Calendar.getInstance() //Today, GMT-3 (Brasilia)
             Log.d("debug: GMT-3", scheduledDate.toString())
             Log.d("debug: GMT", scheduledDate2.toString())
            if (scheduledDate.get(Calendar.DAY_OF_WEEK) != weekday) {
                scheduledDate.add(
                    Calendar.DAY_OF_MONTH,
                    (weekday - scheduledDate.get(Calendar.DAY_OF_WEEK)) % 7
                )

            } else {
                val minOfDay =
                    scheduledDate.get(Calendar.HOUR_OF_DAY) * 60 + scheduledDate.get(Calendar.MINUTE)
                if ( minOfDay >= (hour * 60 + minutes) ) scheduledDate.add(
                    Calendar.DAY_OF_MONTH,
                    7
                ) //Next week
            }
            scheduledDate.set(Calendar.HOUR_OF_DAY, hour)
            scheduledDate.set(Calendar.MINUTE, minutes)
            scheduledDate.timeZone.rawOffset

            return scheduledDate.timeInMillis
        }
    }
}