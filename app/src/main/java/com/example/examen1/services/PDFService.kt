import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.view.View
import androidx.compose.ui.graphics.asAndroidBitmap
import com.example.examen1.models.FoodCorrelation
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PDFService(private val context: Context) {

    fun generateAndShareCorrelationReport(
        correlations: List<FoodCorrelation>,
        profileName: String? = null
    ) {
        try {
            val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val fileName = "correlacion_${System.currentTimeMillis()}.pdf"
            val filePath = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

            // Crear PDF
            val writer = PdfWriter(FileOutputStream(filePath))
            val pdf = PdfDocument(writer)
            val document = Document(pdf)

            // Título
            document.add(
                Paragraph("Informe de Correlaciones")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(20f)
            )

            // Fecha del informe
            document.add(
                Paragraph("Fecha del informe: ${dateFormatter.format(Date())}")
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(12f)
            )

            if (profileName != null) {
                document.add(
                    Paragraph("Perfil: $profileName")
                        .setTextAlignment(TextAlignment.LEFT)
                        .setFontSize(14f)
                )
            }

            // Agregar gráficos
            /*if (allergenChartBitmap != null || symptomChartBitmap != null) {
                document.add(
                    Paragraph("Resumen Gráfico")
                        .setTextAlignment(TextAlignment.LEFT)
                        .setFontSize(16f)
                )

                val chartsTable = Table(UnitValue.createPercentArray(2)).useAllAvailableWidth()

                // Agregar el gráfico de alimentos
                val allergenStream = ByteArrayOutputStream()
                allergenChartBitmap?.compress(Bitmap.CompressFormat.PNG, 100, allergenStream)
                val allergenImage = Image(ImageDataFactory.create(allergenStream.toByteArray()))
                    .setAutoScale(true)
                chartsTable.addCell(allergenImage)

                // Agregar el gráfico de síntomas
                val symptomStream = ByteArrayOutputStream()
                symptomChartBitmap?.compress(Bitmap.CompressFormat.PNG, 100, symptomStream)
                val symptomImage = Image(ImageDataFactory.create(symptomStream.toByteArray()))
                    .setAutoScale(true)
                chartsTable.addCell(symptomImage)

                document.add(chartsTable)
            }*/

            // Detalles de Correlaciones
            document.add(
                Paragraph("Detalles de Correlaciones")
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(16f)
                    .setMarginTop(20f)
            )

            // Tabla de correlaciones
            val table = Table(UnitValue.createPercentArray(3)).useAllAvailableWidth()

            // Encabezados
            table.addCell("Alimentos")
            table.addCell("Síntomas Relacionados")
            table.addCell("Deposiciones Relacionadas")

            // Datos
            correlations.forEach { correlation ->
                // Alimentos
                val allergens = correlation.foodEntry.allergens.joinToString(", ")
                table.addCell("${dateFormatter.format(correlation.foodEntry.date)}\n$allergens")

                // Síntomas
                val symptoms = correlation.relatedSymptoms.joinToString("\n") { symptom ->
                    "${dateFormatter.format(symptom.date)}: ${symptom.symptoms.joinToString(", ")}"
                }
                table.addCell(symptoms)

                // Deposiciones
                val stools = correlation.relatedStoolEntries.joinToString("\n") { stool ->
                    "${dateFormatter.format(stool.date)}: ${stool.stoolType}, ${stool.color}"
                }
                table.addCell(stools)
            }

            document.add(table)
            document.close()

            // Compartir el archivo
            shareFile(filePath)

        } catch (e: Exception) {
            e.printStackTrace()
            // Manejar el error
        }
    }

    private fun shareFile(file: File) {
        val uri = getFileUri(file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_SUBJECT, "Informe de Correlaciones")
            putExtra(Intent.EXTRA_TEXT, "Adjunto el informe de correlaciones de alergias.")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir PDF"))
    }

    private fun getFileUri(file: File): Uri {
        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }
}