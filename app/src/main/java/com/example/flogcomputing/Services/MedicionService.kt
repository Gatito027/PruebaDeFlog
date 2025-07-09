package com.example.flogcomputing.Services

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.flogcomputing.dataBase.MedicionesDataBaseHelper
import com.example.flogcomputing.models.medicion
import kotlin.math.pow
import kotlin.math.sqrt
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class MedicionService(private val context: Context) {
    private lateinit var dbHelper: MedicionesDataBaseHelper
    private lateinit var webSocket: WebSocket
    private var isConnected = false

    // Datos recibidos del dispositivo Bluetooth via WebSocket
    private var currentRRInterval: Double = 0.0
    private var currentRedLight: Double = 0.0
    private var currentInfraredLight: Double = 0.0
    private var currentAcceleration: Triple<Double, Double, Double> = Triple(0.0, 0.0, 0.0)
    private val rrIntervalsBuffer = mutableListOf<Double>()

    // Configuración del WebSocket
    fun connectWebSocket(serverUrl: String) {
        val client = OkHttpClient.Builder()
            .readTimeout(3, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(serverUrl)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                isConnected = true
                Log.d("WebSocket", "Conexión establecida")
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onMessage(webSocket: WebSocket, text: String) {
                // Parsear los datos recibidos del dispositivo Bluetooth
                parseBluetoothData(text)

                // Guardar datos cada cierto tiempo o cuando se reciba un paquete completo
                saveData(1) // Puedes pasar el ID de usuario adecuado
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                Log.d("WebSocket", "Conexión cerrada")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                isConnected = false
                Log.e("WebSocket", "Error en la conexión", t)
            }
        })
    }

    private fun parseBluetoothData(data: String) {
        try {
            // Ejemplo de formato de datos: "RR:0.85,RED:0.8,IR:1.2,ACCX:0.5,ACCY:0.6,ACCZ:0.3"
            val parts = data.split(",")

            parts.forEach { part ->
                val keyValue = part.split(":")
                if (keyValue.size == 2) {
                    when (keyValue[0]) {
                        "RR" -> {
                            currentRRInterval = keyValue[1].toDouble()
                            updateRRIntervalsBuffer(currentRRInterval)
                        }
                        "RED" -> currentRedLight = keyValue[1].toDouble()
                        "IR" -> currentInfraredLight = keyValue[1].toDouble()
                        "ACCX" -> currentAcceleration = currentAcceleration.copy(first = keyValue[1].toDouble())
                        "ACCY" -> currentAcceleration = currentAcceleration.copy(second = keyValue[1].toDouble())
                        "ACCZ" -> currentAcceleration = currentAcceleration.copy(third = keyValue[1].toDouble())
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("parseBluetoothData", "Error al parsear datos", e)
        }
    }

    private fun updateRRIntervalsBuffer(newRR: Double) {
        rrIntervalsBuffer.add(newRR)
        // Mantener un buffer de tamaño razonable (ej. últimos 5-10 intervalos)
        if (rrIntervalsBuffer.size > 10) {
            rrIntervalsBuffer.removeAt(0)
        }
    }

    fun disconnectWebSocket() {
        webSocket.close(1000, "Cierre solicitado")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveData(idUsuario: Int): Boolean {
        if (!isConnected || rrIntervalsBuffer.isEmpty()) return false

        return try {
            dbHelper = MedicionesDataBaseHelper(context)

            val medicion = medicion(
                usuarioId = idUsuario,
                horaFecha = LocalDateTime.now(),
                frecuenciaCardiaca = calculateHeartRate(currentRRInterval),
                variabilidadFrecuenciaCardiaca = calculateHRV(rrIntervalsBuffer),
                saturacionOxigeno = calculateSpO2(currentRedLight, currentInfraredLight),
                actividadFisica = calculateActivity(
                    currentAcceleration.first,
                    currentAcceleration.second,
                    currentAcceleration.third
                )
            )

            dbHelper.addMedicion(medicion)
            true
        } catch (e: Exception) {
            Log.e("saveData", "Error al guardar datos: ${e.message}", e)
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun mostrarDatos(idUsuario: Int):medicion{
        return medicion(
            usuarioId = idUsuario,
            horaFecha = LocalDateTime.now(),
            frecuenciaCardiaca = calculateHeartRate(currentRRInterval),
            variabilidadFrecuenciaCardiaca = calculateHRV(rrIntervalsBuffer),
            saturacionOxigeno = calculateSpO2(currentRedLight, currentInfraredLight),
            actividadFisica = calculateActivity(
                currentAcceleration.first,
                currentAcceleration.second,
                currentAcceleration.third
            )
            )
    }

    // Resto de tus funciones (calculateHeartRate, calculateHRV, etc.) se mantienen igual
    fun calculateHeartRate(rrInterval: Double): Double {
        return 60 / rrInterval
    }

    fun calculateHRV(rrIntervals: List<Double>): Double {
        val meanRR = rrIntervals.average()
        val variance = rrIntervals.map { rr -> (rr - meanRR).pow(2) }.average()
        return sqrt(variance)
    }

    fun calculateSpO2(redLight: Double, infraredLight: Double): Double {
        return 110 - 25 * (redLight / infraredLight)
    }

    fun calculateActivity(ax: Double, ay: Double, az: Double): Double {
        return sqrt(ax.pow(2) + ay.pow(2) + az.pow(2))
    }
}