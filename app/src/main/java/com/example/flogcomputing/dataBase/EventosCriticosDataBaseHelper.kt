package com.example.flogcomputing.dataBase

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.flogcomputing.models.eventoCritico
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EventosCriticosDataBaseHelper(context: Context) : SQLiteOpenHelper(context,
    DATABASE_NAME,null,
    DATABASE_VERSION
)  {
    companion object{
        const val DATABASE_NAME="LocalStorage.db"
        const val DATABASE_VERSION=1
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createEventosQry = """
            CREATE TABLE eventosCriticos (
                eventoId INTEGER PRIMARY KEY AUTOINCREMENT,
                usuarioId INTEGER,
                fechaHora DATETIME,
                tipoEvento VARCHAR(20),
                ubicacion TEXT,
                respuestaUsu TEXT,
                FOREIGN KEY (usuarioId) REFERENCES Alertas(usuarioId)
            );
        """.trimIndent()
        db?.execSQL(createEventosQry)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db?.execSQL("DROP TABLE IF EXISTS eventosCriticos")
        onCreate(db)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addEventoCritico(eventoCritico: eventoCritico):Long{
        val db = this.writableDatabase
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val values = ContentValues().apply {
            put("usuarioId", eventoCritico.usuarioId)
            put("tipoEvento", eventoCritico.tipoEvento)
            put("ubicacion", eventoCritico.ubicacion)
            put("respuestaUsu", eventoCritico.respuestaUsu)
            put("frecuenciaCardiaca", eventoCritico.fechaHora.format(formatter))
        }
        val result = db.insert("eventosCriticos",null,values)
        db.close()
        return result
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getAllEventosCriticos(): List<eventoCritico>{
        val eveCriticosList= mutableListOf<eventoCritico>()
        val db = this.readableDatabase
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val cursor: Cursor = db.rawQuery("SELECT * FROM eventosCriticos",null)
        if(cursor.moveToFirst()){
            do{
                val id=cursor.getInt(cursor.getColumnIndexOrThrow("eventoId"))
                val usuarioId=cursor.getInt(cursor.getColumnIndexOrThrow("usuarioId"))
                val horaFechaStr=cursor.getString(cursor.getColumnIndexOrThrow("fechaHora"))
                val tipoEvento=cursor.getString(cursor.getColumnIndexOrThrow("tipoEvento"))
                val ubicacion= cursor.getString(cursor.getColumnIndexOrThrow("ubicacion"))
                val respuestaUsu = cursor.getString(cursor.getColumnIndexOrThrow("respuestaUsu"))
                val FechaHora = LocalDateTime.parse(horaFechaStr, formatter)
                eveCriticosList.add(eventoCritico(id,usuarioId,FechaHora,tipoEvento,ubicacion,respuestaUsu))
            }while(cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return eveCriticosList
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateEventosCriticos(eventoCritico: eventoCritico):Int {
        val db = this.writableDatabase
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val values = ContentValues().apply {
            put("usuarioId", eventoCritico.usuarioId)
            put("fechaHora", eventoCritico.fechaHora.format(formatter))
            put("tipoEvento", eventoCritico.tipoEvento)
            put("ubicacion", eventoCritico.ubicacion)
            put("respuestaUsu", eventoCritico.respuestaUsu)
        }
        val result = db.update("eventosCriticos",values,"eventoId=?", arrayOf(eventoCritico.id.toString()))
        db.close()
        return result
    }

    fun deleteMedicion(eveCriticoId:Int):Int{
        val db=this.writableDatabase
        val result=db.delete("eventosCriticos","eventoId=?", arrayOf(eveCriticoId.toString()))
        db.close()
        return result
    }
}