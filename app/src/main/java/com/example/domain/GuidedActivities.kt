package com.example.domain

/**
 * Las 6 experiencias infantiles del Documento Técnico §6 y el Prompt Maestro §5.3.
 *
 * Cada actividad tiene objetivo, pasos con interacción real, conclusión aplicable y salida voluntaria.
 * No hay puntajes, respuestas correctas ni comparaciones. Las respuestas son privadas y solo
 * se guardan si el niño elige "Guardar en mi diario".
 *
 * ESTADO DEL CONTENIDO: borrador basado en el documento, PENDIENTE DE REVISIÓN PSICOLÓGICA.
 * Sin dependencias de Android para probarse con JUnit puro.
 */
object GuidedActivities {

    const val CONTENT_STATUS = "Borrador pendiente de revisión psicológica"

    data class Option(val id: String, val label: String, val detail: String = "")

    /** Respuestas acumuladas: id del paso → ids de opciones elegidas (o texto en [Answers.texts]). */
    data class Answers(
        val choices: Map<String, List<String>> = emptyMap(),
        val texts: Map<String, String> = emptyMap()
    ) {
        fun first(stepId: String): String? = choices[stepId]?.firstOrNull()
        fun all(stepId: String): List<String> = choices[stepId].orEmpty()
    }

    sealed interface Step {
        val id: String
        val prompt: String
        val help: String
        /** El niño puede seguir sin responder este paso. */
        val optional: Boolean
    }

    /** Elegir una o varias opciones. [dynamicOptions] permite opciones que dependen de pasos anteriores. */
    data class Choice(
        override val id: String,
        override val prompt: String,
        val options: List<Option> = emptyList(),
        val maxSelected: Int = 1,
        override val help: String = "",
        override val optional: Boolean = false,
        val dynamicOptions: ((Answers) -> List<Option>)? = null
    ) : Step {
        fun optionsFor(answers: Answers): List<Option> = dynamicOptions?.invoke(answers) ?: options
    }

    /** Elegir exactamente dos emociones y verlas juntas (actividad "Dos mareas al mismo tiempo"). */
    data class TwoTides(
        override val id: String,
        override val prompt: String,
        val options: List<Option>,
        override val help: String = "",
        override val optional: Boolean = false
    ) : Step

    /** Pausa guiada antes de seguir (actividad "Antes de responder"). */
    data class Pause(
        override val id: String,
        override val prompt: String,
        val seconds: Int,
        override val help: String = "",
        override val optional: Boolean = true
    ) : Step

    /** Muestra lo que podría pasar con la opción elegida en [sourceStepId] y deja explorar las demás. */
    data class Consequences(
        override val id: String,
        override val prompt: String,
        val sourceStepId: String,
        override val help: String = "",
        override val optional: Boolean = true
    ) : Step

    /** Frase editable construida con las respuestas; guardarla es opcional. */
    data class Phrase(
        override val id: String,
        override val prompt: String,
        val build: (Answers) -> String,
        override val help: String = "",
        override val optional: Boolean = true
    ) : Step

    /** Resumen de lo elegido, en filas "etiqueta → valor". */
    data class Summary(
        override val id: String,
        override val prompt: String,
        val build: (Answers) -> List<Pair<String, String>>,
        override val help: String = "",
        override val optional: Boolean = true
    ) : Step

    data class Activity(
        val id: String,
        val title: String,
        val objective: String,
        val skill: String,
        val minutes: String,
        val intro: String,
        val steps: List<Step>,
        val conclusion: String,
        val dailyTip: String,
        /** Objetos de la isla que se pueden elegir al terminar. */
        val islandOffers: List<String>
    ) {
        fun stepById(id: String): Step? = steps.firstOrNull { it.id == id }
    }

    private fun opts(vararg pairs: Pair<String, String>) = pairs.map { Option(it.first, it.second) }

    private fun label(step: Choice, answers: Answers): String =
        answers.all(step.id).mapNotNull { id -> step.optionsFor(answers).firstOrNull { it.id == id }?.label }
            .joinToString(", ").ifBlank { "No lo elegiste" }

