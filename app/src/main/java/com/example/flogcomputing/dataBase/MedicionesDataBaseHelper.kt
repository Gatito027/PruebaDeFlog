package com.example.flogcomputing.dataBase

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.flogcomputing.models.medicion
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MedicionesDataBaseHelper(context: Context) : SQLiteOpenHelper(context,
    DATABASE_NAME,null,
    DATABASE_VERSION
) {
    companion object {
        const val DATABASE_NAME = "LocalStorage.db"
        const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createMedicionesQry = """
            CREATE TABLE Mediciones (
                medicionId INTEGER PRIMARY KEY AUTOINCREMENT,
                usuarioId INTEGER,
                horaFecha DATETIME,
                frecuenciaCardiaca DOUBLE,
                variabilidadFrecuenciaCardiaca DOUBLE,
                saturacionOxigeno DOUBLE,
                actividadFisica DOUBLE,
                FOREIGN KEY (usuarioId) REFERENCES usuario(idUsuario)
            );
        """.trimIndent()
        db?.execSQL(createMedicionesQry)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db?.execSQL("DROP TABLE IF EXISTS Mediciones")
        onCreate(db)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addMedicion(medicion: medicion): Long {
        val db = this.writableDatabase
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val values = ContentValues().apply {
            put("usuarioId", medicion.usuarioId)
            put("horaFecha", medicion.horaFecha.format(formatter))
            put("actividadFisica", medicion.actividadFisica)
            put("saturacionOxigeno", medicion.saturacionOxigeno)
            put("frecuenciaCardiaca", medicion.frecuenciaCardiaca)
            put("variabilidadFrecuenciaCardiaca", medicion.variabilidadFrecuenciaCardiaca)
        }
        val result = db.insert("Mediciones", null, values)
        db.close()
        return result
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getAllMediciones(): List<medicion> {
        val medicionList = mutableListOf<medicion>()
        val db = this.readableDatabase
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val cursor: Cursor = db.rawQuery("SELECT * FROM Mediciones", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("medicionId"))
                val usuarioId = cursor.getInt(cursor.getColumnIndexOrThrow("usuarioId"))
                val horaFechaStr = cursor.getString(cursor.getColumnIndexOrThrow("horaFecha"))
                val frecuenciaCardiaca =
                    cursor.getDouble(cursor.getColumnIndexOrThrow("frecuenciaCardiaca"))
                val variabilidadFrecuenciaCardiaca =
                    cursor.getDouble(cursor.getColumnIndexOrThrow("variabilidadFrecuenciaCardiaca"))
                val saturacionOxigeno =
                    cursor.getDouble(cursor.getColumnIndexOrThrow("saturacionOxigeno"))
                val actividadFisica =
                    cursor.getDouble(cursor.getColumnIndexOrThrow("actividadFisica"))
                val FechaHora = LocalDateTime.parse(horaFechaStr, formatter)
                medicionList.add(
                    medicion(
                        id,
                        usuarioId,
                        FechaHora,
                        frecuenciaCardiaca,
                        variabilidadFrecuenciaCardiaca,
                        saturacionOxigeno,
                        actividadFisica
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return medicionList
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateMedicion(medicion: medicion): Int {
        val db = this.writableDatabase
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val values = ContentValues().apply {
            put("usuarioId", medicion.usuarioId)
            put("horaFecha", medicion.horaFecha.format(formatter))
            put("actividadFisica", medicion.actividadFisica)
            put("saturacionOxigeno", medicion.saturacionOxigeno)
            put("frecuenciaCardiaca", medicion.frecuenciaCardiaca)
            put("variabilidadFrecuenciaCardiaca", medicion.variabilidadFrecuenciaCardiaca)
        }
        val result =
            db.update("Mediciones", values, "medicionId=?", arrayOf(medicion.id.toString()))
        db.close()
        return result
    }

    fun deleteMedicion(medicionId: Int): Int {
        val db = this.writableDatabase
        val result = db.delete("Mediciones", "medicionId=?", arrayOf(medicionId.toString()))
        db.close()
        return result
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMedicionesDeHoy(): List<medicion> {
        val medicionList = mutableListOf<medicion>()
        val db = this.readableDatabase
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

        val cursor = db.rawQuery(
            """
        SELECT * FROM Mediciones
        WHERE date(horaFecha) = date('now', 'localtime')
        """.trimIndent(), null
        )

        if (cursor.moveToFirst()) {
            do {
                val fechaHora = LocalDateTime.parse(
                    cursor.getString(cursor.getColumnIndexOrThrow("horaFecha")),
                    formatter
                )
                medicionList.add(
                    medicion(
                        cursor.getInt(cursor.getColumnIndexOrThrow("medicionId")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("usuarioId")),
                        fechaHora,
                        cursor.getDouble(cursor.getColumnIndexOrThrow("frecuenciaCardiaca")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("variabilidadFrecuenciaCardiaca")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("saturacionOxigeno")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("actividadFisica"))
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return medicionList
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMedicionesUltimas4Horas(): List<medicion> {
        val medicionList = mutableListOf<medicion>()
        val db = this.readableDatabase
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

        val cursor = db.rawQuery(
            """
        SELECT * FROM Mediciones
        WHERE datetime(horaFecha) >= datetime('now', '-4 hours', 'localtime')
        AND datetime(horaFecha) <= datetime('now', 'localtime')
        """.trimIndent(), null
        )

        if (cursor.moveToFirst()) {
            do {
                val fechaHora = LocalDateTime.parse(cursor.getString(cursor.getColumnIndexOrThrow("horaFecha")), formatter)
                medicionList.add(
                    medicion(
                        cursor.getInt(cursor.getColumnIndexOrThrow("medicionId")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("usuarioId")),
                        fechaHora,
                        cursor.getDouble(cursor.getColumnIndexOrThrow("frecuenciaCardiaca")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("variabilidadFrecuenciaCardiaca")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("saturacionOxigeno")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("actividadFisica"))
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return medicionList
    }
}