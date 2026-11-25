package com.example.roomcontactos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactoDao {

    @Insert
    suspend fun insertar(contacto: Contacto)

    @Update
    suspend fun actualizar(contacto: Contacto)

    @Delete
    suspend fun eliminar(contacto: Contacto)

    @Query("SELECT * FROM contactos ORDER BY nombre ASC")
    fun obtenerContactos(): Flow<List<Contacto>>
}