package com.laurencekl.campushub

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val textoBoasVindas = findViewById<TextView>(R.id.textoBoasVindas)
        val textoEmail = findViewById<TextView>(R.id.textoEmailUsuario)

        val usuario = FirebaseAuth.getInstance().currentUser
        val nome = usuario?.displayName ?: getString(R.string.usuario)

        textoBoasVindas.text = getString(R.string.inicio_titulo, nome)
        textoEmail.text = usuario?.email.orEmpty()
    }
}
