package com.carolash.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
import java.util.concurrent.Executors

/**
 * Guarda todo en un archivo JSON dentro del celular (filesDir). No usa internet.
 * `data` es un estado de Compose: al cambiarlo, las pantallas se actualizan solas.
 */
class Store(private val file: File) {
    var data by mutableStateOf(AppData())
        private set

    private val io = Executors.newSingleThreadExecutor()

    init {
        data = try {
            if (file.exists()) Codec.decode(file.readText()) else AppData()
        } catch (e: Exception) {
            // Archivo dañado: se guarda una copia aparte antes de empezar de cero.
            try { file.copyTo(File(file.parentFile, file.name + ".bak"), overwrite = true) } catch (e2: Exception) { }
            AppData()
        }
    }

    fun update(change: (AppData) -> AppData) {
        val next = change(data)
        data = next
        val text = Codec.encode(next)
        io.execute {
            try {
                val tmp = File(file.parentFile, file.name + ".tmp")
                tmp.writeText(text)
                if (!tmp.renameTo(file)) {
                    file.writeText(text)
                    tmp.delete()
                }
            } catch (e: Exception) {
                // Si no se pudo guardar ahora, se vuelve a intentar con el siguiente cambio.
            }
        }
    }

    /** Copia de respaldo en texto (para guardarla o compartirla). */
    fun backupText(): String = Codec.encode(data)
}
