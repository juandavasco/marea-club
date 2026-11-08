package com.example.domain

/**
 * Mi isla (Documento Técnico §7, Prompt Maestro §6).
 *
 * - Una sola escena base para todos.
 * - Después de una actividad significativa el niño puede ELEGIR uno entre 2 o 3 objetos, o ninguno.
 * - Sin monedas, puntos, niveles, tiendas, rankings, rachas ni candados.
 * - La cantidad de objetos no es un indicador de bienestar.
 *
 * Sin dependencias de Android para probarse con JUnit puro.
 */
object IslandObjects {

    data class IslandObject(val id: String, val name: String, val meaning: String)

    /** Objetos propuestos en el Documento Técnico §7. El arte final lo entrega el equipo de diseño. */
    val catalog: List<IslandObject> = listOf(
        IslandObject("hamaca", "Hamaca", "Descanso y pausa"),
        IslandObject("jardin", "Jardín", "Autoconocimiento y cuidado"),
        IslandObject("puente", "Puente", "Relaciones y conexión"),
        IslandObject("faro", "Faro", "Orientación y apoyo")
    )

    const val MAX_OFFERS = 3

    fun byId(id: String): IslandObject? = catalog.firstOrNull { it.id == id }

    /**
     * Objetos para elegir después de una actividad: los sugeridos por la actividad que
     * aún no están en la isla (máximo 3). Lista vacía = no se muestra la elección.
     */
    fun offersFor(suggested: List<String>, placed: Set<String>): List<IslandObject> =
        suggested.distinct().filter { it !in placed }.mapNotNull(::byId).take(MAX_OFFERS)

    /** Coloca un objeto. Un objeto solo puede estar una vez; colocar uno no quita ni bloquea otros. */
    fun place(placed: Set<String>, objectId: String): Set<String> {
        require(byId(objectId) != null) { "Objeto desconocido: $objectId" }
        return placed + objectId
    }
}
