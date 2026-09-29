package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {
    private val rupiahSymbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
        currencySymbol = "Rp "
        groupingSeparator = '.'
        monetaryDecimalSeparator = ','
    }

    private val rupiahFormat = DecimalFormat("Rp #,###", rupiahSymbols)

    fun formatRupiah(amount: Long): String {
        return rupiahFormat.format(amount)
    }

    fun cleanDigits(input: String): Long {
        val digitsOnly = input.filter { it.isDigit() }
        return digitsOnly.toLongOrNull() ?: 0L
    }

    fun formatNumber(amount: Long): String {
        val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
            groupingSeparator = '.'
        }
        val format = DecimalFormat("#,###", symbols)
        return format.format(amount)
    }
}
