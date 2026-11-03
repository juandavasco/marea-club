package com.example.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Fila 30 de la matriz / §17: "cada botón debe ejecutar una acción real o identificarse como no disponible".
 * Revisa el código fuente: ningún control puede tener una acción vacía.
 */
class NoDeadControlsTest {

    private val emptyAction = Regex("""on(Click|LongClick|CheckedChange|ValueChange)\s*=\s*\{\s*}""")

    @Test
    fun `ningun control tiene una accion vacia`() {
        val sourceRoot = listOf(File("src/main/java"), File("app/src/main/java")).first { it.exists() }
        val offenders = sourceRoot.walkTopDown()
            .filter { it.extension == "kt" }
            .flatMap { file ->
                file.readLines().mapIndexedNotNull { i, line ->
                    if (emptyAction.containsMatchIn(line)) "${file.name}:${i + 1}: ${line.trim()}" else null
                }
            }
            .toList()
        assertTrue("Controles con acción vacía:\n" + offenders.joinToString("\n"), offenders.isEmpty())
    }
}
