package com.laurencekl.campushub

object EventoRegras {

    fun temVagas(inscritos: Long, limite: Long): Boolean {
        return inscritos < limite
    }

    fun notaValida(nota: Int): Boolean {
        return nota in 1..5
    }

    fun calcularMedia(notas: List<Long>): Double {
        return if (notas.isEmpty()) 0.0 else notas.average()
    }
}
