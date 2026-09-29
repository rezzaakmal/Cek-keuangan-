package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val localeId = Locale("id", "ID")
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val isoMonthFormat = SimpleDateFormat("yyyy-MM", Locale.US)
    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", localeId)
    private val displayMonthFormat = SimpleDateFormat("MMMM yyyy", localeId)

    fun getCurrentDate(): String {
        return isoDateFormat.format(Date())
    }

    fun getCurrentMonth(): String {
        return isoMonthFormat.format(Date())
    }

    fun formatMonthYear(monthIso: String): String {
        return try {
            val date = isoMonthFormat.parse(monthIso)
            if (date != null) displayMonthFormat.format(date) else monthIso
        } catch (e: Exception) {
            monthIso
        }
    }

    fun formatDisplayDate(dateIso: String): String {
        return try {
            val date = isoDateFormat.parse(dateIso)
            if (date != null) displayDateFormat.format(date) else dateIso
        } catch (e: Exception) {
            dateIso
        }
    }

    fun adjustMonth(currentMonthIso: String, monthOffset: Int): String {
        return try {
            val cal = Calendar.getInstance()
            val date = isoMonthFormat.parse(currentMonthIso)
            if (date != null) cal.time = date
            cal.add(Calendar.MONTH, monthOffset)
            isoMonthFormat.format(cal.time)
        } catch (e: Exception) {
            currentMonthIso
        }
    }
}
