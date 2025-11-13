package com.tolstykh.eatABurrita

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun padWithZeros(number: Int, totalLength: Int = 2): String {
    return number.toString().padStart(totalLength, '0')
}

fun formatDuration(durationInMillis: Long): String {
    val totalSeconds = durationInMillis / 1000
    val seconds = (totalSeconds % 60).toInt()
    val totalMinutes = totalSeconds / 60
    val minutes = (totalMinutes % 60).toInt()
    val days = (totalMinutes / (60 * 24)).toInt()
    val hours = ((totalMinutes % (60 * 24)) / 60).toInt()
    val secondsStr = padWithZeros(seconds)
    val minutesStr = padWithZeros(minutes)
    val hoursStr = padWithZeros(hours)

    val pluralDays = if (days == 1) "day" else "days"

    return "$days $pluralDays, $hoursStr:$minutesStr:$secondsStr"
}

fun dateFromMilliseconds(milliseconds: Long): String {
    val date = Date(milliseconds)
    val formatter = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault())

    return formatter.format(date)
}
