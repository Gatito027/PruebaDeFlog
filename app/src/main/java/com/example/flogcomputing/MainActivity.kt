package com.example.flogcomputing

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.flogcomputing.dataBase.UsuarioDataBaseHelper
import com.example.flogcomputing.models.Usuario
import com.example.flogcomputing.ui.theme.FlogComputingTheme

class MainActivity : ComponentActivity() {
    //lateinit var binding:ActivityMainBinding
    private lateinit var dbHelper:UsuarioDataBaseHelper
    @RequiresApi(Build.VERSION_CODES.O)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        dbHelper = UsuarioDataBaseHelper(this)
        val newUsuario=Usuario(nombre="Juan",edad=40, contacto = 551, genero = "masculino" )
        dbHelper.addUsuario(newUsuario)
        val usuarios = dbHelper.getAllUsuarios()
        val text=usuarios.joinToString(separator = "\n"){
                usuario -> "ID: ${usuario.id}, nombre= ${usuario.nombre}"
        }
        println(text)
        setContent {
            FlogComputingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = text,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FlogComputingTheme {
        Greeting("Android")
    }
}