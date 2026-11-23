package com.yachaq.minero.logica

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.yachaq.minero.data.remote.model.ResponseHistorial
import java.io.File


object ExportadorPdf {

    private const val ANCHO = 595       // A4 en puntos
    private const val ALTO = 842
    private const val MARGEN = 36f
    private const val ANCHO_UTIL = 523f // ANCHO - 2 * MARGEN
    private const val PIE = 54f

    /**
     * Guarda el PDF en la carpeta Descargas del teléfono y devuelve el nombre del archivo.
     * Ejecutar fuera del hilo principal.
     *  - Android 10+ (API 29): MediaStore, sin permisos.
     *  - Android 7 a 9 (API 24-28): requiere el permiso WRITE_EXTERNAL_STORAGE.
     */
    fun guardarEnDescargas(context: Context, reg: ResponseHistorial): String {
        val nombre = "liquidacion_${(reg.idCalculation ?: "sin_id").take(8)}.pdf"
        val doc = PdfDocument()
        try {
            val w = Escritor(doc)
            dibujar(w, ConstructorLiquidacion.construir(reg))
            w.cerrar()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val valores = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, nombre)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, valores)
                    ?: throw IllegalStateException("No se pudo crear el archivo en Descargas")
                try {
                    resolver.openOutputStream(uri)?.use { doc.writeTo(it) }
                        ?: throw IllegalStateException("No se pudo abrir el archivo")
                    valores.clear()
                    valores.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(uri, valores, null, null)
                } catch (e: Exception) {
                    resolver.delete(uri, null, null)
                    throw e
                }
            } else {
                @Suppress("DEPRECATION")
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                dir.mkdirs()
                val archivo = File(dir, nombre)
                archivo.outputStream().use { doc.writeTo(it) }
                MediaScannerConnection.scanFile(
                    context, arrayOf(archivo.absolutePath), arrayOf("application/pdf"), null
                )
            }
        } finally {
            doc.close()
        }
        return nombre
    }

    // ---------- Contenido ----------

    private fun dibujar(w: Escritor, v: VistaLiquidacion) {
        w.cabecera(v.numero, v.fecha)
        w.resumen(v.resumen, v.total)

        v.bloques.forEach { bloque ->
            when (bloque) {
                is BloqueFilas -> {
                    w.seccion(bloque.titulo)
                    bloque.filas.forEach { w.fila(it) }
                }
                is BloqueTabla -> {
                    w.seccion(bloque.titulo)
                    w.tabla(bloque.columnas, bloque.filas)
                }
            }
        }

        w.totalFinal(v.total)
    }

    // ---------- Escritor de páginas ----------

    private class Escritor(private val doc: PdfDocument) {

        private val NEGRO = Color.rgb(24, 24, 27)
        private val AMARILLO = Color.rgb(250, 204, 21)
        private val TEXTO = Color.rgb(39, 39, 42)
        private val GRIS = Color.rgb(113, 113, 122)
        private val GRIS_CLARO = Color.rgb(212, 212, 216)
        private val ZEBRA = Color.rgb(244, 244, 245)
        private val LINEA = Color.rgb(228, 228, 231)

        private fun paint(
            tam: Float,
            color: Int,
            negrita: Boolean = false,
            alin: Paint.Align = Paint.Align.LEFT
        ) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = tam
            this.color = color
            textAlign = alin
            typeface = if (negrita) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
        }

        private fun relleno(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }

        private val pTexto = paint(9.5f, TEXTO)
        private val pTextoDer = paint(9.5f, TEXTO, alin = Paint.Align.RIGHT)
        private val pNeg = paint(9.5f, TEXTO, negrita = true)
        private val pNegDer = paint(9.5f, TEXTO, negrita = true, alin = Paint.Align.RIGHT)
        private val pNeg11 = paint(11f, TEXTO, negrita = true)
        private val pGris = paint(7.5f, GRIS, negrita = true)
        private val pNegroChico = paint(7.5f, NEGRO, negrita = true)
        private val pPie = paint(7.5f, GRIS)
        private val pPieDer = paint(7.5f, GRIS, alin = Paint.Align.RIGHT)
        private val pSeccion = paint(11f, NEGRO, negrita = true)
        private val pBlancoNeg = paint(9f, Color.WHITE, negrita = true)
        private val pBlancoNegDer = paint(9f, Color.WHITE, negrita = true, alin = Paint.Align.RIGHT)
        private val pMarca = paint(9f, AMARILLO, negrita = true).apply { letterSpacing = 0.25f }
        private val pTitulo = paint(18f, Color.WHITE, negrita = true)
        private val pClaroDer = paint(9f, GRIS_CLARO, alin = Paint.Align.RIGHT)
        private val pTotalEtq = paint(12f, NEGRO, negrita = true)
        private val pTotalVal = paint(18f, NEGRO, negrita = true, alin = Paint.Align.RIGHT)

        private val fillNegro = relleno(NEGRO)
        private val fillAmarillo = relleno(AMARILLO)
        private val fillZebra = relleno(ZEBRA)
        private val pLinea = Paint().apply { color = LINEA; strokeWidth = 0.8f }

        private var pagina: PdfDocument.Page? = null
        private lateinit var c: Canvas
        private var numero = 0
        private var y = 0f
        private var zebra = 0

        init { nuevaPagina() }

        private fun nuevaPagina() {
            cerrarPagina()
            numero++
            val info = PdfDocument.PageInfo.Builder(ANCHO, ALTO, numero).create()
            pagina = doc.startPage(info)
            c = pagina!!.canvas
            y = MARGEN
            zebra = 0
        }

        private fun cerrarPagina() {
            val p = pagina ?: return
            c.drawLine(MARGEN, ALTO - 40f, MARGEN + ANCHO_UTIL, ALTO - 40f, pLinea)
            c.drawText(
                "Yachaq Minero · Valores calculados con los precios y términos vigentes al momento de la liquidación.",
                MARGEN, ALTO - 26f, pPie
            )
            c.drawText("Página $numero", MARGEN + ANCHO_UTIL, ALTO - 26f, pPieDer)
            doc.finishPage(p)
            pagina = null
        }

        private fun asegurar(alto: Float) {
            if (y + alto > ALTO - PIE) nuevaPagina()
        }

        private fun ajustar(t: String, p: Paint, max: Float): String {
            if (p.measureText(t) <= max) return t
            var s = t
            while (s.isNotEmpty() && p.measureText("$s…") > max) s = s.dropLast(1)
            return "$s…"
        }

        fun cerrar() = cerrarPagina()

        fun cabecera(numeroLiq: String, fecha: String) {
            c.drawRect(0f, 0f, ANCHO.toFloat(), 78f, fillNegro)
            c.drawRect(0f, 78f, ANCHO.toFloat(), 82f, fillAmarillo)
            c.drawText("YACHAQ MINERO", MARGEN, 30f, pMarca)
            c.drawText("LIQUIDACIÓN DE CONCENTRADO", MARGEN, 56f, pTitulo)
            c.drawText("N° $numeroLiq", ANCHO - MARGEN, 30f, pBlancoNegDer)
            c.drawText(fecha, ANCHO - MARGEN, 52f, pClaroDer)
            y = 100f
        }

        /** Cuatro recuadros: Mineral, Tonelaje, TMNS y Total a pagar (destacado). */
        fun resumen(items: List<FilaLiq>, total: String) {
            val gap = 8f
            val cols = 4
            val w = (ANCHO_UTIL - gap * (cols - 1)) / cols
            val h = 46f
            val todos = items.take(3) + FilaLiq("Total a pagar", total, resaltar = true)

            todos.forEachIndexed { i, f ->
                val x = MARGEN + i * (w + gap)
                c.drawRoundRect(RectF(x, y, x + w, y + h), 6f, 6f, if (f.resaltar) fillAmarillo else fillZebra)
                c.drawText(f.etiqueta.uppercase(), x + 8f, y + 16f, if (f.resaltar) pNegroChico else pGris)
                c.drawText(ajustar(f.valor, pNeg11, w - 16f), x + 8f, y + 34f, pNeg11)
            }
            y += h + 14f
        }

        fun seccion(titulo: String) {
            asegurar(70f)
            y += 10f
            c.drawRect(MARGEN, y, MARGEN + 4f, y + 14f, fillAmarillo)
            c.drawText(titulo, MARGEN + 12f, y + 11f, pSeccion)
            y += 18f
            c.drawLine(MARGEN, y, MARGEN + ANCHO_UTIL, y, pLinea)
            y += 6f
            zebra = 0
        }

        fun fila(f: FilaLiq) {
            asegurar(17f)
            if (zebra++ % 2 == 0) c.drawRect(MARGEN, y, MARGEN + ANCHO_UTIL, y + 17f, fillZebra)
            val pe = if (f.resaltar) pNeg else pTexto
            val pv = if (f.resaltar) pNegDer else pTextoDer
            c.drawText(ajustar(f.etiqueta, pe, ANCHO_UTIL * 0.60f), MARGEN + 8f, y + 12f, pe)
            c.drawText(ajustar(f.valor, pv, ANCHO_UTIL * 0.36f), MARGEN + ANCHO_UTIL - 8f, y + 12f, pv)
            y += 17f
        }

        fun tabla(columnas: List<String>, filas: List<List<String>>) {
            val anchos = columnas.indices.map {
                if (it == 0) 0.22f * ANCHO_UTIL else 0.78f * ANCHO_UTIL / (columnas.size - 1)
            }

            asegurar(40f)
            c.drawRect(MARGEN, y, MARGEN + ANCHO_UTIL, y + 18f, fillNegro)
            var x = MARGEN
            columnas.forEachIndexed { i, col ->
                if (i == 0) c.drawText(col, x + 8f, y + 12f, pBlancoNeg)
                else c.drawText(col, x + anchos[i] - 8f, y + 12f, pBlancoNegDer)
                x += anchos[i]
            }
            y += 18f
            zebra = 0

            filas.forEach { fila ->
                asegurar(17f)
                if (zebra++ % 2 == 0) c.drawRect(MARGEN, y, MARGEN + ANCHO_UTIL, y + 17f, fillZebra)
                var xx = MARGEN
                fila.forEachIndexed { i, celda ->
                    if (i == 0) {
                        c.drawText(ajustar(celda, pNeg, anchos[i] - 12f), xx + 8f, y + 12f, pNeg)
                    } else {
                        c.drawText(ajustar(celda, pTextoDer, anchos[i] - 12f), xx + anchos[i] - 8f, y + 12f, pTextoDer)
                    }
                    xx += anchos[i]
                }
                y += 17f
            }
        }

        fun totalFinal(valor: String) {
            asegurar(60f)
            y += 16f
            c.drawRoundRect(RectF(MARGEN, y, MARGEN + ANCHO_UTIL, y + 40f), 8f, 8f, fillAmarillo)
            c.drawText("TOTAL A PAGAR", MARGEN + 14f, y + 25f, pTotalEtq)
            c.drawText(valor, MARGEN + ANCHO_UTIL - 14f, y + 27f, pTotalVal)
            y += 46f
        }
    }
}