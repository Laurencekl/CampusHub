package com.laurencekl.campushub

import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.anything
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(AndroidJUnit4::class)
class CampusHubE2ETest {

    companion object {
        private const val SENHA = "123456"
        private const val TITULO_EVENTO = "Evento E2E CampusHub"

        @BeforeClass
        @JvmStatic
        fun configurarFirebase() {
            FirebaseTesteConfig.configurar()
        }
    }

    @Before
    fun prepararTeste() {
        FirebaseAuth.getInstance().signOut()
    }

    @After
    fun finalizarTeste() {
        FirebaseAuth.getInstance().signOut()
    }

    @Test
    fun usuarioCriaContaSaiEEntraNovamente() {
        val email = emailUnico("cadastro")
        val tela = ActivityScenario.launch(LoginActivity::class.java)

        try {
            garantirCampusHubEmPrimeiroPlano(tela)
            onView(withId(R.id.botaoIrParaCadastro)).perform(click())
            onView(withId(R.id.editNome)).perform(replaceText("Aluno E2E"))
            onView(withId(R.id.editEmail)).perform(replaceText(email))
            onView(withId(R.id.editSenha)).perform(replaceText(SENHA))
            onView(withId(R.id.editConfirmarSenha)).perform(
                replaceText(SENHA),
                closeSoftKeyboard()
            )
            onView(withId(R.id.botaoCadastrar)).perform(scrollTo(), click())

            esperarAte {
                onView(withId(R.id.textoEmailUsuario))
                    .check(matches(withText(email)))
            }

            onView(withId(R.id.botaoSair)).perform(scrollTo(), click())
            onView(withText(R.string.sair)).inRoot(isDialog()).perform(click())

            esperarAte {
                onView(withId(R.id.editEmailLogin)).check(matches(isDisplayed()))
            }
            onView(withId(R.id.editEmailLogin)).perform(replaceText(email))
            onView(withId(R.id.editSenhaLogin)).perform(
                replaceText(SENHA),
                closeSoftKeyboard()
            )
            onView(withId(R.id.botaoEntrar)).perform(click())

            esperarAte {
                onView(withId(R.id.textoEmailUsuario))
                    .check(matches(withText(email)))
            }
        } finally {
            tela.close()
        }
    }

    @Test
    fun usuarioEntraSeInscreveEConsultaMeusEventos() {
        val email = criarUsuarioParaLogin()
        val tela = ActivityScenario.launch(LoginActivity::class.java)

        try {
            garantirCampusHubEmPrimeiroPlano(tela)
            onView(withId(R.id.editEmailLogin)).perform(replaceText(email))
            onView(withId(R.id.editSenhaLogin)).perform(
                replaceText(SENHA),
                closeSoftKeyboard()
            )
            onView(withId(R.id.botaoEntrar)).perform(click())

            esperarAte {
                onView(withId(R.id.botaoEventos)).check(matches(isDisplayed()))
            }
            onView(withId(R.id.botaoEventos)).perform(scrollTo(), click())

            esperarAte {
                onView(withText(TITULO_EVENTO)).check(matches(isDisplayed()))
            }
            onData(anything())
                .inAdapterView(withId(R.id.listaEventos))
                .atPosition(0)
                .perform(click())

            esperarAte {
                onView(withId(R.id.textoTituloDetalhes))
                    .check(matches(withText(TITULO_EVENTO)))
                onView(withId(R.id.botaoInscrever)).perform(scrollTo())
                onView(
                    allOf(
                        withId(R.id.botaoInscrever),
                        withText(R.string.inscrever_evento),
                        isEnabled()
                    )
                ).check(matches(isDisplayed()))
            }
            onView(withId(R.id.botaoInscrever)).perform(scrollTo(), click())

            esperarAte {
                onView(withId(R.id.botaoInscrever))
                    .check(matches(withText(R.string.cancelar_inscricao)))
            }
            onView(withId(R.id.botaoVoltarDetalhes)).perform(scrollTo(), click())
            onView(withId(R.id.botaoVoltarEventos)).perform(click())
            onView(withId(R.id.botaoMeusEventos)).perform(scrollTo(), click())

            esperarAte {
                onView(withText(TITULO_EVENTO)).check(matches(isDisplayed()))
            }
        } finally {
            tela.close()
        }
    }

    private fun criarUsuarioParaLogin(): String {
        val email = emailUnico("inscricao")
        val resultado = Tasks.await(
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, SENHA),
            15,
            TimeUnit.SECONDS
        )
        assertNotNull(resultado.user)
        FirebaseAuth.getInstance().signOut()
        return email
    }

    private fun emailUnico(tipo: String): String {
        return "$tipo-${System.nanoTime()}@campushub.test"
    }

    private fun garantirCampusHubEmPrimeiroPlano(
        tela: ActivityScenario<LoginActivity>
    ) {
        val dispositivo = UiDevice.getInstance(
            InstrumentationRegistry.getInstrumentation()
        )

        SystemClock.sleep(1_000)
        if (dispositivo.currentPackageName != "com.laurencekl.campushub") {
            dispositivo.pressBack()
            SystemClock.sleep(1_000)
            tela.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)
        }
    }

    private fun esperarAte(verificacao: () -> Unit) {
        var ultimoErro: Throwable? = null

        repeat(40) {
            try {
                verificacao()
                return
            } catch (erro: Throwable) {
                ultimoErro = erro
                SystemClock.sleep(250)
            }
        }

        throw AssertionError("A tela esperada nao apareceu.", ultimoErro)
    }
}
