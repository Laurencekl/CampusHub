package com.laurencekl.campushub

import android.content.Context
import android.widget.ImageView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide

fun SimpleAdapter.configurarCartaoEvento(contexto: Context) {
    viewBinder = SimpleAdapter.ViewBinder { view, valor, _ ->
        when (view.id) {
            R.id.imagemEventoLista -> {
                Glide.with(view)
                    .load(valor.toString().takeIf { it.isNotBlank() })
                    .placeholder(R.drawable.fundo_imagem_evento)
                    .error(R.drawable.fundo_imagem_evento)
                    .centerCrop()
                    .into(view as ImageView)
                true
            }

            R.id.textoSituacaoEvento -> {
                val textoSituacao = valor.toString()
                val encerrado = textoSituacao == contexto.getString(R.string.evento_encerrado)
                val texto = view as TextView

                texto.text = textoSituacao
                texto.setBackgroundResource(
                    if (encerrado) R.drawable.fundo_status_encerrado
                    else R.drawable.fundo_status_proximo
                )
                texto.setTextColor(
                    ContextCompat.getColor(
                        contexto,
                        if (encerrado) R.color.texto_status_encerrado else R.color.azul_escuro
                    )
                )
                true
            }

            else -> false
        }
    }
}
