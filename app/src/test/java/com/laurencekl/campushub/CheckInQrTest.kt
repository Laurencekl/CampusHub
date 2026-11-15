package com.laurencekl.campushub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CheckInQrTest {

    @Test
    fun criaELeQrValido() {
        val conteudo = CheckInQr.criar("evento-1", "aluno-1")

        assertEquals(CheckInQrDados("evento-1", "aluno-1"), CheckInQr.ler(conteudo))
    }

    @Test
    fun rejeitaQrDeOutroSistema() {
        assertNull(CheckInQr.ler("outro-app|evento-1|aluno-1"))
    }

    @Test
    fun rejeitaQrIncompleto() {
        assertNull(CheckInQr.ler("campushub-checkin|evento-1"))
    }

    @Test
    fun rejeitaIdentificadoresVazios() {
        assertNull(CheckInQr.ler("campushub-checkin||aluno-1"))
    }
}
