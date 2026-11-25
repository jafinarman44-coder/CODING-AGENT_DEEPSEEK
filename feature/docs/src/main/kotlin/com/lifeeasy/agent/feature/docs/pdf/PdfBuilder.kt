// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.docs.pdf

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.lifeeasy.agent.feature.docs.internal.prepare
import com.lifeeasy.agent.feature.docs.model.DocumentRequest
import com.lifeeasy.agent.feature.docs.model.DocumentResult
import java.io.File
import java.io.FileOutputStream

/** Native Android PDF renderer. Uses Android PdfDocument; no network or third-party service. */
class PdfBuilder {
    fun build(request: DocumentRequest, destination: File): DocumentResult {
        prepare(request, destination, "pdf")?.let { return it }
        val document = PdfDocument()
        // Keep the active page in function scope so both catch/finally can clean it up.
        var page: PdfDocument.Page? = null
        return try {
            val pageWidth = 595
            val pageHeight = 842
            val margin = 48f
            val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(38, 55, 70)
                textSize = 11f
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            }
            val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(18, 59, 93)
                textSize = 23f
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
            }
            val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(22, 119, 168)
                textSize = 15f
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
            }
            val rulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(22, 119, 168); strokeWidth = 2f }
            var pageNumber = 0
            var canvas: Canvas? = null
            var y = 0f
            fun newPage() {
                page?.let { document.finishPage(it) }
                pageNumber++
                page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                canvas = page!!.canvas
                canvas!!.drawColor(android.graphics.Color.WHITE)
                canvas!!.drawText("LIFEEASY", margin, 34f, sectionPaint)
                canvas!!.drawLine(margin, 48f, pageWidth - margin, 48f, rulePaint)
                y = 78f
            }
            fun ensureSpace(height: Float) { if (y + height > pageHeight - 58f) newPage() }
            fun drawWrapped(text: String, paint: Paint, lineGap: Float = 5f) {
                val maxWidth = pageWidth - margin * 2
                val normalized = text.replace("\r\n", "\n")
                normalized.split('\n').forEach { rawLine ->
                    if (rawLine.isEmpty()) { y += paint.textSize + lineGap; ensureSpace(1f); return@forEach }
                    var remaining = rawLine
                    while (remaining.isNotEmpty()) {
                        val count = paint.breakText(remaining, true, maxWidth, null).coerceAtLeast(1)
                        var splitAt = count
                        if (count < remaining.length) {
                            val space = remaining.lastIndexOf(' ', count - 1)
                            if (space > 0) splitAt = space
                        }
                        val line = remaining.substring(0, splitAt).trimEnd()
                        ensureSpace(paint.textSize + lineGap)
                        canvas!!.drawText(line, margin, y, paint)
                        y += paint.textSize + lineGap
                        remaining = remaining.substring(splitAt).trimStart()
                    }
                }
            }
            newPage()
            drawWrapped(request.title, headingPaint, 8f)
            canvas!!.drawLine(margin, y, pageWidth - margin, y, rulePaint)
            y += 22f
            request.paragraphs.forEach { drawWrapped(it, bodyPaint, 6f); y += 7f }
            request.sections.forEach { section ->
                ensureSpace(34f)
                drawWrapped(section.heading, sectionPaint, 6f)
                section.paragraphs.forEach { drawWrapped(it, bodyPaint, 6f); y += 5f }
            }
            request.table?.let { table ->
                val columns = table.headers.size
                val colWidth = (pageWidth - margin * 2) / columns.toFloat()
                val cellPaint = Paint(bodyPaint).apply { textSize = 9f }
                val headerPaint = Paint(sectionPaint).apply { textSize = 9f }
                fun tableRow(values: List<String>, header: Boolean) {
                    val maxLines = values.maxOfOrNull { value ->
                        val available = (colWidth - 12f).coerceAtLeast(20f)
                        var left = value
                        var count = 0
                        while (left.isNotEmpty()) { val n = (cellPaint.breakText(left, true, available, null)).coerceAtLeast(1); left = left.substring(n).trimStart(); count++ }
                        count.coerceAtLeast(1)
                    } ?: 1
                    val rowHeight = maxOf(25f, maxLines * 13f + 10f)
                    ensureSpace(rowHeight)
                    if (header) canvas!!.drawRect(margin, y - 15f, pageWidth - margin, y + rowHeight - 5f, Paint().apply { color = android.graphics.Color.rgb(235, 242, 248) })
                    values.forEachIndexed { index, value ->
                        val x = margin + index * colWidth + 5f
                        val p = if (header) headerPaint else cellPaint
                        val lines = wrap(value, p, colWidth - 12f)
                        lines.forEachIndexed { lineIndex, line -> canvas!!.drawText(line, x, y + lineIndex * 13f, p) }
                    }
                    canvas!!.drawLine(margin, y + rowHeight - 5f, pageWidth - margin, y + rowHeight - 5f, Paint().apply { color = android.graphics.Color.LTGRAY; strokeWidth = 0.5f })
                    y += rowHeight
                }
                tableRow(table.headers, true)
                table.rows.forEach { tableRow(it, false) }
            }
            canvas!!.drawLine(margin, pageHeight - 42f, pageWidth - margin, pageHeight - 42f, Paint().apply { color = android.graphics.Color.LTGRAY })
            canvas!!.drawText("Prepared with LIFEEASY  •  Page $pageNumber", margin, pageHeight - 25f, Paint(bodyPaint).apply { textSize = 8f; color = android.graphics.Color.GRAY })
            page?.let { document.finishPage(it); page = null }
            destination.parentFile?.let { if (!it.exists() && !it.mkdirs()) error("Cannot create output directory.") }
            FileOutputStream(destination).use { document.writeTo(it) }
            if (destination.length() > 0L) DocumentResult.success(destination) else DocumentResult.failure("PDF export produced an empty file.")
        } catch (e: Exception) {
            destination.delete()
            DocumentResult.failure(e.message ?: "PDF export failed.")
        } finally {
            page?.let { runCatching { document.finishPage(it) } }
            document.close()
        }
    }

    private fun wrap(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isEmpty()) return listOf("")
        val result = mutableListOf<String>()
        var remaining = text
        while (remaining.isNotEmpty()) {
            val count = paint.breakText(remaining, true, maxWidth, null).coerceAtLeast(1)
            val split = if (count < remaining.length) remaining.lastIndexOf(' ', count - 1).takeIf { it > 0 } ?: count else count
            result += remaining.substring(0, split).trimEnd()
            remaining = remaining.substring(split).trimStart()
        }
        return result
    }
}
