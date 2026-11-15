package com.laurencekl.campushub

data class CheckInQrDados(
    val eventoId: String,
    val alunoId: String
)

object CheckInQr {
    private const val PREFIXO = "campushub-checkin"

    fun criar(eventoId: String, alunoId: String): String {
        return "$PREFIXO|$eventoId|$alunoId"
    }

    fun ler(conteudo: String): CheckInQrDados? {
        val partes = conteudo.split('|')
        if (partes.size != 3 || partes[0] != PREFIXO) return null

        val eventoId = partes[1].trim()
        val alunoId = partes[2].trim()
        if (eventoId.isBlank() || alunoId.isBlank()) return null

        return CheckInQrDados(eventoId, alunoId)
    }
}
