package com.laurencekl.campushub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
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
            arrayOf("imagem", "titulo", "situacao", "informacoes", "local", "vagas", "descricao"),
            intArrayOf(
                R.id.imagemEventoLista,
                R.id.textoTituloEvento,
                R.id.textoSituacaoEvento,
                R.id.textoInformacoesEvento,
                R.id.textoLocalEvento,
                R.id.textoVagasEvento,
                R.id.textoDescricaoEvento
            )
        )
        adaptador.configurarCartaoEvento(this)
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
            intent.putExtra("categoria", eventoSelecionado.categoria)
            intent.putExtra("limiteVagas", eventoSelecionado.limiteVagas)
            intent.putExtra("inscritos", eventoSelecionado.inscritos)
            intent.putExtra("status", eventoSelecionado.status)

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

        val banco = FirebaseFirestore.getInstance()
        banco
            .collection("usuarios")
            .document(usuario.uid)
            .collection("inscricoes")
            .get()
            .continueWithTask { consulta ->
                val consultasEventos = consulta.result.documents.map { inscricao ->
                    banco.collection("eventos").document(inscricao.id).get()
                }
                Tasks.whenAllSuccess<DocumentSnapshot>(consultasEventos)
            }
            .addOnSuccessListener { documentos ->
                val inscricoes = documentos.mapNotNull { documento ->
                    documento.toObject(Evento::class.java)?.copy(id = documento.id)
                }.sortedWith(
                    compareBy<Evento> { it.dataParaOrdenacao() }
                        .thenBy { it.horario }
                )

                eventos.addAll(inscricoes)

                inscricoes.forEach { evento ->
                    dadosDaLista.add(
                        hashMapOf(
                            "imagem" to evento.imagemUrl,
                            "titulo" to evento.titulo,
                            "situacao" to if (evento.estaEncerrado()) {
                                getString(R.string.evento_encerrado)
                            } else {
                                getString(R.string.evento_proximo)
                            },
                            "informacoes" to "${evento.categoria} • ${evento.data} às ${evento.horario}",
                            "local" to evento.local,
                            "vagas" to if (evento.vagasDisponiveis() == 0L) {
                                getString(R.string.evento_lotado)
                            } else {
                                getString(
                                    R.string.vagas_disponiveis,
                                    evento.vagasDisponiveis(),
                                    evento.limiteVagas
                                )
                            },
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
