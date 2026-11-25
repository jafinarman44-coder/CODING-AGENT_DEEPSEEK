// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.docs.printing

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.print.PrintJob
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.lifeeasy.agent.feature.docs.internal.xml
import com.lifeeasy.agent.feature.docs.model.DocumentRequest

/** Explicit approval contract: caller must verify both the voice confirmation and secret code. */
fun interface PrintApproval {
    fun isApprovedByVoiceAndCode(request: DocumentRequest): Boolean
}

/** Uses Android's system print service; it does not silently print or bypass user confirmation. */
class PrintCommand {
    fun print(context: Context, request: DocumentRequest, approval: PrintApproval, onStarted: (PrintJob) -> Unit, onError: (String) -> Unit = {}) {
        try {
            request.validated()
            if (!approval.isApprovedByVoiceAndCode(request)) {
                onError("Printing was not authorized. Voice confirmation and secret-code verification are required.")
                return
            }
            val activity = context.findActivity() ?: run {
                onError("Android printing requires an Activity context.")
                return
            }
            val printManager = activity.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: run {
                onError("Android print service is unavailable on this device.")
                return
            }
            val webView = WebView(activity)
            webView.settings.javaScriptEnabled = false
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String?) {
                    try {
                        val adapter = view.createPrintDocumentAdapter("LIFEEASY-${safeName(request.title)}")
                        val job = printManager.print(safeName(request.title), adapter, null)
                        onStarted(job)
                    } catch (e: Exception) {
                        onError(e.message ?: "Could not start Android print job.")
                        view.destroy()
                    }
                }
            }
            webView.loadDataWithBaseURL(null, request.toPrintHtml(), "text/HTML", "UTF-8", null)
        } catch (e: Exception) {
            onError(e.message ?: "Print request failed.")
        }
    }

    private fun DocumentRequest.toPrintHtml(): String = buildString {
        append("<!doctype html><html><head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"><style>body{font-family:Arial,sans-serif;color:#263746;padding:28px}h1{color:#123b5d;border-bottom:3px solid #1677a8;padding-bottom:12px}h2{color:#1677a8;margin-top:26px}p{line-height:1.55}table{border-collapse:collapse;width:100%;margin-top:18px}td,th{border:1px solid #ccd6df;padding:8px;text-align:left}th{background:#123b5d;color:white}</style></head><body>")
        append("<h1>${xml(title)}</h1>")
        paragraphs.forEach { append("<p>${xml(it).replace("\n", "<br>")}</p>") }
        sections.forEach { section -> append("<h2>${xml(section.heading)}</h2>"); section.paragraphs.forEach { append("<p>${xml(it)}</p>") } }
        table?.let { t -> append("<table><thead><tr>"); t.headers.forEach { append("<th>${xml(it)}</th>") }; append("</tr></thead><tbody>"); t.rows.forEach { row -> append("<tr>"); row.forEach { append("<td>${xml(it)}</td>") }; append("</tr>") }; append("</tbody></table>") }
        append("<footer><p>Prepared with LIFEEASY</p></footer></body></html>")
    }

    private fun safeName(value: String) = value.replace(Regex("[^A-Za-z0-9_-]"), "_").take(50).ifBlank { "LIFEEASY_Document" }
    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.takeIf { it !== this }?.findActivity()
        else -> null
    }
}
