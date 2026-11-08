package com.example.data

import com.example.model.*

/**
 * Contenido fijo de la app: actividades, elementos de la isla, rutas de apoyo y cuestionarios.
 * Separado del almacenamiento (Documento Técnico §21).
 */
object MareaContent {

    /** Rango de edad de trabajo (decisión 1). Cambiar aquí si el equipo lo confirma distinto. */
    const val AGE_RANGE_LABEL = "11 a 14"

    private fun activity(
        id: String,
        title: String,
        category: ActivityCategory,
        durationMin: String,
        description: String,
        recommendedFor: List<MareaType>,
        icon: String
    ) = ActivityItem(id, title, category, durationMin, description, icon, recommendedFor)

    /**
     * "Para calmarme": las herramientas que ya funcionaban en la v1.x (decisión 3 del 7 nov).
     * Ya no son las actividades principales y no cambian la isla.
     */
    val calmTools: List<ActivityItem> = listOf(
        activity("act_respiracion", "Respiración del Océano", ActivityCategory.RESPIRACION, "3 min",
            "Respira siguiendo las olas con Muni.", listOf(MareaType.MOVIDA, MareaType.ALTA, MareaType.TORMENTA, MareaType.NO_SE), "🌊"),
        activity("act_burbujas", "Burbujas de descarga", ActivityCategory.RESPIRACION, "2 min",
            "Revienta burbujas para soltar tensión.", listOf(MareaType.MOVIDA, MareaType.ALTA), "🎈"),
        activity("act_sonidos", "Sonidos del océano", ActivityCategory.RESPIRACION, "1 min o más",
            "Escucha olas, brisa, campanas o lluvia.", listOf(MareaType.TORMENTA, MareaType.ALTA, MareaType.TRANQUILA), "🎧"),
        activity("act_diario", "Diario del Mar", ActivityCategory.ESCRITURA, "5 min",
            "Escribe lo que sientes; queda solo en tu teléfono.", listOf(MareaType.MOVIDA, MareaType.ALTA, MareaType.TRANQUILA), "📓"),
        activity("act_dibujo", "Lienzo libre", ActivityCategory.CREATIVIDAD, "Libre",
            "Raya y pinta con los colores del mar.", listOf(MareaType.TRANQUILA, MareaType.ALTA, MareaType.TORMENTA), "🎨"),
        activity("act_conchas", "Conchas y perlas", ActivityCategory.ATENCION, "2 min",
            "Un juego corto para traer la atención al presente.", listOf(MareaType.MOVIDA, MareaType.TRANQUILA, MareaType.NO_SE), "🐚"),
        activity("act_emociones", "Brújula de necesidades", ActivityCategory.AUTOCONOCIMIENTO, "4 min",
            "Marca lo que tu cuerpo y tu mente necesitan ahora.", listOf(MareaType.NO_SE, MareaType.MOVIDA, MareaType.TRANQUILA), "🧭"),
        activity("act_autoestima", "Constelación de fortalezas", ActivityCategory.AUTOESTIMA, "4 min",
            "Enciende las cualidades que reconoces en ti.", listOf(MareaType.TRANQUILA, MareaType.MOVIDA, MareaType.TORMENTA), "⭐")
    )

    /** Propósito, pasos y alternativas de cada actividad (Documento Técnico §6). */
    data class ActivityGuide(val purpose: String, val steps: List<String>, val alternative: String)

