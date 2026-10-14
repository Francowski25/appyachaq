package com.yachaq.minero.logica

import com.yachaq.minero.data.*

object Calculadora {

    fun calcular(e: Entrada, t: Terminos = Terminos()): Resultado? {
        val terminos = Terminos(
            factorConcentrado = t.factorConcentrado,
            humedad = t.humedad,
            merma = t.merma,
            leyConcentradoCu = 25.23,
            leyConcentradoAg = 0.0,
            leyConcentradoAu = 0.0,
            pagableCu = t.pagableCu,
            deduccionCu = 1.35,
            gastosRefCu = 0.19,
            proteccionCu = t.proteccionCu,
            pagableAg = t.pagableAg,
            deduccionAg = t.deduccionAg,
            gastosRefAg = t.gastosRefAg,
            pagableAu = t.pagableAu,
            deduccionAu = t.deduccionAu,
            gastosRefAu = t.gastosRefAu,
            maquila = t.maquila,
            analisis = t.analisis,
            trasladoInterno = t.trasladoInterno,
            igvPorcentaje = t.igvPorcentaje,
            detraccionPorcentaje = t.detraccionPorcentaje
        )

        val esCobre = e.material.contains("Cobre")
        val esPlata = e.material.contains("Plata")
        val esOro = e.material.contains("Oro")

        val leyCu = if (esCobre) e.ley else 0.0
        val leyAg = if (esPlata) e.ley else 0.0
        val leyAu = if (esOro) e.ley else 0.0

        val precioCu = if (esCobre) e.precio else 0.0
        val precioAg = if (esPlata) e.precio else 0.0
        val precioAu = if (esOro) e.precio else 0.0

        val leyActiva = when {
            esCobre -> leyCu
            esOro -> leyAu
            esPlata -> leyAg
            else -> return null
        }

        val concentrado = Concentrado(
            tonelaje = e.tonelaje,
            ley = leyActiva,
            t = terminos
        )
        if (!concentrado.esValido()) return null

        val pagos = Pagos(
            material = e.material,
            leyConcentradoCu = if (esCobre) terminos.leyConcentradoCu else 0.0,
            leyConcentradoAg = if (esPlata) e.ley else 0.0,
            leyConcentradoAu = if (esOro) e.ley else 0.0,
            precioCu = precioCu,
            precioAg = precioAg,
            precioAu = precioAu,
            t = terminos
        )

        val reducciones = Reducciones(
            contenidoPagableCu = pagos.contenidoPagableCu,
            contenidoPagableAg = pagos.contenidoPagableAg,
            contenidoPagableAu = pagos.contenidoPagableAu,
            tmns = concentrado.toneladasConcentrado,
            t = terminos
        )

        val pagoPorTm = pagos.pagoPorTm(reducciones)

        val liquidacion = Liquidacion(
            pagoPorTm = pagoPorTm,
            cantidadConcentrado = concentrado.toneladasConcentrado,
            alquilerPlanta = e.alquilerPlanta,
            tonelaje = e.tonelaje,
            adelanto = e.adelanto,
            t = terminos
        )

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