package com.laurencekl.campushub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var textoQuantidadeInscricoes: TextView
    private lateinit var textoQuantidadeFavoritos: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val textoBoasVindas = findViewById<TextView>(R.id.textoBoasVindas)
        val textoEmail = findViewById<TextView>(R.id.textoEmailUsuario)
        textoQuantidadeInscricoes = findViewById(R.id.textoQuantidadeInscricoes)
        textoQuantidadeFavoritos = findViewById(R.id.textoQuantidadeFavoritos)
        val botaoEventos = findViewById<Button>(R.id.botaoEventos)
        val botaoMeusEventos = findViewById<Button>(R.id.botaoMeusEventos)
        val botaoMeusFavoritos = findViewById<Button>(R.id.botaoMeusFavoritos)
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

        botaoMeusFavoritos.setOnClickListener {
            val intent = Intent(this, MeusFavoritosActivity::class.java)
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

    override fun onResume() {
        super.onResume()
        carregarResumo()
    }

    private fun carregarResumo() {
        val usuario = FirebaseAuth.getInstance().currentUser ?: return
        val banco = FirebaseFirestore.getInstance()

        textoQuantidadeInscricoes.text = getString(R.string.carregando_quantidade)
        textoQuantidadeFavoritos.text = getString(R.string.carregando_quantidade)

        banco.collection("usuarios")
            .document(usuario.uid)
            .collection("inscricoes")
            .get()
            .addOnSuccessListener { documentos ->
                textoQuantidadeInscricoes.text = documentos.size().toString()
            }
            .addOnFailureListener {
                textoQuantidadeInscricoes.text = getString(R.string.quantidade_indisponivel)
            }

        banco.collection("usuarios")
            .document(usuario.uid)
            .collection("favoritos")
            .get()
            .addOnSuccessListener { documentos ->
                textoQuantidadeFavoritos.text = documentos.size().toString()
            }
            .addOnFailureListener {
                textoQuantidadeFavoritos.text = getString(R.string.quantidade_indisponivel)
            }
    }
}
