package br.edu.ufabc.pocnotification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import br.edu.ufabc.pocnotification.BroadcastReceiver.Companion.calculateNextTriggerDateInMillis
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val alarmManager: AlarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, BroadcastReceiver::class.java)
        Log.d("INTENT", intent.toString())
        val reminder = BroadcastReceiver()

        var hour = 0
        var minutes = 0

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        reminder.createNotificationChannel(this)

        val timePickerButton = findViewById<Button>(R.id.bt_pick_time)
        timePickerButton.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .build()

            picker.addOnPositiveButtonClickListener {
                hour = picker.hour
                minutes = picker.minute
            }
            picker.show(this.supportFragmentManager, "picker")
        }

        val notificationId = reminder.generateUniqueId()
        val notificationSwitch = findViewById<SwitchMaterial>(R.id.switch_notification)
        notificationSwitch.setOnCheckedChangeListener { _, _ ->
            if (notificationSwitch.isChecked) {
                Log.d("MAIN", "Switch on")
                val newNotification = Notification(1,"comum", emptyList(), hour, minutes, true)
                val checkBocContainer = findViewById<LinearLayout>(R.id.create_notification_weekdays_picker) as LinearLayout
//
//                for (i in 0 until checkBocContainer.childCount) {
//                    val checkbox = checkBocContainer.getChildAt(i) as CheckBox
//                    if (checkbox.isChecked) {

                val pendingIntent: PendingIntent = PendingIntent.getBroadcast(this,
                    1,
                    intent.putExtra("garbageCategory", newNotification.category)
                        .putExtra("addressId",1), PendingIntent.FLAG_IMMUTABLE)


                findViewById<TextView>(R.id.tv_notification_id).text = notificationId.toString()
                Log.e("debug context", this.toString())
                reminder.setAlarm(this, newNotification, 5, pendingIntent)
//                    }
            } else {
                Log.d("MAIN", "Switch off")

                val pendingIntent: PendingIntent = PendingIntent.getBroadcast(this, notificationId, intent, PendingIntent.FLAG_IMMUTABLE)

                reminder.cancelAlarm(this, pendingIntent)
            }

        }
    }
}



