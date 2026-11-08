package com.example.domain

/**
 * Bitácora del acompañamiento (Prompt Maestro §11, Documento Técnico §12).
 *
 * Contenido para adultos: observar, reflexionar y mejorar respuestas. No es terapia,
 * no califica a los padres ni diagnostica al niño. No hay puntajes ni respuestas "correctas":
 * cada opción tiene un comentario que invita a pensar. Nada de lo que el adulto elige se guarda.
 *
 * ESTADO: borrador del equipo, pendiente de revisión por un profesional de psicología con
 * experiencia en infancia, adolescencia y orientación familiar.
 */
object AdultGuide {
    const val CONTENT_STATUS = "Borrador pendiente de revisión psicológica"
    const val SUBTITLE = "Un espacio para comprender las emociones, reconocer necesidades y desarrollar formas más conscientes de acompañar."

    data class Route(val id: String, val number: Int, val title: String, val description: String)

    /** Una opción con un comentario para pensar (nunca "correcto" / "incorrecto"). */
    data class Option(val id: String, val label: String, val note: String)

    data class Reflection(val id: String, val prompt: String, val options: List<Option>, val multiple: Boolean = false)

    data class Experience(
        val id: String,
        val routeId: String,
        val title: String,
        val objective: String,
        val situation: String,
        val reflections: List<Reflection>,
        val learning: String,
        val dailyApplication: String,
        val minutes: String
    )

    val routes = listOf(
        Route("leer", 1, "Leer la marea", "Reconocer señales y cambios de comportamiento sin sacar conclusiones apresuradas."),
        Route("superficie", 2, "Bajo la superficie", "Explorar experiencias, necesidades y circunstancias detrás de una conducta, considerando varias explicaciones."),
        Route("rumbo", 3, "Regular el propio rumbo", "Reconocer cómo las emociones y reacciones del adulto influyen en la relación."),
        Route("corriente", 4, "Volver a encontrar la corriente", "Reparar conversaciones y vínculos después de un conflicto.")
    )

    private const val HECHO = "Es un hecho: algo que se puede ver u oír."
    private const val SUPOSICION = "Es una interpretación. Puede ser cierta o no; vale la pena comprobarla antes de responder."
    private const val POSIBLE = "Es una posibilidad. No hace falta adivinar: se puede preguntar más tarde, con calma."

