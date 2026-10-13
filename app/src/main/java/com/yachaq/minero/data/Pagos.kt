package com.yachaq.minero.data

class Pagos(
    // ===== Leyes del concentrado =====
    val leyConcentradoCu: Double,
    val leyConcentradoAg: Double,
    val leyConcentradoAu: Double,

    // ===== Cotizaciones =====
    val precioCu: Double,
    val precioAg: Double,
    val precioAu: Double,

    // ===== Términos: Cobre =====
    val pagableCu: Double,
    val deduccionCu: Double,
    val gastosRefCu: Double,
    val proteccionCu: Double,

    // ===== Términos: Plata =====
    val pagableAg: Double,
    val deduccionAg: Double,
    val gastosRefAg: Double,

    // ===== Términos: Oro =====
    val pagableAu: Double,
    val deduccionAu: Double,
    val gastosRefAu: Double
) {
    private companion object {
        const val LB_KG = 0.45359
        const val OZ_GR = 31.1035
    }

    // ==================== CONTENIDO PAGABLE ====================

    /** D46: (leyConcentradoCu − deduccionCu) / 100 × (pagableCu / 100) */
    val contenidoPagableCu: Double
        get() = ((leyConcentradoCu - deduccionCu) / 100) * (pagableCu / 100)

    /** D47: contenido pagable de Ag */
    val contenidoPagableAg: Double
        get() {
            val agGr = leyConcentradoAg * OZ_GR
            val pagable = maxOf(0.0, agGr - deduccionAg) / OZ_GR
            return pagable * (pagableAg / 100)
        }

    /** D48: contenido pagable de Au */
    val contenidoPagableAu: Double
        get() {
            val auGr = leyConcentradoAu * OZ_GR
            val pagable = maxOf(0.0, auGr - deduccionAu) / OZ_GR
            return pagable * (pagableAu / 100)
        }

    // ==================== PRECIOS AJUSTADOS ====================

    /**
     * F46: 1000 × (precioCu − proteccionCu) / 0.45359
     *
     * ⚠️ Según el Excel, se resta PROTECCION, no gastosRefCu.
     */
    val precioAjustadoCu: Double
        get() = 1000 * maxOf(0.0, precioCu - proteccionCu) / LB_KG

    /** F47: precioAg − gastosRefAg */
    val precioAjustadoAg: Double
        get() = maxOf(0.0, precioAg - gastosRefAg)

    /** F48: precioAu − gastosRefAu */
    val precioAjustadoAu: Double
        get() = maxOf(0.0, precioAu - gastosRefAu)

    // ==================== PAGOS POR METAL ====================

    /** H46: contenidoPagableCu × precioAjustadoCu */
    val pagoCu: Double
        get() = contenidoPagableCu * precioAjustadoCu

    /** H47: contenidoPagableAg × precioAjustadoAg */
    val pagoAg: Double
        get() = contenidoPagableAg * precioAjustadoAg

    /** H48: contenidoPagableAu × precioAjustadoAu */
    val pagoAu: Double
        get() = contenidoPagableAu * precioAjustadoAu

    // ==================== TOTAL ====================

    /** H50: total pago (suma de los 3 metales) */
    val totalPago: Double
        get() = pagoCu + pagoAg + pagoAu

    // ==================== PAGO POR TM DE CONCENTRADO ====================

    /**
     * C64: PAGO POR TM DE CONCENTRADO
     *
     * = totalPago − maquila − reduccionRefinacion − analisisPorTmns − trasladoPorTmns
     */
    fun pagoPorTm(reducciones: Reducciones): Double {
        return totalPago -
                reducciones.maquila -
                reducciones.reduccionRefinacion -
                reducciones.analisisPorTmns -
                reducciones.trasladoPorTmns
    }
}