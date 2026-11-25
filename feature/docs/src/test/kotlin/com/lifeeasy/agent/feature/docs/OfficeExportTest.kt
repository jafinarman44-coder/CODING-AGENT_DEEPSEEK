// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL.
package com.lifeeasy.agent.feature.docs

import com.lifeeasy.agent.feature.docs.model.DocumentRequest
import com.lifeeasy.agent.feature.docs.model.DocumentSection
import com.lifeeasy.agent.feature.docs.model.DocumentTable
import com.lifeeasy.agent.feature.docs.presentation.PptxBuilder
import com.lifeeasy.agent.feature.docs.spreadsheet.XlsxBuilder
import com.lifeeasy.agent.feature.docs.word.DocxBuilder
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory

class OfficeExportTest {
    @get:Rule val temporaryFolder = TemporaryFolder()
    private val temp: File get() = temporaryFolder.root

    private val sample = DocumentRequest(
        title = "LIFEEASY — Meeting & Plan",
        paragraphs = listOf("বাংলা content", "Second paragraph with <XML> & symbols."),
        sections = listOf(DocumentSection("Action items", listOf("Owner: A", "Due: Friday"))),
        table = DocumentTable(listOf("Task", "Owner"), listOf(listOf("Prepare report", "Team")))
    )

    @Test fun docxPackageContainsValidMainPartsAndEscapesText() {
        val output = File(temp, "meeting.docx")
        assertTrue(DocxBuilder().build(sample, output).success)
        ZipFile(output).use { zip ->
            assertNotNull(zip.getEntry("[Content_Types].xml"))
            assertNotNull(zip.getEntry("word/document.xml"))
            assertNotNull(zip.getEntry("word/styles.xml"))
            val xml = zip.getInputStream(zip.getEntry("word/document.xml")).bufferedReader().readText()
            assertTrue(xml.contains("&lt;XML&gt; &amp; symbols."))
            parseXml(xml)
        }
    }

    @Test fun xlsxPackageContainsWorkbookAndWorksheet() {
        val output = File(temp, "report.xlsx")
        assertTrue(XlsxBuilder().build(sample, output).success)
        ZipFile(output).use { zip ->
            assertNotNull(zip.getEntry("xl/workbook.xml"))
            assertNotNull(zip.getEntry("xl/worksheets/sheet1.xml"))
            parseXml(zip.getInputStream(zip.getEntry("xl/worksheets/sheet1.xml")).bufferedReader().readText())
        }
    }

    @Test fun pptxPackageContainsPresentationAndEditableSlides() {
        val output = File(temp, "slides.pptx")
        assertTrue(PptxBuilder().build(sample, output).success)
        ZipFile(output).use { zip ->
            assertNotNull(zip.getEntry("ppt/presentation.xml"))
            assertNotNull(zip.getEntry("ppt/slides/slide1.xml"))
            parseXml(zip.getInputStream(zip.getEntry("ppt/slides/slide1.xml")).bufferedReader().readText())
        }
    }

    @Test fun invalidTableShapeIsRejectedBeforeWriting() {
        val output = File(temp, "bad.docx")
        val invalid = DocumentRequest("Bad", table = DocumentTable(listOf("A", "B"), listOf(listOf("only one"))))
        val result = DocxBuilder().build(invalid, output)
        assertFalse(result.success)
        assertFalse(output.exists())
    }

    private fun parseXml(value: String) {
        val factory = DocumentBuilderFactory.newInstance().apply {
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        }
        factory.newDocumentBuilder().parse(value.byteInputStream())
    }
}
