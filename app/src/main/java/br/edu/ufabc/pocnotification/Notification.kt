package br.edu.ufabc.pocnotification

import android.os.Parcelable

data class Notification(
    val id: Long,
    val category: String,
    val weekdays: List<String>,
    val hours: Int,
    val minutes: Int,
    val isActive: Boolean,
)
