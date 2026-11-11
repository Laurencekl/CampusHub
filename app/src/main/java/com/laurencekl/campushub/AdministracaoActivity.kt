package com.laurencekl.campushub

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Locale

class AdministracaoActivity : AppCompatActivity() {

    private lateinit var campoTitulo: EditText
    private lateinit var campoDescricao: EditText
    private lateinit var campoData: EditText
    private lateinit var campoHorario: EditText
    private lateinit var campoLocal: EditText
    private lateinit var campoEndereco: EditText
    private lateinit var campoVagas: EditText
    private lateinit var campoCategoria: Spinner
    private lateinit var campoPublicar: CheckBox
    private lateinit var seletorEventos: Spinner
    private lateinit var botaoCadastrar: Button
    private lateinit var progresso: ProgressBar
    private val eventosOrganizador = mutableListOf<Pair<String, String>>()
    private var eventoEmEdicao: String? = null
    private var inscritosEventoEmEdicao = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_administracao)

        campoTitulo = findViewById(R.id.campoTituloEvento)
        campoDescricao = findViewById(R.id.campoDescricaoEvento)
        campoData = findViewById(R.id.campoDataEvento)
        campoHorario = findViewById(R.id.campoHorarioEvento)
        campoLocal = findViewById(R.id.campoLocalEvento)
        campoEndereco = findViewById(R.id.campoEnderecoEvento)
        campoVagas = findViewById(R.id.campoVagasEvento)
        campoCategoria = findViewById(R.id.campoCategoriaEvento)
        campoPublicar = findViewById(R.id.campoPublicarEvento)
        seletorEventos = findViewById(R.id.seletorEventosOrganizador)
        botaoCadastrar = findViewById(R.id.botaoCadastrarEvento)
        progresso = findViewById(R.id.progressoCadastrarEvento)

        val categorias = listOf(
            getString(R.string.categoria_tecnologia),
            getString(R.string.categoria_cultura),
            getString(R.string.categoria_musica)
        )
        campoCategoria.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categorias
        )
        carregarEventosDoOrganizador()

        botaoCadastrar.setOnClickListener {
            cadastrarEvento()
        }

        findViewById<Button>(R.id.botaoAlterarStatusEvento).setOnClickListener {
            alterarStatusEvento()
        }

        findViewById<Button>(R.id.botaoEditarEvento).setOnClickListener {
            carregarEventoParaEdicao()
        }

        findViewById<Button>(R.id.botaoVoltarAdministracao).setOnClickListener {
            finish()
        }
    }

    private fun cadastrarEvento() {
        val titulo = campoTitulo.text.toString().trim()
        val descricao = campoDescricao.text.toString().trim()
        val data = campoData.text.toString().trim()
        val horario = campoHorario.text.toString().trim()
        val local = campoLocal.text.toString().trim()
        val endereco = campoEndereco.text.toString().trim()
        val vagas = campoVagas.text.toString().toLongOrNull()
        val usuario = FirebaseAuth.getInstance().currentUser ?: return

        if (!validarCampos(titulo, descricao, data, horario, local, endereco, vagas)) {
            return
        }

        val evento = hashMapOf<String, Any>(
            "titulo" to titulo,
            "descricao" to descricao,
            "data" to data,
            "horario" to horario,
            "local" to local,
            "endereco" to endereco,
            "categoria" to campoCategoria.selectedItem.toString(),
            "limiteVagas" to vagas!!
        )

        val editando = eventoEmEdicao != null
        if (!editando) {
            evento["inscritos"] = 0L
            evento["organizadorId"] = usuario.uid
            evento["organizadorNome"] = usuario.displayName ?: usuario.email.orEmpty()
            evento["status"] = if (campoPublicar.isChecked) "publicado" else "rascunho"
        }

        mostrarCarregamento(true)
        val colecao = FirebaseFirestore.getInstance().collection("eventos")
        val documento = colecao.document(eventoEmEdicao ?: colecao.document().id)
        documento.set(evento, SetOptions.merge())
            .addOnSuccessListener {
                mostrarCarregamento(false)
                limparCampos()
                carregarEventosDoOrganizador()
                val mensagem = if (editando) R.string.evento_editado else R.string.evento_cadastrado
                Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                mostrarCarregamento(false)
                Toast.makeText(this, R.string.erro_cadastrar_evento, Toast.LENGTH_LONG).show()
            }
    }

    private fun carregarEventoParaEdicao() {
        val evento = eventosOrganizador.getOrNull(seletorEventos.selectedItemPosition) ?: return

        FirebaseFirestore.getInstance().collection("eventos").document(evento.first).get()
            .addOnSuccessListener { documento ->
                campoTitulo.setText(documento.getString("titulo"))
                campoDescricao.setText(documento.getString("descricao"))
                campoData.setText(documento.getString("data"))
                campoHorario.setText(documento.getString("horario"))
                campoLocal.setText(documento.getString("local"))
                campoEndereco.setText(documento.getString("endereco"))
                campoVagas.setText(documento.getLong("limiteVagas")?.toString())
                inscritosEventoEmEdicao = documento.getLong("inscritos") ?: 0L

                val categoria = documento.getString("categoria")
                for (posicao in 0 until campoCategoria.count) {
                    if (campoCategoria.getItemAtPosition(posicao).toString() == categoria) {
                        campoCategoria.setSelection(posicao)
                    }
                }

                eventoEmEdicao = documento.id
                campoPublicar.visibility = View.GONE
                botaoCadastrar.setText(R.string.salvar_alteracoes)
                campoTitulo.requestFocus()
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.erro_eventos_organizador, Toast.LENGTH_LONG).show()
            }
    }

    private fun carregarEventosDoOrganizador() {
        val usuario = FirebaseAuth.getInstance().currentUser ?: return

        FirebaseFirestore.getInstance()
            .collection("eventos")
            .whereEqualTo("organizadorId", usuario.uid)
            .get()
            .addOnSuccessListener { documentos ->
                eventosOrganizador.clear()

                val itens = documentos.map { documento ->
                    val status = documento.getString("status") ?: "publicado"
                    eventosOrganizador.add(documento.id to status)
                    getString(
                        R.string.evento_organizador_item,
                        documento.getString("titulo").orEmpty(),
                        status
                    )
                }.ifEmpty { listOf(getString(R.string.nenhum_evento_organizador)) }

                seletorEventos.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    itens
                )
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.erro_eventos_organizador, Toast.LENGTH_LONG).show()
            }
    }

    private fun alterarStatusEvento() {
        val posicao = seletorEventos.selectedItemPosition
        val evento = eventosOrganizador.getOrNull(posicao) ?: return

        val novoStatus = when (evento.second) {
            "rascunho" -> "publicado"
            "publicado" -> "encerrado"
            else -> null
        }

        if (novoStatus == null) {
            Toast.makeText(this, R.string.evento_ja_encerrado, Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseFirestore.getInstance().collection("eventos")
            .document(evento.first)
            .update("status", novoStatus)
            .addOnSuccessListener {
                Toast.makeText(this, R.string.status_evento_atualizado, Toast.LENGTH_SHORT).show()
                carregarEventosDoOrganizador()
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.erro_status_evento, Toast.LENGTH_LONG).show()
            }
    }

    private fun validarCampos(
        titulo: String,
        descricao: String,
        data: String,
        horario: String,
        local: String,
        endereco: String,
        vagas: Long?
    ): Boolean {
        val campos = listOf(
            campoTitulo to titulo,
            campoDescricao to descricao,
            campoData to data,
            campoHorario to horario,
            campoLocal to local,
            campoEndereco to endereco
        )

        campos.forEach { (campo, valor) ->
            if (valor.isBlank()) {
                campo.error = getString(R.string.campo_obrigatorio)
                campo.requestFocus()
                return false
            }
        }

        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR"))
        formato.isLenient = false

        try {
            formato.parse("$data $horario")
        } catch (_: Exception) {
            campoData.error = getString(R.string.data_horario_invalido)
            campoData.requestFocus()
            return false
        }

        if (vagas == null || vagas <= 0) {
            campoVagas.error = getString(R.string.vagas_invalidas)
            campoVagas.requestFocus()
            return false
        }

        if (vagas < inscritosEventoEmEdicao) {
            campoVagas.error = getString(R.string.limite_menor_inscritos)
            campoVagas.requestFocus()
            return false
        }

        return true
    }

    private fun mostrarCarregamento(carregando: Boolean) {
        progresso.visibility = if (carregando) View.VISIBLE else View.GONE
        botaoCadastrar.isEnabled = !carregando
    }

    private fun limparCampos() {
        eventoEmEdicao = null
        inscritosEventoEmEdicao = 0L
        campoTitulo.text.clear()
        campoDescricao.text.clear()
        campoData.text.clear()
        campoHorario.text.clear()
        campoLocal.text.clear()
        campoEndereco.text.clear()
        campoVagas.text.clear()
        campoCategoria.setSelection(0)
        campoPublicar.isChecked = false
        campoPublicar.visibility = View.VISIBLE
        botaoCadastrar.setText(R.string.cadastrar_evento)
        campoTitulo.requestFocus()
    }
}