    // ---------------------------------------------------------------------------------
    // 1. Mi mapa de situaciones
    // ---------------------------------------------------------------------------------
    private val mapaSituacion = Choice(
        "situacion", "¿Qué situación quieres mirar?",
        opts(
            "no_sale" to "Cuando algo no sale como esperaba",
            "examen" to "Antes de un examen o una exposición",
            "discusion" to "Cuando discuto con alguien",
            "mucha_gente" to "Cuando estoy con mucha gente",
            "solo" to "Cuando me quedo solo o sola",
            "cambio_planes" to "Cuando cambian los planes"
        ),
        help = "Elige la que más se parezca. No tienes que contar detalles."
    )
    private val mapaLugar = Choice(
        "lugar", "¿Dónde suele pasar?",
        opts(
            "colegio" to "En el colegio", "casa" to "En casa", "amigos" to "Con amigos",
            "celular" to "En el celular o internet", "calle" to "En la calle o el transporte", "otro" to "En otro lugar"
        ),
        optional = true
    )
    private val mapaSiento = Choice(
        "siento", "¿Qué marea sueles sentir ahí?",
        opts(
            "TRANQUILA" to "Tranquila", "MOVIDA" to "Movida", "ALTA" to "Alta",
            "TORMENTA" to "Tormenta", "NO_SE" to "No sé qué siento"
        )
    )
    private val mapaReaccion = Choice(
        "reaccion", "¿Cómo sueles reaccionar?",
        opts(
            "callado" to "Me quedo en silencio", "alejo" to "Me alejo", "hablo" to "Hablo con alguien",
            "enojo" to "Me enojo o subo la voz", "lloro" to "Lloro", "distraigo" to "Me distraigo con otra cosa",
            "como_si_nada" to "Hago como si nada"
        ),
        maxSelected = 2,
        help = "Puedes elegir hasta dos."
    )
    val mapaDeSituaciones = Activity(
        id = "act_mapa_situaciones",
        title = "Mi mapa de situaciones",
        objective = "Reconocer situaciones cotidianas y relacionarlas con lo que sientes.",
        skill = "Identificar contextos y vocabulario emocional",
        minutes = "5 min",
        intro = "Vas a separar tres cosas que a veces se mezclan: qué pasó, qué sentiste y cómo reaccionaste.",
        steps = listOf(
            mapaSituacion, mapaLugar, mapaSiento, mapaReaccion,
            Summary("mapa", "Tu mapa", build = { a ->
                listOf(
                    "Qué pasa" to label(mapaSituacion, a),
                    "Dónde" to label(mapaLugar, a),
                    "Qué siento" to label(mapaSiento, a),
                    "Cómo reacciono" to label(mapaReaccion, a)
                )
            })
        ),
        conclusion = "Separar lo que pasó, lo que sentiste y lo que hiciste te ayuda a entenderte. Una emoción puede aparecer en un lugar y no en otro; eso no te define.",
        dailyTip = "La próxima vez que te pase, intenta nombrar las tres partes en tu cabeza.",
        islandOffers = listOf("jardin", "faro")
    )

