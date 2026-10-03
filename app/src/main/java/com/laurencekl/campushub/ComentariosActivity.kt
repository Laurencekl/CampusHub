package com.laurencekl.campushub

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ComentariosActivity : AppCompatActivity() {

    private val comentarios = mutableListOf<Comentario>()
    private lateinit var adaptador: ComentarioAdapter
    private lateinit var textoStatus: TextView
    private lateinit var campoComentario: EditText
    private lateinit var eventoId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comentarios)

        eventoId = intent.getStringExtra("eventoId").orEmpty()
        val tituloEvento = intent.getStringExtra("tituloEvento").orEmpty()
        val usuario = FirebaseAuth.getInstance().currentUser

        findViewById<TextView>(R.id.textoTituloEventoComentarios).text = tituloEvento
        textoStatus = findViewById(R.id.textoStatusComentarios)
        campoComentario = findViewById(R.id.campoComentario)

        adaptador = ComentarioAdapter(
            this,
            comentarios,
            usuario?.uid.orEmpty(),
            aoEditar = { comentario -> abrirEdicao(comentario) },
            aoExcluir = { comentario -> confirmarExclusao(comentario) }
        )
        findViewById<ListView>(R.id.listaComentarios).adapter = adaptador

        findViewById<Button>(R.id.botaoPublicarComentario).setOnClickListener {
            publicarComentario()
        }

        findViewById<Button>(R.id.botaoVoltarComentarios).setOnClickListener {
            finish()
        }

        carregarComentarios()
    }

    private fun referenciaComentarios() = FirebaseFirestore.getInstance()
        .collection("eventos")
        .document(eventoId)
        .collection("comentarios")

    private fun carregarComentarios() {
        textoStatus.text = getString(R.string.carregando_comentarios)

        referenciaComentarios()
            .orderBy("criadoEm", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documentos ->
                comentarios.clear()
                comentarios.addAll(
                    documentos.map { documento ->
                        documento.toObject(Comentario::class.java).copy(id = documento.id)
                    }
                )

                textoStatus.text = if (comentarios.isEmpty()) {
                    getString(R.string.nenhum_comentario)
                } else {
                    ""
                }
                adaptador.notifyDataSetChanged()
            }
            .addOnFailureListener {
                textoStatus.text = getString(R.string.erro_comentarios)
            }
    }

    private fun publicarComentario() {
        val usuario = FirebaseAuth.getInstance().currentUser
        val texto = campoComentario.text.toString().trim()

        if (usuario == null) {
            Toast.makeText(this, R.string.usuario_nao_autenticado, Toast.LENGTH_SHORT).show()
            return
        }

        if (texto.isEmpty()) {
            campoComentario.error = getString(R.string.comentario_vazio)
            return
        }

        val autorNome = usuario.displayName
            ?: usuario.email
            ?: getString(R.string.usuario)

        val dados = hashMapOf<String, Any>(
            "texto" to texto,
            "autorId" to usuario.uid,
            "autorNome" to autorNome,
            "criadoEm" to FieldValue.serverTimestamp()
        )

        referenciaComentarios().add(dados)
            .addOnSuccessListener {
                campoComentario.text.clear()
                Toast.makeText(this, R.string.comentario_publicado, Toast.LENGTH_SHORT).show()
                carregarComentarios()
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.erro_salvar_comentario, Toast.LENGTH_SHORT).show()
            }
    }

    private fun abrirEdicao(comentario: Comentario) {
        val campoEdicao = EditText(this)
        campoEdicao.setText(comentario.texto)
        campoEdicao.setSelection(comentario.texto.length)

        AlertDialog.Builder(this)
            .setTitle(R.string.editar_comentario_titulo)
            .setView(campoEdicao)
            .setPositiveButton(R.string.salvar) { _, _ ->
                val novoTexto = campoEdicao.text.toString().trim()

                if (novoTexto.isNotEmpty()) {
                    referenciaComentarios().document(comentario.id)
                        .update(
                            mapOf(
                                "texto" to novoTexto,
                                "editadoEm" to FieldValue.serverTimestamp()
                            )
                        )
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                R.string.comentario_editado,
                                Toast.LENGTH_SHORT
                            ).show()
                            carregarComentarios()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this,
                                R.string.erro_salvar_comentario,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    private fun confirmarExclusao(comentario: Comentario) {
        AlertDialog.Builder(this)
            .setTitle(R.string.excluir_comentario_titulo)
            .setMessage(R.string.excluir_comentario_mensagem)
            .setPositiveButton(R.string.sim_excluir) { _, _ ->
                referenciaComentarios().document(comentario.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            R.string.comentario_excluido,
                            Toast.LENGTH_SHORT
                        ).show()
                        carregarComentarios()
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this,
                            R.string.erro_excluir_comentario,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }
}
