package com.laurencekl.campushub

data class Evento(
    val id: String = "",
    val titulo: String = "",
    val descricao: String = "",
    val data: String = "",
    val horario: String = "",
    val local: String = ""
) {
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
}
