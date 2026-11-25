// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.docs.model

/** A portable document model shared by all export formats. */
data class DocumentRequest(
    val title: String,
    val paragraphs: List<String> = emptyList(),
    val sections: List<DocumentSection> = emptyList(),
    val table: DocumentTable? = null,
    val author: String = "MAHADI HASAN JEWEL"
) {
    fun validated(): DocumentRequest {
        require(title.isNotBlank()) { "Document title must not be blank." }
        require(title.length <= 240) { "Document title is too long (maximum 240 characters)." }
        require(paragraphs.size <= 10_000) { "Too many paragraphs." }
        require(sections.size <= 500) { "Too many sections." }
        require(paragraphs.sumOf(String::length) <= 2_000_000) { "Document text exceeds the 2 MB limit." }
        require(sections.all { it.heading.length <= 240 && it.paragraphs.size <= 10_000 }) { "Invalid section content." }
        table?.let {
            require(it.headers.isNotEmpty()) { "A table must have at least one column." }
            require(it.rows.size <= 100_000) { "Table has too many rows." }
            require(it.rows.all { row -> row.size == it.headers.size }) { "Every table row must match the header column count." }
        }
        return this
    }
}

data class DocumentSection(val heading: String, val paragraphs: List<String>)
data class DocumentTable(val headers: List<String>, val rows: List<List<String>>)

data class DocumentResult(val success: Boolean, val file: java.io.File? = null, val error: String? = null) {
    companion object {
        fun success(file: java.io.File) = DocumentResult(true, file, null)
        fun failure(message: String) = DocumentResult(false, null, message)
    }
}
