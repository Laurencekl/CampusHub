package com.laurencekl.campushub

object UsuarioRegras {

    private val formatoEmail = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

    fun nomeValido(nome: String): Boolean {
        return nome.isNotBlank()
    }

    fun emailValido(email: String): Boolean {
        return formatoEmail.matches(email)
    }

    fun senhaCadastroValida(senha: String): Boolean {
        return senha.length >= 6
    }

    fun senhaLoginValida(senha: String): Boolean {
        return senha.isNotBlank()
    }

    fun senhasIguais(senha: String, confirmacao: String): Boolean {
        return senha == confirmacao
    }
}
