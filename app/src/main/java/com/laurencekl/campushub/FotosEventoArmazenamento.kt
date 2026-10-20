package com.laurencekl.campushub

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.util.AtomicFile
import androidx.exifinterface.media.ExifInterface
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlin.math.max

data class FotoEventoLocal(
    val id: String,
    val eventoId: String,
    val autorId: String,
    val autorNome: String,
    val criadoEm: Long
)

class FotosEventoArmazenamento(private val contexto: Context) {
    private val pasta = File(contexto.filesDir, "fotos_eventos")

    fun listar(eventoId: String): List<FotoEventoLocal> = pasta.listFiles()
        .orEmpty()
        .filter { it.extension == "json" }
        .mapNotNull { arquivo -> runCatching { ler(arquivo) }.getOrNull() }
        .filter { it.eventoId == eventoId && imagem(it.id).isFile }
        .sortedByDescending { it.criadoEm }

    fun salvar(uri: Uri, eventoId: String, autorId: String, autorNome: String) {
        require(eventoId.isNotBlank() && autorId.isNotBlank())
        if (!pasta.isDirectory && !pasta.mkdirs()) throw IOException("Pasta indisponível")
        val foto = FotoEventoLocal(
            UUID.randomUUID().toString(), eventoId, autorId, autorNome, System.currentTimeMillis()
        )
        val bitmap = decodificar(uri)
        val arquivo = imagem(foto.id)
        try {
            arquivo.outputStream().use {
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it)) {
                    throw IOException("Imagem inválida")
                }
            }
            val dados = JSONObject()
                .put("id", foto.id)
                .put("eventoId", foto.eventoId)
                .put("autorId", foto.autorId)
                .put("autorNome", foto.autorNome)
                .put("criadoEm", foto.criadoEm)
            val registro = AtomicFile(File(pasta, "${foto.id}.json"))
            val saida = registro.startWrite()
            try {
                saida.write(dados.toString().toByteArray(Charsets.UTF_8))
                registro.finishWrite(saida)
            } catch (erro: Exception) {
                registro.failWrite(saida)
                throw erro
            }
        } catch (erro: Exception) {
            arquivo.delete()
            throw erro
        } finally {
            bitmap.recycle()
        }
    }

    fun excluir(id: String, usuarioId: String) {
        val arquivo = File(pasta, "${UUID.fromString(id)}.json")
        val foto = ler(arquivo)
        require(usuarioId.isNotBlank() && foto.autorId == usuarioId)
        if (!arquivo.delete()) throw IOException("Não foi possível excluir o registro")
        imagem(id).delete()
    }

    fun carregarMiniatura(id: String): Bitmap? {
        val arquivo = imagem(id)
        val tamanho = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(arquivo.path, tamanho)
        val opcoes = BitmapFactory.Options().apply {
            inSampleSize = amostragem(tamanho.outWidth, tamanho.outHeight, 640)
        }
        return BitmapFactory.decodeFile(arquivo.path, opcoes)
    }

    private fun ler(arquivo: File): FotoEventoLocal {
        val dados = JSONObject(AtomicFile(arquivo).readFully().toString(Charsets.UTF_8))
        val id = dados.getString("id")
        require(UUID.fromString(id).toString() == arquivo.nameWithoutExtension)
        return FotoEventoLocal(
            id, dados.getString("eventoId"), dados.getString("autorId"),
            dados.getString("autorNome"), dados.getLong("criadoEm")
        )
    }

    private fun imagem(id: String) = File(pasta, "${UUID.fromString(id)}.jpg")

    private fun decodificar(uri: Uri): Bitmap {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val fonte = ImageDecoder.createSource(contexto.contentResolver, uri)
            return ImageDecoder.decodeBitmap(fonte) { decoder, info, _ ->
                val proporcao = 1280f / max(info.size.width, info.size.height)
                if (proporcao < 1f) {
                    decoder.setTargetSize(
                        (info.size.width * proporcao).toInt().coerceAtLeast(1),
                        (info.size.height * proporcao).toInt().coerceAtLeast(1)
                    )
                }
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        }
        val tamanho = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contexto.contentResolver.openInputStream(uri).use {
            BitmapFactory.decodeStream(it, null, tamanho)
        }
        if (tamanho.outWidth <= 0 || tamanho.outHeight <= 0) throw IOException("Imagem inválida")
        val opcoes = BitmapFactory.Options().apply {
            inSampleSize = amostragem(tamanho.outWidth, tamanho.outHeight, 1280)
        }
        val bitmap = contexto.contentResolver.openInputStream(uri).use {
            BitmapFactory.decodeStream(it, null, opcoes)
        } ?: throw IOException("Imagem inválida")
        val orientacao = contexto.contentResolver.openInputStream(uri).use {
            if (it == null) ExifInterface.ORIENTATION_NORMAL else runCatching {
                ExifInterface(it).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
                )
            }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
        }
        val matriz = Matrix().apply {
            when (orientacao) {
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> setScale(-1f, 1f)
                ExifInterface.ORIENTATION_ROTATE_180 -> setRotate(180f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> setScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> { setRotate(90f); postScale(-1f, 1f) }
                ExifInterface.ORIENTATION_ROTATE_90 -> setRotate(90f)
                ExifInterface.ORIENTATION_TRANSVERSE -> { setRotate(270f); postScale(-1f, 1f) }
                ExifInterface.ORIENTATION_ROTATE_270 -> setRotate(270f)
            }
        }
        if (matriz.isIdentity) return bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matriz, true)
            .also { if (it !== bitmap) bitmap.recycle() }
    }

    private fun amostragem(largura: Int, altura: Int, limite: Int): Int {
        var valor = 1
        while (max(largura, altura) / valor > limite) valor *= 2
        return valor
    }
}