    // ---------------------------------------------------------------------------------
    // 2. Dos mareas al mismo tiempo
    // ---------------------------------------------------------------------------------
    private val dosEscena = Choice(
        "escena", "Piensa en un momento como este:",
        opts(
            "lugar_nuevo" to "Ir a un lugar nuevo",
            "fin_anio" to "Terminar el año escolar",
            "cambio" to "Mudarme o cambiar de colegio",
            "presentar" to "Presentar algo frente a otros",
            "despedida" to "Despedirme de alguien",
            "fiesta" to "Un cumpleaños o una fiesta"
        )
    )
    private val emociones = opts(
        "alegria" to "Alegría", "nervios" to "Nervios", "tristeza" to "Tristeza", "emocion" to "Emoción",
        "miedo" to "Miedo", "calma" to "Calma", "preocupacion" to "Preocupación", "orgullo" to "Orgullo",
        "enojo" to "Enojo", "cansancio" to "Cansancio"
    )
    private val dosMareas = TwoTides(
        "dos", "Elige dos cosas que podrías sentir al mismo tiempo",
        emociones,
        help = "Elige exactamente dos. Pueden parecer opuestas."
    )
    private val dosFuerte = Choice(
        "fuerte", "¿Cuál se siente más fuerte?",
        dynamicOptions = { a ->
            val chosen = a.all("dos").mapNotNull { id -> emociones.firstOrNull { it.id == id } }
            chosen + Option("iguales", "Las dos igual") + Option("cambia", "Depende del momento")
        },
        optional = true
    )
    val dosMareasAlMismoTiempo = Activity(
        id = "act_dos_mareas",
        title = "Dos mareas al mismo tiempo",
        objective = "Explorar que pueden existir emociones distintas a la vez.",
        skill = "Matices emocionales y tolerancia a la ambivalencia",
        minutes = "4 min",
        intro = "A veces sentimos dos cosas a la vez, como \"me alegra ir, pero también me da nervios\". Vamos a mirarlo.",
        steps = listOf(
            dosEscena, dosMareas, dosFuerte,
            Summary("frase", "Tu frase", build = { a ->
                val chosen = a.all("dos").mapNotNull { id -> emociones.firstOrNull { it.id == id }?.label?.lowercase() }
                val text = if (chosen.size == 2) "Siento ${chosen[0]} y también ${chosen[1]}." else "Puedo sentir más de una cosa."
                listOf("Momento" to label(dosEscena, a), "Lo que siento" to text)
            })
        ),
        conclusion = "Sentir dos cosas a la vez no es estar confundido o confundida: es normal cuando algo te importa. Puedes darle espacio a las dos.",
        dailyTip = "Prueba decir: \"Me siento ___ y también ___\".",
        islandOffers = listOf("jardin", "hamaca")
    )

    // ---------------------------------------------------------------------------------
    // 3. Antes de responder
    // ---------------------------------------------------------------------------------
    private val antesEscena = Choice(
        "escena", "Elige una escena",
        listOf(
            Option("mensaje", "Te llega un mensaje molesto en el grupo del curso", "Desacuerdos"),
            Option("presion", "Tus amigos quieren que hagas algo con lo que no estás de acuerdo", "Presión social"),
            Option("excluido", "Te dejan por fuera de un plan", "Exclusión"),
            Option("secreto", "Un amigo cuenta algo tuyo sin permiso", "Conflicto entre amigos"),
            Option("risa", "Te equivocas frente a la clase y alguien se ríe", "Situación escolar"),
            Option("empujon", "Alguien te empuja en el descanso", "Impulsividad")
        )
    )
    private val antesOpciones = Choice(
        "respuesta", "¿Qué podrías hacer?",
        listOf(
            Option("ya", "Responder enseguida lo primero que pienso", "Sacas lo que sientes rápido, pero a veces sale algo que después no querías decir."),
            Option("esperar", "Esperar un rato antes de responder", "Te da tiempo para que la marea baje; la otra persona puede tener que esperar tu respuesta."),
            Option("preguntar", "Preguntar qué quiso decir", "Puede aclarar un malentendido; también puede que la respuesta no te guste."),
            Option("decir", "Decir con calma cómo me sentí", "La otra persona conoce tu punto de vista; no controlas cómo va a reaccionar."),
            Option("adulto", "Hablar con un adulto de confianza", "Tienes apoyo y otra mirada; puede tomar un poco más de tiempo."),
            Option("alejarme", "Alejarme por ahora", "Te cuidas en el momento; quizá más adelante quieras hablarlo.")
        ),
        help = "No hay una respuesta correcta para todos."
    )
    val antesDeResponder = Activity(
        id = "act_antes_responder",
        title = "Antes de responder",
        objective = "Practicar una pausa antes de reaccionar.",
        skill = "Pausa y elección de alternativas",
        minutes = "5 min",
        intro = "Vas a leer una escena, hacer una pausa y mirar varias formas de responder con sus posibles efectos.",
        steps = listOf(
            antesEscena,
            Pause("pausa", "Antes de responder, haz una pausa", seconds = 5, help = "Respira mientras la ola sube y baja."),
            antesOpciones,
            Consequences("efectos", "Lo que podría pasar", sourceStepId = "respuesta", help = "Toca otras opciones para comparar.")
        ),
        conclusion = "La pausa no es quedarte callado para siempre: es darte tiempo para elegir. No garantiza que todo salga bien, pero te da más opciones.",
        dailyTip = "Cuando algo te moleste, cuenta tres respiraciones antes de responder.",
        islandOffers = listOf("puente", "faro")
    )

