package com.example.domain

/**
 * Orientación de la autoevaluación preliminar NO clínica.
 *
 * Decisión 4 del equipo: no se muestra ningún puntaje, nivel ni etiqueta.
 * Las respuestas solo sirven para ordenar qué sugerencias de apoyo aparecen primero.
 * Las rutas de apoyo se muestran SIEMPRE, sin importar las respuestas.
 */
object SelfCheckGuidance {

    enum class Focus {
        /** Las respuestas no muestran mucha carga: sugerencias de cuidado cotidiano primero. */
        CUIDADO_COTIDIANO,
        /** Hay algo de carga: sugerir conversar con alguien de confianza primero. */
        CONVERSAR,
        /** Hay bastante carga: sugerir hablar pronto con un adulto y las líneas de apoyo primero. */
        BUSCAR_APOYO
    }

    /**
     * [answerWeights]: peso de cada respuesta (0, 1 o 2, según el cuestionario).
     * [maxWeightPerQuestion]: peso máximo posible por pregunta.
     * Preguntas sin responder no cuentan.
     */
    fun focus(answerWeights: List<Int>, maxWeightPerQuestion: Int = 2): Focus {
        if (answerWeights.isEmpty() || maxWeightPerQuestion <= 0) return Focus.CUIDADO_COTIDIANO
        val ratio = answerWeights.sum().toFloat() / (answerWeights.size * maxWeightPerQuestion)
        return when {
            ratio >= 0.65f -> Focus.BUSCAR_APOYO
            ratio >= 0.35f -> Focus.CONVERSAR
            else -> Focus.CUIDADO_COTIDIANO
        }
    }
}
