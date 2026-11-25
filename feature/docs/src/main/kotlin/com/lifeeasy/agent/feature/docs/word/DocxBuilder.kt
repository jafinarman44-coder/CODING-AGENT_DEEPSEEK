// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.docs.word

import com.lifeeasy.agent.feature.docs.internal.*
import com.lifeeasy.agent.feature.docs.model.*
import java.io.File

/** Creates a standards-based Office Open XML .docx without a heavyweight runtime dependency. */
class DocxBuilder {
    fun build(request: DocumentRequest, destination: File): DocumentResult {
        prepare(request, destination, "docx")?.let { return it }
        return runBuild(destination) {
            val body = buildString {
                append(paragraph(request.title, "Title"))
                request.paragraphs.forEach { append(paragraph(it)) }
                request.sections.forEach { section ->
                    append(paragraph(section.heading, "Heading1"))
                    section.paragraphs.forEach { append(paragraph(it)) }
                }
                request.table?.let { table ->
                    append("<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblBorders><w:top w:val=\"single\" w:sz=\"4\"/><w:left w:val=\"single\" w:sz=\"4\"/><w:bottom w:val=\"single\" w:sz=\"4\"/><w:right w:val=\"single\" w:sz=\"4\"/><w:insideH w:val=\"single\" w:sz=\"4\"/><w:insideV w:val=\"single\" w:sz=\"4\"/></w:tblBorders></w:tblPr>")
                    (listOf(table.headers) + table.rows).forEachIndexed { rowIndex, row ->
                        append("<w:tr>")
                        row.forEach { cell ->
                            val headerFill = if (rowIndex == 0) "<w:shd w:fill=\"123B5D\"/>" else ""
                            append("<w:tc><w:tcPr><w:tcW w:w=\"2400\" w:type=\"auto\"/>$headerFill</w:tcPr>")
                            append(paragraph(cell, if (rowIndex == 0) "TableHeader" else null)); append("</w:tc>")
                        }
                        append("</w:tr>")
                    }
                    append("</w:tbl>")
                }
                append("<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/><w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>")
            }
            writePackage(destination, linkedMapOf(
                "[Content_Types].xml" to """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/><Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/></Types>""",
                "_rels/.rels" to """<?xml version="1.0" encoding="UTF-8"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/></Relationships>""",
                "word/document.xml" to """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:body>$body</w:body></w:document>""",
                "word/_rels/document.xml.rels" to """<?xml version="1.0" encoding="UTF-8"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/></Relationships>""",
                "word/styles.xml" to """<?xml version="1.0" encoding="UTF-8"?><w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:style w:type="paragraph" w:styleId="Normal"><w:name w:val="Normal"/><w:rPr><w:rFonts w:ascii="Aptos" w:hAnsi="Aptos"/><w:sz w:val="22"/></w:rPr></w:style><w:style w:type="paragraph" w:styleId="Title"><w:name w:val="Title"/><w:pPr><w:spacing w:after="360"/></w:pPr><w:rPr><w:b/><w:color w:val="123B5D"/><w:sz w:val="36"/></w:rPr></w:style><w:style w:type="paragraph" w:styleId="Heading1"><w:name w:val="heading 1"/><w:pPr><w:keepNext/><w:spacing w:before="280" w:after="120"/></w:pPr><w:rPr><w:b/><w:color w:val="1677A8"/><w:sz w:val="28"/></w:rPr></w:style><w:style w:type="paragraph" w:styleId="TableHeader"><w:name w:val="Table Header"/><w:rPr><w:b/><w:color w:val="FFFFFF"/></w:rPr></w:style><w:style w:type="table" w:styleId="TableGrid"><w:name w:val="Table Grid"/><w:tblPr><w:tblBorders><w:top w:val="single" w:sz="4"/><w:left w:val="single" w:sz="4"/><w:bottom w:val="single" w:sz="4"/><w:right w:val="single" w:sz="4"/><w:insideH w:val="single" w:sz="4"/><w:insideV w:val="single" w:sz="4"/></w:tblBorders></w:tblPr></w:style></w:styles>"""
            ))
        }
    }

    private fun paragraph(text: String, style: String? = null): String {
        val pStyle = style?.let { "<w:pPr><w:pStyle w:val=\"$it\"/></w:pPr>" }.orEmpty()
        return "<w:p>$pStyle<w:r><w:t xml:space=\"preserve\">${xml(text)}</w:t></w:r></w:p>"
    }
}