    // ---------------------------------------------------------------------------------
    // 4. Lo que necesito y lo que puedo pedir
    // ---------------------------------------------------------------------------------
    private val necesitoSituacion = Choice(
        "situacion", "¿Qué está pasando?",
        opts(
            "cansado" to "Estoy muy cansado o cansada después del colegio",
            "solo" to "Me siento solo o sola",
            "tarea" to "Tengo mucha tarea y no sé por dónde empezar",
            "no_escuchan" to "Cuento algo y siento que no me escuchan",
            "limite" to "Alguien hace algo que no me gusta",
            "triste" to "Estoy triste y no sé bien por qué"
        )
    )
    private val necesidades = opts(
        "espacio" to "Necesito espacio", "compania" to "Necesito compañía", "ayuda" to "Necesito ayuda",
        "escuchado" to "Quiero que me escuchen", "limite" to "Necesito poner un límite"
    )
    private val necesitoQue = Choice("necesidad", "¿Qué crees que necesitas?", necesidades, help = "Elige la que más se acerque.")
    private val necesitoQuien = Choice(
        "quien", "¿A quién se lo podrías pedir?",
        opts("familia" to "A alguien de mi familia", "amigo" to "A un amigo o amiga", "profe" to "A un profesor u orientador", "prefiero" to "Prefiero no decirlo"),
        optional = true
    )
    val loQueNecesito = Activity(
        id = "act_lo_que_necesito",
        title = "Lo que necesito y lo que puedo pedir",
        objective = "Identificar lo que necesitas y practicar cómo pedirlo.",
        skill = "Reconocer necesidades y comunicarte con asertividad",
        minutes = "5 min",
        intro = "Vas a relacionar una situación con lo que necesitas y armar una frase para pedirlo.",
        steps = listOf(
            necesitoSituacion, necesitoQue, necesitoQuien,
            Phrase("frase", "Tu frase para pedirlo", build = { a ->
                when (a.first("necesidad")) {
                    "espacio" -> "¿Me das un momento? Necesito estar un rato a solas y después hablamos."
                    "compania" -> "¿Puedes quedarte conmigo un rato?"
                    "ayuda" -> "Necesito ayuda con esto, ¿me ayudas?"
                    "escuchado" -> "¿Puedes escucharme un momento, sin darme consejos por ahora?"
                    "limite" -> "No me gusta que hagas eso. Por favor, para."
                    else -> "Necesito algo y quiero contártelo."
                }
            }, help = "Puedes cambiar las palabras para que suenen como tú.")
        ),
        conclusion = "Pedir lo que necesitas es una habilidad que se practica. No siempre te dirán que sí, y aun así tienes derecho a pedir.",
        dailyTip = "Elige un momento tranquilo para usar tu frase.",
        islandOffers = listOf("puente", "faro", "hamaca")
    )

