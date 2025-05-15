package com.example.examen1.services

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.examen1.models.*
import com.example.examen1.viewmodels.FoodEntryViewModel
import com.example.examen1.viewmodels.SymptomEntryViewModel
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PDFGenerator(
    private val context: Context,
    private val foodEntryViewModel: FoodEntryViewModel,
    private val symptomEntryViewModel: SymptomEntryViewModel
) {
    fun generateHistoryPDF(
        foodEntries: List<FoodEntry>,
        symptomEntries: List<SymptomEntry>,
        stoolEntries: List<StoolEntry>,
        controlEntries: List<AllergenControl>
    ): File {
        val fileName = "historial_${System.currentTimeMillis()}.pdf"
        val filePath = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        // Crear PDF
        val writer = PdfWriter(filePath)
        val pdf = PdfDocument(writer)
        val document = Document(pdf)

        // Título
        document.add(
            Paragraph("Historial de Registros")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20f)
                .setBold()
        )

        // Fecha del informe
        document.add(
            Paragraph("Fecha del informe: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(
                Date()
            )}")
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(12f)
        )

        // Sección de Alimentos
        if (foodEntries.isNotEmpty()) {
            addFoodEntriesSection(document, foodEntries)
        }

        // Sección de Síntomas
        if (symptomEntries.isNotEmpty()) {
            addSymptomEntriesSection(document, symptomEntries)
        }

        // Sección de Deposiciones
        if (stoolEntries.isNotEmpty()) {
            addStoolEntriesSection(document, stoolEntries)
        }

        // Sección de Control de Alérgenos
        if (controlEntries.isNotEmpty()) {
            addControlEntriesSection(document, controlEntries)
        }

        document.close()
        return filePath
    }

    private fun addFoodEntriesSection(document: Document, entries: List<FoodEntry>) {
        document.add(
            Paragraph("Registros de Alimentación")
                .setFontSize(16f)
                .setBold()
        )

        val table = Table(UnitValue.createPercentArray(4)).useAllAvailableWidth()
        table.addHeaderCell("Fecha")
        table.addHeaderCell("Hora")
        table.addHeaderCell("Alérgenos")
        table.addHeaderCell("Notas")

        entries.forEach { entry ->
            table.addCell(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(entry.date))
            table.addCell(entry.time)
            val allergenNames = entry.allergens.mapNotNull { allergenId ->
                foodEntryViewModel.allergens.find { it.id == allergenId }?.name
            }.joinToString(", ")
            table.addCell(allergenNames)
            table.addCell(entry.notes)
        }

        document.add(table)
        document.add(Paragraph("\n"))
    }

    private fun addSymptomEntriesSection(document: Document, entries: List<SymptomEntry>) {
        document.add(
            Paragraph("Registros de Síntomas")
                .setFontSize(16f)
                .setBold()
        )

        val table = Table(UnitValue.createPercentArray(4)).useAllAvailableWidth()
        table.addHeaderCell("Fecha")
        table.addHeaderCell("Hora")
        table.addHeaderCell("Síntomas")
        table.addHeaderCell("Notas")

        entries.forEach { entry ->
            table.addCell(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(entry.date))
            table.addCell(entry.time)
            // Traducir los síntomas
            val symptomNames = entry.symptoms.mapNotNull { symptomId ->
                symptomEntryViewModel.predefinedSymptoms.find { it.id == symptomId }?.name
            }.joinToString(", ")
            val allSymptoms = if (entry.customSymptoms.isNotEmpty()) {
                "$symptomNames, ${entry.customSymptoms.joinToString(", ")}"
            } else {
                symptomNames
            }
            table.addCell(allSymptoms)
            table.addCell(entry.notes)
        }

        document.add(table)
        document.add(Paragraph("\n"))
    }

    private fun addStoolEntriesSection(document: Document, entries: List<StoolEntry>) {
        document.add(
            Paragraph("Registros de Deposiciones")
                .setFontSize(16f)
                .setBold()
        )

        val table = Table(UnitValue.createPercentArray(5)).useAllAvailableWidth()
        table.addHeaderCell("Fecha")
        table.addHeaderCell("Hora")
        table.addHeaderCell("Tipo")
        table.addHeaderCell("Color")
        table.addHeaderCell("Notas")

        entries.forEach { entry ->
            table.addCell(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(entry.date))
            table.addCell(entry.time)
            table.addCell(entry.stoolType.displayName)
            table.addCell(entry.color.displayName)
            table.addCell(entry.notes)
        }

        document.add(table)
        document.add(Paragraph("\n"))
    }

    private fun addControlEntriesSection(document: Document, entries: List<AllergenControl>) {
        document.add(
            Paragraph("Control de Alérgenos")
                .setFontSize(16f)
                .setBold()
        )

        val table = Table(UnitValue.createPercentArray(4)).useAllAvailableWidth()
        table.addHeaderCell("Tipo")
        table.addHeaderCell("Fecha Inicio")
        table.addHeaderCell("Fecha Fin")
        table.addHeaderCell("Notas")

        entries.forEach { entry ->
            table.addCell(entry.controlType.displayName)
            table.addCell(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(entry.startDateAsDate))
            table.addCell(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(entry.endDateAsDate))
            table.addCell(entry.notes)
        }

        document.add(table)
    }

    fun generateDetailedPDF(
        title: String,
        content: (Document) -> Unit
    ): File {
        val fileName = "${title.toLowerCase().replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
        val filePath = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        // Crear PDF
        val writer = PdfWriter(filePath)
        val pdf = PdfDocument(writer)
        val document = Document(pdf)

        // Título
        document.add(
            Paragraph(title)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20f)
                .setBold()
        )

        // Fecha del informe
        document.add(
            Paragraph("Fecha del informe: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(
                Date()
            )}")
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(12f)
        )

        // Contenido específico
        content(document)

        document.close()
        return filePath
    }

    // Método específico para generar informe detallado de síntomas
    fun generateSymptomDetailReport(symptomEntry: SymptomEntry): File {
        return generateDetailedPDF("Informe de Síntomas") { document ->
            // Detalles del informe
            document.add(
                Paragraph("Fecha: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(symptomEntry.date)}")
                    .setFontSize(14f)
            )
            document.add(
                Paragraph("Hora: ${symptomEntry.time}")
                    .setFontSize(14f)
            )

            // Síntomas
            document.add(
                Paragraph("Síntomas registrados:")
                    .setFontSize(16f)
                    .setBold()
                    .setMarginTop(16f)
            )

            // Lista de síntomas
            val symptomNames = symptomEntry.symptoms.mapNotNull { symptomId ->
                symptomEntryViewModel.predefinedSymptoms.find { it.id == symptomId }?.name
            }

            if (symptomNames.isNotEmpty()) {
                document.add(
                    Paragraph(symptomNames.joinToString(", "))
                        .setFontSize(14f)
                )
            }

            // Síntomas personalizados
            if (symptomEntry.customSymptoms.isNotEmpty()) {
                document.add(
                    Paragraph("Síntomas personalizados:")
                        .setFontSize(16f)
                        .setBold()
                        .setMarginTop(16f)
                )
                document.add(
                    Paragraph(symptomEntry.customSymptoms.joinToString(", "))
                        .setFontSize(14f)
                )
            }

            // Notas
            if (symptomEntry.notes.isNotEmpty()) {
                document.add(
                    Paragraph("Notas:")
                        .setFontSize(16f)
                        .setBold()
                        .setMarginTop(16f)
                )
                document.add(
                    Paragraph(symptomEntry.notes)
                        .setFontSize(14f)
                )
            }

            // Imágenes
            if (symptomEntry.imagesPaths.isNotEmpty()) {
                document.add(
                    Paragraph("Imágenes:")
                        .setFontSize(16f)
                        .setBold()
                        .setMarginTop(16f)
                )

                // Crear una tabla para las imágenes (2 columnas)
                val imageTable = Table(UnitValue.createPercentArray(2)).useAllAvailableWidth()

                symptomEntry.imagesPaths.forEach { imagePath ->
                    try {
                        val file = File(imagePath)
                        if (file.exists()) {
                            // Redimensionar la imagen para el PDF
                            val bitmap = BitmapFactory.decodeFile(imagePath)
                            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true)

                            // Convertir a bytes para iText
                            val stream = java.io.ByteArrayOutputStream()
                            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                            val byteArray = stream.toByteArray()

                            // Crear imagen y añadirla a la tabla
                            val image = Image(ImageDataFactory.create(byteArray))
                                .setWidth(UnitValue.createPercentValue(90f))
                                .setAutoScale(true)

                            imageTable.addCell(image)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                document.add(imageTable)
            }
        }
    }

    // Método para generar informe detallado de deposiciones
    fun generateStoolDetailReport(stoolEntry: StoolEntry): File {
        return generateDetailedPDF("Informe de Deposición") { document ->
            // Detalles básicos
            document.add(
                Paragraph("Fecha: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(stoolEntry.date)}")
                    .setFontSize(14f)
            )
            document.add(
                Paragraph("Hora: ${stoolEntry.time}")
                    .setFontSize(14f)
            )

            // Tipo y color
            document.add(
                Paragraph("Características:")
                    .setFontSize(16f)
                    .setBold()
                    .setMarginTop(16f)
            )

            val table = Table(UnitValue.createPercentArray(2)).useAllAvailableWidth()
            table.addCell("Tipo")
            table.addCell(stoolEntry.stoolType.displayName)
            table.addCell("Color")
            table.addCell(stoolEntry.color.displayName)
            document.add(table)

            // Notas
            if (stoolEntry.notes.isNotEmpty()) {
                document.add(
                    Paragraph("Notas:")
                        .setFontSize(16f)
                        .setBold()
                        .setMarginTop(16f)
                )
                document.add(
                    Paragraph(stoolEntry.notes)
                        .setFontSize(14f)
                )
            }

            // Imágenes
            if (stoolEntry.imagesPaths.isNotEmpty()) {
                document.add(
                    Paragraph("Imágenes:")
                        .setFontSize(16f)
                        .setBold()
                        .setMarginTop(16f)
                )

                // Tabla para imágenes
                val imageTable = Table(UnitValue.createPercentArray(2)).useAllAvailableWidth()

                stoolEntry.imagesPaths.forEach { imagePath ->
                    try {
                        val file = File(imagePath)
                        if (file.exists()) {
                            // Redimensionar la imagen para el PDF
                            val bitmap = BitmapFactory.decodeFile(imagePath)
                            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true)

                            // Convertir a bytes para iText
                            val stream = java.io.ByteArrayOutputStream()
                            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                            val byteArray = stream.toByteArray()

                            // Crear imagen y añadirla a la tabla
                            val image = Image(ImageDataFactory.create(byteArray))
                                .setWidth(UnitValue.createPercentValue(90f))
                                .setAutoScale(true)

                            imageTable.addCell(image)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                document.add(imageTable)
            }
        }
    }
}

fun sharePDF(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_SUBJECT, "Historial de Alergias")
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Compartir PDF"))
}