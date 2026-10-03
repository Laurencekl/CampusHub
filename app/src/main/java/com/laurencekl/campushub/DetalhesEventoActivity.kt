package com.laurencekl.campushub

import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.view.View
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
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
        val categoria = intent.getStringExtra("categoria")
            ?: getString(R.string.categoria_tecnologia)
        var limiteVagas = intent.getLongExtra("limiteVagas", 30)
        var quantidadeInscritos = intent.getLongExtra("inscritos", 0)
        val botaoInscrever = findViewById<Button>(R.id.botaoInscrever)
        val botaoFavoritar = findViewById<Button>(R.id.botaoFavoritar)
        val textoVagas = findViewById<TextView>(R.id.textoVagasDetalhes)
        val textoMedia = findViewById<TextView>(R.id.textoMediaAvaliacoes)
        val textoStatusAvaliacao = findViewById<TextView>(R.id.textoStatusAvaliacao)
        val barraAvaliacao = findViewById<RatingBar>(R.id.barraAvaliacao)
        val botaoAvaliar = findViewById<Button>(R.id.botaoAvaliarEvento)

        findViewById<TextView>(R.id.textoTituloDetalhes).text = titulo
        findViewById<TextView>(R.id.textoCategoriaDetalhes).text = categoria
        findViewById<TextView>(R.id.textoDataDetalhes).text = data
        findViewById<TextView>(R.id.textoHorarioDetalhes).text = horario
        findViewById<TextView>(R.id.textoLocalDetalhes).text = local
        findViewById<TextView>(R.id.textoDescricaoDetalhes).text = descricao

        val usuario = FirebaseAuth.getInstance().currentUser
        val bancoDados = FirebaseFirestore.getInstance()
        var inscrito = false
        var favorito = false
        var possuiAvaliacao = false
        val eventoEncerrado = Evento(data = data, horario = horario).estaEncerrado()

        val referenciaInscricao = if (usuario != null && eventoId.isNotEmpty()) {
            bancoDados
                .collection("usuarios")
                .document(usuario.uid)
                .collection("inscricoes")
                .document(eventoId)
        } else {
            null
        }

        val referenciaEvento = if (eventoId.isNotEmpty()) {
            bancoDados.collection("eventos").document(eventoId)
        } else {
            null
        }

        val referenciaFavorito = if (usuario != null && eventoId.isNotEmpty()) {
            bancoDados
                .collection("usuarios")
                .document(usuario.uid)
                .collection("favoritos")
                .document(eventoId)
        } else {
            null
        }

        val referenciaAvaliacoes = referenciaEvento?.collection("avaliacoes")
        val referenciaAvaliacaoUsuario = if (usuario != null) {
            referenciaAvaliacoes?.document(usuario.uid)
        } else {
            null
        }

        fun carregarMediaAvaliacoes() {
            if (referenciaAvaliacoes == null) {
                textoMedia.text = getString(R.string.sem_avaliacoes)
                return
            }

            referenciaAvaliacoes.get()
                .addOnSuccessListener { documentos ->
                    val notas = documentos.mapNotNull { it.getLong("nota") }

                    textoMedia.text = if (notas.isEmpty()) {
                        getString(R.string.sem_avaliacoes)
                    } else {
                        val media = notas.average()
                        val mediaFormatada = String.format(
                            Locale.forLanguageTag("pt-BR"),
                            "%.1f",
                            media
                        )
                        getString(
                            R.string.media_avaliacoes,
                            mediaFormatada,
                            notas.size
                        )
                    }
                }
                .addOnFailureListener {
                    textoMedia.text = getString(R.string.erro_carregar_avaliacoes)
                }
        }

        fun configurarAvaliacaoDoUsuario() {
            barraAvaliacao.visibility = View.GONE
            botaoAvaliar.visibility = View.GONE

            when {
                !eventoEncerrado -> {
                    textoStatusAvaliacao.text =
                        getString(R.string.avaliacao_evento_futuro)
                }
                !inscrito -> {
                    textoStatusAvaliacao.text =
                        getString(R.string.avaliacao_apenas_inscritos)
                }
                referenciaAvaliacaoUsuario == null -> {
                    textoStatusAvaliacao.text =
                        getString(R.string.usuario_nao_autenticado)
                }
                else -> {
                    textoStatusAvaliacao.text = getString(R.string.carregando_avaliacao)
                    barraAvaliacao.visibility = View.VISIBLE
                    botaoAvaliar.visibility = View.VISIBLE
                    botaoAvaliar.isEnabled = false

                    referenciaAvaliacaoUsuario.get()
                        .addOnSuccessListener { documento ->
                            possuiAvaliacao = documento.exists()
                            val nota = documento.getLong("nota") ?: 0
                            barraAvaliacao.rating = nota.toFloat()
                            botaoAvaliar.text = if (possuiAvaliacao) {
                                getString(R.string.atualizar_avaliacao)
                            } else {
                                getString(R.string.avaliar_evento)
                            }
                            textoStatusAvaliacao.text = if (possuiAvaliacao) {
                                getString(R.string.sua_avaliacao, nota)
                            } else {
                                getString(R.string.escolha_nota)
                            }
                            botaoAvaliar.isEnabled = true
                        }
                        .addOnFailureListener {
                            textoStatusAvaliacao.text =
                                getString(R.string.erro_carregar_avaliacao)
                            botaoAvaliar.isEnabled = true
                        }
                }
            }
        }

        fun atualizarVagas() {
            val vagasDisponiveis = (limiteVagas - quantidadeInscritos).coerceAtLeast(0)

            textoVagas.text = if (vagasDisponiveis == 0L) {
                getString(R.string.evento_lotado)
            } else {
                getString(R.string.vagas_disponiveis, vagasDisponiveis, limiteVagas)
            }
        }

        fun atualizarBotao() {
            val eventoLotado = quantidadeInscritos >= limiteVagas

            when {
                inscrito -> {
                    botaoInscrever.text = getString(R.string.cancelar_inscricao)
                    botaoInscrever.isEnabled = true
                }
                eventoEncerrado -> {
                    botaoInscrever.text = getString(R.string.evento_encerrado)
                    botaoInscrever.isEnabled = false
                }
                eventoLotado -> {
                    botaoInscrever.text = getString(R.string.evento_lotado)
                    botaoInscrever.isEnabled = false
                }
                else -> {
                    botaoInscrever.text = getString(R.string.inscrever_evento)
                    botaoInscrever.isEnabled = true
                }
            }
        }

        atualizarVagas()
        atualizarBotao()
        carregarMediaAvaliacoes()

        if (referenciaFavorito != null) {
            botaoFavoritar.isEnabled = false

            referenciaFavorito.get()
                .addOnSuccessListener { documento ->
                    favorito = documento.exists()
                    botaoFavoritar.text = if (favorito) {
                        getString(R.string.remover_favoritos)
                    } else {
                        getString(R.string.favoritar_evento)
                    }
                    botaoFavoritar.isEnabled = true
                }
                .addOnFailureListener {
                    botaoFavoritar.isEnabled = true
                }
        }

        if (referenciaInscricao != null) {
            botaoInscrever.isEnabled = false

            referenciaInscricao
                .get()
                .addOnSuccessListener { documento ->
                    inscrito = documento.exists()
                    atualizarBotao()
                    configurarAvaliacaoDoUsuario()
                }
                .addOnFailureListener {
                    atualizarBotao()
                    configurarAvaliacaoDoUsuario()
                }
        } else {
            configurarAvaliacaoDoUsuario()
        }

        referenciaEvento?.get()
            ?.addOnSuccessListener { documento ->
                limiteVagas = documento.getLong("limiteVagas") ?: 30
                quantidadeInscritos = documento.getLong("inscritos") ?: 0
                atualizarVagas()
                atualizarBotao()
            }

        botaoInscrever.setOnClickListener {
            if (usuario == null || referenciaInscricao == null || referenciaEvento == null) {
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

                        bancoDados.runTransaction { transacao ->
                            val evento = transacao.get(referenciaEvento)
                            val inscritosAtuais = evento.getLong("inscritos") ?: 0
                            val novaQuantidade = (inscritosAtuais - 1).coerceAtLeast(0)

                            if (inscritosAtuais > 0) {
                                transacao.update(
                                    referenciaEvento,
                                    "inscritos",
                                    novaQuantidade
                                )
                            }
                            transacao.delete(referenciaInscricao)

                            novaQuantidade
                        }
                            .addOnSuccessListener {
                                inscrito = false
                                quantidadeInscritos = it
                                atualizarVagas()
                                atualizarBotao()
                                configurarAvaliacaoDoUsuario()
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

                bancoDados.runTransaction { transacao ->
                    val evento = transacao.get(referenciaEvento)
                    val limiteAtual = evento.getLong("limiteVagas") ?: 30
                    val inscritosAtuais = evento.getLong("inscritos") ?: 0

                    if (inscritosAtuais >= limiteAtual) {
                        throw FirebaseFirestoreException(
                            "EVENTO_LOTADO",
                            FirebaseFirestoreException.Code.ABORTED
                        )
                    }

                    val novaQuantidade = inscritosAtuais + 1
                    val inscricao = hashMapOf<String, Any>(
                        "eventoId" to eventoId,
                        "titulo" to titulo,
                        "descricao" to descricao,
                        "data" to data,
                        "horario" to horario,
                        "local" to local,
                        "categoria" to categoria,
                        "limiteVagas" to limiteAtual,
                        "inscritos" to novaQuantidade,
                        "inscritoEm" to FieldValue.serverTimestamp()
                    )

                    transacao.update(referenciaEvento, "inscritos", novaQuantidade)
                    transacao.set(referenciaInscricao, inscricao)

                    Pair(limiteAtual, novaQuantidade)
                }
                    .addOnSuccessListener { resultado ->
                        inscrito = true
                        limiteVagas = resultado.first
                        quantidadeInscritos = resultado.second
                        atualizarVagas()
                        atualizarBotao()
                        configurarAvaliacaoDoUsuario()
                        Toast.makeText(
                            this,
                            getString(R.string.inscricao_realizada),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { erro ->
                        if (erro.message == "EVENTO_LOTADO") {
                            quantidadeInscritos = limiteVagas
                            atualizarVagas()
                            atualizarBotao()
                        } else {
                            botaoInscrever.isEnabled = true
                        }
                        Toast.makeText(
                            this,
                            if (erro.message == "EVENTO_LOTADO") {
                                getString(R.string.evento_lotado)
                            } else {
                                getString(R.string.erro_inscricao)
                            },
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }

        botaoAvaliar.setOnClickListener {
            val nota = barraAvaliacao.rating.toInt()

            if (nota !in 1..5) {
                Toast.makeText(
                    this,
                    getString(R.string.selecione_nota),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (
                usuario == null ||
                referenciaAvaliacaoUsuario == null ||
                !inscrito ||
                !eventoEncerrado
            ) {
                Toast.makeText(
                    this,
                    getString(R.string.avaliacao_nao_permitida),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            botaoAvaliar.isEnabled = false
            val dadosAvaliacao = hashMapOf<String, Any>(
                "usuarioId" to usuario.uid,
                "nota" to nota,
                "atualizadoEm" to FieldValue.serverTimestamp()
            )

            referenciaAvaliacaoUsuario.set(dadosAvaliacao)
                .addOnSuccessListener {
                    possuiAvaliacao = true
                    botaoAvaliar.text = getString(R.string.atualizar_avaliacao)
                    botaoAvaliar.isEnabled = true
                    textoStatusAvaliacao.text = getString(R.string.sua_avaliacao, nota)
                    Toast.makeText(
                        this,
                        getString(R.string.avaliacao_salva),
                        Toast.LENGTH_SHORT
                    ).show()
                    carregarMediaAvaliacoes()
                }
                .addOnFailureListener {
                    botaoAvaliar.isEnabled = true
                    Toast.makeText(
                        this,
                        getString(R.string.erro_salvar_avaliacao),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        botaoFavoritar.setOnClickListener {
            if (usuario == null || referenciaFavorito == null) {
                Toast.makeText(
                    this,
                    getString(R.string.usuario_nao_autenticado),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            botaoFavoritar.isEnabled = false

            if (favorito) {
                referenciaFavorito.delete()
                    .addOnSuccessListener {
                        favorito = false
                        botaoFavoritar.text = getString(R.string.favoritar_evento)
                        botaoFavoritar.isEnabled = true
                        Toast.makeText(
                            this,
                            getString(R.string.evento_desfavoritado),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener {
                        botaoFavoritar.isEnabled = true
                        Toast.makeText(
                            this,
                            getString(R.string.erro_favorito),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                val dadosFavorito = hashMapOf<String, Any>(
                    "titulo" to titulo,
                    "descricao" to descricao,
                    "data" to data,
                    "horario" to horario,
                    "local" to local,
                    "categoria" to categoria,
                    "limiteVagas" to limiteVagas,
                    "inscritos" to quantidadeInscritos,
                    "favoritadoEm" to FieldValue.serverTimestamp()
                )

                referenciaFavorito.set(dadosFavorito)
                    .addOnSuccessListener {
                        favorito = true
                        botaoFavoritar.text = getString(R.string.remover_favoritos)
                        botaoFavoritar.isEnabled = true
                        Toast.makeText(
                            this,
                            getString(R.string.evento_favoritado),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener {
                        botaoFavoritar.isEnabled = true
                        Toast.makeText(
                            this,
                            getString(R.string.erro_favorito),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }

        findViewById<Button>(R.id.botaoComentarios).setOnClickListener {
            val intentComentarios = Intent(this, ComentariosActivity::class.java)
            intentComentarios.putExtra("eventoId", eventoId)
            intentComentarios.putExtra("tituloEvento", titulo)
            startActivity(intentComentarios)
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
