package com.laurencekl.campushub

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RecuperarSenhaActivity : AppCompatActivity() {

    private lateinit var editEmail: EditText
    private lateinit var botaoEnviar: Button
    private lateinit var progressoRecuperacao: ProgressBar

    private val autenticacao = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_senha)

        editEmail = findViewById(R.id.editEmailRecuperacao)
        botaoEnviar = findViewById(R.id.botaoEnviarLink)
        progressoRecuperacao = findViewById(R.id.progressoRecuperacao)

        botaoEnviar.setOnClickListener {
            enviarEmailDeRecuperacao()
        }

        findViewById<Button>(R.id.botaoVoltarLogin).setOnClickListener {
            finish()
        }
    }

    private fun enviarEmailDeRecuperacao() {
        val email = editEmail.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.error = getString(R.string.email_invalido)
            editEmail.requestFocus()
            return
        }

        mostrarCarregamento(true)

        autenticacao.sendPasswordResetEmail(email)
            .addOnCompleteListener { resultado ->
                mostrarCarregamento(false)

                if (resultado.isSuccessful) {
                    Toast.makeText(
                        this,
                        R.string.email_recuperacao_enviado,
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    val mensagem = resultado.exception?.localizedMessage
                        ?: getString(R.string.erro_recuperacao)
                    Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun mostrarCarregamento(carregando: Boolean) {
        progressoRecuperacao.visibility = if (carregando) View.VISIBLE else View.GONE
        botaoEnviar.isEnabled = !carregando
    }
}