    val activityGuides: Map<String, ActivityGuide> = mapOf(
        "act_respiracion" to ActivityGuide(
            "Bajar el ritmo del cuerpo cuando la marea está movida.",
            listOf("Sigue el círculo: cuando crece, inhala.", "Cuando se encoge, exhala despacio.", "Completa 3 ciclos o sal cuando quieras."),
            "Si respirar así te incomoda, prueba las Burbujas de descarga."
        ),
        "act_diario" to ActivityGuide(
            "Poner en palabras lo que sientes para ordenarlo.",
            listOf("Elige una pregunta o cámbiala.", "Escribe lo que quieras, sin corregir.", "Guarda: queda solo en tu teléfono y puedes borrarlo."),
            "Si no quieres escribir, prueba el Lienzo de Expresión Libre."
        ),
        "act_emociones" to ActivityGuide(
            "Reconocer qué necesitas ahora mismo.",
            listOf("Lee las necesidades.", "Marca las que sientas cercanas.", "Termina cuando quieras; no hay respuestas correctas."),
            "Si no sabes qué sientes, la opción «No sé qué siento» también es válida."
        ),
        "act_autoestima" to ActivityGuide(
            "Recordar tus fortalezas en días difíciles.",
            listOf("Toca las estrellas con cualidades que reconoces en ti.", "Enciende al menos una.", "Termina cuando quieras."),
            "Si hoy no te salen, puedes pedirle a alguien de confianza que te diga una."
        ),
        "act_conchas" to ActivityGuide(
            "Llevar la atención al presente con un juego corto.",
            listOf("Observa las conchas.", "Encuentra la perla.", "Repite o termina."),
            "Si te frustra, cambia a Sonidos del océano."
        ),
        "act_burbujas" to ActivityGuide(
            "Soltar tensión con algo sencillo.",
            listOf("Toca las burbujas para reventarlas.", "Si quieres, escribe una preocupación y suéltala.", "Sal cuando quieras."),
            "Si prefieres quietud, prueba Sonidos del océano."
        ),
        "act_sonidos" to ActivityGuide(
            "Darle a tu mente un sonido tranquilo para descansar.",
            listOf("Elige un sonido.", "Escucha al menos un minuto.", "Pausa o cambia cuando quieras."),
            "Si quieres moverte, prueba Burbujas de descarga."
        ),
        "act_dibujo" to ActivityGuide(
            "Expresar con colores lo que es difícil decir.",
            listOf("Elige un color.", "Dibuja o raya libremente.", "Termina cuando quieras; el dibujo no se guarda."),
            "Si prefieres palabras, prueba el Diario del Mar."
        )
    )

    private const val LINEA_AMIGA_URL = "https://www.medellin.gov.co/es/secretaria-de-salud/linea-amiga/"
    private const val LINEA_106_URL = "https://www.medellin.gov.co/es/sala-de-prensa/noticias/la-alcaldia-de-medellin-activo-la-linea-106-para-consultas-de-salud-mental/"
    private const val ESCUCHADEROS_URL = "https://www.elcolombiano.com/medellin/necesita-acompanamiento-en-salud-mental-hay-tres-nuevos-escuchaderos-en-estas-estaciones-del-metro-de-medellin-AA36724150"
    private const val SOCIAL_123_URL = "https://www.medellin.gov.co/es/secretaria-de-inclusion-social-y-familia/123-social/"
    private const val VERIFICADO = "2026-11-02"

    /** Número que se marca desde la consulta anónima y las mareas Alta/Tormenta. */
    const val URGENT_PHONE = "106"
    const val URGENT_PHONE_ALT = "6044444448"

