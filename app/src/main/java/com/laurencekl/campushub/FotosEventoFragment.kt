package com.laurencekl.campushub

import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class FotosEventoFragment : Fragment(R.layout.fragment_fotos_evento) {
    private val executor = Executors.newSingleThreadExecutor()
    private val principal = Handler(Looper.getMainLooper())
    private lateinit var armazenamento: FotosEventoArmazenamento
    private var autorSelecao: String? = null
    private val eventoId get() = requireArguments().getString("eventoId").orEmpty()
    private val seletor = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) publicar(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        armazenamento = FotosEventoArmazenamento(requireContext().applicationContext)
        autorSelecao = savedInstanceState?.getString("autorSelecao")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<RecyclerView>(R.id.listaFotosEvento).layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        view.findViewById<Button>(R.id.botaoCompartilharFoto).setOnClickListener {
            val usuario = FirebaseAuth.getInstance().currentUser
            if (usuario == null || eventoId.isBlank()) {
                avisar(R.string.login_para_foto)
            } else {
                autorSelecao = usuario.uid
                try {
                    seletor.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                } catch (_: ActivityNotFoundException) {
                    avisar(R.string.erro_abrir_galeria)
                }
            }
        }
        executar(R.string.carregando_fotos, R.string.erro_carregar_fotos) { }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("autorSelecao", autorSelecao)
        super.onSaveInstanceState(outState)
    }

    private fun publicar(uri: Uri) {
        val usuario = FirebaseAuth.getInstance().currentUser
        if (usuario == null || usuario.uid != autorSelecao) {
            avisar(R.string.login_para_foto)
            return
        }
        val nome = usuario.displayName?.takeIf { it.isNotBlank() }
            ?: usuario.email ?: getString(R.string.usuario)
        executar(R.string.salvando_foto, R.string.erro_salvar_foto, R.string.foto_publicada) {
            check(FirebaseAuth.getInstance().currentUser?.uid == usuario.uid)
            armazenamento.salvar(uri, eventoId, usuario.uid, nome)
        }
    }

    private fun confirmarExclusao(foto: FotoEventoLocal) {
        val usuarioId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (foto.autorId != usuarioId) return
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.excluir_foto_titulo)
            .setMessage(R.string.excluir_foto_mensagem)
            .setPositiveButton(R.string.sim_excluir) { _, _ ->
                executar(R.string.excluindo_foto, R.string.erro_excluir_foto, R.string.foto_excluida) {
                    check(FirebaseAuth.getInstance().currentUser?.uid == usuarioId)
                    armazenamento.excluir(foto.id, usuarioId)
                }
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    private fun executar(estado: Int, erro: Int, sucesso: Int? = null, acao: () -> Unit) {
        val tela = view ?: return
        val botao = tela.findViewById<Button>(R.id.botaoCompartilharFoto)
        val status = tela.findViewById<TextView>(R.id.textoStatusFotos)
        val lista = tela.findViewById<RecyclerView>(R.id.listaFotosEvento)
        botao.isEnabled = false
        status.visibility = View.VISIBLE
        status.setText(estado)
        lista.isEnabled = false
        executor.execute {
            val resultado = runCatching {
                acao()
                armazenamento.listar(eventoId)
            }
            principal.post {
                if (view !== tela) return@post
                botao.isEnabled = true
                lista.isEnabled = true
                resultado.onSuccess { fotos ->
                    status.setText(R.string.nenhuma_foto_evento)
                    status.visibility = if (fotos.isEmpty()) View.VISIBLE else View.GONE
                    lista.visibility = if (fotos.isEmpty()) View.GONE else View.VISIBLE
                    lista.adapter = FotosAdapter(fotos)
                    if (sucesso != null) avisar(sucesso)
                }.onFailure {
                    status.setText(erro)
                }
            }
        }
    }

    private inner class FotosAdapter(private val fotos: List<FotoEventoLocal>) :
        RecyclerView.Adapter<FotoViewHolder>() {
        override fun getItemCount() = fotos.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = FotoViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_foto_evento, parent, false)
        )

        override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
            val foto = fotos[position]
            holder.id = foto.id
            val imagem = holder.itemView.findViewById<ImageView>(R.id.imagemFotoEvento)
            imagem.setImageDrawable(null)
            imagem.contentDescription = getString(R.string.foto_participante_descricao, foto.autorNome)
            holder.itemView.findViewById<TextView>(R.id.textoAutorFoto).text = foto.autorNome
            holder.itemView.findViewById<TextView>(R.id.textoDataFoto).text =
                SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.forLanguageTag("pt-BR"))
                    .format(Date(foto.criadoEm))
            holder.itemView.findViewById<Button>(R.id.botaoExcluirFoto).apply {
                visibility = if (foto.autorId == FirebaseAuth.getInstance().currentUser?.uid) {
                    View.VISIBLE
                } else View.GONE
                setOnClickListener { confirmarExclusao(foto) }
            }
            executor.execute {
                val bitmap = runCatching { armazenamento.carregarMiniatura(foto.id) }.getOrNull()
                principal.post {
                    if (view != null && holder.id == foto.id) imagem.setImageBitmap(bitmap)
                }
            }
        }

        override fun onViewRecycled(holder: FotoViewHolder) {
            holder.id = null
            holder.itemView.findViewById<ImageView>(R.id.imagemFotoEvento).setImageDrawable(null)
            super.onViewRecycled(holder)
        }
    }

    private class FotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var id: String? = null
    }

    private fun avisar(mensagem: Int) {
        context?.let { Toast.makeText(it, mensagem, Toast.LENGTH_SHORT).show() }
    }

    override fun onDestroyView() {
        view?.findViewById<RecyclerView>(R.id.listaFotosEvento)?.adapter = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        executor.shutdown()
        super.onDestroy()
    }

    companion object {
        fun novaInstancia(eventoId: String) = FotosEventoFragment().apply {
            arguments = Bundle().apply { putString("eventoId", eventoId) }
        }
    }
}
