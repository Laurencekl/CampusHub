package com.laurencekl.campushub

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Locale

class ComentarioAdapter(
    private val contexto: Context,
    private val comentarios: List<Comentario>,
    private val usuarioId: String,
    private val aoEditar: (Comentario) -> Unit,
    private val aoExcluir: (Comentario) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = comentarios.size

    override fun getItem(position: Int): Comentario = comentarios[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(contexto)
            .inflate(R.layout.item_comentario, parent, false)
        val comentario = getItem(position)

        view.findViewById<TextView>(R.id.textoAutorComentario).text = comentario.autorNome
        view.findViewById<TextView>(R.id.textoComentario).text = comentario.texto

        val data = comentario.criadoEm?.toDate()
        view.findViewById<TextView>(R.id.textoDataComentario).text = if (data == null) {
            contexto.getString(R.string.agora)
        } else {
            SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.forLanguageTag("pt-BR"))
                .format(data)
        }

        val botaoEditar = view.findViewById<Button>(R.id.botaoEditarComentario)
        val botaoExcluir = view.findViewById<Button>(R.id.botaoExcluirComentario)
        val comentarioDoUsuario = comentario.autorId == usuarioId

        botaoEditar.visibility = if (comentarioDoUsuario) View.VISIBLE else View.GONE
        botaoExcluir.visibility = if (comentarioDoUsuario) View.VISIBLE else View.GONE

        botaoEditar.setOnClickListener { aoEditar(comentario) }
        botaoExcluir.setOnClickListener { aoExcluir(comentario) }

        return view
    }
}
