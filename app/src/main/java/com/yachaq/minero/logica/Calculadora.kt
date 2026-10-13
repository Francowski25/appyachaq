package com.yachaq.minero.logica

import com.yachaq.minero.data.*

object Calculadora {

    fun calcular(e: Entrada, t: Terminos = Terminos()): Resultado? {
        // 1. Determinar ley y precios según material
        val leyCu = if (e.material.contains("Cobre")) e.ley else 0.0
        val leyAg = if (e.material.contains("Plata")) e.ley else 0.0
        val leyAu = if (e.material.contains("Oro")) e.ley else 0.0

        val precioCu = if (e.material.contains("Cobre")) e.precio else 0.0
        val precioAg = if (e.material.contains("Plata")) e.precio else 0.0
        val precioAu = if (e.material.contains("Oro")) e.precio else 0.0

        // 2. Concentrado
        val leyActiva = when {
            leyCu > 0 -> leyCu
            leyAu > 0 -> leyAu
            leyAg > 0 -> leyAg
            else -> return null
        }
        val concentrado = Concentrado(tonelaje = e.tonelaje, ley = leyActiva)
        if (!concentrado.esValido()) return null

        // 3. Pagos
        val pagos = Pagos(
            leyConcentradoCu = if (leyCu > 0) t.leyConcentradoCu else 0.0,            leyConcentradoAg = leyAg,
            leyConcentradoAu = leyAu,
            precioCu = precioCu,
            precioAg = precioAg,
            precioAu = precioAu,
            pagableCu = t.pagableCu,
            deduccionCu = t.deduccionCu,
            gastosRefCu = t.gastosRefCu,
            proteccionCu = t.proteccionCu,
            pagableAg = t.pagableAg,
            deduccionAg = t.deduccionAg,
            gastosRefAg = t.gastosRefAg,
            pagableAu = t.pagableAu,
            deduccionAu = t.deduccionAu,
            gastosRefAu = t.gastosRefAu
        )

        // 4. Reducciones
        val reducciones = Reducciones(
            maquila = t.maquila,
            analisis = t.analisis,
            trasladoInterno = t.trasladoInterno,
            contenidoPagableCu = pagos.contenidoPagableCu,
            contenidoPagableAg = pagos.contenidoPagableAg,
            contenidoPagableAu = pagos.contenidoPagableAu,
            gastosRefCu = t.gastosRefCu,
            gastosRefAg = t.gastosRefAg,
            gastosRefAu = t.gastosRefAu,
            tmns = concentrado.toneladasConcentrado
        )

        // 5. Pago por TM
        val pagoPorTm = pagos.pagoPorTm(reducciones)

        // 6. Liquidación
        val liquidacion = Liquidacion(
            pagoPorTm = pagoPorTm,
            cantidadConcentrado = concentrado.toneladasConcentrado,
            alquilerPlanta = e.alquilerPlanta,
            tonelaje = e.tonelaje,
            adelanto = e.adelanto,
            igvPorcentaje = t.igvPorcentaje,
            detraccionPorcentaje = t.detraccionPorcentaje
        )

        // 7. Resultado
        return Resultado(
            tmHumedas = concentrado.tmHumedas,
            tmns = concentrado.toneladasConcentrado,
            pagoCu = pagos.pagoCu,
            pagoAg = pagos.pagoAg,
            pagoAu = pagos.pagoAu,
            totalPago = pagos.totalPago,
            maquila = reducciones.maquila,
            reduccionRefinacion = reducciones.reduccionRefinacion,
            analisis = reducciones.analisisPorTmns,
            trasladoInterno = reducciones.trasladoPorTmns,
            pagoPorTm = pagoPorTm,
            valorNeto = liquidacion.valorNeto,
            igv = liquidacion.igv,
            valorTotal = liquidacion.valorTotal,
            detraccion = liquidacion.detraccion,
            alquilerTotal = liquidacion.alquilerTotal,
            totalAPagar = liquidacion.totalAPagar
        )
    }
}