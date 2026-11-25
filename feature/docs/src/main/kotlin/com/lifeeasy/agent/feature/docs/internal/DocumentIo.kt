// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.docs.internal

import com.lifeeasy.agent.feature.docs.model.DocumentRequest
import com.lifeeasy.agent.feature.docs.model.DocumentResult
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal fun prepare(request: DocumentRequest, destination: File, extension: String): DocumentResult? {
    return try {
        request.validated()
        require(destination.name.endsWith(extension, ignoreCase = true)) { "Destination must use .$extension extension." }
        destination.parentFile?.let { if (!it.exists() && !it.mkdirs()) error("Cannot create output directory.") }
        if (destination.exists() && !destination.canWrite()) error("Destination is not writable.")
        null
    } catch (e: Exception) {
        DocumentResult.failure(e.message ?: "Invalid document request.")
    }
}

internal fun writePackage(destination: File, entries: Map<String, String>) {
    FileOutputStream(destination).use { fileOut ->
        ZipOutputStream(fileOut).use { zip ->
            entries.forEach { (path, content) ->
                zip.putNextEntry(ZipEntry(path))
                zip.write(content.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
        }
    }
}

internal fun xml(value: String): String = buildString(value.length) {
    value.forEach { c ->
        when (c) {
            '&' -> append("&amp;")
            '<' -> append("&lt;")
            '>' -> append("&gt;")
            '"' -> append("&quot;")
            '\'' -> append("&apos;")
            else -> if (c == '\n' || c == '\t' || c >= ' ') append(c)
        }
    }
}

internal fun runBuild(destination: File, block: () -> Unit): DocumentResult = try {
    block()
    if (!destination.isFile || destination.length() == 0L) DocumentResult.failure("Export did not produce a file.")
    else DocumentResult.success(destination)
} catch (e: Exception) {
    destination.delete()
    DocumentResult.failure(e.message ?: "Document export failed.")
}
