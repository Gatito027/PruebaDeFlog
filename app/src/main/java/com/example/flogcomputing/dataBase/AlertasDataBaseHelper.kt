package com.example.flogcomputing.dataBase

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.flogcomputing.models.alertas
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AlertasDataBaseHelper(context: Context) : SQLiteOpenHelper(context,
    DATABASE_NAME,null,
    DATABASE_VERSION
) {
    companion object{
        const val DATABASE_NAME="LocalStorage.db"
        const val DATABASE_VERSION=1
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createAlertasQry="""
            CREATE TABLE Alertas (
                alertaId INTEGER PRIMARY KEY AUTOINCREMENT,
                usuarioId INTEGER,
                FechaHora DATETIME,
                tipoAlerta VARCHAR(20),
                estado VARCHAR(50),
                FOREIGN KEY (usuarioId) REFERENCES usuario(idUsuario)
            );
        """.trimIndent()
        db?.execSQL(createAlertasQry)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db?.execSQL("DROP TABLE IF EXISTS Alertas")
        onCreate(db)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addAlerta(alertas: alertas):Long{
        val db = this.writableDatabase
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val values = ContentValues().apply {
            put("usuarioId", alertas.usuarioId)
            put("tipoAlerta", alertas.tipoAlerta)
            put("estado", alertas.estado)
            put("FechaHora", alertas.FechaHora.format(formatter))
        }
        val result = db.insert("Alertas",null,values)
        db.close()
        return result
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getAllAlertas(): List<alertas>{
        val alertasList= mutableListOf<alertas>()
        val db = this.readableDatabase
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val cursor: Cursor = db.rawQuery("SELECT * FROM Alertas",null)
        if(cursor.moveToFirst()){
            do{
                val id=cursor.getInt(cursor.getColumnIndexOrThrow("alertaId"))
                val usuarioId=cursor.getInt(cursor.getColumnIndexOrThrow("usuarioId"))
                val tipoAlerta=cursor.getString(cursor.getColumnIndexOrThrow("tipoAlerta"))
                val estado=cursor.getString(cursor.getColumnIndexOrThrow("estado"))
                val FechaHoraStr= cursor.getString(cursor.getColumnIndexOrThrow("FechaHora"))
                val FechaHora = LocalDateTime.parse(FechaHoraStr, formatter)
                alertasList.add(alertas(id,usuarioId,FechaHora,tipoAlerta,estado))
            }while(cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return alertasList
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateAlerta(alertas: alertas):Int {
        val db = this.writableDatabase
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val values = ContentValues().apply {
            put("usuarioId", alertas.usuarioId)
            put("tipoAlerta", alertas.tipoAlerta)
            put("estado", alertas.estado)
            put("FechaHora", alertas.FechaHora.format(formatter))
        }
        val result = db.update("Alertas",values,"alertaId=?", arrayOf(alertas.id.toString()))
        db.close()
        return result
    }

    fun deleteAlerta(alertaId:Int):Int{
        val db=this.writableDatabase
        val result=db.delete("Alertas","alertaId=?", arrayOf(alertaId.toString()))
        db.close()
        return result
    }
}