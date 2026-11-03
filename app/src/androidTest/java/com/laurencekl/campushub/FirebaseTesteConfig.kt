package com.laurencekl.campushub

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings

object FirebaseTesteConfig {

    private var configurado = false

    @Synchronized
    fun configurar() {
        if (configurado) return

        FirebaseAuth.getInstance().useEmulator("127.0.0.1", 9099)

        val bancoDados = FirebaseFirestore.getInstance()
        bancoDados.useEmulator("127.0.0.1", 8080)
        bancoDados.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
            .build()

        configurado = true
    }
}
