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

class MeusEventosActivity : AppCompatActivity() {

    private val eventos = mutableListOf<Evento>()
    private val dadosDaLista = mutableListOf<HashMap<String, String>>()
    private lateinit var adaptador: SimpleAdapter
    private lateinit var textoStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meus_eventos)

        val listaMeusEventos = findViewById<ListView>(R.id.listaMeusEventos)
        textoStatus = findViewById(R.id.textoStatusMeusEventos)

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
        listaMeusEventos.adapter = adaptador

        listaMeusEventos.setOnItemClickListener { _, _, position, _ ->
            val eventoSelecionado = eventos[position]
            val intent = Intent(this, DetalhesEventoActivity::class.java)

            intent.putExtra("eventoId", eventoSelecionado.id)
            intent.putExtra("titulo", eventoSelecionado.titulo)
            intent.putExtra("descricao", eventoSelecionado.descricao)
            intent.putExtra("data", eventoSelecionado.data)
            intent.putExtra("horario", eventoSelecionado.horario)
            intent.putExtra("local", eventoSelecionado.local)

            startActivity(intent)
        }

        findViewById<Button>(R.id.botaoVoltarMeusEventos).setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        carregarInscricoes()
    }

    private fun carregarInscricoes() {
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
            .collection("inscricoes")
            .get()
            .addOnSuccessListener { documentos ->
                val inscricoes = documentos.map { documento ->
                    documento.toObject(Evento::class.java).copy(id = documento.id)
                }.sortedWith(
                    compareBy<Evento> { it.dataParaOrdenacao() }
                        .thenBy { it.horario }
                )

                eventos.addAll(inscricoes)

                inscricoes.forEach { evento ->
                    dadosDaLista.add(
                        hashMapOf(
                            "titulo" to evento.titulo,
                            "informacoes" to "${evento.data} às ${evento.horario} • ${evento.local}",
                            "descricao" to evento.descricao
                        )
                    )
                }

                textoStatus.text = when (inscricoes.size) {
                    0 -> getString(R.string.nenhuma_inscricao)
                    1 -> getString(R.string.um_evento_inscrito)
                    else -> getString(R.string.eventos_inscritos, inscricoes.size)
                }
                adaptador.notifyDataSetChanged()
            }
            .addOnFailureListener {
                textoStatus.text = getString(R.string.erro_carregar_inscricoes)
            }
    }
}
