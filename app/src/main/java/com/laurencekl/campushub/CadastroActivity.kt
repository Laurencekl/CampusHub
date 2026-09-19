package com.laurencekl.campushub

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class CadastroActivity : AppCompatActivity() {

    private lateinit var editNome: EditText
    private lateinit var editEmail: EditText
    private lateinit var editSenha: EditText
    private lateinit var editConfirmarSenha: EditText
    private lateinit var botaoCadastrar: Button
    private lateinit var botaoIrParaLogin: Button
    private lateinit var progressoCadastro: ProgressBar

    private val autenticacao = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        editNome = findViewById(R.id.editNome)
        editEmail = findViewById(R.id.editEmail)
        editSenha = findViewById(R.id.editSenha)
        editConfirmarSenha = findViewById(R.id.editConfirmarSenha)
        botaoCadastrar = findViewById(R.id.botaoCadastrar)
        botaoIrParaLogin = findViewById(R.id.botaoIrParaLogin)
        progressoCadastro = findViewById(R.id.progressoCadastro)

        botaoCadastrar.setOnClickListener {
            cadastrarUsuario()
        }

        botaoIrParaLogin.setOnClickListener {
            finish()
        }
    }

    private fun cadastrarUsuario() {
        val nome = editNome.text.toString().trim()
        val email = editEmail.text.toString().trim()
        val senha = editSenha.text.toString()
        val confirmarSenha = editConfirmarSenha.text.toString()

        if (!validarCampos(nome, email, senha, confirmarSenha)) {
            return
        }

        mostrarCarregamento(true)

        autenticacao.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { resultado ->
                if (resultado.isSuccessful) {
                    salvarNomeDoUsuario(nome)
                } else {
                    mostrarCarregamento(false)
                    val mensagem = resultado.exception?.localizedMessage
                        ?: getString(R.string.erro_cadastro)
                    Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun validarCampos(
        nome: String,
        email: String,
        senha: String,
        confirmarSenha: String
    ): Boolean {
        if (nome.isBlank()) {
            editNome.error = getString(R.string.campo_obrigatorio)
            editNome.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.error = getString(R.string.email_invalido)
            editEmail.requestFocus()
            return false
        }

        if (senha.length < 6) {
            editSenha.error = getString(R.string.senha_curta)
            editSenha.requestFocus()
            return false
        }

        if (senha != confirmarSenha) {
            editConfirmarSenha.error = getString(R.string.senhas_diferentes)
            editConfirmarSenha.requestFocus()
            return false
        }

        return true
    }

    private fun salvarNomeDoUsuario(nome: String) {
        val alteracaoPerfil = UserProfileChangeRequest.Builder()
            .setDisplayName(nome)
            .build()

        autenticacao.currentUser?.updateProfile(alteracaoPerfil)
            ?.addOnCompleteListener {
                mostrarCarregamento(false)
                Toast.makeText(this, R.string.conta_criada, Toast.LENGTH_LONG).show()
                abrirTelaInicial()
            }
    }

    private fun mostrarCarregamento(carregando: Boolean) {
        progressoCadastro.visibility = if (carregando) View.VISIBLE else View.GONE
        botaoCadastrar.isEnabled = !carregando
    }

    private fun abrirTelaInicial() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
