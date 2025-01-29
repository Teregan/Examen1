import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.view.View
import androidx.compose.ui.graphics.asAndroidBitmap
import com.example.examen1.models.FoodCorrelation
import com.example.examen1.models.FoodEntry
import com.example.examen1.models.StoolEntry
import com.example.examen1.models.SymptomEntry
import com.example.examen1.pages.DayData
import com.example.examen1.viewmodels.FoodEntryViewModel
import com.example.examen1.viewmodels.SymptomEntryViewModel
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
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

class PDFService(
    private val context: Context,
    private val foodEntryViewModel: FoodEntryViewModel,
    private val symptomEntryViewModel: SymptomEntryViewModel
) {

    // Mapeo para los tipos de deposición
    private val stoolTypeMap = mapOf(
        "HARD" to "Dura",
        "NORMAL" to "Normal",
        "SOFT" to "Blanda",
        "LIQUID" to "Líquida"
    )

    // Mapeo para los colores de deposición
    private val stoolColorMap = mapOf(
        "BROWN" to "Café",
        "GREEN" to "Verde",
        "YELLOW" to "Amarillo",
        "BLACK" to "Negro",
        "RED" to "Rojo"
    )

    private fun getAllergenName(allergenId: String): String {
        return foodEntryViewModel.allergens.find { it.id == allergenId }?.name ?: allergenId
    }

    private fun getSymptomName(symptomId: String): String {
        return symptomEntryViewModel.predefinedSymptoms.find { it.id == symptomId }?.name ?: symptomId
    }

    private fun getStoolTypeName(type: String): String {
        return stoolTypeMap[type] ?: type
    }

    private fun getStoolColorName(color: String): String {
        return stoolColorMap[color] ?: color
    }

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
                val allergens = correlation.foodEntry.allergens
                    .map { getAllergenName(it) }
                    .joinToString(", ")
                table.addCell("${dateFormatter.format(correlation.foodEntry.date)}\n$allergens")

                // Síntomas
                val symptoms = correlation.relatedSymptoms.joinToString("\n") { symptom ->
                    val translatedSymptoms = symptom.symptoms
                        .map { getSymptomName(it) }
                        .joinToString(", ")
                    "${dateFormatter.format(symptom.date)}: $translatedSymptoms"
                }
                table.addCell(symptoms)

                // Deposiciones
                val stools = correlation.relatedStoolEntries.joinToString("\n") { stool ->
                    "${dateFormatter.format(stool.date)}: ${getStoolTypeName(stool.stoolType.name)}, ${getStoolColorName(stool.color.name)}"
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

    fun generateMonthlyCalendarPDF(
        currentMonth: Calendar,
        daysData: List<DayData>,
        foodEntries: List<FoodEntry>,
        symptomEntries: List<SymptomEntry>,
        stoolEntries: List<StoolEntry>
    ) {
        try {
            val dateFormatter = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
            val dayFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fileName = "calendario_${dateFormatter.format(currentMonth.time)}.pdf"
            val filePath = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

            // Definir el rango de fechas para el mes actual
            val startOfMonth = Calendar.getInstance().apply {
                time = currentMonth.time
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val endOfMonth = Calendar.getInstance().apply {
                time = currentMonth.time
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time

            // Filtrar entradas para el mes actual
            val monthFoodEntries = foodEntries.filter {
                it.date in startOfMonth..endOfMonth
            }
            val monthSymptomEntries = symptomEntries.filter {
                it.date in startOfMonth..endOfMonth
            }
            val monthStoolEntries = stoolEntries.filter {
                it.date in startOfMonth..endOfMonth
            }

            // Crear PDF
            val writer = PdfWriter(FileOutputStream(filePath))
            val pdf = PdfDocument(writer)
            val document = Document(pdf)

            // Título
            document.add(
                Paragraph("Calendario de Registros - ${dateFormatter.format(currentMonth.time).capitalize()}")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(20f)
                    .setMarginBottom(20f)
            )

            // Crear tabla para el calendario
            val calendarTable = Table(UnitValue.createPercentArray(7)).useAllAvailableWidth()

            // Encabezados de días
            val dias = arrayOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
            dias.forEach { dia ->
                calendarTable.addCell(
                    Cell()
                        .add(Paragraph(dia))
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBold()
                )
            }

            // Calcular el primer día del mes y los días vacíos iniciales
            val firstDayOfMonth = Calendar.getInstance().apply {
                time = currentMonth.time
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val startingDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1

            // Añadir celdas vacías
            repeat(startingDayOfWeek) {
                calendarTable.addCell(Cell().setHeight(50f))
            }

            // Añadir días del mes
            daysData.forEach { dayData ->
                val cell = Cell().setHeight(50f)

                // Añadir número de día
                cell.add(
                    Paragraph(dayData.day.toString())
                        .setTextAlignment(TextAlignment.LEFT)
                        .setFontSize(10f)
                )

                // Indicadores de registros
                val indicadores = mutableListOf<String>()
                if (dayData.hasFoodEntry) indicadores.add("🍽 Alimentos")
                if (dayData.hasSymptomEntry) indicadores.add("❗ Síntomas")
                if (dayData.hasStoolEntry) indicadores.add("🚽 Deposiciones")

                cell.add(
                    Paragraph(indicadores.joinToString("\n"))
                        .setTextAlignment(TextAlignment.LEFT)
                        .setFontSize(8f)
                )

                calendarTable.addCell(cell)
            }

            document.add(calendarTable)

            // Sección de detalles de registros
            document.add(
                Paragraph("Detalle de Registros")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(16f)
                    .setMarginTop(20f)
            )

            // Función auxiliar para agregar registros por día
            fun addDailyRecords(entries: List<Any>, title: String) {
                val dayEntries = when (entries.firstOrNull()) {
                    is FoodEntry -> {
                        (entries as List<FoodEntry>).groupBy { entry ->
                            val cal = Calendar.getInstance()
                            cal.time = entry.date
                            cal.get(Calendar.DAY_OF_MONTH)
                        }
                    }
                    is SymptomEntry -> {
                        (entries as List<SymptomEntry>).groupBy { entry ->
                            val cal = Calendar.getInstance()
                            cal.time = entry.date
                            cal.get(Calendar.DAY_OF_MONTH)
                        }
                    }
                    is StoolEntry -> {
                        (entries as List<StoolEntry>).groupBy { entry ->
                            val cal = Calendar.getInstance()
                            cal.time = entry.date
                            cal.get(Calendar.DAY_OF_MONTH)
                        }
                    }
                    else -> emptyMap()
                }

                dayEntries.forEach { (day, dailyEntries) ->
                    document.add(
                        Paragraph("$title - Día $day")
                            .setFontSize(12f)
                            .setBold()
                    )

                    dailyEntries.forEach { entry ->
                        val detailText = when (entry) {
                            is FoodEntry -> {
                                val allergens = entry.allergens.joinToString(", ")
                                "Hora: ${entry.time} - Alérgenos: $allergens"
                            }
                            is SymptomEntry -> {
                                val symptoms = entry.symptoms.joinToString(", ")
                                "Hora: ${entry.time} - Síntomas: $symptoms"
                            }
                            is StoolEntry -> {
                                "Hora: ${entry.time} - Tipo: ${entry.stoolType}, Color: ${entry.color}"
                            }
                            else -> ""
                        }

                        document.add(
                            Paragraph(detailText)
                                .setFontSize(10f)
                        )
                    }
                }
            }

            // Agregar registros detallados
            addDailyRecords(monthFoodEntries, "Registros de Alimentos")
            addDailyRecords(monthSymptomEntries, "Registros de Síntomas")
            addDailyRecords(monthStoolEntries, "Registros de Deposiciones")

            document.close()

            // Compartir archivo
            shareFile(filePath)

        } catch (e: Exception) {
            e.printStackTrace()
            // Manejar error
        }
    }
}