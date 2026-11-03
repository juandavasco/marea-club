package com.example.data

import com.example.domain.IslandRules
import com.example.model.*

/**
 * Contenido fijo de la app: actividades, elementos de la isla, rutas de apoyo y cuestionarios.
 * Separado del almacenamiento (Documento Técnico §21).
 */
object MareaContent {

    /** Rango de edad de trabajo (decisión 1). Cambiar aquí si el equipo lo confirma distinto. */
    const val AGE_RANGE_LABEL = "11 a 14"

    private fun rewardFor(activityId: String): IslandRules.Unlock =
        requireNotNull(IslandRules.unlockForSource(activityId)) { "Actividad sin elemento de isla: $activityId" }

    private fun activity(
        id: String,
        title: String,
        category: ActivityCategory,
        durationMin: String,
        description: String,
        recommendedFor: List<MareaType>
    ): ActivityItem {
        val reward = rewardFor(id)
        return ActivityItem(id, title, category, durationMin, description, reward.elementName, reward.icon, recommendedFor)
    }

    val activities: List<ActivityItem> = listOf(
        activity(
            "act_respiracion", "Respiración del Océano", ActivityCategory.RESPIRACION, "3 min",
            "Respira siguiendo las olas con Muni para soltar tensiones y serenar tu mente.",
            listOf(MareaType.MOVIDA, MareaType.ALTA, MareaType.TORMENTA, MareaType.NO_SE)
        ),
        activity(
            "act_diario", "Diario del Mar", ActivityCategory.ESCRITURA, "5 min",
            "Un espacio íntimo y seguro para escribir lo que sientes sin juicios ni notas. Lo que escribas queda solo en tu teléfono.",
            listOf(MareaType.MOVIDA, MareaType.ALTA, MareaType.TRANQUILA)
        ),
        activity(
            "act_emociones", "Brújula Emocional", ActivityCategory.AUTOCONOCIMIENTO, "4 min",
            "Identifica qué necesita tu cuerpo y tu mente en este instante de marea.",
            listOf(MareaType.NO_SE, MareaType.MOVIDA, MareaType.TRANQUILA)
        ),
        activity(
            "act_autoestima", "Constelación de Fortalezas", ActivityCategory.AUTOESTIMA, "4 min",
            "Descubre y enciende las cualidades que te hacen único y valioso.",
            listOf(MareaType.TRANQUILA, MareaType.MOVIDA, MareaType.TORMENTA)
        ),
        activity(
            "act_conchas", "Reto de Conchas y Perlas", ActivityCategory.ATENCION, "2 min",
            "Entrena tu atención y presencia encontrando la perla escondida en la costa.",
            listOf(MareaType.MOVIDA, MareaType.TRANQUILA, MareaType.NO_SE)
        ),
        activity(
            "act_dibujo", "Lienzo de Expresión Libre", ActivityCategory.CREATIVIDAD, "Libre",
            "Pinta tus sensaciones con los colores oceánicos de Marea Club.",
            listOf(MareaType.TRANQUILA, MareaType.ALTA, MareaType.TORMENTA)
        )
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
        "act_dibujo" to ActivityGuide(
            "Expresar con colores lo que es difícil decir.",
            listOf("Elige un color.", "Dibuja o raya libremente.", "Termina cuando quieras; el dibujo no se guarda."),
            "Si prefieres palabras, prueba el Diario del Mar."
        )
    )

