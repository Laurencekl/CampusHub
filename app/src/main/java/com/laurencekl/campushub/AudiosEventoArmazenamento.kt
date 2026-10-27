package com.laurencekl.campushub

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException
import java.util.UUID

data class AudioEventoLocal(
    val id: String,
    val eventoId: String,
    val autorId: String,
    val autorNome: String,
    val criadoEm: Long
)

class AudiosEventoArmazenamento(private val contexto: Context) {
    private val preferencias = contexto.getSharedPreferences("audios_eventos", Context.MODE_PRIVATE)
    private val pasta = File(contexto.filesDir, "audios_eventos")

    fun listar(eventoId: String): List<AudioEventoLocal> {
        val ids = preferencias.getStringSet("ids_$eventoId", emptySet()).orEmpty()

        return ids.mapNotNull { id ->
            val autorId = preferencias.getString("${id}_autorId", null) ?: return@mapNotNull null
            val autorNome = preferencias.getString("${id}_autorNome", null) ?: return@mapNotNull null
            val criadoEm = preferencias.getLong("${id}_criadoEm", 0)

            AudioEventoLocal(id, eventoId, autorId, autorNome, criadoEm)
        }.filter { arquivo(it.id).isFile }
            .sortedByDescending { it.criadoEm }
    }

    fun salvar(uri: Uri, eventoId: String, autorId: String, autorNome: String) {
        require(eventoId.isNotBlank() && autorId.isNotBlank())
        if (!pasta.isDirectory && !pasta.mkdirs()) throw IOException("Pasta indisponível")

        val id = UUID.randomUUID().toString()
        val destino = arquivo(id)
        val chaveIds = "ids_$eventoId"
        val ids = preferencias.getStringSet(chaveIds, emptySet()).orEmpty().toMutableSet()

        try {
            contexto.contentResolver.openInputStream(uri).use { entrada ->
                if (entrada == null) throw IOException("Áudio inválido")
                destino.outputStream().use { saida -> entrada.copyTo(saida) }
            }
            if (destino.length() == 0L) throw IOException("Áudio vazio")

            ids.add(id)
            val salvo = preferencias.edit()
                .putStringSet(chaveIds, ids)
                .putString("${id}_autorId", autorId)
                .putString("${id}_autorNome", autorNome)
                .putLong("${id}_criadoEm", System.currentTimeMillis())
                .commit()

            if (!salvo) throw IOException("Não foi possível salvar os dados")
        } catch (erro: Exception) {
            destino.delete()
            throw erro
        }
    }

    fun caminho(id: String): String = arquivo(id).path

    fun excluir(audio: AudioEventoLocal) {
        val chaveIds = "ids_${audio.eventoId}"
        val ids = preferencias.getStringSet(chaveIds, emptySet()).orEmpty().toMutableSet()
        ids.remove(audio.id)

        preferencias.edit()
            .putStringSet(chaveIds, ids)
            .remove("${audio.id}_autorId")
            .remove("${audio.id}_autorNome")
            .remove("${audio.id}_criadoEm")
            .apply()
        arquivo(audio.id).delete()
    }

    private fun arquivo(id: String) = File(pasta, "${UUID.fromString(id)}.m4a")
}
