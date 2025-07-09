package com.example.flogcomputing.dataBase

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.flogcomputing.models.Usuario
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class UsuarioDataBaseHelper(context:Context): SQLiteOpenHelper(context,
    DATABASE_NAME,null,
    DATABASE_VERSION
) {
    companion object{
        const val DATABASE_NAME="LocalStorage.db"
        const val DATABASE_VERSION=1
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createQry = """
            CREATE TABLE usuario (
                idUsuario INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre VARCHAR(240),
                edad INTEGER,
                genero VARCHAR(10),
                contacto INTEGER
            );
            """.trimIndent()
        db?.execSQL(createQry)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db?.execSQL("DROP TABLE IF EXISTS usuario")
        onCreate(db)
    }

    fun addUsuario(usuario: Usuario):Long{
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("nombre", usuario.nombre)
            put("edad", usuario.edad)
            put("genero", usuario.genero)
            put("contacto", usuario.contacto)
        }
        val result = db.insert("usuario",null,values)
        db.close()
        return result
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getAllUsuarios(): List<Usuario>{
        val usuarioList= mutableListOf<Usuario>()
        val db = this.readableDatabase
        //val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val cursor: Cursor = db.rawQuery("SELECT * FROM usuario",null)
        if(cursor.moveToFirst()){
            do{
                val id=cursor.getInt(cursor.getColumnIndexOrThrow("idUsuario"))
                val nombre=cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                val edad=cursor.getInt(cursor.getColumnIndexOrThrow("edad"))
                val genero=cursor.getString(cursor.getColumnIndexOrThrow("genero"))
                val contacto=cursor.getInt(cursor.getColumnIndexOrThrow("contacto"))
                //val fechaRegistroStr= cursor.getString(cursor.getColumnIndexOrThrow("fechaRegistro"))
                //val fechaRegistro = LocalDateTime.parse(fechaRegistroStr, formatter)
                usuarioList.add(Usuario(id,nombre,edad,genero,contacto))
            }while(cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return usuarioList
    }

    fun updateUsuario(usuario: Usuario):Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("nombre", usuario.nombre)
            put("edad", usuario.edad)
            put("genero", usuario.genero)
            put("contacto", usuario.contacto)
        }
        val result = db.update("usuario",values,"idUsuario=?", arrayOf(usuario.id.toString()))
        db.close()
        return result
    }

    fun deleteUsuario(usuarioId:Int):Int{
        val db=this.writableDatabase
        val result=db.delete("usuario","idUsuario=?", arrayOf(usuarioId.toString()))
        db.close()
        return result
    }
}