    val islandElements: List<IslandElement> = listOf(
        IslandElement("arbol_verde", "Árbol Sabio de Muni", ActivityCategory.AUTOCONOCIMIENTO, "Brinda sombra y refugio a las ideas."),
        IslandElement("sofa_azul", "Sillón de Arena Azul", ActivityCategory.ESCRITURA, "Lugar cómodo para reposar y escribir."),
        IslandElement("estanque_muni", "Estanque Cristalino", ActivityCategory.RESPIRACION, "Aguas tranquilas donde Muni nada a gusto."),
        IslandElement("muelle_madera", "Muelle del Horizonte", ActivityCategory.ESCRITURA, "Para contemplar la marea y pensar en tu semana."),
        IslandElement("farol_calma", "Farol de la Calma", ActivityCategory.RESPIRACION, "Ilumina las noches de tormenta."),
        IslandElement("flores_coral", "Flores de Coral", ActivityCategory.RESPIRACION, "Nacidas de tus momentos de respiración."),
        IslandElement("camino_caracoles", "Camino de Caracoles", ActivityCategory.ATENCION, "Marca tus pasos de concentración."),
        IslandElement("estrella_marina", "Estrella de Fortalezas", ActivityCategory.AUTOESTIMA, "Recuerda lo valiente y capaz que eres."),
        IslandElement("arrecife_colores", "Arrecife Creativo", ActivityCategory.CREATIVIDAD, "Colorido homenaje a tu imaginación.")
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

    val questionnaires: List<QuestionnaireDefinition> = listOf(
        QuestionnaireDefinition(
            id = "quest_bienestar",
            title = "Bienestar Emocional General",
            iconEmoji = "🌊",
            subtitle = "Equilibrio, tranquilidad y vitalidad cotidiana",
            introduction = "Este cuestionario te ayuda a explorar cómo ha estado tu sensación general de calma, energía y satisfacción en las últimas semanas.",
            objective = "Identificar si tus emociones cotidianas fluyen con soltura o si hay una acumulación de mareas difíciles.",
            questions = listOf(
                QuestionItem(
                    "q1",
                    "¿Con qué frecuencia te has sentido tranquilo/a y a gusto contigo mismo/a?",
                    listOf(QuestionOption("La mayor parte de los días", 0), QuestionOption("Algunos días sí y otros no", 1), QuestionOption("Casi nunca o muy pocas veces", 2))
                ),
                QuestionItem(
                    "q2",
                    "¿Sientes que tienes energía y motivación para hacer las cosas que te gustan?",
                    listOf(QuestionOption("Sí, la mayoría del tiempo", 0), QuestionOption("A veces me cuesta arrancar", 1), QuestionOption("Me siento sin ganas casi siempre", 2))
                ),
                QuestionItem(
                    "q3",
                    "Cuando algo te sale mal o te frustra, ¿logras calmarte después de un rato?",
                    listOf(QuestionOption("Sí, encuentro formas de estar mejor", 0), QuestionOption("Me quedo pensando un buen rato", 1), QuestionOption("El malestar me dura días enteros", 2))
                ),
                QuestionItem(
                    "q4",
                    "¿Sientes que cuentas con espacios o momentos donde puedes estar en paz?",
                    listOf(QuestionOption("Sí, tengo lugares y momentos seguros", 0), QuestionOption("A veces es difícil encontrarlos", 1), QuestionOption("Casi nunca me siento en paz", 2))
                )
            )
        ),
        QuestionnaireDefinition(
            id = "quest_estres",
            title = "Estrés Académico y Escolar",
            iconEmoji = "📚",
            subtitle = "Tareas, exámenes, exigencias y descanso",
            introduction = "A veces el colegio o las responsabilidades pueden sentirse como una marea muy rápida.",
            objective = "Reconocer si la carga de estudio te está sobrepasando y qué pasos te pueden devolver la tranquilidad.",
            questions = listOf(
                QuestionItem(
                    "q1",
                    "¿Sientes nervios intensos o dolor de estómago antes de exámenes o presentaciones?",
                    listOf(QuestionOption("Rara vez, me siento preparado/a", 0), QuestionOption("A veces, pero se me pasa al empezar", 1), QuestionOption("Casi siempre y me paraliza", 2))
                ),
                QuestionItem(
                    "q2",
                    "¿Sientes que el tiempo libre no te alcanza para descansar por pensar en las tareas?",
                    listOf(QuestionOption("Logro separar estudio y descanso", 0), QuestionOption("A veces me preocupa lo pendiente", 1), QuestionOption("Siento que nunca puedo desconectar", 2))
                ),
                QuestionItem(
                    "q3",
                    "¿Te exiges notas perfectas y te culpas mucho si no alcanzas la máxima calificación?",
                    listOf(QuestionOption("Acepto que equivocarse es normal", 0), QuestionOption("Me da rabia o tristeza un rato", 1), QuestionOption("Siento que si fallo no valgo nada", 2))
                ),
                QuestionItem(
                    "q4",
                    "¿Has dejado de dormir bien por quedarte pensando en cosas del colegio?",
                    listOf(QuestionOption("Duermo tranquilo/a", 0), QuestionOption("Ocasionalmente me cuesta conciliar el sueño", 1), QuestionOption("Me despierto angustiado/a con frecuencia", 2))
                )
            )
        ),
        QuestionnaireDefinition(
            id = "quest_autoestima",
            title = "Autoestima y Confianza",
            iconEmoji = "⭐",
            subtitle = "Cómo te ves, tus fortalezas y tu valor",
            introduction = "Tu valor como persona no depende de la opinión de otros ni de un día difícil.",
            objective = "Explorar la relación contigo mismo/a y reconocer tus cualidades positivas.",
            questions = listOf(
                QuestionItem(
                    "q1",
                    "¿Te resulta fácil reconocer al menos tres cosas buenas en ti (talentos, bondad, humor)?",
                    listOf(QuestionOption("Sí, reconozco mis cualidades", 0), QuestionOption("Me cuesta un poco, pero sé que las tengo", 1), QuestionOption("Siento que no soy bueno/a en nada", 2))
                ),
                QuestionItem(
                    "q2",
                    "¿Te comparas constantemente con tus compañeros o con lo que ves en redes?",
                    listOf(QuestionOption("Casi no me comparo, soy yo mismo/a", 0), QuestionOption("A veces siento que otros son mejores", 1), QuestionOption("Todo el tiempo siento que me quedo atrás", 2))
                ),
                QuestionItem(
                    "q3",
                    "Cuando recibes un cumplido o felicitación, ¿cómo reaccionas?",
                    listOf(QuestionOption("Lo agradezco con alegría", 0), QuestionOption("Me da pena pero me gusta", 1), QuestionOption("Siento que no lo merezco o que mienten", 2))
                ),
                QuestionItem(
                    "q4",
                    "¿Sientes que tienes derecho a opinar y decir 'no' cuando algo no te agrada?",
                    listOf(QuestionOption("Sí, expreso lo que pienso con respeto", 0), QuestionOption("A veces cedo para evitar problemas", 1), QuestionOption("Nunca me atrevo a decir lo que quiero", 2))
                )
            )
        ),
        QuestionnaireDefinition(
            id = "quest_preocupaciones",
            title = "Preocupaciones y Nerviosismo",
            iconEmoji = "🌪️",
            subtitle = "Inquietud mental, pensamientos rápidos y tensión",
            introduction = "Tener inquietudes es natural, pero cuando la mente se llena de '¿y si pasa algo malo?', se vuelve pesado.",
            objective = "Detectar señales de alerta de intranquilidad y ofrecer herramientas de desconexión.",
            questions = listOf(
                QuestionItem(
                    "q1",
                    "¿Tu cabeza se llena de pensamientos rápidos que saltan de una preocupación a otra?",
                    listOf(QuestionOption("Casi nunca, mi mente suele estar serena", 0), QuestionOption("A veces cuando tengo muchas cosas juntas", 1), QuestionOption("Casi todos los días y no sé cómo frenarlo", 2))
                ),
                QuestionItem(
                    "q2",
                    "¿Sientes tensión física como hombros apretados, respiración corta o mandíbula cerrada?",
                    listOf(QuestionOption("Mi cuerpo se siente relajado", 0), QuestionOption("A veces cuando estoy bajo presión", 1), QuestionOption("Tengo esa tensión en el cuerpo casi a diario", 2))
                ),
                QuestionItem(
                    "q3",
                    "¿Te preocupas con mucha anticipación por cosas que tal vez ni siquiera ocurran?",
                    listOf(QuestionOption("Prefiero ocuparme cuando llegue el momento", 0), QuestionOption("A veces imagino escenarios difíciles", 1), QuestionOption("Siempre me preparo para lo peor", 2))
                ),
                QuestionItem(
                    "q4",
                    "¿Te cuesta quedarte quieto/a o concentrarte en una película o lectura por estar nervioso/a?",
                    listOf(QuestionOption("Puedo disfrutar y concentrarme", 0), QuestionOption("A veces me distraigo con facilidad", 1), QuestionOption("Siento una inquietud interna constante", 2))
                )
            )
        ),
        QuestionnaireDefinition(
            id = "quest_animo",
            title = "Estado de Ánimo y Vitalidad",
            iconEmoji = "🌧️",
            subtitle = "Días grises, ganas de hacer cosas y disfrute",
            introduction = "Todas las mareas tienen momentos de bajamar, pero si el agua se queda estancada, conviene pedir compañía.",
            objective = "Monitorear la presencia de tristeza prolongada o pérdida de interés.",
            questions = listOf(
                QuestionItem(
                    "q1",
                    "¿En las últimas dos semanas te has sentido con tristeza o ganas de llorar sin motivo claro?",
                    listOf(QuestionOption("No, he tenido buen ánimo en general", 0), QuestionOption("Uno o dos días, pero luego me recuperé", 1), QuestionOption("Varios días a la semana o casi todos los días", 2))
                ),
                QuestionItem(
                    "q2",
                    "Las actividades que antes te encantaban (jugar, dibujar, salir), ¿siguen dándote alegría?",
                    listOf(QuestionOption("Sí, las disfruto igual que siempre", 0), QuestionOption("A veces no tengo tanta energía", 1), QuestionOption("Ya nada me produce entusiasmo", 2))
                ),
                QuestionItem(
                    "q3",
                    "¿Prefieres quedarte encerrado/a solo/a en tu cuarto evitando hablar con los demás?",
                    listOf(QuestionOption("Me gusta compartir con mi familia y amigos", 0), QuestionOption("A veces necesito un rato a solas", 1), QuestionOption("Evito a todo el mundo la mayor parte del tiempo", 2))
                ),
                QuestionItem(
                    "q4",
                    "¿Sientes que el futuro puede ser bonito y traer cosas positivas para ti?",
                    listOf(QuestionOption("Sí, me ilusionan varias cosas", 0), QuestionOption("Tengo dudas, pero mantengo la esperanza", 1), QuestionOption("Siento que todo va a salir mal", 2))
                )
            )
        ),
        QuestionnaireDefinition(
            id = "quest_relaciones",
            title = "Relaciones Sociales y Presión",
            iconEmoji = "👥",
            subtitle = "Amigos, encajar en el grupo y límites personales",
            introduction = "Compartir con otros enriquece la vida, pero no debes cambiar quién eres para sentirte aceptado/a.",
            objective = "Valorar si tus amistades te hacen sentir seguro/a o si sientes presión incómoda.",
            questions = listOf(
                QuestionItem(
                    "q1",
                    "¿Sientes que tus amigos/as te aceptan tal como eres sin pedirte que cambies?",
                    listOf(QuestionOption("Sí, me siento seguro/a con ellos", 0), QuestionOption("A veces me siento un poco juzgado/a", 1), QuestionOption("Siento que tengo que fingir para encajar", 2))
                ),
                QuestionItem(
                    "q2",
                    "¿Has hecho cosas que no querías hacer solo por miedo a que se burlen o te dejen de hablar?",
                    listOf(QuestionOption("No, mantengo mis límites", 0), QuestionOption("Alguna vez me ha pasado", 1), QuestionOption("Me pasa con mucha frecuencia", 2))
                ),
                QuestionItem(
                    "q3",
                    "Si alguien del colegio te hace comentarios hirientes o te excluye, ¿tienes con quién hablarlo?",
                    listOf(QuestionOption("Sí, cuento con mis padres, profesores o amigos", 0), QuestionOption("Me da un poco de miedo contarlo", 1), QuestionOption("Me lo guardo todo porque nadie me ayudaría", 2))
                ),
                QuestionItem(
                    "q4",
                    "¿Te sientes solo/a en el colegio durante los descansos o recreos?",
                    listOf(QuestionOption("Casi siempre tengo compañía agradable", 0), QuestionOption("A veces me siento un poco fuera de lugar", 1), QuestionOption("Casi siempre me siento aislado/a y triste", 2))
                )
            )
        ),
        QuestionnaireDefinition(
            id = "quest_expresion",
            title = "Reconocimiento y Expresión Emocional",
            iconEmoji = "🎨",
            subtitle = "Ponerle nombre a lo que sientes y soltarlo",
            introduction = "Aprender a nombrar las emociones es el primer paso para que dejen de ser olas desbordadas.",
            objective = "Descubrir qué tan cómodo/a te sientes expresando tus estados interiores.",
            questions = listOf(
                QuestionItem(
                    "q1",
                    "Cuando sientes un nudo en la garganta o el pecho, ¿identificas qué emoción es?",
                    listOf(QuestionOption("Sí, sé si es tristeza, miedo, enojo o cansancio", 0), QuestionOption("A veces me confundo, pero lo intento", 1), QuestionOption("Casi nunca sé qué me está pasando", 2))
                ),
                QuestionItem(
                    "q2",
                    "¿Te cuesta pedir un abrazo, tiempo a solas o ayuda cuando tienes un día difícil?",
                    listOf(QuestionOption("Pido lo que necesito sin problema", 0), QuestionOption("A veces me cuesta expresarlo", 1), QuestionOption("Prefiero guardarme todo aunque me duela", 2))
                ),
                QuestionItem(
                    "q3",
                    "¿Utilizas formas creativas para canalizar lo que sientes (escribir, dibujar, respirar, música)?",
                    listOf(QuestionOption("Sí, me ayuda mucho crear y expresarme", 0), QuestionOption("A veces lo intento", 1), QuestionOption("No sé cómo desahogarme de forma sana", 2))
                ),
                QuestionItem(
                    "q4",
                    "¿Te permites llorar o sentirte vulnerable sin criticarte a ti mismo/a?",
                    listOf(QuestionOption("Sí, sé que todas las emociones son válidas", 0), QuestionOption("A veces siento algo de culpa", 1), QuestionOption("Siento que llorar es de débiles y me lo prohíbo", 2))
                )
            )
        )
    )
}
