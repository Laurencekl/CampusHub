package com.laurencekl.campushub

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudiosEventoActivity : AppCompatActivity() {
    private lateinit var armazenamento: AudiosEventoArmazenamento
    private lateinit var adaptador: ArrayAdapter<String>
    private lateinit var textoVazio: TextView
    private var audios = emptyList<AudioEventoLocal>()
    private var reprodutor: MediaPlayer? = null
    private val eventoId get() = intent.getStringExtra("eventoId").orEmpty()

    private val gravador = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        val usuario = FirebaseAuth.getInstance().currentUser
        val uri = resultado.data?.data

        if (resultado.resultCode == RESULT_OK && usuario != null && uri != null) {
            val nome = usuario.displayName ?: usuario.email ?: getString(R.string.usuario)
            runCatching { armazenamento.salvar(uri, eventoId, usuario.uid, nome) }
                .onSuccess {
                    carregarAudios()
                    avisar(R.string.audio_publicado)
                }
                .onFailure { avisar(R.string.erro_salvar_audio) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audios_evento)

        armazenamento = AudiosEventoArmazenamento(applicationContext)
        textoVazio = findViewById(R.id.textoSemAudios)
        findViewById<TextView>(R.id.textoTituloEventoAudio).text =
            intent.getStringExtra("tituloEvento").orEmpty()

        adaptador = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        findViewById<ListView>(R.id.listaAudiosEvento).apply {
            adapter = adaptador
            setOnItemClickListener { _, _, position, _ -> reproduzir(audios[position]) }
            setOnItemLongClickListener { _, _, position, _ ->
                confirmarExclusao(audios[position])
                true
            }
        }

        findViewById<Button>(R.id.botaoGravarAudio).setOnClickListener { gravarAudio() }
        findViewById<Button>(R.id.botaoVoltarAudios).setOnClickListener { finish() }
        carregarAudios()
    }

    private fun gravarAudio() {
        if (FirebaseAuth.getInstance().currentUser == null || eventoId.isBlank()) {
            avisar(R.string.login_para_audio)
            return
        }
        try {
            gravador.launch(Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION))
        } catch (_: ActivityNotFoundException) {
            avisar(R.string.erro_abrir_gravador)
        }
    }

    private fun carregarAudios() {
        audios = armazenamento.listar(eventoId)
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR"))
        val itens = audios.map {
            getString(R.string.item_audio, it.autorNome, formato.format(Date(it.criadoEm)))
        }
        adaptador.clear()
        adaptador.addAll(itens)
        textoVazio.visibility = if (audios.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun reproduzir(audio: AudioEventoLocal) {
        reprodutor?.release()
        runCatching {
            reprodutor = MediaPlayer().apply {
                setDataSource(armazenamento.caminho(audio.id))
                setOnCompletionListener { liberarReprodutor() }
                prepare()
                start()
            }
            avisar(R.string.audio_reproduzindo)
        }.onFailure {
            liberarReprodutor()
            avisar(R.string.erro_reproduzir_audio)
        }
    }

    private fun confirmarExclusao(audio: AudioEventoLocal) {
        if (audio.autorId != FirebaseAuth.getInstance().currentUser?.uid) {
            avisar(R.string.apenas_autor_exclui_audio)
            return
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.excluir_audio_titulo)
            .setMessage(R.string.excluir_audio_mensagem)
            .setPositiveButton(R.string.sim_excluir) { _, _ ->
                liberarReprodutor()
                armazenamento.excluir(audio)
                carregarAudios()
                avisar(R.string.audio_excluido)
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    private fun liberarReprodutor() {
        reprodutor?.release()
        reprodutor = null
    }

    private fun avisar(mensagem: Int) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        liberarReprodutor()
        super.onStop()
    }
}
