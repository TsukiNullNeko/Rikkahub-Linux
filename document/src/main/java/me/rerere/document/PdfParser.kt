package me.rerere.document

import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File

/**
 * Desktop PDF text extraction backed by Apache PDFBox (replaces the Android-only MuPDF bindings).
 */
object PdfParser {
    fun parserPdf(file: File): String {
        val document = Loader.loadPDF(file)
        try {
            val stripper = PDFTextStripper()
            val result = StringBuilder()
            for (i in 1..document.numberOfPages) {
                stripper.startPage = i
                stripper.endPage = i
                result.append("---")
                result.append("Page ").append(i).append(":\n")
                result.append(stripper.getText(document))
                result.appendLine()
            }
            return result.toString()
        } finally {
            document.close()
        }
    }
}
