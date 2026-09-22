package com.laurencekl.campushub

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class PerfilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        val textoInicial = findViewById<TextView>(R.id.textoInicialPerfil)
        val textoNome = findViewById<TextView>(R.id.textoNomePerfil)
        val textoEmail = findViewById<TextView>(R.id.textoEmailPerfil)

        val usuario = FirebaseAuth.getInstance().currentUser
        val nome = usuario?.displayName ?: getString(R.string.usuario)
        val email = usuario?.email.orEmpty()

        textoInicial.text = nome.firstOrNull()?.uppercase() ?: "A"
        textoNome.text = nome
        textoEmail.text = email

        findViewById<Button>(R.id.botaoVoltarPerfil).setOnClickListener {
            finish()
        }
    }
}
