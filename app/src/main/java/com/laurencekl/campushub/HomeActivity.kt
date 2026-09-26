package com.laurencekl.campushub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val textoBoasVindas = findViewById<TextView>(R.id.textoBoasVindas)
        val textoEmail = findViewById<TextView>(R.id.textoEmailUsuario)
        val botaoEventos = findViewById<Button>(R.id.botaoEventos)
        val botaoMeusEventos = findViewById<Button>(R.id.botaoMeusEventos)
        val botaoPerfil = findViewById<Button>(R.id.botaoPerfil)
        val botaoSair = findViewById<Button>(R.id.botaoSair)

        val autenticacao = FirebaseAuth.getInstance()
        val usuario = autenticacao.currentUser
        val nome = usuario?.displayName ?: getString(R.string.usuario)

        textoBoasVindas.text = getString(R.string.inicio_titulo, nome)
        textoEmail.text = usuario?.email.orEmpty()

        botaoEventos.setOnClickListener {
            val intent = Intent(this, EventosActivity::class.java)
            startActivity(intent)
        }

        botaoMeusEventos.setOnClickListener {
            val intent = Intent(this, MeusEventosActivity::class.java)
            startActivity(intent)
        }

        botaoPerfil.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }

        botaoSair.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.confirmar_saida_titulo)
                .setMessage(R.string.confirmar_saida_mensagem)
                .setPositiveButton(R.string.sair) { _, _ ->
                    autenticacao.signOut()

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton(R.string.cancelar, null)
                .show()
        }
    }
}
