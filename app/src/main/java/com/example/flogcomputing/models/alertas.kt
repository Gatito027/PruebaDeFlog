package com.example.flogcomputing.models

import java.time.LocalDateTime

data class alertas(
    val id: Int = 0,
    val usuarioId: Int,
    val FechaHora: LocalDateTime,
    val tipoAlerta: String,
    val estado: String
    )