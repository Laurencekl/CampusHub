package com.laurencekl.campushub

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class AdministracaoActivity : AppCompatActivity() {

    private lateinit var campoTitulo: EditText
    private lateinit var campoDescricao: EditText
    private lateinit var campoData: EditText
    private lateinit var campoHorario: EditText
    private lateinit var campoLocal: EditText
    private lateinit var campoVagas: EditText
    private lateinit var campoCategoria: Spinner
    private lateinit var botaoCadastrar: Button
    private lateinit var progresso: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_administracao)

        campoTitulo = findViewById(R.id.campoTituloEvento)
        campoDescricao = findViewById(R.id.campoDescricaoEvento)
        campoData = findViewById(R.id.campoDataEvento)
        campoHorario = findViewById(R.id.campoHorarioEvento)
        campoLocal = findViewById(R.id.campoLocalEvento)
        campoVagas = findViewById(R.id.campoVagasEvento)
        campoCategoria = findViewById(R.id.campoCategoriaEvento)
        botaoCadastrar = findViewById(R.id.botaoCadastrarEvento)
        progresso = findViewById(R.id.progressoCadastrarEvento)

        val categorias = listOf(
            getString(R.string.categoria_tecnologia),
            getString(R.string.categoria_carreira),
            getString(R.string.categoria_cultura),
            getString(R.string.categoria_esportes)
        )
        campoCategoria.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categorias
        )

        botaoCadastrar.setOnClickListener {
            cadastrarEvento()
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
        val vagas = campoVagas.text.toString().toLongOrNull()

        if (!validarCampos(titulo, descricao, data, horario, local, vagas)) {
            return
        }

        val evento = hashMapOf<String, Any>(
            "titulo" to titulo,
            "descricao" to descricao,
            "data" to data,
            "horario" to horario,
            "local" to local,
            "categoria" to campoCategoria.selectedItem.toString(),
            "limiteVagas" to vagas!!,
            "inscritos" to 0L
        )

        mostrarCarregamento(true)
        FirebaseFirestore.getInstance()
            .collection("eventos")
            .add(evento)
            .addOnSuccessListener {
                mostrarCarregamento(false)
                limparCampos()
                Toast.makeText(this, R.string.evento_cadastrado, Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                mostrarCarregamento(false)
                Toast.makeText(this, R.string.erro_cadastrar_evento, Toast.LENGTH_LONG).show()
            }
    }

    private fun validarCampos(
        titulo: String,
        descricao: String,
        data: String,
        horario: String,
        local: String,
        vagas: Long?
    ): Boolean {
        val campos = listOf(
            campoTitulo to titulo,
            campoDescricao to descricao,
            campoData to data,
            campoHorario to horario,
            campoLocal to local
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

        return true
    }

    private fun mostrarCarregamento(carregando: Boolean) {
        progresso.visibility = if (carregando) View.VISIBLE else View.GONE
        botaoCadastrar.isEnabled = !carregando
    }

    private fun limparCampos() {
        campoTitulo.text.clear()
        campoDescricao.text.clear()
        campoData.text.clear()
        campoHorario.text.clear()
        campoLocal.text.clear()
        campoVagas.text.clear()
        campoCategoria.setSelection(0)
        campoTitulo.requestFocus()
    }
}