    val experiences = listOf(
        Experience(
            id = "bit_antes_reaccion",
            routeId = "leer",
            title = "Lo que cambió antes de la reacción",
            objective = "Observar señales y antecedentes sin reducir la situación a una conducta aislada.",
            situation = "Al llegar del colegio, Sara (12 años) tira la maleta, no saluda y se encierra en su cuarto. Esa mañana usted le había pedido que ordenara su habitación.",
            reflections = listOf(
                Reflection(
                    "hechos", "¿Qué observó realmente? Toque cada frase para ver si es un hecho o una suposición.",
                    listOf(
                        Option("maleta", "Tiró la maleta", HECHO),
                        Option("grosera", "Está siendo grosera conmigo", SUPOSICION),
                        Option("saludo", "No saludó", HECHO),
                        Option("importa", "No le importa lo que le pedí", SUPOSICION),
                        Option("cuarto", "Se encerró en su cuarto", HECHO)
                    ),
                    multiple = true
                ),
                Reflection(
                    "antes", "¿Qué pudo cambiar antes de la reacción?",
                    listOf(
                        Option("colegio", "Algo pasó en el colegio", POSIBLE),
                        Option("cansancio", "Está cansada o con hambre", POSIBLE),
                        Option("amigos", "Tuvo un problema con amigos o en el celular", POSIBLE),
                        Option("casa", "Algo en casa la preocupa", POSIBLE),
                        Option("nose", "No lo sé todavía", "Reconocer que no se sabe es un buen punto de partida para preguntar sin acusar.")
                    ),
                    multiple = true
                )
            ),
            learning = "Una conducta es la parte visible de algo más amplio. Separar lo que ocurrió de lo que suponemos ayuda a responder a lo que pasa y no a lo que imaginamos.",
            dailyApplication = "Antes de responder, pregúntese: ¿qué vi exactamente? ¿Qué pudo pasar antes? Si la marea está alta, espere a que baje para conversar.",
            minutes = "5 min"
        ),
        Experience(
            id = "bit_mas_explicacion",
            routeId = "superficie",
            title = "Más de una explicación",
            objective = "Evitar conclusiones inmediatas sobre la conducta.",
            situation = "Desde hace una semana, Tomás (13 años) responde con monosílabos y pasa más tiempo con los audífonos puestos.",
            reflections = listOf(
                Reflection(
                    "primera", "¿Cuál es la primera explicación que se le ocurre?",
                    listOf(
                        Option("edad", "Está en la edad", "Es una explicación frecuente, pero puede ocultar algo que sí necesita atención. ¿Qué otras hay?"),
                        Option("esconde", "Está escondiendo algo", "Es una posibilidad, aunque partir de la sospecha suele cerrar la conversación. ¿Qué otras hay?"),
                        Option("preocupa", "Algo le preocupa", "Es una posibilidad. ¿Qué otras podrían existir?"),
                        Option("espacio", "Quiere más espacio propio", "Necesitar espacio es parte de crecer. ¿Qué otras explicaciones caben?")
                    )
                ),
                Reflection(
                    "otras", "Elija otras explicaciones posibles, distintas a la primera.",
                    listOf(
                        Option("cansado", "Está cansado por el colegio", POSIBLE),
                        Option("amigo", "Pasó algo con un amigo o amiga", POSIBLE),
                        Option("musica", "Descubrió música o contenidos que le gustan", POSIBLE),
                        Option("cambios", "Está viviendo cambios propios de su edad", POSIBLE),
                        Option("triste", "Se siente triste o solo", "Es una posibilidad. Si lo nota durante varias semanas, vale la pena consultar con un profesional.")
                    ),
                    multiple = true
                ),
                Reflection(
                    "pregunta", "¿Qué podría decirle para abrir la conversación?",
                    listOf(
                        Option("exige", "«¿Qué te pasa? Dímelo ya.»", "Suena a exigencia; suele cerrar la conversación."),
                        Option("abierta", "«Te noto más callado estos días. Si quieres hablar, aquí estoy.»", "Describe sin acusar y deja la puerta abierta."),
                        Option("concreta", "«¿Cómo te fue hoy en el entrenamiento?»", "Una pregunta concreta y cotidiana suele ser más fácil de responder."),
                        Option("nada", "No decir nada", "Dar espacio puede ayudar, pero conviene que sepa que puede acudir a usted.")
                    )
                )
            ),
            learning = "Ante una misma conducta suelen existir varias explicaciones. Quedarse con la primera puede llevar a etiquetar. Preguntar con curiosidad, sin conclusiones, abre la conversación.",
            dailyApplication = "Antes de concluir, piense en al menos dos explicaciones distintas y haga una pregunta abierta, sin etiquetas como «perezoso» o «grosero».",
            minutes = "6 min"
        ),
        Experience(
            id = "bit_mi_reaccion",
            routeId = "rumbo",
            title = "Mi reacción también cuenta",
            objective = "Reconocer que la respuesta adulta influye en la interacción.",
            situation = "Le pide a su hija que deje el celular para cenar. Ella responde «ya voy» por tercera vez. Usted siente que la paciencia se acaba.",
            reflections = listOf(
                Reflection(
                    "siento", "¿Qué sintió en ese momento? Puede elegir varias.",
                    listOf(
                        Option("rabia", "Rabia", "Nombrar lo que siente es el primer paso para elegir cómo responder."),
                        Option("cansancio", "Cansancio", "El cansancio reduce la paciencia de cualquiera. Reconocerlo ayuda."),
                        Option("frustracion", "Frustración", "Nombrar lo que siente es el primer paso para elegir cómo responder."),
                        Option("preocupacion", "Preocupación", "A veces detrás del enojo hay preocupación por el bienestar del otro."),
                        Option("impotencia", "Impotencia", "Es una emoción frecuente cuando sentimos que no nos escuchan.")
                    ),
                    multiple = true
                ),
                Reflection(
                    "hago", "¿Qué suele hacer en una situación así?",
                    listOf(
                        Option("grito", "Subo la voz", "Es una reacción frecuente cuando la paciencia se agota. ¿Qué efecto suele tener en la conversación?"),
                        Option("quito", "Quito el celular sin hablar", "Detiene la situación, pero sin explicación puede sentirse como castigo. ¿Qué pasa después?"),
                        Option("rindo", "Lo dejo pasar", "Evita el conflicto en el momento. ¿Qué pasa con su necesidad de cenar juntos?"),
                        Option("calma", "Explico con calma lo que necesito", "Es una forma de expresar un límite. ¿Qué le ayuda a lograrlo cuando está cansado o cansada?")
                    )
                ),
                Reflection(
                    "alternativa", "¿Qué alternativa podría probar la próxima vez?",
                    listOf(
                        Option("pausa", "Hacer una pausa y respirar antes de hablar", "Una pausa breve permite responder en lugar de reaccionar."),
                        Option("limite", "Acercarme y decir un límite claro: «en dos minutos dejamos el celular»", "Un límite concreto y anticipado suele funcionar mejor que repetir la orden."),
                        Option("opcion", "Ofrecer una opción: «termina ese mensaje y vienes»", "Dar una opción reconoce lo que está haciendo y mantiene el límite."),
                        Option("despues", "Hablarlo después, cuando estemos tranquilos", "Conversar con calma permite acordar algo para la próxima vez.")
                    )
                )
            ),
            learning = "Las emociones del adulto también influyen en la interacción. Reconocerlas no es culparse: permite elegir una respuesta en lugar de reaccionar en automático.",
            dailyApplication = "Cuando sienta que la marea sube, haga una pausa breve. Si reaccionó de una forma con la que no se siente bien, puede reconocerlo después: eso también enseña.",
            minutes = "6 min"
        ),
        Experience(
            id = "bit_rabia",
            routeId = "rumbo",
            title = "Cuando la rabia aparece",
            objective = "Comprender la rabia y revisar respuestas seguras.",
            situation = "Después de perder un partido, Mateo (11 años) grita, patea una silla y dice «odio todo».",
            reflections = listOf(
                Reflection(
                    "senales", "¿Qué señales muestran cómo se siente?",
                    listOf(
                        Option("grita", "Grita", "Es una señal de una emoción intensa."),
                        Option("patea", "Patea una silla", "La emoción es válida; esta conducta necesita un límite porque puede lastimar."),
                        Option("dice", "Dice «odio todo»", "Suele expresar frustración del momento, no una idea fija."),
                        Option("cuerpo", "Tiene el cuerpo tenso", "El cuerpo también muestra la emoción; bajar la tensión ayuda antes de hablar.")
                    ),
                    multiple = true
                ),
                Reflection(
                    "respuesta", "¿Qué respuesta acoge la emoción sin aceptar la conducta que lastima?",
                    listOf(
                        Option("drama", "«¡Deja el drama por un partido!»", "Minimiza lo que siente; puede aumentar la rabia."),
                        Option("valida", "«Entiendo que estés muy enojado por perder. No está bien patear cosas. Vamos a calmarnos y luego hablamos.»", "Nombra la emoción, pone un límite claro a la conducta y propone un siguiente paso."),
                        Option("castigo", "«Vete a tu cuarto, estás castigado.»", "Puede detener la conducta, pero no ayuda a entender ni a manejar la emoción."),
                        Option("nada", "No decir nada y esperar a que se le pase", "Dar espacio puede servir si todos están seguros; conviene volver a hablar después.")
                    )
                ),
                Reflection(
                    "pausas", "¿Qué pausas podría ofrecer?",
                    listOf(
                        Option("respirar", "Respirar juntos", "Las pausas ayudan a bajar la intensidad antes de conversar."),
                        Option("caminar", "Salir a caminar un momento", "Las pausas ayudan a bajar la intensidad antes de conversar."),
                        Option("agua", "Tomar agua", "Las pausas ayudan a bajar la intensidad antes de conversar."),
                        Option("rincon", "Ir a un lugar tranquilo", "Las pausas ayudan a bajar la intensidad antes de conversar."),
                        Option("cojin", "Apretar un cojín", "Descargar la tensión sin lastimar a nadie es una alternativa segura.")
                    ),
                    multiple = true
                )
            ),
            learning = "La rabia avisa que algo importa o se siente injusto. Validarla no es aprobar cualquier conducta: se puede acoger la emoción y, al mismo tiempo, poner límites respetuosos a lo que lastima.",
            dailyApplication = "Primero seguridad y calma; después conversación. Evite castigar la emoción («no te enojes») y no normalice las agresiones. Si la rabia es muy frecuente o hay riesgo, busque orientación profesional.",
            minutes = "6 min"
        ),
        Experience(
            id = "bit_despues_conflicto",
            routeId = "corriente",
            title = "Después del conflicto",
            objective = "Promover reparación, escucha y acuerdos.",
            situation = "Ayer usted y su hijo discutieron fuerte por las notas del colegio. Hoy casi no se hablan.",
            reflections = listOf(
                Reflection(
                    "empezar", "¿Cómo podría empezar la reparación?",
                    listOf(
                        Option("esperar", "Esperar a que él pida perdón", "Exigir el perdón primero suele alargar la distancia. Un adulto puede dar el primer paso."),
                        Option("reconocer", "Reconocer mi parte: «Ayer subí la voz y no me gustó cómo te hablé»", "Reconocer su parte enseña responsabilidad sin culpar al otro."),
                        Option("nada", "Hacer como si nada hubiera pasado", "Puede aliviar el momento, pero el tema queda sin resolver."),
                        Option("proponer", "Proponer un momento para hablar con calma", "Ofrece un espacio sin forzar la conversación.")
                    )
                ),
                Reflection(
                    "ayuda", "¿Qué podría ayudar en esa conversación?",
                    listOf(
                        Option("escuchar", "Escuchar sin interrumpir", "Ayuda a que la otra persona se sienta tomada en cuenta."),
                        Option("preguntar", "Preguntar cómo lo vivió", "Muestra interés por su experiencia, no solo por el resultado."),
                        Option("acuerdo", "Buscar un acuerdo pequeño", "Un acuerdo concreto y revisable ayuda a seguir adelante."),
                        Option("noforzar", "No forzar una reconciliación inmediata", "Cada persona necesita su tiempo para volver a acercarse."),
                        Option("razon", "Volver a explicar por qué yo tenía razón", "Puede convertir la reparación en otro sermón; mejor centrarse en escuchar.")
                    ),
                    multiple = true
                )
            ),
            learning = "Los conflictos son parte de cualquier relación. Lo que fortalece el vínculo es la reparación: reconocer, escuchar y acordar, sin culpabilizar ni exigir un perdón inmediato.",
            dailyApplication = "Después de una discusión, dé tiempo, reconozca su parte y proponga un acuerdo pequeño que puedan revisar juntos.",
            minutes = "5 min"
        )
    )

