package com.laurencekl.campushub

import com.google.firebase.Timestamp

data class Comentario(
    val id: String = "",
    val texto: String = "",
    val autorId: String = "",
    val autorNome: String = "",
    val criadoEm: Timestamp? = null
)
