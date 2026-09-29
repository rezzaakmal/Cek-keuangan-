package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Keuangan Mandiri", appName)
    }

    @Test
    fun `currency formatter formats rupiah properly`() {
        val formatted = CurrencyFormatter.formatRupiah(50000)
        assertTrue(formatted.contains("50.000"))
    }

    @Test
    fun `date utils adjusts month forward and backward`() {
        val nextMonth = DateUtils.adjustMonth("2026-09", 1)
        assertEquals("2026-10", nextMonth)

        val prevMonth = DateUtils.adjustMonth("2026-09", -1)
        assertEquals("2026-08", prevMonth)
    }
}
