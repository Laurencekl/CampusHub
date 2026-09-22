package com.laurencekl.campushub

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class EditarPerfilActivity : AppCompatActivity() {

    private lateinit var editNome: EditText
    private lateinit var botaoSalvar: Button
    private lateinit var progressoEdicao: ProgressBar

    private val autenticacao = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)

        editNome = findViewById(R.id.editNomePerfil)
        botaoSalvar = findViewById(R.id.botaoSalvarPerfil)
        progressoEdicao = findViewById(R.id.progressoEdicaoPerfil)

        editNome.setText(autenticacao.currentUser?.displayName.orEmpty())

        botaoSalvar.setOnClickListener {
            salvarAlteracoes()
        }

        findViewById<Button>(R.id.botaoCancelarEdicao).setOnClickListener {
            finish()
        }
    }

    private fun salvarAlteracoes() {
        val nome = editNome.text.toString().trim()

        if (nome.isBlank()) {
            editNome.error = getString(R.string.campo_obrigatorio)
            editNome.requestFocus()
            return
        }

        val alteracaoPerfil = UserProfileChangeRequest.Builder()
            .setDisplayName(nome)
            .build()

        mostrarCarregamento(true)

        autenticacao.currentUser?.updateProfile(alteracaoPerfil)
            ?.addOnCompleteListener { resultado ->
                mostrarCarregamento(false)

                if (resultado.isSuccessful) {
                    Toast.makeText(this, R.string.perfil_atualizado, Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val mensagem = resultado.exception?.localizedMessage
                        ?: getString(R.string.erro_atualizar_perfil)
                    Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun mostrarCarregamento(carregando: Boolean) {
        progressoEdicao.visibility = if (carregando) View.VISIBLE else View.GONE
        botaoSalvar.isEnabled = !carregando
    }
}
