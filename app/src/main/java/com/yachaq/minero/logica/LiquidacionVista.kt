package com.yachaq.minero.logica

import com.yachaq.minero.data.remote.model.ResponseHistorial
import java.util.Locale

/**
 * Modelo único de lo que se muestra de una liquidación.
 * Lo usan tanto la pantalla de Detalle como el PDF, de modo que
 * lo que se ve en pantalla es exactamente lo que se exporta.
 */
data class FilaLiq(
    val etiqueta: String,
    val valor: String,
    val resaltar: Boolean = false
)

sealed interface BloqueLiq {
    val titulo: String
}

data class BloqueFilas(
    override val titulo: String,
    val filas: List<FilaLiq>
) : BloqueLiq

data class BloqueTabla(
    override val titulo: String,
    val columnas: List<String>,
    val filas: List<List<String>>
) : BloqueLiq

data class VistaLiquidacion(
    val numero: String,
    val fecha: String,
    val resumen: List<FilaLiq>,   // Mineral, Tonelaje, TMNS
    val total: String,            // Total a pagar
    val bloques: List<BloqueLiq>
)

object ConstructorLiquidacion {

    // ---------- Formato ----------

    private fun n(v: Double?, dec: Int = 2): String =
        String.format(Locale.US, "%,.${dec}f", v ?: 0.0)

    private fun usd(v: Double?): String = "$ " + n(v)

    private fun pct(v: Double?): String = n(v) + " %"

    /** Porcentaje guardado como fracción (0.18 -> 18.00 %). */
    private fun frac(v: Double?): String = n((v ?: 0.0) * 100) + " %"

    private fun fecha(s: String?): String =
        s?.let { FormateadorFecha.formatearParaMostrar(it) } ?: "-"

    private fun hay(v: Double?): Boolean = (v ?: 0.0) > 0.0

    // ---------- Construcción ----------

