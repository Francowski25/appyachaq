package com.yachaq.minero.data

class Pagos(
    val material: String,
    val leyConcentradoCu: Double,
    val leyConcentradoAg: Double,
    val leyConcentradoAu: Double,
    val precioCu: Double,
    val precioAg: Double,
    val precioAu: Double,
    val t: Terminos
) {
    private companion object {
        const val LB_KG = 0.45359
        const val OZ_GR = 31.1035
    }

    val contenidoPagableCu: Double
        get() = ((leyConcentradoCu - t.deduccionCu) / 100) * (t.pagableCu / 100)

    val contenidoPagableAg: Double
        get() {
            val agGr = leyConcentradoAg * OZ_GR
            val pagable = maxOf(0.0, agGr - t.deduccionAg) / OZ_GR
            return pagable * (t.pagableAg / 100)
        }

    val contenidoPagableAu: Double
        get() {
            val auGr = leyConcentradoAu * OZ_GR
            val pagable = maxOf(0.0, auGr - t.deduccionAu) / OZ_GR
            return pagable * (t.pagableAu / 100)
        }

    /*EN REALIDAD DEBE SER gastosRefCu y no proteccionCu*/
    val precioAjustadoCu: Double
        get() = 1000 * maxOf(0.0, precioCu - t.proteccionCu) / LB_KG

    val precioAjustadoAg: Double
        get() = maxOf(0.0, precioAg - t.gastosRefAg)

    val precioAjustadoAu: Double
        get() = maxOf(0.0, precioAu - t.gastosRefAu)

    val pagoCu: Double get() = contenidoPagableCu * precioAjustadoCu
    val pagoAg: Double get() = contenidoPagableAg * precioAjustadoAg
    val pagoAu: Double get() = contenidoPagableAu * precioAjustadoAu

    val totalPago: Double get() = pagoCu + pagoAg + pagoAu

    fun pagoPorTm(reducciones: Reducciones): Double {
        return totalPago -
                reducciones.maquila -
                reducciones.reduccionRefinacion -
                reducciones.analisisPorTmns -
                reducciones.trasladoPorTmns
    }
}