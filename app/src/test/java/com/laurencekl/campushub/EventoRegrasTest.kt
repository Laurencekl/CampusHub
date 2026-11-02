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
}