    // ---------------------------------------------------------------------------------
    // 5. Lo que no se ve de mí
    // ---------------------------------------------------------------------------------
    private val noSeVeIntereses = Choice(
        "intereses", "Cosas que me interesan",
        opts(
            "musica" to "Música", "dibujo" to "Dibujar", "deporte" to "Deportes", "videojuegos" to "Videojuegos",
            "leer" to "Leer", "naturaleza" to "Naturaleza", "cocinar" to "Cocinar", "ciencia" to "Ciencia",
            "ayudar" to "Ayudar a otros", "bailar" to "Bailar", "animales" to "Animales", "tecnologia" to "Tecnología"
        ),
        maxSelected = 3, help = "Elige hasta tres.", optional = true
    )
    private val noSeVeCapacidades = Choice(
        "capacidades", "Algo que se me da bien",
        opts(
            "escuchar" to "Escuchar", "reir" to "Hacer reír", "organizar" to "Organizar", "crear" to "Crear cosas",
            "aprender" to "Aprender cosas nuevas", "paciencia" to "Tener paciencia", "defender" to "Defender a otros",
            "resolver" to "Resolver problemas"
        ),
        maxSelected = 3, help = "Elige hasta tres.", optional = true
    )
    private val noSeVeValores = Choice(
        "valores", "Algo que es importante para mí",
        opts(
            "honestidad" to "La honestidad", "amistad" to "La amistad", "familia" to "La familia", "justicia" to "La justicia",
            "libertad" to "La libertad", "respeto" to "El respeto", "aprender" to "Aprender", "divertirme" to "Divertirme"
        ),
        maxSelected = 2, help = "Elige hasta dos.", optional = true
    )
    private val pareceFuera = Choice(
        "fuera", "Por fuera, a veces parezco…",
        opts("tranquilo" to "tranquilo o tranquila", "serio" to "serio o seria", "distraido" to "distraído o distraída",
            "alegre" to "alegre", "callado" to "callado o callada", "fuerte" to "fuerte"),
        optional = true
    )
    private val pareceDentro = Choice(
        "dentro", "…pero por dentro puedo estar",
        opts("preocupado" to "preocupado o preocupada", "nervioso" to "nervioso o nerviosa", "cansado" to "cansado o cansada",
            "emocionado" to "emocionado o emocionada", "triste" to "triste", "pensando" to "pensando mucho"),
        optional = true
    )
    val loQueNoSeVe = Activity(
        id = "act_lo_que_no_se_ve",
        title = "Lo que no se ve de mí",
        objective = "Reflexionar sobre partes de ti que otras personas podrían no conocer.",
        skill = "Autoconocimiento y perspectiva",
        minutes = "6 min",
        intro = "Vas a mirar tus intereses, lo que sabes hacer y lo que te importa. Nada de esto necesita la aprobación de nadie.",
        steps = listOf(
            noSeVeIntereses, noSeVeCapacidades, noSeVeValores, pareceFuera, pareceDentro,
            Phrase("frase", "Lo que no se ve", build = { a ->
                val f = pareceFuera.optionsFor(a).firstOrNull { it.id == a.first("fuera") }?.label
                val d = pareceDentro.optionsFor(a).firstOrNull { it.id == a.first("dentro") }?.label
                if (f != null && d != null) "A veces parezco $f, pero por dentro puedo estar $d." else "Hay partes de mí que no se ven a simple vista."
            }, help = "Es solo para ti. Compartirlo es tu decisión."),
            Summary("resumen", "Partes de ti", build = { a ->
                listOf(
                    "Me interesa" to label(noSeVeIntereses, a),
                    "Se me da bien" to label(noSeVeCapacidades, a),
                    "Me importa" to label(noSeVeValores, a)
                )
            })
        ),
        conclusion = "Lo que vives por dentro no siempre se ve por fuera, y eso es normal. Tus intereses, lo que sabes hacer y lo que te importa son tuyos.",
        dailyTip = "Si quieres, cuéntale a alguien de confianza una de estas cosas.",
        islandOffers = listOf("jardin", "puente")
    )

