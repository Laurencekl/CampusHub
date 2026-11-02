package com.laurencekl.campushub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EventoRegrasTest {

    @Test
    fun eventoComVagasRetornaVerdadeiro() {
        assertTrue(EventoRegras.temVagas(5, 10))
    }

    @Test
    fun eventoLotadoRetornaFalso() {
        assertFalse(EventoRegras.temVagas(10, 10))
    }

    @Test
    fun eventoAcimaDoLimiteRetornaFalso() {
        assertFalse(EventoRegras.temVagas(12, 10))
    }

    @Test
    fun notaUmEhValida() {
        assertTrue(EventoRegras.notaValida(1))
    }

    @Test
    fun notaCincoEhValida() {
        assertTrue(EventoRegras.notaValida(5))
    }

    @Test
    fun notaZeroEhInvalida() {
        assertFalse(EventoRegras.notaValida(0))
    }

    @Test
    fun notaSeisEhInvalida() {
        assertFalse(EventoRegras.notaValida(6))
    }

    @Test
    fun calculaMediaDasNotas() {
        assertEquals(4.0, EventoRegras.calcularMedia(listOf(3, 4, 5)), 0.0)
    }

    @Test
    fun listaVaziaTemMediaZero() {
        assertEquals(0.0, EventoRegras.calcularMedia(emptyList()), 0.0)
    }

    @Test
    fun filtraEventoPeloTitulo() {
        val eventos = listOf(
            Evento(titulo = "Feira de Tecnologia"),
            Evento(titulo = "Festival de Música")
        )

        val resultado = EventoRegras.filtrar(eventos, "tecnologia", null, null)

        assertEquals(1, resultado.size)
        assertEquals("Feira de Tecnologia", resultado.first().titulo)
    }

    @Test
    fun filtraEventoPelaCategoria() {
        val eventos = listOf(
            Evento(titulo = "Palestra", categoria = "Tecnologia"),
            Evento(titulo = "Show", categoria = "Música")
        )

        val resultado = EventoRegras.filtrar(eventos, "", "Música", null)

        assertEquals(1, resultado.size)
        assertEquals("Show", resultado.first().titulo)
    }

    @Test
    fun filtraSomenteEventosProximos() {
        val eventos = listOf(
            Evento(titulo = "Evento antigo", data = "01/01/2000", horario = "10:00"),
            Evento(titulo = "Evento futuro", data = "31/12/2099", horario = "10:00")
        )

        val resultado = EventoRegras.filtrar(eventos, "", null, false)

        assertEquals(1, resultado.size)
        assertEquals("Evento futuro", resultado.first().titulo)
    }

    @Test
    fun calculaVagasDisponiveisSemResultadoNegativo() {
        assertEquals(0, Evento(limiteVagas = 10, inscritos = 12).vagasDisponiveis())
    }

    @Test
    fun converteDataParaOrdenacao() {
        assertEquals(20261102, Evento(data = "02/11/2026").dataParaOrdenacao())
    }

    @Test
    fun dataInvalidaVaiParaFinalDaOrdenacao() {
        assertEquals(Int.MAX_VALUE, Evento(data = "data inválida").dataParaOrdenacao())
    }

    @Test
    fun identificaEventoEncerrado() {
        assertTrue(Evento(data = "01/01/2000", horario = "10:00").estaEncerrado())
    }

    @Test
    fun identificaEventoProximo() {
        assertFalse(Evento(data = "31/12/2099", horario = "10:00").estaEncerrado())
    }
}