    fun experiencesFor(routeId: String) = experiences.filter { it.routeId == routeId }
    fun byId(id: String) = experiences.firstOrNull { it.id == id }
}

/**
 * Conexión familiar (Prompt Maestro §12, Documento Técnico §13).
 * Guías para hacer en persona, de 5 a 15 minutos. Son voluntarias, no se registran ni se califican:
 * la app no guarda quién participó ni qué se dijo.
 */
object FamilyConnection {
    const val CONTENT_STATUS = "Borrador pendiente de revisión psicológica"

    data class Guide(
        val id: String,
        val title: String,
        val objective: String,
        val minutes: String,
        val steps: List<String>,
        val examples: List<String>,
        val careNote: String
    )

    val guides = listOf(
        Guide(
            id = "con_dos_perspectivas",
            title = "Dos perspectivas, una situación",
            objective = "Reconocer que dos personas pueden vivir de forma diferente una misma situación.",
            minutes = "10 min",
            steps = listOf(
                "Elijan juntos una situación que vivieron hace poco: un paseo, un día especial o un desacuerdo pequeño.",
                "Cada uno piensa en silencio: ¿qué fue lo más importante para mí? ¿Cómo me sentí?",
                "Cada uno decide qué quiere contar. Mientras uno habla, el otro escucha sin corregir.",
                "Comenten en qué se parecen y en qué no sus maneras de vivirla."
            ),
            examples = listOf("La última salida en familia", "El primer día de clases", "Una visita a los abuelos"),
            careNote = "No se trata de saber quién tiene la razón ni de llegar a la misma conclusión. Nadie tiene que contar lo que no quiera."
        ),
        Guide(
            id = "con_me_gustaria",
            title = "Lo que me gustaría que supieras",
            objective = "Facilitar que cada uno comunique algo importante, solo si lo desea.",
            minutes = "5 a 10 min",
            steps = listOf(
                "Cada persona elige una de las frases guía.",
                "La completa en voz alta o por escrito, si quiere. También puede pasar.",
                "Quien escucha agradece y no hace preguntas en ese momento."
            ),
            examples = listOf(
                "Me gustaría que supieras que…",
                "Algo que me ayuda cuando estoy mal es…",
                "Me siento bien cuando tú…",
                "Algo que me cuesta decirte es…"
            ),
            careNote = "No es un interrogatorio ni un momento para confesar. Si algo preocupa, se puede retomar otro día, con calma."
        ),
        Guide(
            id = "con_acuerdo_semana",
            title = "Un acuerdo para nuestra semana",
            objective = "Construir un compromiso pequeño, mutuo y revisable.",
            minutes = "10 a 15 min",
            steps = listOf(
                "Elijan una situación cotidiana que quieran mejorar: las mañanas, las tareas, las pantallas o la hora de dormir.",
                "Propongan un acuerdo pequeño y realista para esta semana.",
                "Definan qué hará cada uno, también el adulto.",
                "Acuerden un día para revisar cómo les fue y ajustarlo si hace falta."
            ),
            examples = listOf(
                "Los días de colegio dejamos el celular en la sala a las 9:30 p. m.; el adulto también.",
                "El domingo preparamos juntos la maleta de la semana."
            ),
            careNote = "Eviten acuerdos de castigo o de control. Si no se cumple, se conversa y se ajusta: no es una prueba."
        ),
        Guide(
            id = "con_sin_preguntas",
            title = "Tiempo sin preguntas",
            objective = "Compartir un rato juntos sin exigir conversación.",
            minutes = "10 a 15 min",
            steps = listOf(
                "Elijan una actividad corta que disfruten los dos.",
                "Durante ese rato no hay preguntas sobre emociones, notas ni rendimiento.",
                "Si surge una conversación, que sea porque alguien quiere hablar."
            ),
            examples = listOf("Cocinar algo sencillo", "Caminar o montar en bicicleta", "Dibujar o armar algo", "Un juego de mesa", "Escuchar música que le guste al otro"),
            careNote = "No se registra ni se califica la participación. Estar presentes ya es suficiente."
        ),
        Guide(
            id = "con_senal_apoyo",
            title = "Nuestra señal de apoyo",
            objective = "Acordar una señal para pedir compañía o espacio sin tener que explicar.",
            minutes = "10 min",
            steps = listOf(
                "Elijan una palabra, un gesto o un emoji que sea fácil de usar.",
                "Acuerden qué significa: «necesito compañía» o «necesito espacio».",
                "Acuerden cómo responderá el adulto cuando la vea.",
                "Pruébenla una vez para recordarla."
            ),
            examples = listOf("Un pulgar hacia abajo", "La palabra «faro»", "Un emoji de ola 🌊"),
            careNote = "Es voluntaria y se puede cambiar. No reemplaza Mis salvavidas ni pedir ayuda urgente: en una emergencia, llamen al 123."
        )
    )

    fun byId(id: String) = guides.firstOrNull { it.id == id }
}
