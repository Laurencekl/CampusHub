package com.laurencekl.campushub

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Evento(
    val id: String = "",
    val titulo: String = "",
    val descricao: String = "",
    val data: String = "",
    val horario: String = "",
    val local: String = "",
    val categoria: String = "Tecnologia",
    val limiteVagas: Long = 30,
    val inscritos: Long = 0
) {
    fun vagasDisponiveis(): Long {
        return (limiteVagas - inscritos).coerceAtLeast(0)
    }

    fun dataParaOrdenacao(): Int {
        val partes = data.split("/")

        if (partes.size != 3) {
            return Int.MAX_VALUE
        }

        val dia = partes[0].toIntOrNull() ?: return Int.MAX_VALUE
        val mes = partes[1].toIntOrNull() ?: return Int.MAX_VALUE
        val ano = partes[2].toIntOrNull() ?: return Int.MAX_VALUE

        return ano * 10000 + mes * 100 + dia
    }

    fun estaEncerrado(): Boolean {
        val formato = SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale.forLanguageTag("pt-BR")
        )
        formato.isLenient = false

        val inicioDoEvento = formato.parse("$data $horario") ?: return false
        return inicioDoEvento.before(Date())
    }
}
