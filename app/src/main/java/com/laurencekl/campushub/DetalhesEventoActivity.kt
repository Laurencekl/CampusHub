package com.laurencekl.campushub

import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class DetalhesEventoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes_evento)

        val eventoId = intent.getStringExtra("eventoId").orEmpty()
        val titulo = intent.getStringExtra("titulo").orEmpty()
        val descricao = intent.getStringExtra("descricao").orEmpty()
        val data = intent.getStringExtra("data").orEmpty()
        val horario = intent.getStringExtra("horario").orEmpty()
        val local = intent.getStringExtra("local").orEmpty()
        val botaoInscrever = findViewById<Button>(R.id.botaoInscrever)

        findViewById<TextView>(R.id.textoTituloDetalhes).text = titulo
        findViewById<TextView>(R.id.textoDataDetalhes).text = data
        findViewById<TextView>(R.id.textoHorarioDetalhes).text = horario
        findViewById<TextView>(R.id.textoLocalDetalhes).text = local
        findViewById<TextView>(R.id.textoDescricaoDetalhes).text = descricao

        val usuario = FirebaseAuth.getInstance().currentUser
        var inscrito = false

        val referenciaInscricao = if (usuario != null && eventoId.isNotEmpty()) {
            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(usuario.uid)
                .collection("inscricoes")
                .document(eventoId)
        } else {
            null
        }

        fun atualizarBotao() {
            botaoInscrever.text = if (inscrito) {
                getString(R.string.cancelar_inscricao)
            } else {
                getString(R.string.inscrever_evento)
            }
            botaoInscrever.isEnabled = true
        }

        if (referenciaInscricao != null) {
            botaoInscrever.isEnabled = false

            referenciaInscricao
                .get()
                .addOnSuccessListener { documento ->
                    inscrito = documento.exists()
                    atualizarBotao()
                }
                .addOnFailureListener {
                    atualizarBotao()
                }
        }

        botaoInscrever.setOnClickListener {
            if (usuario == null || referenciaInscricao == null) {
                Toast.makeText(
                    this,
                    getString(R.string.usuario_nao_autenticado),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (inscrito) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.confirmar_cancelamento_titulo)
                    .setMessage(R.string.confirmar_cancelamento_mensagem)
                    .setPositiveButton(R.string.sim_cancelar) { _, _ ->
                        botaoInscrever.isEnabled = false

                        referenciaInscricao.delete()
                            .addOnSuccessListener {
                                inscrito = false
                                atualizarBotao()
                                Toast.makeText(
                                    this,
                                    getString(R.string.inscricao_cancelada),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener {
                                botaoInscrever.isEnabled = true
                                Toast.makeText(
                                    this,
                                    getString(R.string.erro_cancelamento),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                    .setNegativeButton(R.string.nao_manter, null)
                    .show()
            } else {
                botaoInscrever.isEnabled = false

                val inscricao = hashMapOf<String, Any>(
                    "eventoId" to eventoId,
                    "titulo" to titulo,
                    "descricao" to descricao,
                    "data" to data,
                    "horario" to horario,
                    "local" to local,
                    "inscritoEm" to FieldValue.serverTimestamp()
                )

                referenciaInscricao.set(inscricao)
                    .addOnSuccessListener {
                        inscrito = true
                        atualizarBotao()
                        Toast.makeText(
                            this,
                            getString(R.string.inscricao_realizada),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener {
                        botaoInscrever.isEnabled = true
                        Toast.makeText(
                            this,
                            getString(R.string.erro_inscricao),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }

        findViewById<Button>(R.id.botaoCompartilharEvento).setOnClickListener {
            val mensagem = getString(
                R.string.mensagem_compartilhar_evento,
                titulo,
                data,
                horario,
                local
            )

            val intentCompartilhar = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, mensagem)
            }

            startActivity(
                Intent.createChooser(
                    intentCompartilhar,
                    getString(R.string.compartilhar_evento)
                )
            )
        }

        findViewById<Button>(R.id.botaoAdicionarCalendario).setOnClickListener {
            val formatoData = SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale.forLanguageTag("pt-BR")
            )
            formatoData.isLenient = false

            val inicioEvento = formatoData.parse("$data $horario")?.time

            if (inicioEvento == null) {
                Toast.makeText(
                    this,
                    getString(R.string.erro_abrir_calendario),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val intentCalendario = Intent(Intent.ACTION_INSERT).apply {
                this.data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, titulo)
                putExtra(CalendarContract.Events.DESCRIPTION, descricao)
                putExtra(CalendarContract.Events.EVENT_LOCATION, local)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, inicioEvento)
                putExtra(
                    CalendarContract.EXTRA_EVENT_END_TIME,
                    inicioEvento + 60 * 60 * 1000
                )
            }

            if (intentCalendario.resolveActivity(packageManager) != null) {
                startActivity(intentCalendario)
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.erro_abrir_calendario),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        findViewById<Button>(R.id.botaoVoltarDetalhes).setOnClickListener {
            finish()
        }
    }
}
