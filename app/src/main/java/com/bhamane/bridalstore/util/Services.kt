package com.bhamane.bridalstore.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.bhamane.bridalstore.data.RentalEntity
import com.bhamane.bridalstore.data.SaleWithItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelExporter {
    suspend fun export(
        context: Context,
        sales: List<SaleWithItems>,
        rentals: List<RentalEntity>
    ): File = withContext(Dispatchers.IO) {
        val workbook = XSSFWorkbook()

        fun formatDate(ms: Long): String =
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(ms))

        val salesSheet = workbook.createSheet("Sales")
        val salesHeader = salesSheet.createRow(0)
        listOf(
            "Bill No", "Date", "Customer", "Phone", "Address",
            "Items", "Subtotal", "Discount", "Final Total"
        ).forEachIndexed { index, value ->
            salesHeader.createCell(index).setCellValue(value)
        }

        sales.forEachIndexed { rowIndex, saleWithItems ->
            val row = salesSheet.createRow(rowIndex + 1)
            val sale = saleWithItems.sale
            val values = listOf(
                sale.billNumber,
                formatDate(sale.date),
                sale.customerName,
                sale.phone,
                sale.address,
                saleWithItems.items.joinToString(" | ") {
                    "${it.name} x${it.quantity} @ ${it.unitPrice}"
                },
                sale.subtotal.toString(),
                sale.discount.toString(),
                sale.finalTotal.toString()
            )
            values.forEachIndexed { index, value ->
                row.createCell(index).setCellValue(value)
            }
        }

        val rentalsSheet = workbook.createSheet("Rentals")
        val rentalsHeader = rentalsSheet.createRow(0)
        listOf(
            "Rental No", "Item", "Customer", "Phone", "Start",
            "Return", "Price", "Status", "Photo URI"
        ).forEachIndexed { index, value ->
            rentalsHeader.createCell(index).setCellValue(value)
        }

        rentals.forEachIndexed { rowIndex, rental ->
            val row = rentalsSheet.createRow(rowIndex + 1)
            val values = listOf(
                rental.rentalNumber,
                rental.itemName,
                rental.customerName,
                rental.phone,
                formatDate(rental.startDate),
                formatDate(rental.returnDate),
                rental.rentalPrice.toString(),
                rental.status,
                rental.photoUri ?: ""
            )
            values.forEachIndexed { index, value ->
                row.createCell(index).setCellValue(value)
            }
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        val file = File(context.cacheDir, "Bhamane_Bills_${timestamp}.xlsx")
        FileOutputStream(file).use { output ->
            workbook.write(output)
        }
        workbook.close()
        file
    }

    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            file
        )

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(sendIntent, "Share Excel report")
        )
    }
}

object WhatsAppReporter {
    fun send(
        context: Context,
        date: String,
        sales: Double,
        rentals: Double,
        bills: Int,
        rentalCount: Int
    ) {
        val message = """
            🌸 BHAMANE BRIDAL STORE
            Daily Business Report
            📅 $date

            💰 Sales: ₹$sales
            🧾 Bills: $bills
            👑 Rental Income: ₹$rentals
            📦 Rental Orders: $rentalCount

            📊 Total Business: ₹${sales + rentals}
        """.trimIndent()

        val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            setPackage("com.whatsapp")
        }

        try {
            context.startActivity(whatsappIntent)
        } catch (_: Exception) {
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            context.startActivity(
                Intent.createChooser(fallbackIntent, "Send daily report")
            )
        }
    }
}
