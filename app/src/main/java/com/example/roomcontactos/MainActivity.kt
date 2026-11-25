package com.example.roomcontactos

import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roomcontactos.ui.theme.RoomContactosTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val dao = database.contactoDao()

        setContent {
            RoomContactosTheme {
                PantallaContactos(dao)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaContactos(dao: ContactoDao) {

    val contactos by dao.obtenerContactos()
        .collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contactoEditar by remember {
        mutableStateOf<Contacto?>(null)
    }
    var contactoEliminar by remember {
        mutableStateOf<Contacto?>(null)
    }

    val azul = Color(0xFF315DA8)

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbar)
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mis contactos",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = azul,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .imePadding()
        ) {

            Text(
                text = "Administrador de contactos",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Almacenamiento local con Room",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre completo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo electrónico") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val nombreLimpio = nombre.trim()
                    val correoLimpio = correo.trim()
                    val editando = contactoEditar

                    if (
                        nombreLimpio.isBlank() ||
                        correoLimpio.isBlank()
                    ) {
                        scope.launch {
                            snackbar.showSnackbar(
                                "Completa todos los campos"
                            )
                        }
                    } else if (
                        !Patterns.EMAIL_ADDRESS
                            .matcher(correoLimpio).matches()
                    ) {
                        scope.launch {
                            snackbar.showSnackbar(
                                "Introduce un correo válido"
                            )
                        }
                    } else {
                        scope.launch {

                            if (editando == null) {
                                dao.insertar(
                                    Contacto(
                                        nombre = nombreLimpio,
                                        correo = correoLimpio
                                    )
                                )
                            } else {
                                dao.actualizar(
                                    editando.copy(
                                        nombre = nombreLimpio,
                                        correo = correoLimpio
                                    )
                                )
                            }

                            nombre = ""
                            correo = ""
                            contactoEditar = null

                            snackbar.showSnackbar(
                                if (editando == null)
                                    "Contacto guardado"
                                else
                                    "Contacto actualizado"
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = azul
                )
            ) {
                Text(
                    if (contactoEditar == null)
                        "Guardar contacto"
                    else
                        "Actualizar contacto"
                )
            }

            if (contactoEditar != null) {
                OutlinedButton(
                    onClick = {
                        nombre = ""
                        correo = ""
                        contactoEditar = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar edición")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    "Contactos guardados",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "${contactos.size} registros",
                    color = azul,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (contactos.isEmpty()) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No hay contactos registrados",
                        color = MaterialTheme
                            .colorScheme.onSurfaceVariant
                    )
                }

            } else {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    items(
                        items = contactos,
                        key = { it.id }
                    ) { contacto ->

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults
                                .cardElevation(3.dp)
                        ) {

                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {

                                Text(
                                    contacto.nombre,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    contacto.correo,
                                    fontSize = 14.sp
                                )

                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement =
                                        Arrangement.End
                                ) {

                                    TextButton(
                                        onClick = {
                                            contactoEditar = contacto
                                            nombre = contacto.nombre
                                            correo = contacto.correo
                                        }
                                    ) {
                                        Text("Editar", color = azul)
                                    }

                                    TextButton(
                                        onClick = {
                                            contactoEliminar = contacto
                                        }
                                    ) {
                                        Text(
                                            "Eliminar",
                                            color = Color(0xFFD32F2F)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    contactoEliminar?.let { contacto ->

        AlertDialog(
            onDismissRequest = {
                contactoEliminar = null
            },
            title = {
                Text("Eliminar contacto")
            },
            text = {
                Text(
                    "¿Deseas eliminar a ${contacto.nombre}?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        contactoEliminar = null

                        scope.launch {
                            dao.eliminar(contacto)

                            if (contactoEditar?.id == contacto.id) {
                                contactoEditar = null
                                nombre = ""
                                correo = ""
                            }

                            snackbar.showSnackbar(
                                "Contacto eliminado"
                            )
                        }
                    }
                ) {
                    Text(
                        "Eliminar",
                        color = Color(0xFFD32F2F)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        contactoEliminar = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}