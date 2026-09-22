package com.laurencekl.campushub

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DetalhesEventoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes_evento)

        val titulo = intent.getStringExtra("titulo").orEmpty()
        val descricao = intent.getStringExtra("descricao").orEmpty()
        val data = intent.getStringExtra("data").orEmpty()
        val horario = intent.getStringExtra("horario").orEmpty()
        val local = intent.getStringExtra("local").orEmpty()

        findViewById<TextView>(R.id.textoTituloDetalhes).text = titulo
        findViewById<TextView>(R.id.textoDataDetalhes).text = data
        findViewById<TextView>(R.id.textoHorarioDetalhes).text = horario
        findViewById<TextView>(R.id.textoLocalDetalhes).text = local
        findViewById<TextView>(R.id.textoDescricaoDetalhes).text = descricao

        findViewById<Button>(R.id.botaoInscrever).setOnClickListener {
            Toast.makeText(
                this,
                getString(R.string.inscricao_proxima_etapa),
                Toast.LENGTH_SHORT
            ).show()
        }

        findViewById<Button>(R.id.botaoVoltarDetalhes).setOnClickListener {
            finish()
        }
    }
}
