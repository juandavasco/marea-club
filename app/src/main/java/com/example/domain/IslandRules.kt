package com.example.domain

/**
 * Reglas explícitas de "Mi isla" (Documento Técnico §7).
 *
 * Cada acción de autocuidado desbloquea UN elemento concreto de la isla y el sistema
 * puede explicar qué acción lo produjo. El nivel se deriva solo de los elementos
 * desbloqueados: no hay rachas, rankings ni penalizaciones por no usar la app
 * ni por registrar emociones difíciles.
 *
 * Este archivo no depende de Android para poder probarse con JUnit puro.
 */
object IslandRules {

    data class Unlock(
        /** Id de la acción (actividad, herramienta o reflexión). */
        val sourceId: String,
        /** Nombre visible de la acción. */
        val sourceTitle: String,
        /** Id del elemento de la isla que desbloquea. */
        val elementId: String,
        /** Nombre visible del elemento. */
        val elementName: String,
        val icon: String
    )

    val unlocks: List<Unlock> = listOf(
        Unlock("act_respiracion", "Respiración del Océano", "flores_coral", "Flores de Coral", "🌸"),
        Unlock("act_diario", "Diario del Mar", "sofa_azul", "Sillón de Arena Azul", "🛋️"),
        Unlock("act_emociones", "Brújula Emocional", "arbol_verde", "Árbol Sabio de Muni", "🌳"),
        Unlock("act_autoestima", "Constelación de Fortalezas", "estrella_marina", "Estrella de Fortalezas", "⭐"),
        Unlock("act_conchas", "Reto de Conchas y Perlas", "camino_caracoles", "Camino de Caracoles", "🐚"),
        Unlock("act_dibujo", "Lienzo de Expresión Libre", "arrecife_colores", "Arrecife Creativo", "🎨"),
        Unlock("act_burbujas", "Burbujas de descarga", "farol_calma", "Farol de la Calma", "🏮"),
        Unlock("act_sonidos", "Sonidos del océano", "estanque_muni", "Estanque Cristalino", "💧"),
        Unlock("reflexion_semanal", "Reflexión semanal", "muelle_madera", "Muelle del Horizonte", "🪵")
    )

    /** Elementos por nivel. 9 elementos → 4 niveles (el último se alcanza con los 9). */
    const val ELEMENTS_PER_LEVEL = 3

    val levelNames: List<String> = listOf("Orilla Inicial", "Mi Refugio", "Santuario Marino", "Paraíso de Muni")

    val totalElements: Int get() = unlocks.size

    fun unlockForSource(sourceId: String): Unlock? = unlocks.firstOrNull { it.sourceId == sourceId }

    fun unlockForElement(elementId: String): Unlock? = unlocks.firstOrNull { it.elementId == elementId }

    data class Outcome(
        val unlock: Unlock?,
        /** true si el elemento se desbloqueó con esta acción; false si ya estaba. */
        val newlyUnlocked: Boolean,
        val unlockedAfter: Set<String>
    )

    /** Aplica una acción completada sobre el conjunto de elementos desbloqueados. */
    fun apply(unlocked: Set<String>, sourceId: String): Outcome {
        val unlock = unlockForSource(sourceId) ?: return Outcome(null, false, unlocked)
        val isNew = unlock.elementId !in unlocked
        return Outcome(unlock, isNew, unlocked + unlock.elementId)
    }

    data class Progress(
        val level: Int,
        val levelName: String,
        /** Avance hacia el siguiente nivel, 0–100. En el último nivel es 100. */
        val percentToNext: Int,
        val unlockedCount: Int,
        val total: Int
    )

    fun progress(unlocked: Set<String>): Progress {
        val known = unlocked.count { id -> unlocks.any { it.elementId == id } }
        val total = totalElements
        val level = if (known >= total) levelNames.size else (known / ELEMENTS_PER_LEVEL) + 1
        val percent = if (known >= total) 100 else ((known % ELEMENTS_PER_LEVEL) * 100) / ELEMENTS_PER_LEVEL
        return Progress(
            level = level,
            levelName = levelNames[(level - 1).coerceIn(0, levelNames.lastIndex)],
            percentToNext = percent,
            unlockedCount = known,
            total = total
        )
    }

    /** La siguiente acción sugerida: la primera cuyo elemento aún no está desbloqueado. */
    fun nextSuggestion(unlocked: Set<String>): Unlock? = unlocks.firstOrNull { it.elementId !in unlocked }
}
