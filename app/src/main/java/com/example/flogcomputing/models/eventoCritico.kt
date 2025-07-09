package com.example.flogcomputing.models

import java.time.LocalDateTime

data class eventoCritico (
    val id: Int=0,
    val usuarioId:Int,
    val fechaHora: LocalDateTime,
    val tipoEvento: String,
    val ubicacion: String,
    val respuestaUsu: String
)