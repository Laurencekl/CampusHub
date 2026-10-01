package com.laurencekl.campushub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MeusFavoritosActivity : AppCompatActivity() {

    private val eventos = mutableListOf<Evento>()
    private val dadosDaLista = mutableListOf<HashMap<String, String>>()
    private lateinit var adaptador: SimpleAdapter
    private lateinit var textoStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meus_favoritos)

        val listaFavoritos = findViewById<ListView>(R.id.listaFavoritos)
        textoStatus = findViewById(R.id.textoStatusFavoritos)

        adaptador = SimpleAdapter(
            this,
            dadosDaLista,
            R.layout.item_evento,
            arrayOf("titulo", "informacoes", "descricao"),
            intArrayOf(
                R.id.textoTituloEvento,
                R.id.textoInformacoesEvento,
                R.id.textoDescricaoEvento
            )
        )
        listaFavoritos.adapter = adaptador

        listaFavoritos.setOnItemClickListener { _, _, position, _ ->
            val evento = eventos[position]
            val intent = Intent(this, DetalhesEventoActivity::class.java)

            intent.putExtra("eventoId", evento.id)
            intent.putExtra("titulo", evento.titulo)
            intent.putExtra("descricao", evento.descricao)
            intent.putExtra("data", evento.data)
            intent.putExtra("horario", evento.horario)
            intent.putExtra("local", evento.local)
            intent.putExtra("categoria", evento.categoria)
            intent.putExtra("limiteVagas", evento.limiteVagas)
            intent.putExtra("inscritos", evento.inscritos)

            startActivity(intent)
        }

        findViewById<Button>(R.id.botaoVoltarFavoritos).setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        carregarFavoritos()
    }

    private fun carregarFavoritos() {
        val usuario = FirebaseAuth.getInstance().currentUser

        if (usuario == null) {
            textoStatus.text = getString(R.string.usuario_nao_autenticado)
            return
        }

        textoStatus.text = getString(R.string.carregando_eventos)
        eventos.clear()
        dadosDaLista.clear()
        adaptador.notifyDataSetChanged()

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(usuario.uid)
            .collection("favoritos")
            .get()
            .addOnSuccessListener { documentos ->
                val favoritos = documentos.map { documento ->
                    documento.toObject(Evento::class.java).copy(id = documento.id)
                }.sortedWith(
                    compareBy<Evento> { it.dataParaOrdenacao() }
                        .thenBy { it.horario }
                )

                eventos.addAll(favoritos)

                favoritos.forEach { evento ->
                    dadosDaLista.add(
                        hashMapOf(
                            "titulo" to evento.titulo,
                            "informacoes" to "${evento.categoria} • ${evento.data} às ${evento.horario} • ${evento.local}",
                            "descricao" to evento.descricao
                        )
                    )
                }

                textoStatus.text = when (favoritos.size) {
                    0 -> getString(R.string.nenhum_favorito)
                    1 -> getString(R.string.um_evento_favorito)
                    else -> getString(R.string.eventos_favoritos, favoritos.size)
                }
                adaptador.notifyDataSetChanged()
            }
            .addOnFailureListener {
                textoStatus.text = getString(R.string.erro_carregar_favoritos)
            }
    }
}
