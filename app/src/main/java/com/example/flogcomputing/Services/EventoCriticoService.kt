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
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun Monitorizarparametros(medicion: medicion) {
        if (medicion.frecuenciaCardiaca >= 160 || medicion.frecuenciaCardiaca <= 40) {
            dbHelper = EventosCriticosDataBaseHelper(context)
            val locationHelper = LocationHelper(context)
            val location = locationHelper.getLastKnownLocation()
            val ubicacionStr = location?.let { "${it.latitude}, ${it.longitude}" } ?: "No disponible"

            val data = eventoCritico(
                usuarioId = medicion.usuarioId,
                fechaHora = LocalDateTime.now(),
                tipoEvento = "Frecuencia Cardiaca anormal",
                ubicacion = ubicacionStr,
                respuestaUsu = "Frecuencia cardiaca: ${medicion.frecuenciaCardiaca}"
            )
            dbHelper.addEventoCritico(data)
            // TODO: Implementar lógica para la nube
        }
    }
}