    // ---------------------------------------------------------------------------------
    // 6. Mi propio ritmo
    // ---------------------------------------------------------------------------------
    private val ritmoSituacion = Choice(
        "situacion", "¿Qué se parece a lo que vives?",
        opts(
            "tareas" to "Tengo varias tareas para la misma semana",
            "comparo" to "Me comparo con alguien que saca mejores notas",
            "familia" to "Siento que esperan mucho de mí",
            "rapido" to "Todos parecen ir más rápido que yo",
            "pidieron" to "Me pidieron algo y no sé si puedo",
            "perfecto" to "Quiero que todo me quede perfecto"
        )
    )
    private val ritmoOtros = Choice(
        "otros", "Lo que creo que otros esperan de mí",
        opts("todo" to "Que lo haga todo", "rapido" to "Que lo haga rápido", "perfecto" to "Que lo haga perfecto", "nose" to "No estoy seguro o segura"),
        optional = true
    )
    private val ritmoYo = Choice(
        "yo", "Lo que yo espero de mí",
        opts("todo" to "Hacerlo todo", "rapido" to "Hacerlo rápido", "perfecto" to "Hacerlo perfecto", "manera" to "Hacerlo a mi manera", "nose" to "No estoy seguro o segura"),
        optional = true
    )
    private val ritmoPuedo = Choice(
        "puedo", "Lo que de verdad puedo hoy",
        opts("todo" to "Todo", "parte" to "Una parte", "paso" to "Un paso pequeño", "descanso" to "Necesito descansar primero")
    )
    private val ritmoApoyo = Choice(
        "apoyo", "¿Qué ritmo o apoyo te serviría?",
        opts(
            "pasos" to "Dividirlo en pasos pequeños", "tiempo" to "Pedir más tiempo", "ayuda" to "Pedir ayuda",
            "pausa" to "Hacer una pausa y volver", "no" to "Decir que no por ahora"
        ),
        maxSelected = 2, help = "Puedes elegir hasta dos."
    )
    val miPropioRitmo = Activity(
        id = "act_mi_ritmo",
        title = "Mi propio ritmo",
        objective = "Diferenciar lo que otros esperan, lo que esperas tú y lo que puedes hoy.",
        skill = "Autonomía y respeto por tu propio ritmo",
        minutes = "5 min",
        intro = "Vas a separar tres expectativas que a veces pesan juntas y elegir qué ritmo te sirve. No hay cronómetro.",
        steps = listOf(
            ritmoSituacion, ritmoOtros, ritmoYo, ritmoPuedo, ritmoApoyo,
            Summary("resumen", "Tu ritmo", build = { a ->
                listOf(
                    "Otros esperan" to label(ritmoOtros, a),
                    "Yo espero" to label(ritmoYo, a),
                    "Hoy puedo" to label(ritmoPuedo, a),
                    "Me serviría" to label(ritmoApoyo, a)
                )
            })
        ),
        conclusion = "Cada persona necesita tiempos distintos. Pausar y retomar también es avanzar, y no necesitas compararte para saber lo que vales.",
        dailyTip = "Escribe el siguiente paso pequeño que sí puedes dar hoy.",
        islandOffers = listOf("hamaca", "jardin")
    )

    val all: List<Activity> = listOf(
        mapaDeSituaciones, dosMareasAlMismoTiempo, antesDeResponder, loQueNecesito, loQueNoSeVe, miPropioRitmo
    )

    fun byId(id: String): Activity? = all.firstOrNull { it.id == id }

    /** Firma de interacción: los tipos de paso en orden. Sirve para comprobar que no hay dos actividades iguales. */
    fun interactionSignature(activity: Activity): String =
        activity.steps.joinToString("-") { step ->
            when (step) {
                is Choice -> if (step.maxSelected > 1) "Multi" else "Choice"
                is TwoTides -> "TwoTides"
                is Pause -> "Pause"
                is Consequences -> "Consequences"
                is Phrase -> "Phrase"
                is Summary -> "Summary"
            }
        }

    /** Texto que se guarda en el diario SOLO si el niño lo elige. */
    fun diaryText(activity: Activity, answers: Answers): String {
        val lines = mutableListOf<String>()
        activity.steps.forEach { step ->
            when (step) {
                is Summary -> step.build(answers).forEach { (k, v) -> lines += "$k: $v" }
                is Phrase -> (answers.texts[step.id] ?: step.build(answers)).takeIf { it.isNotBlank() }?.let { lines += it }
                else -> Unit
            }
        }
        return lines.joinToString("\n")
    }
}