    fun construir(r: ResponseHistorial): VistaLiquidacion {
        val hayCu = hay(r.leyCu)
        val hayAg = hay(r.leyAg)
        val hayAu = hay(r.leyAu)

        val bloques = mutableListOf<BloqueLiq>()

        // 1. Planta
        bloques += BloqueFilas(
            "Planta de procesamiento",
            listOf(
                FilaLiq("Planta", r.plantName ?: "-"),
                FilaLiq("Ubicación", r.plantLocation ?: "-"),
                FilaLiq("Tipo de planta", if (r.plantType == "PERSONAL") "Personal" else "Oficial"),
                FilaLiq(
                    "Términos aplicados",
                    if (r.termsSource == "PERSONALIZADO") "Personalizados por el usuario"
                    else "Generales de la planta"
                ),
                FilaLiq("Alquiler de planta", "${usd(r.plantRentalCostPerTM)} /TM")
            )
        )

        // 2. Datos de ingreso
        val ingreso = mutableListOf(
            FilaLiq("Mineral", r.material ?: "-"),
            FilaLiq("Tonelaje bruto", "${n(r.tonelaje)} TM")
        )
        if (hayCu) ingreso += FilaLiq("Ley de cobre (Cu)", "${n(r.leyCu)} %")
        if (hayAg) ingreso += FilaLiq("Ley de plata (Ag)", "${n(r.leyAg)} oz/TM")
        if (hayAu) ingreso += FilaLiq("Ley de oro (Au)", "${n(r.leyAu)} oz/TM")
        ingreso += FilaLiq("Adelanto recibido", usd(r.adelanto))
        bloques += BloqueFilas("Datos de ingreso", ingreso)

        // 3. Precios de mercado
        val precios = mutableListOf<FilaLiq>()
        if (hayCu) precios += FilaLiq("Cobre (Cu)", "${usd(r.precioCu)} /lb")
        if (hayAg) precios += FilaLiq("Plata (Ag)", "${usd(r.precioAg)} /oz")
        if (hayAu) precios += FilaLiq("Oro (Au)", "${usd(r.precioAu)} /oz")
        precios += FilaLiq("Cotización vigente al", fecha(r.preciosUpdatedAt))
        bloques += BloqueFilas("Precios de mercado utilizados", precios)

        // 4. Términos
        val terminos = mutableListOf(
            FilaLiq("Factor de concentrado", n(r.factorConcentrado)),
            FilaLiq("Humedad", pct(r.humedad)),
            FilaLiq("Merma", n(r.merma)),
            FilaLiq("Maquila", "${usd(r.maquila)} /TM"),
            FilaLiq("Análisis", usd(r.analisis)),
            FilaLiq("Traslado interno", usd(r.trasladoInterno)),
            FilaLiq("IGV", frac(r.igvPorcentaje)),
            FilaLiq("Detracción", frac(r.detraccionPorcentaje))
        )
        if (hayCu) {
            terminos += FilaLiq("Cu · Ley del concentrado", pct(r.leyConcentradoCu))
            terminos += FilaLiq("Cu · Pagable", pct(r.pagableCu))
            terminos += FilaLiq("Cu · Deducción", pct(r.deduccionCu))
            terminos += FilaLiq("Cu · Gastos de refinación", "${usd(r.gastosRefCu)} /lb")
            terminos += FilaLiq("Cu · Protección de precio", "${usd(r.proteccionCu)} /lb")
        }
        if (hayAg) {
            terminos += FilaLiq("Ag · Pagable", pct(r.pagableAg))
            terminos += FilaLiq("Ag · Deducción", "${usd(r.deduccionAg)} /oz")
            terminos += FilaLiq("Ag · Gastos de refinación", "${usd(r.gastosRefAg)} /oz")
        }
        if (hayAu) {
            terminos += FilaLiq("Au · Pagable", pct(r.pagableAu))
            terminos += FilaLiq("Au · Deducción", "${usd(r.deduccionAu)} /oz")
            terminos += FilaLiq("Au · Gastos de refinación", "${usd(r.gastosRefAu)} /oz")
        }
        bloques += BloqueFilas("Términos comerciales aplicados", terminos)

        // 5. Concentrado
        bloques += BloqueFilas(
            "Concentrado obtenido",
            listOf(
                FilaLiq("TM húmedas", "${n(r.tmHumedas)} TM"),
                FilaLiq("TMNS (toneladas métricas netas secas)", "${n(r.tmns)} TM", resaltar = true)
            )
        )

        // 6. Pago por metal (tabla)
        val filasMetal = mutableListOf<List<String>>()
        if (hayCu) {
            filasMetal += listOf(
                "Cobre (Cu)",
                "${n((r.contenidoPagableCu ?: 0.0) * 100)} %",
                "${usd(r.precioAjustadoCu)} /TM",
                "${usd(r.pagoCu)} /TM"
            )
        }
        if (hayAg) {
            filasMetal += listOf(
                "Plata (Ag)",
                "${n(r.contenidoPagableAg, 4)} oz/TM",
                "${usd(r.precioAjustadoAg)} /oz",
                "${usd(r.pagoAg)} /TM"
            )
        }
        if (hayAu) {
            filasMetal += listOf(
                "Oro (Au)",
                "${n(r.contenidoPagableAu, 4)} oz/TM",
                "${usd(r.precioAjustadoAu)} /oz",
                "${usd(r.pagoAu)} /TM"
            )
        }
        bloques += BloqueTabla(
            "Pago por metal",
            listOf("Metal", "Contenido pagable", "Precio ajustado", "Pago por TM"),
            filasMetal
        )

        // 7. Deducciones por TM de concentrado
        bloques += BloqueFilas(
            "Deducciones por TM de concentrado",
            listOf(
                FilaLiq("Pago total por metales", "${usd(r.pagoTotal)} /TM", resaltar = true),
                FilaLiq("(−) Maquila", "${usd(r.maquila)} /TM"),
                FilaLiq("(−) Reducción por refinación", "${usd(r.reduccionRefinacion)} /TM"),
                FilaLiq("(−) Análisis por TMNS", "${usd(r.analisisPorTmns)} /TM"),
                FilaLiq("(−) Traslado por TMNS", "${usd(r.trasladoPorTmns)} /TM"),
                FilaLiq("Pago neto por TM", "${usd(r.pagoPorTm)} /TM", resaltar = true)
            )
        )

        // 8. Liquidación final
        bloques += BloqueFilas(
            "Liquidación final",
            listOf(
                FilaLiq("Valor neto (pago neto por TM × TMNS)", usd(r.valorNeto)),
                FilaLiq("(+) IGV (${frac(r.igvPorcentaje)})", usd(r.igv)),
                FilaLiq("Valor total", usd(r.valorTotal), resaltar = true),
                FilaLiq("(−) Detracción (${frac(r.detraccionPorcentaje)})", usd(r.detraccion)),
                FilaLiq("(−) Alquiler de planta", usd(r.alquilerTotal)),
                FilaLiq("(−) Adelanto", usd(r.adelanto))
            )
        )

        return VistaLiquidacion(
            numero = (r.idCalculation ?: "-").take(8).uppercase(),
            fecha = fecha(r.calculatedAt),
            resumen = listOf(
                FilaLiq("Mineral", r.material ?: "-"),
                FilaLiq("Tonelaje bruto", "${n(r.tonelaje)} TM"),
                FilaLiq("TMNS", "${n(r.tmns)} TM")
            ),
            total = usd(r.totalAPagar),
            bloques = bloques
        )
    }
}