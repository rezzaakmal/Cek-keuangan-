package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.TransactionEntity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object CsvExporter {

    fun exportAndShare(
        context: Context,
        transactions: List<TransactionEntity>,
        monthName: String
    ): Result<String> {
        return try {
            val reportDir = File(context.cacheDir, "reports")
            if (!reportDir.exists()) {
                reportDir.mkdirs()
            }

            val sanitizedMonth = monthName.replace(" ", "_")
            val fileName = "Laporan_Keuangan_${sanitizedMonth}.csv"
            val file = File(reportDir, fileName)

            FileOutputStream(file).use { fos ->
                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                    // UTF-8 BOM so Microsoft Excel correctly displays Indonesian characters and formatting
                    writer.write("\uFEFF")

                    // CSV Headers
                    writer.write("\"Tanggal\",\"Jenis\",\"Kategori\",\"Keperluan/Catatan\",\"Nominal (Rp)\"\r\n")

                    for (tx in transactions) {
                        val typeLabel = if (tx.type == "in") "Pemasukan" else "Pengeluaran"
                        val safeCategory = tx.category.replace("\"", "\"\"")
                        val safeDesc = tx.description.replace("\"", "\"\"")
                        val signedAmount = if (tx.type == "in") tx.amount else -tx.amount

                        writer.write("\"${tx.date}\",\"$typeLabel\",\"$safeCategory\",\"$safeDesc\",$signedAmount\r\n")
                    }
                    writer.flush()
                }
            }

            // Share via Android Intent
            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Laporan Keuangan $monthName")
                putExtra(Intent.EXTRA_TEXT, "Laporan Keuangan $monthName dari Pencatat Keuangan Mandiri")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Ekspor Laporan ke Excel / Aplikasi Lain").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)

            Result.success(file.name)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
