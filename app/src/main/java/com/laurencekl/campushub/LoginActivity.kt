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

class LoginActivity : AppCompatActivity() {

    private lateinit var editEmail: EditText
    private lateinit var editSenha: EditText
    private lateinit var botaoEntrar: Button
    private lateinit var botaoIrParaCadastro: Button
    private lateinit var progressoLogin: ProgressBar

    private val autenticacao = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editEmail = findViewById(R.id.editEmailLogin)
        editSenha = findViewById(R.id.editSenhaLogin)
        botaoEntrar = findViewById(R.id.botaoEntrar)
        botaoIrParaCadastro = findViewById(R.id.botaoIrParaCadastro)
        progressoLogin = findViewById(R.id.progressoLogin)

        botaoEntrar.setOnClickListener {
            realizarLogin()
        }

        botaoIrParaCadastro.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()

        if (autenticacao.currentUser != null) {
            abrirTelaInicial()
        }
    }

    private fun realizarLogin() {
        val email = editEmail.text.toString().trim()
        val senha = editSenha.text.toString()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.error = getString(R.string.email_invalido)
            editEmail.requestFocus()
            return
        }

        if (senha.isBlank()) {
            editSenha.error = getString(R.string.campo_obrigatorio)
            editSenha.requestFocus()
            return
        }

        mostrarCarregamento(true)

        autenticacao.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener { resultado ->
                mostrarCarregamento(false)

                if (resultado.isSuccessful) {
                    abrirTelaInicial()
                } else {
                    val mensagem = resultado.exception?.localizedMessage
                        ?: getString(R.string.erro_login)
                    Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun mostrarCarregamento(carregando: Boolean) {
        progressoLogin.visibility = if (carregando) View.VISIBLE else View.GONE
        botaoEntrar.isEnabled = !carregando
    }

    private fun abrirTelaInicial() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
