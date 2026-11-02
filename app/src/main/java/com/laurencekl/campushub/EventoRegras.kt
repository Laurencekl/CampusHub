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

    fun filtrar(
        eventos: List<Evento>,
        pesquisa: String,
        categoria: String?,
        encerrado: Boolean?
    ): List<Evento> {
        val textoPesquisado = pesquisa.trim()

        return eventos.filter { evento ->
            val correspondeAoTitulo = textoPesquisado.isEmpty() ||
                evento.titulo.contains(textoPesquisado, ignoreCase = true)
            val correspondeACategoria = categoria == null ||
                evento.categoria.equals(categoria, ignoreCase = true)
            val correspondeASituacao = encerrado == null ||
                evento.estaEncerrado() == encerrado

            correspondeAoTitulo && correspondeACategoria && correspondeASituacao
        }
    }
}
