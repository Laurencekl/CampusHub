package com.laurencekl.campushub

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.MediaController
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VideosEventoActivity : AppCompatActivity() {
    private lateinit var armazenamento: VideosEventoArmazenamento
    private lateinit var adaptador: ArrayAdapter<String>
    private lateinit var textoVazio: TextView
    private lateinit var reprodutor: VideoView
    private var videos = emptyList<VideoEventoLocal>()
    private val eventoId get() = intent.getStringExtra("eventoId").orEmpty()

    private val camera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == RESULT_OK) salvarVideo(resultado.data?.data)
    }

    private val galeria = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> salvarVideo(uri) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videos_evento)

        armazenamento = VideosEventoArmazenamento(applicationContext)
        textoVazio = findViewById(R.id.textoSemVideos)
        reprodutor = findViewById(R.id.reprodutorVideo)
        reprodutor.setMediaController(MediaController(this))
        findViewById<TextView>(R.id.textoTituloEventoVideo).text =
            intent.getStringExtra("tituloEvento").orEmpty()

        adaptador = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        findViewById<ListView>(R.id.listaVideosEvento).apply {
            adapter = adaptador
            setOnItemClickListener { _, _, position, _ -> reproduzir(videos[position]) }
        }

        findViewById<Button>(R.id.botaoGravarVideo).setOnClickListener { abrirCamera() }
        findViewById<Button>(R.id.botaoEscolherVideo).setOnClickListener {
            galeria.launch("video/*")
        }
        findViewById<Button>(R.id.botaoVoltarVideos).setOnClickListener { finish() }
        carregarVideos()
    }

    private fun abrirCamera() {
        try {
            camera.launch(Intent(MediaStore.ACTION_VIDEO_CAPTURE))
        } catch (_: ActivityNotFoundException) {
            avisar(R.string.erro_abrir_camera_video)
        }
    }

    private fun salvarVideo(uri: Uri?) {
        val usuario = FirebaseAuth.getInstance().currentUser
        if (uri == null || usuario == null || eventoId.isBlank()) {
            avisar(R.string.erro_salvar_video)
            return
        }
        val nome = usuario.displayName ?: usuario.email ?: getString(R.string.usuario)
        runCatching { armazenamento.salvar(uri, eventoId, usuario.uid, nome) }
            .onSuccess {
                carregarVideos()
                avisar(R.string.video_publicado)
            }
            .onFailure { avisar(R.string.erro_salvar_video) }
    }

    private fun carregarVideos() {
        videos = armazenamento.listar(eventoId)
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR"))
        val itens = videos.map {
            getString(R.string.item_video, it.autorNome, formato.format(Date(it.criadoEm)))
        }
        adaptador.clear()
        adaptador.addAll(itens)
        textoVazio.visibility = if (videos.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun reproduzir(video: VideoEventoLocal) {
        reprodutor.setVideoPath(armazenamento.caminho(video.id))
        reprodutor.setOnPreparedListener { reprodutor.start() }
        reprodutor.setOnErrorListener { _, _, _ ->
            avisar(R.string.erro_reproduzir_video)
            true
        }
    }

    private fun avisar(mensagem: Int) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        reprodutor.stopPlayback()
        super.onStop()
    }
}
