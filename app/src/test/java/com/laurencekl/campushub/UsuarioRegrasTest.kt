package com.laurencekl.campushub

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UsuarioRegrasTest {

    @Test
    fun nomePreenchidoEhValido() {
        assertTrue(UsuarioRegras.nomeValido("Laurence Miguel"))
    }

    @Test
    fun nomeVazioEhInvalido() {
        assertFalse(UsuarioRegras.nomeValido("   "))
    }

    @Test
    fun emailCompletoEhValido() {
        assertTrue(UsuarioRegras.emailValido("aluno@email.com"))
    }

    @Test
    fun emailSemArrobaEhInvalido() {
        assertFalse(UsuarioRegras.emailValido("alunoemail.com"))
    }

    @Test
    fun senhaDeCadastroComSeisCaracteresEhValida() {
        assertTrue(UsuarioRegras.senhaCadastroValida("123456"))
    }

    @Test
    fun senhaCurtaDeCadastroEhInvalida() {
        assertFalse(UsuarioRegras.senhaCadastroValida("12345"))
    }

    @Test
    fun senhaVaziaNaoPodeFazerLogin() {
        assertFalse(UsuarioRegras.senhaLoginValida(""))
    }

    @Test
    fun confirmacaoDeSenhaDeveSerIgual() {
        assertTrue(UsuarioRegras.senhasIguais("123456", "123456"))
        assertFalse(UsuarioRegras.senhasIguais("123456", "654321"))
    }
}
