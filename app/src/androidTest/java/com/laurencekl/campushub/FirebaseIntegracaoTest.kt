package com.laurencekl.campushub

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(AndroidJUnit4::class)
class FirebaseIntegracaoTest {

    companion object {
        private lateinit var autenticacao: FirebaseAuth
        private lateinit var bancoDados: FirebaseFirestore

        @BeforeClass
        @JvmStatic
        fun configurarEmuladores() {
            FirebaseTesteConfig.configurar()
            autenticacao = FirebaseAuth.getInstance()
            bancoDados = FirebaseFirestore.getInstance()
        }
    }

    @After
    fun sairDaConta() {
        autenticacao.signOut()
    }

    @Test
    fun criaContaNoFirebaseAuth() {
        val usuario = criarUsuario()

        assertNotNull(usuario.uid)
        assertTrue(usuario.email!!.endsWith("@campushub.test"))
    }

    @Test
    fun fazLoginComUsuarioCriado() {
        val usuarioCriado = criarUsuario()
        val email = usuarioCriado.email!!
        autenticacao.signOut()

        val resultado = aguardar(
            autenticacao.signInWithEmailAndPassword(email, "123456")
        )

        assertEquals(usuarioCriado.uid, resultado.user?.uid)
    }

    @Test
    fun salvaFavoritoNoFirestore() {
        val usuario = criarUsuario()
        val favorito = referenciaUsuario(usuario, "favoritos").document("evento-favorito")

        aguardar(favorito.set(mapOf("titulo" to "Feira de Tecnologia")))

        assertTrue(aguardar(favorito.get()).exists())
    }

    @Test
    fun leFavoritoSalvo() {
        val usuario = criarUsuario()
        val favorito = referenciaUsuario(usuario, "favoritos").document("evento-leitura")
        aguardar(favorito.set(mapOf("titulo" to "Semana Acadêmica")))

        val documento = aguardar(favorito.get())

        assertEquals("Semana Acadêmica", documento.getString("titulo"))
    }

    @Test
    fun removeFavorito() {
        val usuario = criarUsuario()
        val favorito = referenciaUsuario(usuario, "favoritos").document("evento-removido")
        aguardar(favorito.set(mapOf("titulo" to "Evento removido")))

        aguardar(favorito.delete())

        assertFalse(aguardar(favorito.get()).exists())
    }

    @Test
    fun salvaInscricaoNoFirestore() {
        val usuario = criarUsuario()
        val inscricao = referenciaUsuario(usuario, "inscricoes").document("evento-inscricao")

        aguardar(inscricao.set(mapOf("eventoId" to "evento-inscricao")))

        assertTrue(aguardar(inscricao.get()).exists())
    }

    @Test
    fun leInscricaoSalva() {
        val usuario = criarUsuario()
        val inscricao = referenciaUsuario(usuario, "inscricoes").document("evento-leitura")
        aguardar(inscricao.set(mapOf("eventoId" to "evento-leitura")))

        val documento = aguardar(inscricao.get())

        assertEquals("evento-leitura", documento.getString("eventoId"))
    }

    @Test
    fun publicaComentarioNoEvento() {
        val usuario = criarUsuario()
        val comentario = referenciaComentario("evento-comentario")

        aguardar(
            comentario.set(
                mapOf(
                    "texto" to "Evento muito interessante!",
                    "autorId" to usuario.uid,
                    "autorNome" to "Aluno de teste",
                    "criadoEm" to FieldValue.serverTimestamp()
                )
            )
        )

        assertEquals(
            "Evento muito interessante!",
            aguardar(comentario.get()).getString("texto")
        )
    }

    @Test
    fun editaComentarioDoProprioUsuario() {
        val usuario = criarUsuario()
        val comentario = referenciaComentario("evento-edicao")
        aguardar(
            comentario.set(
                mapOf(
                    "texto" to "Texto original",
                    "autorId" to usuario.uid,
                    "autorNome" to "Aluno de teste",
                    "criadoEm" to FieldValue.serverTimestamp()
                )
            )
        )

        aguardar(
            comentario.update(
                mapOf(
                    "texto" to "Texto editado",
                    "editadoEm" to FieldValue.serverTimestamp()
                )
            )
        )

        assertEquals("Texto editado", aguardar(comentario.get()).getString("texto"))
    }

    @Test
    fun salvaEAtualizaAvaliacaoDeAlunoInscrito() {
        val usuario = criarUsuario()
        val eventoId = "evento-avaliacao"
        val inscricao = referenciaUsuario(usuario, "inscricoes").document(eventoId)
        val avaliacao = bancoDados.collection("eventos")
            .document(eventoId)
            .collection("avaliacoes")
            .document(usuario.uid)
        aguardar(inscricao.set(mapOf("eventoId" to eventoId)))

        aguardar(
            avaliacao.set(
                mapOf(
                    "usuarioId" to usuario.uid,
                    "nota" to 4,
                    "atualizadoEm" to FieldValue.serverTimestamp()
                )
            )
        )
        aguardar(avaliacao.update("nota", 5))

        assertEquals(5, aguardar(avaliacao.get()).getLong("nota")?.toInt())
    }

    private fun criarUsuario(): FirebaseUser {
        autenticacao.signOut()
        val email = "teste-${System.nanoTime()}@campushub.test"
        val resultado = aguardar(
            autenticacao.createUserWithEmailAndPassword(email, "123456")
        )
        return requireNotNull(resultado.user)
    }

    private fun referenciaUsuario(
        usuario: FirebaseUser,
        colecao: String
    ) = bancoDados.collection("usuarios")
        .document(usuario.uid)
        .collection(colecao)

    private fun referenciaComentario(eventoId: String): DocumentReference {
        return bancoDados.collection("eventos")
            .document(eventoId)
            .collection("comentarios")
            .document()
    }

    private fun <T> aguardar(tarefa: Task<T>): T {
        return Tasks.await(tarefa, 15, TimeUnit.SECONDS)
    }
}