    /**
     * Rutas de apoyo. Solo las que tienen [PsychologicalRoute.verified] = true se muestran.
     * Las demás quedan registradas para que el equipo las confirme (ver docs/rutas-por-verificar.md).
     */
    val allRoutes: List<PsychologicalRoute> = listOf(
        PsychologicalRoute(
            id = "route_linea_amiga",
            name = "Línea Amiga Saludable",
            category = RouteCategory.TELEFONICA,
            phone = "(604) 444 44 48",
            whatsapp = "+573016044444",
            address = "Línea telefónica y WhatsApp (opción «Salud»)",
            comuna = "Toda Medellín",
            schedule = "24 horas, 7 días a la semana",
            requiresAppointment = false,
            costDescription = "Gratuita",
            description = "Escucha y orientación en salud mental de la Secretaría de Salud de Medellín.",
            mapQuery = "Secretaria de Salud de Medellin",
            verificationSource = "Alcaldía de Medellín, página Línea Amiga",
            verificationUrl = LINEA_AMIGA_URL,
            verificationDate = VERIFICADO,
            verified = true
        ),
        PsychologicalRoute(
            id = "route_linea_106",
            name = "Línea 106",
            category = RouteCategory.TELEFONICA,
            phone = "106",
            whatsapp = null,
            address = "Marcación corta desde cualquier teléfono",
            comuna = "Medellín y corregimientos",
            schedule = "24 horas, 7 días a la semana",
            requiresAppointment = false,
            costDescription = "Gratuita",
            description = "Funciona igual que la Línea Amiga Saludable: psicología y orientación en salud mental.",
            mapQuery = "Medellin",
            verificationSource = "Alcaldía de Medellín, noticia del 6 nov 2023",
            verificationUrl = LINEA_106_URL,
            verificationDate = VERIFICADO,
            verified = true
        ),
        PsychologicalRoute(
            id = "route_escuchadero_san_javier",
            name = "Escuchadero Estación San Javier",
            category = RouteCategory.ESCUCHADERO,
            phone = "(604) 444 44 48",
            whatsapp = null,
            address = "Estación San Javier del Metro, zona de torniquetes del Metrocable J",
            comuna = "Comuna 13 (San Javier)",
            schedule = "Confirma el horario en la Línea Amiga",
            requiresAppointment = false,
            costDescription = "Servicio público de la Alcaldía",
            description = "Espacio presencial de escucha en salud mental. El teléfono es el de la Línea Amiga para informes.",
            mapQuery = "Estacion San Javier Metro Medellin",
            verificationSource = "El Colombiano, 19 may 2026",
            verificationUrl = ESCUCHADEROS_URL,
            verificationDate = VERIFICADO,
            verified = true
        ),
        PsychologicalRoute(
            id = "route_escuchadero_acevedo",
            name = "Escuchadero Estación Acevedo",
            category = RouteCategory.ESCUCHADERO,
            phone = "(604) 444 44 48",
            whatsapp = null,
            address = "Estación Acevedo del Metro, junto al PAC, tercer nivel",
            comuna = "Comuna 5 (Castilla)",
            schedule = "Confirma el horario en la Línea Amiga",
            requiresAppointment = false,
            costDescription = "Servicio público de la Alcaldía",
            description = "Espacio presencial de escucha en salud mental. El teléfono es el de la Línea Amiga para informes.",
            mapQuery = "Estacion Acevedo Metro Medellin",
            verificationSource = "El Colombiano, 19 may 2026",
            verificationUrl = ESCUCHADEROS_URL,
            verificationDate = VERIFICADO,
            verified = true
        ),
        PsychologicalRoute(
            id = "route_escuchadero_la_aurora",
            name = "Escuchadero Estación La Aurora",
            category = RouteCategory.ESCUCHADERO,
            phone = "(604) 444 44 48",
            whatsapp = null,
            address = "Estación La Aurora, Metrocable línea J",
            comuna = "San Javier / La Aurora",
            schedule = "Confirma el horario en la Línea Amiga",
            requiresAppointment = false,
            costDescription = "Servicio público de la Alcaldía",
            description = "Espacio presencial de escucha en salud mental. El teléfono es el de la Línea Amiga para informes.",
            mapQuery = "Estacion La Aurora Metrocable Medellin",
            verificationSource = "El Colombiano, 19 may 2026",
            verificationUrl = ESCUCHADEROS_URL,
            verificationDate = VERIFICADO,
            verified = true
        ),
        PsychologicalRoute(
            id = "route_linea_123",
            name = "Línea 123 (emergencias)",
            category = RouteCategory.TELEFONICA,
            phone = "123",
            whatsapp = null,
            address = "Línea única de emergencias",
            comuna = "Medellín y Área Metropolitana",
            schedule = "Emergencias",
            requiresAppointment = false,
            costDescription = "Gratuita",
            description = "Para una emergencia en la que alguien esté en peligro ahora mismo. Incluye 123 Social para emergencias sociales y familiares.",
            mapQuery = "Medellin 123",
            verificationSource = "Alcaldía de Medellín, página 123 Social",
            verificationUrl = SOCIAL_123_URL,
            verificationDate = VERIFICADO,
            verified = true
        ),
        // --- Pendientes de verificación por el equipo: NO se muestran en la app ---
        PsychologicalRoute(
            id = "route_escuchadero_san_antonio", name = "Escuchadero Estación San Antonio", category = RouteCategory.ESCUCHADERO,
            phone = "", address = "Estación San Antonio del Metro", comuna = "Comuna 10 (La Candelaria)", schedule = "",
            requiresAppointment = false, costDescription = "", description = "", mapQuery = "Estacion San Antonio Metro Medellin",
            verificationSource = "Sin fuente encontrada", verificationUrl = "", verificationDate = "", verified = false
        ),
        PsychologicalRoute(
            id = "route_consultorio_udea", name = "Consultorio Psicológico UdeA", category = RouteCategory.UNIVERSITARIO,
            phone = "(604) 219 57 58", address = "Ciudad Universitaria, Universidad de Antioquia", comuna = "Comuna 4", schedule = "",
            requiresAppointment = true, costDescription = "", description = "", mapQuery = "Universidad de Antioquia",
            verificationSource = "Sin fuente encontrada", verificationUrl = "", verificationDate = "", verified = false
        ),
        PsychologicalRoute(
            id = "route_consultorio_upb", name = "Centro de Atención Psicológica UPB", category = RouteCategory.UNIVERSITARIO,
            phone = "(604) 448 83 88", address = "Campus Laureles, UPB", comuna = "Comuna 11", schedule = "",
            requiresAppointment = true, costDescription = "", description = "", mapQuery = "Universidad Pontificia Bolivariana Laureles",
            verificationSource = "Sin fuente encontrada", verificationUrl = "", verificationDate = "", verified = false
        )
    )

    val visibleRoutes: List<PsychologicalRoute> get() = allRoutes.filter { it.verified }
}
