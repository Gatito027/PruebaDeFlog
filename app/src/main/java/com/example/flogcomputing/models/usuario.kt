package com.example.flogcomputing.models

data class Usuario(
    val id: Int = 0,
    val nombre: String,
    val edad: Int,
    val genero: String,
    val contacto: Int,
    //val fechaRegistro: LocalDateTime
)