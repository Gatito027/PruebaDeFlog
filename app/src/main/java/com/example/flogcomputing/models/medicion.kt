package com.example.flogcomputing.models

import java.time.LocalDateTime

data class medicion (
    val id: Int =0,
    val usuarioId: Int,
    val horaFecha: LocalDateTime,
    val frecuenciaCardiaca: Double,
    val variabilidadFrecuenciaCardiaca: Double,
    val saturacionOxigeno: Double,
    val actividadFisica:Double
)