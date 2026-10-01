package com.laurencekl.campushub

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.firebase.firestore.FirebaseFirestore

class EventosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eventos)

        val listaEventos = findViewById<ListView>(R.id.listaEventos)
        val textoStatus = findViewById<TextView>(R.id.textoStatusEventos)
        val campoPesquisa = findViewById<EditText>(R.id.campoPesquisaEventos)
        val filtroCategoria = findViewById<Spinner>(R.id.filtroCategoria)
        val dadosDaLista = mutableListOf<HashMap<String, String>>()
        val todosOsEventos = mutableListOf<Evento>()
        val eventosDaLista = mutableListOf<Evento>()
        val categorias = listOf(
            getString(R.string.todas_categorias),
            getString(R.string.categoria_tecnologia),
            getString(R.string.categoria_carreira),
            getString(R.string.categoria_cultura),
            getString(R.string.categoria_esportes)
        )
        var categoriaSelecionada = categorias.first()

        filtroCategoria.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categorias
        )

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

        fun mostrarEventos(eventos: List<Evento>) {
            eventosDaLista.clear()
            eventosDaLista.addAll(eventos)
            dadosDaLista.clear()

            eventos.forEach { evento ->
                dadosDaLista.add(
                    hashMapOf(
                        "titulo" to evento.titulo,
                        "informacoes" to "${evento.categoria} • ${evento.data} às ${evento.horario} • ${evento.local}",
                        "descricao" to evento.descricao
                    )
                )
            }

            textoStatus.text = if (eventos.isEmpty()) {
                getString(R.string.nenhum_resultado)
            } else {
                getString(R.string.eventos_encontrados, eventos.size)
            }
            adaptador.notifyDataSetChanged()
        }

        fun filtrarEventos(texto: String) {
            val pesquisa = texto.trim()
            val eventosFiltrados = todosOsEventos.filter { evento ->
                val correspondeAoNome = pesquisa.isEmpty() ||
                    evento.titulo.contains(pesquisa, ignoreCase = true)
                val correspondeACategoria = categoriaSelecionada == categorias.first() ||
                    evento.categoria.equals(categoriaSelecionada, ignoreCase = true)

                correspondeAoNome && correspondeACategoria
            }

            mostrarEventos(eventosFiltrados)
        }

        campoPesquisa.addTextChangedListener { texto ->
            filtrarEventos(texto.toString())
        }

        filtroCategoria.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                categoriaSelecionada = categorias[position]
                filtrarEventos(campoPesquisa.text.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        FirebaseFirestore.getInstance()
            .collection("eventos")
            .get()
            .addOnSuccessListener { documentos ->
                val eventos = documentos.map { documento ->
                    documento.toObject(Evento::class.java).copy(id = documento.id)
                }.sortedWith(
                    compareBy<Evento> { it.dataParaOrdenacao() }
                        .thenBy { it.horario }
                )

                todosOsEventos.clear()
                todosOsEventos.addAll(eventos)

                if (eventos.isEmpty()) {
                    textoStatus.text = getString(R.string.nenhum_evento)
                    eventosDaLista.clear()
                    dadosDaLista.clear()
                    adaptador.notifyDataSetChanged()
                } else {
                    filtrarEventos(campoPesquisa.text.toString())
                }
            }
            .addOnFailureListener {
                textoStatus.text = getString(R.string.erro_carregar_eventos)
            }

        listaEventos.setOnItemClickListener { _, _, position, _ ->
            val eventoSelecionado = eventosDaLista[position]
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

            startActivity(intent)
        }

        findViewById<Button>(R.id.botaoVoltarEventos).setOnClickListener {
            finish()
        }
    }
}
