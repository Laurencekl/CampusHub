package com.laurencekl.campushub

import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class EventosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eventos)

        val listaEventos = findViewById<ListView>(R.id.listaEventos)
        val textoStatus = findViewById<TextView>(R.id.textoStatusEventos)
        val dadosDaLista = mutableListOf<HashMap<String, String>>()

        val adaptador = SimpleAdapter(
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
        listaEventos.adapter = adaptador

        FirebaseFirestore.getInstance()
            .collection("eventos")
            .get()
            .addOnSuccessListener { documentos ->
                val eventos = documentos.map { documento ->
                    documento.toObject(Evento::class.java).copy(id = documento.id)
                }.sortedBy { it.titulo }

                eventos.forEach { evento ->
                    dadosDaLista.add(
                        hashMapOf(
                            "titulo" to evento.titulo,
                            "informacoes" to "${evento.data} às ${evento.horario} • ${evento.local}",
                            "descricao" to evento.descricao
                        )
                    )
                }

                textoStatus.text = if (eventos.isEmpty()) {
                    getString(R.string.nenhum_evento)
                } else {
                    getString(R.string.eventos_encontrados, eventos.size)
                }
                adaptador.notifyDataSetChanged()
            }
            .addOnFailureListener {
                textoStatus.text = getString(R.string.erro_carregar_eventos)
            }

        findViewById<Button>(R.id.botaoVoltarEventos).setOnClickListener {
            finish()
        }
    }
}
