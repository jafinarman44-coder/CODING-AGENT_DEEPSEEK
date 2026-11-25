// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.docs.spreadsheet

import com.lifeeasy.agent.feature.docs.internal.*
import com.lifeeasy.agent.feature.docs.model.*
import java.io.File

/** Writes a minimal, interoperable SpreadsheetML workbook (.xlsx). */
class XlsxBuilder {
    fun build(request: DocumentRequest, destination: File): DocumentResult {
        prepare(request, destination, "xlsx")?.let { return it }
        return runBuild(destination) {
            val tableHeaderIndex = request.table?.let {
                1 + request.paragraphs.size + request.sections.sumOf { section -> section.paragraphs.size + 1 }
            }
            val rows = buildList {
                add(listOf(request.title))
                request.paragraphs.forEach { add(listOf(it)) }
                request.sections.forEach { section ->
                    add(listOf(section.heading)); section.paragraphs.forEach { add(listOf(it)) }
                }
                request.table?.let { add(it.headers); addAll(it.rows) }
            }
            val sheetRows = rows.mapIndexed { rowIndex, cells ->
                val cellsXml = cells.mapIndexed { colIndex, value ->
                    val ref = "${columnName(colIndex + 1)}${rowIndex + 1}"
                    val style = when {
                        rowIndex == 0 -> " s=\"1\""
                        tableHeaderIndex == rowIndex -> " s=\"2\""
                        else -> ""
                    }
                    "<c r=\"$ref\" t=\"inlineStr\"$style><is><t xml:space=\"preserve\">${xml(value)}</t></is></c>"
                }.joinToString("")
                "<row r=\"${rowIndex + 1}\">$cellsXml</row>"
            }.joinToString("")
            writePackage(destination, linkedMapOf(
                "[Content_Types].xml" to """<?xml version="1.0" encoding="UTF-8"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/><Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/></Types>""",
                "_rels/.rels" to """<?xml version="1.0" encoding="UTF-8"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>""",
                "xl/workbook.xml" to """<?xml version="1.0" encoding="UTF-8"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="LIFEEASY" sheetId="1" r:id="rId1"/></sheets></workbook>""",
                "xl/_rels/workbook.xml.rels" to """<?xml version="1.0" encoding="UTF-8"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/><Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/></Relationships>""",
                "xl/worksheets/sheet1.xml" to """<?xml version="1.0" encoding="UTF-8"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetViews><sheetView workbookViewId="0"><pane ySplit="1" topLeftCell="A2" activePane="bottomLeft" state="frozen"/></sheetView></sheetViews><sheetFormatPr defaultRowHeight="20"/><cols><col min="1" max="20" width="28" customWidth="1"/></cols><sheetData>$sheetRows</sheetData>${request.table?.let { "<autoFilter ref=\"A${(tableHeaderIndex ?: 0) + 1}:${columnName(it.headers.size)}${rows.size}\"/>" }.orEmpty()}</worksheet>""",
                "xl/styles.xml" to """<?xml version="1.0" encoding="UTF-8"?><styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><fonts count="2"><font><sz val="11"/><name val="Aptos"/></font><font><b/><color rgb="FFFFFFFF"/><sz val="12"/><name val="Aptos Display"/></font></fonts><fills count="3"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill><fill><patternFill patternType="solid"><fgColor rgb="FF123B5D"/><bgColor indexed="64"/></patternFill></fill></fills><borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders><cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs><cellXfs count="3"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/><xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyFont="1" applyFill="1"/><xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyFont="1" applyFill="1"/></cellXfs><cellStyles count="1"><cellStyle name="Normal" xfId="0" builtinId="0"/></cellStyles></styleSheet>"""
            ))
        }
    }

    private fun columnName(number: Int): String {
        var n = number
        val result = StringBuilder()
        while (n > 0) { val rem = (n - 1) % 26; result.append(('A'.code + rem).toChar()); n = (n - 1) / 26 }
        return result.reverse().toString()
    }
}
