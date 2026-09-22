package com.laurencekl.campushub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class PerfilActivity : AppCompatActivity() {

    private lateinit var textoInicial: TextView
    private lateinit var textoNome: TextView
    private lateinit var textoEmail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        textoInicial = findViewById(R.id.textoInicialPerfil)
        textoNome = findViewById(R.id.textoNomePerfil)
        textoEmail = findViewById(R.id.textoEmailPerfil)

        findViewById<Button>(R.id.botaoEditarPerfil).setOnClickListener {
            val intent = Intent(this, EditarPerfilActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.botaoVoltarPerfil).setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        mostrarDadosDoUsuario()
    }

    private fun mostrarDadosDoUsuario() {
        val usuario = FirebaseAuth.getInstance().currentUser
        val nome = usuario?.displayName ?: getString(R.string.usuario)
        val email = usuario?.email.orEmpty()

        textoInicial.text = nome.firstOrNull()?.uppercase() ?: "A"
        textoNome.text = nome
        textoEmail.text = email
    }
}
