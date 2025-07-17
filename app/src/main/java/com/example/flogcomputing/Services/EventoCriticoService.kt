package com.example.flogcomputing.Services

import android.content.Context
import android.annotation.SuppressLint
import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.flogcomputing.dataBase.EventosCriticosDataBaseHelper
import com.example.flogcomputing.dataBase.MedicionesDataBaseHelper
import com.example.flogcomputing.models.eventoCritico
import com.example.flogcomputing.models.medicion
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.LocalDateTime
import kotlin.coroutines.resume

class LocationHelper(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(): Location? = suspendCancellableCoroutine { cont ->
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location -> cont.resume(location) }
            .addOnFailureListener { cont.resume(null) }
    }
}

class EventoCriticoService (private val context: Context) {
    private lateinit var dbHelper: EventosCriticosDataBaseHelper
    private lateinit var dbHistorial: MedicionesDataBaseHelper
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun Monitorizarparametros(medicion: medicion) {
        dbHelper = EventosCriticosDataBaseHelper(context)
        dbHistorial = MedicionesDataBaseHelper(context)
        val Historial4Hrs = dbHistorial.getMedicionesUltimas4Horas()
        var sinMovimiento = 0
        var enviarDatos : Boolean = false
        var data = eventoCritico(
            usuarioId = medicion.usuarioId,
            fechaHora = LocalDateTime.now(),
            tipoEvento = "",
            ubicacion = "",
            respuestaUsu = "")
        val locationHelper = LocationHelper(context)
        val location = locationHelper.getLastKnownLocation()
        val ubicacionStr = location?.let { "${it.latitude}, ${it.longitude}" } ?: "No disponible"
        if (Historial4Hrs.isNotEmpty()) {
            // Iterar sobre cada elemento del historial
            for (registro in Historial4Hrs) {
                // Verificar si el registro tiene el campo actividadFisica y si es igual a 0
                if (registro.actividadFisica.toInt() == 0) {
                    sinMovimiento++
                }
            }
        }
            if (Historial4Hrs.size == sinMovimiento) {
                data = eventoCritico(
                    usuarioId = medicion.usuarioId,
                    fechaHora = LocalDateTime.now(),
                    tipoEvento = "No se detecto movimiento",
                    ubicacion = ubicacionStr,
                    respuestaUsu = "Mas de 4 horas sin movimiento"
                )
                enviarDatos = true
            }
        if (medicion.frecuenciaCardiaca >= 160 || medicion.frecuenciaCardiaca <= 40) {
            data = eventoCritico(
                usuarioId = medicion.usuarioId,
                fechaHora = LocalDateTime.now(),
                tipoEvento = "Frecuencia Cardiaca anormal",
                ubicacion = ubicacionStr,
                respuestaUsu = "Frecuencia cardiaca: ${medicion.frecuenciaCardiaca}"
            )
            enviarDatos=true
        }
        if(medicion.saturacionOxigeno <= 80){
            data = eventoCritico(
                usuarioId = medicion.usuarioId,
                fechaHora = LocalDateTime.now(),
                tipoEvento = "Saturacion de oxigeno baja",
                ubicacion = ubicacionStr,
                respuestaUsu = "Saturacion de oxigeno: ${medicion.saturacionOxigeno}"
            )
            enviarDatos=true
        }
        if(medicion.variabilidadFrecuenciaCardiaca <= 0.4 ){
            data = eventoCritico(
                usuarioId = medicion.usuarioId,
                fechaHora = LocalDateTime.now(),
                tipoEvento = "Vaiabilidad en la frecuencia cardiaca baja",
                ubicacion = ubicacionStr,
                respuestaUsu = "Variabilidad de la frecuencia cardiaca: ${medicion.variabilidadFrecuenciaCardiaca}"
            )
            enviarDatos=true
        }
        if(enviarDatos){
            dbHelper.addEventoCritico(data)
            // TODO: Implementar lógica para la nube
        }
    }
}