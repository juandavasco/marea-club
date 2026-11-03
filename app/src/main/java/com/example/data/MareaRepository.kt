package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class MareaRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("marea_club_prefs", Context.MODE_PRIVATE)

    private val _logs = MutableStateFlow<List<MareaLogEntry>>(emptyList())
    val logs: StateFlow<List<MareaLogEntry>> = _logs.asStateFlow()

    private val _activeMarea = MutableStateFlow<MareaType?>(null)
    val activeMarea: StateFlow<MareaType?> = _activeMarea.asStateFlow()

    private val _islandState = MutableStateFlow(IslandState())
    val islandState: StateFlow<IslandState> = _islandState.asStateFlow()

    private val _calmMomentsCount = MutableStateFlow(5)
    val calmMomentsCount: StateFlow<Int> = _calmMomentsCount.asStateFlow()

    private val _activitiesExploredCount = MutableStateFlow(8)
    val activitiesExploredCount: StateFlow<Int> = _activitiesExploredCount.asStateFlow()

    private val _reflections = MutableStateFlow<List<ReflectionEntry>>(emptyList())
    val reflections: StateFlow<List<ReflectionEntry>> = _reflections.asStateFlow()

    private val _weeklyOverview = MutableStateFlow<List<WeeklyDayData>>(emptyList())
    val weeklyOverview: StateFlow<List<WeeklyDayData>> = _weeklyOverview.asStateFlow()

    private val _isOnboarded = MutableStateFlow(false)
    val isOnboarded: StateFlow<Boolean> = _isOnboarded.asStateFlow()

    private val _consultations = MutableStateFlow<List<AnonymousConsultation>>(emptyList())
    val consultations: StateFlow<List<AnonymousConsultation>> = _consultations.asStateFlow()

    private val _currentProfileType = MutableStateFlow(UserProfileType.CHILD)
    val currentProfileType: StateFlow<UserProfileType> = _currentProfileType.asStateFlow()

    private val _familyLinkState = MutableStateFlow(FamilyLinkState())
    val familyLinkState: StateFlow<FamilyLinkState> = _familyLinkState.asStateFlow()

    private val _accompanimentRequests = MutableStateFlow<List<AccompanimentRequest>>(emptyList())
    val accompanimentRequests: StateFlow<List<AccompanimentRequest>> = _accompanimentRequests.asStateFlow()

    val parentGuides: List<ParentGuideArticle> = listOf(
        ParentGuideArticle(
            id = "guide_escuchar",
            title = "Cómo escuchar a chicos de 11 a 14 años sin que sientan un interrogatorio",
            category = "Comunicación Asertiva",
            readTime = "4 min",
            summary = "Estrategias para abrir conversaciones cotidianas respetando su ritmo y necesidad de privacidad.",
            fullContent = "Entre los 11 y los 14 años, el cerebro atraviesa una poda sináptica intensa que hace que los chicos busquen mayor autonomía y a la vez sientan inseguridad. Cuando los adultos preguntamos repetidamente '¿Cómo te fue?', '¿Qué hiciste?', '¿Por qué tienes esa cara?', suelen experimentar la conversación como una invasión o un examen.\n\nPara favorecer el diálogo sin presionar:\n- Comparte primero algo de tu día ('Hoy tuve una mañana pesada en el trabajo y me ayudó escuchar música'). Esto modela vulnerabilidad y quita el foco de interrogatorio.\n- Aprovecha momentos compartidos con atención indirecta (mientras van en metro/bus, cocinan juntos o dan una vuelta); mirar hacia adelante en vez de fijar la mirada cara a cara reduce la sensación de tensión.\n- Haz preguntas abiertas sobre intereses concretos en vez de balances generales ('¿Qué fue lo más curioso que pasó hoy?' o '¿Cómo va ese proyecto de dibujo?').\n- Si responden con monosílabos, no te enojes ni fuerces la charla. Diles con calma: 'Entiendo que hoy quieras silencio, aquí estoy si en algún momento quieres contarme algo'.",
            practicalTips = listOf(
                "Evita corregir o juzgar lo que sienten en los primeros 5 minutos de charla.",
                "Usa el silencio cómodo como espacio de seguridad, no como motivo de reproche.",
                "Fomenta momentos breves de conexión sin pantallas de por medio."
            )
        ),
        ParentGuideArticle(
            id = "guide_validar",
            title = "Validar antes de resolver: el poder de acompañar la marea",
            category = "Regulación Emocional",
            readTime = "3 min",
            summary = "Aprende por qué frases como 'no es para tanto' anulan la confianza y cómo reemplazarlas por presencia empática.",
            fullContent = "El impulso instintivo de los padres cuando su hijo sufre o se frustra es solucionar el problema de inmediato o minimizarlo ('no llores por eso', 'en dos días ni te acordarás', 'tienes que ser fuerte'). Aunque la intención es tranquilizar, el mensaje recibido es 'lo que siento está mal o no tiene importancia'.\n\nValidar no significa estar de acuerdo con todo lo que dicen o hacen, sino reconocer que su emoción es real para ellos en ese instante:\n- Usa frases de reflejo: 'Veo que esto te causó mucha rabia', 'Tiene sentido que te sientas decepcionado si esperabas otra cosa'.\n- Ofrece contención física o de presencia: a veces un abrazo en silencio, un vaso con agua o simplemente sentarse cerca vale más que una larga lección moral.\n- Pregunta antes de aconsejar: '¿Quieres que solo te escuche y te dé un abrazo, o prefieres que pensemos juntos qué se puede hacer?'. Esta pregunta les devuelve el control sobre su situación.",
            practicalTips = listOf(
                "Primero acompaña la emoción; la solución del problema viene cuando la marea baje.",
                "Valida la emoción pero pon límites claros a conductas dañinas (puedes sentir enojo, pero no tirar cosas).",
                "Reconoce tus propias emociones ante ellos: disculparte cuando perdiste la paciencia genera un vínculo indestructible."
            )
        ),
        ParentGuideArticle(
            id = "guide_senales",
            title = "Cambios del desarrollo vs. señales de alerta psicológica",
            category = "Salud Mental Precoz",
            readTime = "5 min",
            summary = "Criterios claros para distinguir la fluctuación hormonal normal de situaciones que ameritan atención especializada.",
            fullContent = "Durante la preadolescencia es esperable observar cambios de humor repentinos, necesidad de pasar más tiempo a solas en la habitación, cuestionamiento a normas familiares y fluctuaciones en sus gustos. Esto forma parte de la individuación sana.\n\nSin embargo, conviene consultar a profesionales cuando las conductas se vuelven persistentes y afectan su vida cotidiana:\n\nSeñales normales del desarrollo:\n- Pasar de la risa al enfado en pocas horas, pero recuperarse al rato.\n- Preferir estar con amigos que en reuniones de adultos familiares.\n- Necesidad de privacidad en sus cosas y reflexiones.\n\nSeñales de alerta que requieren apoyo profesional:\n- Aislamiento total y prolongado por semanas (dejar de hablar con amigos y abandonar todas las actividades disfrutadas).\n- Caída drástica e inexplicable en el rendimiento escolar acompañada de apatía severa.\n- Cambios drásticos en patrones de sueño (insomnio constante o dormir casi todo el día) o alteraciones en la alimentación.\n- Expresiones de desesperanza, sentirse una carga o insinuaciones sobre no querer estar vivo/a.\n- Presencia de marcas, cortes o lesiones inexplicadas en el cuerpo.\n\nEn Medellín dispones de la Línea Amiga 106 y los Escuchaderos del Metro para consultar de forma gratuita y sin cita previa si tienes dudas.",
            practicalTips = listOf(
                "La duración e intensidad de los cambios es el factor clave: más de 2 semanas de tristeza continua o anhedonia amerita consulta.",
                "No temas preguntar con cariño: 'He notado que estás muy triste últimamente, ¿te gustaría que hablemos con alguien que nos oriente?'.",
                "Acude a los Escuchaderos del Metro para una primera asesoría sin costo."
            )
        ),
        ParentGuideArticle(
            id = "guide_limites",
            title = "Límites saludables y confianza mutua en la preadolescencia",
            category = "Crianza Respetuosa",
            readTime = "4 min",
            summary = "Cómo pactar límites sobre pantallas, horarios y responsabilidades desde el respeto y la coherencia.",
            fullContent = "Los límites en la etapa de 11 a 14 años no son muros de castigo, sino las orillas firmes que contienen la marea y dan seguridad. Sin límites claros, los chicos se sienten desprotegidos; con límites asfixiantes y punitivos, aprenden a mentir u ocultar para defender su identidad.\n\nClaves para acuerdos efectivos:\n- Co-construir las reglas: en vez de imponer unilateralmente, siéntense en un momento de calma y pregunten: 'Para que el colegio y el descanso funcionen bien, ¿a qué hora acordamos apagar pantallas por la noche?'. Cuando ellos participan del acuerdo, se comprometen mejor.\n- Consecuencias naturales y proporcionales: si no se cumple un acuerdo, la consecuencia debe estar vinculada lógicamente con el hecho, no con una humillación o pérdida arbitraria de afecto.\n- Espacio privado sagrado: respetar sus cuadernos, diarios íntimos y conversaciones privadas es indispensable. La supervisión parental en esta edad se basa en la presencia y la confianza, no en el espionaje silencioso.",
            practicalTips = listOf(
                "Define 3 o 4 reglas no negociables (sueño, seguridad, respeto) y sé más flexible en detalles menores.",
                "Mantén una rutina de noche sin pantallas en las habitaciones para favorecer la melatonina.",
                "Elogia el esfuerzo y la honestidad cuando reconozcan un error."
            )
        ),
        ParentGuideArticle(
            id = "guide_rutas_familia",
            title = "Red de apoyo en Medellín: Guía para familias",
            category = "Rutas Locales Medellín",
            readTime = "3 min",
            summary = "Todo lo que los padres deben saber sobre los recursos psicológicos públicos en Medellín y el Valle de Aburrá.",
            fullContent = "La ciudad de Medellín cuenta con una de las redes públicas de apoyo en salud mental comunitaria más amplias del país. Todos estos canales son de acceso público, confidencial y respetuoso:\n\n1. Línea Amiga (604 444 44 48 o marcando 106):\nAtención 24/7 atendida por psicólogos clínicos de la Secretaría de Salud. Puedes llamar como madre/padre para pedir pautas de manejo sobre tu hijo/a o motivarlo a que llame él/ella mismo.\n\n2. Escuchaderos del Metro de Medellín:\nUbicados en estaciones estratégicas (San Antonio, Acevedo, San Javier, Oriente) y en Parques Biblioteca (como Belén). Atención presencial sin cita y 100% gratuita. Ideal para cuando sientas que necesitas un consejo profesional rápido cara a cara.\n\n3. Consultorios Psicológicos Universitarios (UdeA y UPB):\nOfrecen procesos de psicoterapia infantil y familiar con tarifas comunitarias o gratuitas según el estrato socioeconómico.\n\n4. Línea 123 Social:\nPara situaciones de crisis aguda en el hogar donde se requiera asistencia inmediata.",
            practicalTips = listOf(
                "Guarda el contacto de WhatsApp de la Línea Amiga (+57 301 604 44 44) en tu celular.",
                "Normaliza ante tu familia que pedir ayuda a un psicólogo es como ir al odontólogo cuando duele una muela.",
                "Recuerda que cuidar de tu propia salud emocional como adulto es el mejor ejemplo para tus hijos."
            )
        )
    )

    val availableActivities: List<ActivityItem> = listOf(
        ActivityItem(
            id = "act_respiracion",
            title = "Respiración del Océano",
            category = ActivityCategory.RESPIRACION,
            durationMin = "3 min",
            description = "Respira siguiendo las olas con Muni para soltar tensiones y serenar tu mente.",
            rewardTitle = "Flores de Coral",
            rewardIcon = "🌸",
            recommendedFor = listOf(MareaType.MOVIDA, MareaType.ALTA, MareaType.TORMENTA, MareaType.NO_SE)
        ),
        ActivityItem(
            id = "act_diario",
            title = "Diario del Mar",
            category = ActivityCategory.ESCRITURA,
            durationMin = "5 min",
            description = "Un espacio íntimo y seguro para escribir lo que sientes sin juicios ni notas.",
            rewardTitle = "Pérgola de Madera",
            rewardIcon = "🪵",
            recommendedFor = listOf(MareaType.MOVIDA, MareaType.ALTA, MareaType.TRANQUILA)
        ),
        ActivityItem(
            id = "act_emociones",
            title = "Brújula Emocional",
            category = ActivityCategory.AUTOCONOCIMIENTO,
            durationMin = "4 min",
            description = "Identifica qué necesita tu cuerpo y tu mente en este instante de marea.",
            rewardTitle = "Estanque Cristalino",
            rewardIcon = "💧",
            recommendedFor = listOf(MareaType.NO_SE, MareaType.MOVIDA, MareaType.TRANQUILA)
        ),
        ActivityItem(
            id = "act_autoestima",
            title = "Constelación de Fortalezas",
            category = ActivityCategory.AUTOESTIMA,
            durationMin = "4 min",
            description = "Descubre y enciende las cualidades que te hacen único y valioso.",
            rewardTitle = "Estrella Marina Radiante",
            rewardIcon = "⭐",
            recommendedFor = listOf(MareaType.TRANQUILA, MareaType.MOVIDA, MareaType.TORMENTA)
        ),
        ActivityItem(
            id = "act_conchas",
            title = "Reto de Conchas y Perlas",
            category = ActivityCategory.ATENCION,
            durationMin = "2 min",
            description = "Entrena tu atención y presencia encontrando la perla escondida en la costa.",
            rewardTitle = "Camino de Caracoles",
            rewardIcon = "🐚",
            recommendedFor = listOf(MareaType.MOVIDA, MareaType.TRANQUILA, MareaType.NO_SE)
        ),
        ActivityItem(
            id = "act_dibujo",
            title = "Lienzo de Expresión Libre",
            category = ActivityCategory.CREATIVIDAD,
            durationMin = "Libre",
            description = "Pinta tus sensaciones con los colores oceánicos de Marea Club.",
            rewardTitle = "Arrecife de Colores",
            rewardIcon = "🎨",
            recommendedFor = listOf(MareaType.TRANQUILA, MareaType.ALTA, MareaType.TORMENTA)
        )
    )

    val allIslandElements: List<IslandElement> = listOf(
        IslandElement("arbol_verde", "Árbol Sabio de Muni", 1, ActivityCategory.AUTOCONOCIMIENTO, true, "Brinda sombra y refugio a las ideas."),
        IslandElement("sofa_azul", "Sillón de Arena Azul", 1, ActivityCategory.ESCRITURA, true, "Lugar cómodo para reposar y escribir."),
        IslandElement("estanque_muni", "Estanque Cristalino", 1, ActivityCategory.RESPIRACION, true, "Aguas tranquilas donde Muni nada a gusto."),
        IslandElement("muelle_madera", "Muelle del Horizonte", 2, ActivityCategory.ESCRITURA, true, "Para contemplar la marea y respirar."),
        IslandElement("farol_calma", "Farol de la Calma", 2, ActivityCategory.RESPIRACION, true, "Ilumina las noches de tormenta."),
        IslandElement("flores_coral", "Flores de Coral", 2, ActivityCategory.RESPIRACION, false, "Nacidas de tus momentos de respiración."),
        IslandElement("camino_caracoles", "Camino de Caracoles", 3, ActivityCategory.ATENCION, false, "Marca tus pasos de concentración."),
        IslandElement("estrella_marina", "Estrella de Fortalezas", 3, ActivityCategory.AUTOESTIMA, false, "Recuerda lo valiente y capaz que eres."),
        IslandElement("arrecife_colores", "Arrecife Creativo", 4, ActivityCategory.CREATIVIDAD, false, "Colorido homenaje a tu imaginación.")
    )

    private val _favoriteRouteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteRouteIds: StateFlow<Set<String>> = _favoriteRouteIds.asStateFlow()

    private val _userName = MutableStateFlow("Navegante")
    val userName: StateFlow<String> = _userName.asStateFlow()

    val rawPsychologicalRoutes: List<PsychologicalRoute> = listOf(
        PsychologicalRoute(
            id = "route_linea_amiga",
            name = "Línea Amiga de Medellín",
            category = RouteCategory.TELEFONICA,
            phone = "(604) 444 44 48",
            whatsapp = "+573016044444",
            address = "Canal telefónico y digital gratuito (Toda Medellín)",
            comuna = "Toda la ciudad / Alcaldía de Medellín",
            schedule = "24 horas, 7 días a la semana",
            requiresAppointment = false,
            costDescription = "100% Gratuito",
            description = "Servicio oficial de escucha, orientación psicológica y acompañamiento en momentos de crisis para jóvenes, adolescentes y familias.",
            mapQuery = "Secretaria de Salud de Medellin",
            verificationSource = "Secretaría de Salud de Medellín",
            verificationDate = "Octubre 2026"
        ),
        PsychologicalRoute(
            id = "route_linea_106",
            name = "Línea 106 (Atención a Niños y Jóvenes)",
            category = RouteCategory.TELEFONICA,
            phone = "106",
            whatsapp = null,
            address = "Línea gratuita de ayuda infantil y juvenil",
            comuna = "Toda la ciudad",
            schedule = "24 horas al día",
            requiresAppointment = false,
            costDescription = "100% Gratuito desde cualquier celular o fijo",
            description = "Atención especializada y confidencial para niños, niñas y adolescentes que necesiten desahogarse o recibir protección.",
            mapQuery = "Medellin",
            verificationSource = "Ministerio de Salud / Alcaldía de Medellín",
            verificationDate = "Octubre 2026"
        ),
        PsychologicalRoute(
            id = "route_escuchadero_san_antonio",
            name = "Escuchadero Estación San Antonio",
            category = RouteCategory.ESCUCHADERO,
            phone = "(604) 444 44 48",
            whatsapp = "+573016044444",
            address = "Estación San Antonio del Metro (Conexión Líneas A, B y Tranvía)",
            comuna = "Comuna 10 (La Candelaria - Centro)",
            schedule = "Lunes a sábado, 8:00 a.m. a 5:00 p.m.",
            requiresAppointment = false,
            costDescription = "100% Gratuito (Sin cita previa)",
            description = "Punto presencial de atención psicológica individual y confidencial en el corazón del sistema Metro de Medellín.",
            mapQuery = "Estacion San Antonio Metro Medellin",
            verificationSource = "Metro de Medellín / Secretaría de Salud",
            verificationDate = "Octubre 2026"
        ),
        PsychologicalRoute(
            id = "route_escuchadero_acevedo",
            name = "Escuchadero Estación Acevedo",
            category = RouteCategory.ESCUCHADERO,
            phone = "(604) 444 44 48",
            whatsapp = "+573016044444",
            address = "Estación Acevedo del Metro (Línea A y Metrocable K y P)",
            comuna = "Comuna 5 (Castilla / Nororiental)",
            schedule = "Lunes a sábado, 8:00 a.m. a 5:00 p.m.",
            requiresAppointment = false,
            costDescription = "100% Gratuito (Sin cita previa)",
            description = "Orientación presencial gratuita accesible para la zona norte y nororiental de la ciudad.",
            mapQuery = "Estacion Acevedo Metro Medellin",
            verificationSource = "Metro de Medellín / Secretaría de Salud",
            verificationDate = "Octubre 2026"
        ),
        PsychologicalRoute(
            id = "route_escuchadero_san_javier",
            name = "Escuchadero Estación San Javier",
            category = RouteCategory.ESCUCHADERO,
            phone = "(604) 444 44 48",
            whatsapp = "+573016044444",
            address = "Estación San Javier del Metro (Línea B y Metrocable J)",
            comuna = "Comuna 13 (San Javier)",
            schedule = "Lunes a sábado, 8:00 a.m. a 5:00 p.m.",
            requiresAppointment = false,
            costDescription = "100% Gratuito (Sin cita previa)",
            description = "Atención cálida e inmediata para jóvenes y vecinos de la Comuna 13 y zona occidental.",
            mapQuery = "Estacion San Javier Metro Medellin",
            verificationSource = "Metro de Medellín / Secretaría de Salud",
            verificationDate = "Octubre 2026"
        ),
        PsychologicalRoute(
            id = "route_escuchadero_oriente",
            name = "Escuchadero Estación Oriente",
            category = RouteCategory.ESCUCHADERO,
            phone = "(604) 444 44 48",
            whatsapp = "+573016044444",
            address = "Estación Oriente (Línea T - Tranvía de Ayacucho)",
            comuna = "Comuna 8 (Villa Hermosa)",
            schedule = "Lunes a sábado, 8:00 a.m. a 5:00 p.m.",
            requiresAppointment = false,
            costDescription = "100% Gratuito (Sin cita previa)",
            description = "Espacio de escucha activa y apoyo psicológico ubicado al final del recorrido del Tranvía.",
            mapQuery = "Estacion Oriente Tranvia Medellin",
            verificationSource = "Metro de Medellín / Secretaría de Salud",
            verificationDate = "Octubre 2026"
        ),
        PsychologicalRoute(
            id = "route_escuchadero_belen",
            name = "Escuchadero Parque Biblioteca Belén",
            category = RouteCategory.ESCUCHADERO,
            phone = "(604) 444 44 48",
            whatsapp = "+573016044444",
            address = "Calle 30A # 76-147, Parque Biblioteca Belén",
            comuna = "Comuna 16 (Belén)",
            schedule = "Lunes a viernes, 8:00 a.m. a 5:00 p.m.",
            requiresAppointment = false,
            costDescription = "100% Gratuito (Sin cita previa)",
            description = "Punto comunitario de orientación psicológica y salud mental en entorno pedagógico y cultural.",
            mapQuery = "Parque Biblioteca Belen Medellin",
            verificationSource = "Red de Bibliotecas / Secretaría de Salud",
            verificationDate = "Octubre 2026"
        ),
        PsychologicalRoute(
            id = "route_consultorio_udea",
            name = "Consultorio Psicológico UdeA",
            category = RouteCategory.UNIVERSITARIO,
            phone = "(604) 219 57 58",
            whatsapp = null,
            address = "Calle 67 # 53-108, Bloque 9, Ciudad Universitaria",
            comuna = "Comuna 4 (Aranjuez / Sevilla)",
            schedule = "Lunes a viernes, 8:00 a.m. a 4:30 p.m.",
            requiresAppointment = true,
            costDescription = "Gratuito (Estratos 1 y 2) / Tarifa social comunitaria",
            description = "Servicio docente-asistencial de la Universidad de Antioquia abierto a toda la comunidad, con psicólogos clínicos y terapeutas.",
            mapQuery = "Universidad de Antioquia Bloque 9",
            verificationSource = "Facultad de Ciencias Sociales UdeA",
            verificationDate = "Octubre 2026"
        ),
        PsychologicalRoute(
            id = "route_consultorio_upb",
            name = "Centro de Atención Psicológica UPB",
            category = RouteCategory.UNIVERSITARIO,
            phone = "(604) 448 83 88",
            whatsapp = null,
            address = "Circular 1 # 70-01, Edificio 11, Campus Laureles",
            comuna = "Comuna 11 (Laureles - Estadio)",
            schedule = "Lunes a viernes, 8:00 a.m. a 5:00 p.m.",
            requiresAppointment = true,
            costDescription = "Tarifa social solidaria según estrato",
            description = "Atención psicológica individual, infantil y familiar con profesionales y estudiantes de posgrado supervisados.",
            mapQuery = "Universidad Pontificia Bolivariana Laureles",
            verificationSource = "Facultad de Psicología UPB Medellín",
            verificationDate = "Octubre 2026"
        ),
        PsychologicalRoute(
            id = "route_linea_123_social",
            name = "Línea 123 Social Medellín",
            category = RouteCategory.TELEFONICA,
            phone = "123",
            whatsapp = null,
            address = "Sistema Integrado de Emergencias y Seguridad Metropolitano",
            comuna = "Toda la ciudad y Área Metropolitana",
            schedule = "24 horas, los 365 días del año",
            requiresAppointment = false,
            costDescription = "100% Gratuito",
            description = "Activación inmediata de unidades móviles de salud mental, crisis emocional aguda o vulneración de derechos.",
            mapQuery = "Medellin 123",
            verificationSource = "Alcaldía de Medellín / 123 Social",
            verificationDate = "Octubre 2026"
        )
    )

    val psychologicalRoutesMedellin: List<PsychologicalRoute>
        get() {
            val favs = _favoriteRouteIds.value
            return rawPsychologicalRoutes.map { it.copy(isFavorite = favs.contains(it.id)) }
        }

    // 7 Self-Evaluation Questionnaires for Children and Adolescents (11-14 years)
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

    init {
        // Enforce clean zero-start requested by user
        if (!prefs.getBoolean("clean_fresh_start_v5", false)) {
            prefs.edit().clear().putBoolean("clean_fresh_start_v5", true).putBoolean("is_onboarded", true).apply()
        }
        loadData()
    }

    private fun loadData() {
        _isOnboarded.value = prefs.getBoolean("is_onboarded", true)

        val level = prefs.getInt("island_level", 1)
        val stage = prefs.getInt("island_stage", 1)
        val levelName = prefs.getString("island_level_name", "Orilla Inicial") ?: "Orilla Inicial"
        val progress = prefs.getInt("island_progress", 0)
        val skyModeName = prefs.getString("island_sky_mode", SkyMode.DIA.name) ?: SkyMode.DIA.name
        val skyMode = try { SkyMode.valueOf(skyModeName) } catch (_: Exception) { SkyMode.DIA }

        val unlockedString = prefs.getString("unlocked_elements", "") ?: ""
        val unlockedSet = unlockedString.split(",").filter { it.isNotBlank() }.toSet()

        _islandState.value = IslandState(
            level = level,
            levelName = levelName,
            stage = stage,
            progressPercent = progress,
            skyMode = skyMode,
            unlockedElementIds = unlockedSet
        )

        _calmMomentsCount.value = prefs.getInt("calm_count", 0)
        _activitiesExploredCount.value = prefs.getInt("activities_count", 0)
        _userName.value = prefs.getString("user_name", "Navegante") ?: "Navegante"

        val favsStr = prefs.getString("favorite_routes", "") ?: ""
        _favoriteRouteIds.value = favsStr.split(",").filter { it.isNotBlank() }.toSet()

        // Load consultations
        val savedConsJson = prefs.getString("saved_consultations", null)
        if (!savedConsJson.isNullOrEmpty()) {
            try {
                val cList = mutableListOf<AnonymousConsultation>()
                val cArr = JSONArray(savedConsJson)
                for (i in 0 until cArr.length()) {
                    val obj = cArr.getJSONObject(i)
                    cList.add(
                        AnonymousConsultation(
                            id = obj.getString("id"),
                            timestamp = obj.getLong("timestamp"),
                            message = obj.getString("message"),
                            muniResponse = obj.getString("muniResponse"),
                            recommendedRoute = obj.getString("recommendedRoute"),
                            isEmergencyFlagged = obj.optBoolean("isEmergencyFlagged", false)
                        )
                    )
                }
                _consultations.value = cList
            } catch (_: Exception) {
                _consultations.value = emptyList()
            }
        }

        val savedMareaName = prefs.getString("active_marea", null)
        if (savedMareaName != null) {
            _activeMarea.value = try { MareaType.valueOf(savedMareaName) } catch (_: Exception) { null }
        } else {
            _activeMarea.value = null
        }

        // Load logs (empty by default for clean start)
        val savedLogsJson = prefs.getString("saved_logs", null)
        if (!savedLogsJson.isNullOrEmpty()) {
            try {
                val list = mutableListOf<MareaLogEntry>()
                val jsonArr = JSONArray(savedLogsJson)
                for (i in 0 until jsonArr.length()) {
                    val obj = jsonArr.getJSONObject(i)
                    val mareaType = MareaType.valueOf(obj.getString("type"))
                    list.add(
                        MareaLogEntry(
                            id = obj.getString("id"),
                            type = mareaType,
                            dayOfWeek = obj.getString("dayOfWeek"),
                            timestamp = obj.getLong("timestamp"),
                            situation = obj.getString("situation"),
                            timeOfDay = obj.optString("timeOfDay", "Hoy"),
                            location = obj.optString("location", "Cotidiano"),
                            note = obj.optString("note", "")
                        )
                    )
                }
                _logs.value = list
            } catch (_: Exception) {
                _logs.value = emptyList()
            }
        } else {
            _logs.value = emptyList()
        }

        // Load reflections (empty by default for clean start)
        val savedReflectionsJson = prefs.getString("saved_reflections", null)
        if (!savedReflectionsJson.isNullOrEmpty()) {
            try {
                val list = mutableListOf<ReflectionEntry>()
                val arr = JSONArray(savedReflectionsJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        ReflectionEntry(
                            id = obj.getString("id"),
                            dateText = obj.getString("dateText"),
                            prompt = obj.getString("prompt"),
                            text = obj.getString("text")
                        )
                    )
                }
                _reflections.value = list
            } catch (_: Exception) {
                _reflections.value = emptyList()
            }
        } else {
            _reflections.value = emptyList()
        }

        // Load family profile & link state
        val profileStr = prefs.getString("current_profile_type", UserProfileType.CHILD.name) ?: UserProfileType.CHILD.name
        _currentProfileType.value = try { UserProfileType.valueOf(profileStr) } catch (_: Exception) { UserProfileType.CHILD }

        val isLinked = prefs.getBoolean("family_is_linked", false)
        val linkCode = prefs.getString("family_link_code", "MAR-482") ?: "MAR-482"
        val childName = _userName.value
        val parentName = prefs.getString("family_parent_name", "Acompañante Familiar") ?: "Acompañante Familiar"
        val linkedTimestamp = prefs.getLong("family_linked_time", 0L)
        val shareTides = prefs.getBoolean("family_share_tides", true)
        val shareIsland = prefs.getBoolean("family_share_island", true)
        val shareActivities = prefs.getBoolean("family_share_activities", true)

        _familyLinkState.value = FamilyLinkState(
            isLinked = isLinked,
            linkCode = linkCode,
            childName = childName,
            parentName = parentName,
            linkedSinceTimestamp = linkedTimestamp,
            shareTidesSummary = shareTides,
            shareIslandProgress = shareIsland,
            shareActivitiesSummary = shareActivities
        )

        // Load accompaniment requests
        val accJson = prefs.getString("saved_accompaniment_requests", null)
        if (!accJson.isNullOrEmpty()) {
            try {
                val accList = mutableListOf<AccompanimentRequest>()
                val arr = JSONArray(accJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    accList.add(
                        AccompanimentRequest(
                            id = obj.getString("id"),
                            timestamp = obj.getLong("timestamp"),
                            note = obj.getString("note"),
                            status = try { AccompanimentStatus.valueOf(obj.getString("status")) } catch (_: Exception) { AccompanimentStatus.PENDING }
                        )
                    )
                }
                _accompanimentRequests.value = accList
            } catch (_: Exception) {
                _accompanimentRequests.value = emptyList()
            }
        }

        updateWeeklyData()
    }

    fun completeOnboarding() {
        _isOnboarded.value = true
        prefs.edit().putBoolean("is_onboarded", true).apply()
    }

    private fun initDefaultLogs() {
        val now = System.currentTimeMillis()
        val defaultList = listOf(
            MareaLogEntry("1", MareaType.TRANQUILA, "Lunes", now - 6 * 86400000L, "Lectura y música", "Tarde", "En casa", "Me sentí en paz descansando un rato."),
            MareaLogEntry("2", MareaType.MOVIDA, "Martes", now - 5 * 86400000L, "Exámenes en el cole", "Mañana", "En la escuela", "Mucha energía y pendientes."),
            MareaLogEntry("3", MareaType.TRANQUILA, "Miércoles", now - 4 * 86400000L, "Paseo al aire libre", "Tarde", "En el parque", "El viento fresco me ayudó a despejar la mente."),
            MareaLogEntry("4", MareaType.MOVIDA, "Jueves", now - 3 * 86400000L, "Trabajo en equipo", "Tarde", "Con amigos", "Tuvimos que ponernos de acuerdo."),
            MareaLogEntry("5", MareaType.TORMENTA, "Viernes", now - 2 * 86400000L, "Discusión pequeña", "Noche", "En casa", "Sentí un nudo en el pecho, pero respiré hondo."),
            MareaLogEntry("6", MareaType.TRANQUILA, "Sábado", now - 1 * 86400000L, "Dormir hasta tarde", "Mañana", "En mi cuarto", "Pude desconectar y ver películas."),
            MareaLogEntry("7", MareaType.MOVIDA, "Domingo", now, "Preparar la semana", "Tarde", "En casa", "Pensando en lo que viene pero con tranquilidad.")
        )
        _logs.value = defaultList
        saveLogs()
    }

    private fun initDefaultReflections() {
        _reflections.value = listOf(
            ReflectionEntry(
                id = "ref_1",
                dateText = "Esta semana",
                prompt = "¿Qué descubriste sobre tus emociones esta semana?",
                text = "Noté que cuando respiro lento antes de empezar a estudiar, el oleaje se calma mucho más rápido."
            )
        )
    }

    private fun getCurrentDayOfWeekName(): String {
        val cal = java.util.Calendar.getInstance()
        return when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> "Lunes"
            java.util.Calendar.TUESDAY -> "Martes"
            java.util.Calendar.WEDNESDAY -> "Miércoles"
            java.util.Calendar.THURSDAY -> "Jueves"
            java.util.Calendar.FRIDAY -> "Viernes"
            java.util.Calendar.SATURDAY -> "Sábado"
            java.util.Calendar.SUNDAY -> "Domingo"
            else -> "Lunes"
        }
    }

    private fun updateWeeklyData() {
        val days = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        val map = _logs.value.groupBy { it.dayOfWeek }

        val week = days.map { day ->
            val entries = map[day]
            val lastEntry = entries?.lastOrNull()
            if (lastEntry == null) {
                // Serene, gentle resting baseline (0.08f) for clean start
                WeeklyDayData(day, day.take(3), MareaType.TRANQUILA, 0.08f)
            } else {
                val marea = lastEntry.type
                val height = when (marea) {
                    MareaType.TRANQUILA -> 0.65f
                    MareaType.MOVIDA -> 0.85f
                    MareaType.ALTA -> 0.75f
                    MareaType.TORMENTA -> 0.95f
                    MareaType.NO_SE -> 0.45f
                }
                WeeklyDayData(day, day.take(3), marea, height)
            }
        }
        _weeklyOverview.value = week
    }

    fun recordMarea(type: MareaType, situation: String, timeOfDay: String, location: String, note: String) {
        val todayDayOfWeek = getCurrentDayOfWeekName()
        val entry = MareaLogEntry(
            id = System.currentTimeMillis().toString(),
            type = type,
            dayOfWeek = todayDayOfWeek,
            timestamp = System.currentTimeMillis(),
            situation = situation.ifBlank { type.defaultSituation },
            timeOfDay = timeOfDay,
            location = location,
            note = note
        )
        val updated = listOf(entry) + _logs.value
        _logs.value = updated
        _activeMarea.value = type
        prefs.edit().putString("active_marea", type.name).apply()
        saveLogs()
        addIslandProgress(10)
        updateWeeklyData()
    }

    fun completeCalmActivity(name: String) {
        completeActivity("act_respiracion")
    }

    fun completeActivity(activityId: String) {
        _calmMomentsCount.value += 1
        _activitiesExploredCount.value += 1
        prefs.edit()
            .putInt("calm_count", _calmMomentsCount.value)
            .putInt("activities_count", _activitiesExploredCount.value)
            .apply()

        // Unlock specific rewards linked to this activity
        val elementToUnlock = when (activityId) {
            "act_respiracion" -> "flores_coral"
            "act_conchas" -> "camino_caracoles"
            "act_autoestima" -> "estrella_marina"
            "act_dibujo" -> "arrecife_colores"
            else -> null
        }

        if (elementToUnlock != null && !_islandState.value.unlockedElementIds.contains(elementToUnlock)) {
            val newSet = _islandState.value.unlockedElementIds + elementToUnlock
            _islandState.value = _islandState.value.copy(unlockedElementIds = newSet)
            prefs.edit().putString("unlocked_elements", newSet.joinToString(",")).apply()
        }

        addIslandProgress(15)
    }

    fun addIslandProgress(amount: Int) {
        val currentProgress = _islandState.value.progressPercent + amount
        if (currentProgress >= 100) {
            val newLevel = _islandState.value.level + 1
            val newStage = when {
                newLevel >= 4 -> 4
                newLevel >= 3 -> 3
                newLevel >= 2 -> 2
                else -> 1
            }
            val newName = when (newStage) {
                1 -> "Cala Inicial"
                2 -> "Mi Refugio"
                3 -> "Santuario Marino"
                else -> "Paraíso de Muni"
            }
            _islandState.value = _islandState.value.copy(
                level = newLevel,
                levelName = newName,
                stage = newStage,
                progressPercent = currentProgress - 100
            )
        } else {
            _islandState.value = _islandState.value.copy(
                progressPercent = currentProgress
            )
        }
        prefs.edit()
            .putInt("island_level", _islandState.value.level)
            .putInt("island_stage", _islandState.value.stage)
            .putString("island_level_name", _islandState.value.levelName)
            .putInt("island_progress", _islandState.value.progressPercent)
            .apply()
    }

    fun setIslandSkyMode(skyMode: SkyMode) {
        _islandState.value = _islandState.value.copy(skyMode = skyMode)
        prefs.edit().putString("island_sky_mode", skyMode.name).apply()
    }

    fun addReflection(text: String) {
        if (text.isBlank()) return
        val entry = ReflectionEntry(
            id = System.currentTimeMillis().toString(),
            dateText = "Hoy",
            prompt = "¿Qué descubriste sobre tus emociones esta semana?",
            text = text
        )
        val updated = listOf(entry) + _reflections.value
        _reflections.value = updated

        val arr = JSONArray()
        for (r in updated) {
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("dateText", r.dateText)
            obj.put("prompt", r.prompt)
            obj.put("text", r.text)
            arr.put(obj)
        }
        prefs.edit().putString("saved_reflections", arr.toString()).apply()
        completeActivity("act_diario")
    }

    fun toggleFavoriteRoute(routeId: String) {
        val current = _favoriteRouteIds.value.toMutableSet()
        if (current.contains(routeId)) {
            current.remove(routeId)
        } else {
            current.add(routeId)
        }
        _favoriteRouteIds.value = current
        prefs.edit().putString("favorite_routes", current.joinToString(",")).apply()
    }

    fun setUserName(name: String) {
        val clean = name.trim().ifBlank { "Navegante" }
        _userName.value = clean
        prefs.edit().putString("user_name", clean).apply()
    }

    fun evaluateQuestionnaire(questionnaireId: String, selectedOptions: Map<Int, Int>): EvaluationResult {
        val definition = questionnaires.find { it.id == questionnaireId } ?: questionnaires.first()
        var totalScore = 0
        selectedOptions.forEach { (qIdx, optIdx) ->
            val q = definition.questions.getOrNull(qIdx)
            val opt = q?.options?.getOrNull(optIdx)
            totalScore += opt?.score ?: 0
        }

        val maxPossible = definition.questions.size * 2
        val scoreRatio = if (maxPossible > 0) totalScore.toFloat() / maxPossible else 0f

        return when {
            scoreRatio >= 0.65f -> {
                EvaluationResult(
                    questionnaireId = questionnaireId,
                    title = definition.title,
                    score = totalScore,
                    level = EvaluationLevel.APOYO_RECOMENDADO,
                    description = "Tus respuestas reflejan que estás navegando por un oleaje alto y persistente en ${definition.title.lowercase()}. No tienes que afrontar estas cargas en soledad.",
                    recommendedSteps = listOf(
                        "Habla con un adulto de confianza (tus padres, un orientador o un familiar cercano).",
                        "Contacta la Línea Amiga de Medellín (106 o 604 444 44 48) para recibir orientación cálida y profesional.",
                        "Si estás cerca de una estación del Metro (San Antonio, Acevedo, San Javier u Oriente), acércate a los Escuchaderos gratuitos sin cita.",
                        "Utiliza la actividad de 'Respiración del Océano' para recuperar el ritmo y la calma física."
                    ),
                    suggestedRouteId = "route_linea_amiga"
                )
            }
            scoreRatio >= 0.35f -> {
                EvaluationResult(
                    questionnaireId = questionnaireId,
                    title = definition.title,
                    score = totalScore,
                    level = EvaluationLevel.OBSERVAR,
                    description = "Identificamos mareas en movimiento e inquietudes periódicas. Son situaciones comunes en esta etapa, pero conviene observarlas y darles un espacio de cuidado.",
                    recommendedSteps = listOf(
                        "Escribe lo que sientes en tu 'Diario del Mar' para ordenar pensamientos.",
                        "Prueba la actividad de 'Constelación de Fortalezas' para recordar tus herramientas personales.",
                        "Conversa con un amigo/a o familiar sobre cómo te has sentido esta semana.",
                        "Si el oleaje aumenta, recuerda que tienes los Escuchaderos del Metro disponibles para ti."
                    ),
                    suggestedRouteId = "route_escuchadero_san_antonio"
                )
            }
            else -> {
                EvaluationResult(
                    questionnaireId = questionnaireId,
                    title = definition.title,
                    score = totalScore,
                    level = EvaluationLevel.ESTABLE,
                    description = "Tus indicadores muestran un mar sereno y buenas herramientas de regulación en ${definition.title.lowercase()}. ¡Qué lindo que puedas disfrutar de tus momentos de tranquilidad!",
                    recommendedSteps = listOf(
                        "Continúa visitando tu isla personal para nutrir tu rincón seguro.",
                        "Practica momentos de calma y creatividad en el 'Lienzo de Expresión Libre'.",
                        "Comparte tu serenidad y sé un puerto amable para tus compañeros/as."
                    ),
                    suggestedRouteId = null
                )
            }
        }
    }

    fun submitAnonymousConsultation(message: String): AnonymousConsultation {
        val lower = message.lowercase()
        val isEmergency = lower.contains("suicid") || lower.contains("morir") || lower.contains("matar") ||
                lower.contains("lastimar") || lower.contains("abuso") || lower.contains("no quiero vivir") ||
                lower.contains("emergencia") || lower.contains("desesperad") || lower.contains("cortar")

        val response = if (isEmergency) {
            "Te leemos con mucho respeto y preocupación cariñosa. Si sientes que estás en peligro o que el dolor es insoportable, por favor comunícate de inmediato a la Línea Amiga (604 444 44 48 / 106) o a la Línea 123 Social en Medellín. Hay profesionales esperándote las 24 horas, no estás solo/a en este momento."
        } else {
            "Gracias por expresar lo que sientes con tanta honestidad. Poner en palabras tus emociones es el primer paso para desatar los nudos. Este canal conserva tu consulta en tu dispositivo de forma confidencial. Si deseas hablar con alguien en persona, te sugerimos los Escuchaderos del Metro o la Línea Amiga de Medellín."
        }

        val recommendedRoute = if (isEmergency) "Línea Amiga (106) / 123 Social (Urgencias 24/7)" else "Línea Amiga Medellín (106 / WhatsApp 301 604 44 44)"

        val item = AnonymousConsultation(
            id = System.currentTimeMillis().toString(),
            timestamp = System.currentTimeMillis(),
            message = message,
            muniResponse = response,
            recommendedRoute = recommendedRoute,
            isEmergencyFlagged = isEmergency
        )
        val updated = listOf(item) + _consultations.value
        _consultations.value = updated

        // Save consultations
        val cArr = JSONArray()
        for (c in updated.take(30)) {
            val cObj = JSONObject()
            cObj.put("id", c.id)
            cObj.put("timestamp", c.timestamp)
            cObj.put("message", c.message)
            cObj.put("muniResponse", c.muniResponse)
            cObj.put("recommendedRoute", c.recommendedRoute)
            cObj.put("isEmergencyFlagged", c.isEmergencyFlagged)
            cArr.put(cObj)
        }
        prefs.edit().putString("saved_consultations", cArr.toString()).apply()

        return item
    }

    fun switchProfile(profileType: UserProfileType) {
        _currentProfileType.value = profileType
        prefs.edit().putString("current_profile_type", profileType.name).apply()
    }

    fun requestAccompaniment(note: String): AccompanimentRequest {
        val request = AccompanimentRequest(
            id = System.currentTimeMillis().toString(),
            timestamp = System.currentTimeMillis(),
            note = note.ifBlank { "Quiero que conversemos un ratito o me des un abrazo" },
            status = AccompanimentStatus.PENDING
        )
        val updated = listOf(request) + _accompanimentRequests.value
        _accompanimentRequests.value = updated
        saveAccompanimentRequests()
        return request
    }

    fun acknowledgeAccompanimentRequest(requestId: String) {
        val updated = _accompanimentRequests.value.map {
            if (it.id == requestId) it.copy(status = AccompanimentStatus.ACKNOWLEDGED) else it
        }
        _accompanimentRequests.value = updated
        saveAccompanimentRequests()
    }

    fun generateNewLinkCode(): String {
        val randomNum = (100..999).random()
        val newCode = "MAR-$randomNum"
        _familyLinkState.value = _familyLinkState.value.copy(linkCode = newCode)
        saveFamilyLinkState()
        return newCode
    }

    fun linkFamilyWithCode(code: String, parentName: String = "Acompañante Familiar"): Boolean {
        val cleanInput = code.trim().uppercase()
        val currentCode = _familyLinkState.value.linkCode.trim().uppercase()

        // Match either exact code or demo prefix
        val isValid = cleanInput == currentCode || cleanInput.startsWith("MAR-") || cleanInput == "MAR-482"
        if (isValid) {
            _familyLinkState.value = _familyLinkState.value.copy(
                isLinked = true,
                parentName = parentName.ifBlank { "Acompañante Familiar" },
                childName = _userName.value,
                linkedSinceTimestamp = System.currentTimeMillis()
            )
            saveFamilyLinkState()
            return true
        }
        return false
    }

    fun unlinkFamily() {
        _familyLinkState.value = _familyLinkState.value.copy(
            isLinked = false,
            linkedSinceTimestamp = 0L
        )
        saveFamilyLinkState()
    }

    fun updateFamilyPermissions(shareTides: Boolean, shareIsland: Boolean, shareActivities: Boolean) {
        _familyLinkState.value = _familyLinkState.value.copy(
            shareTidesSummary = shareTides,
            shareIslandProgress = shareIsland,
            shareActivitiesSummary = shareActivities
        )
        saveFamilyLinkState()
    }

    private fun saveFamilyLinkState() {
        val state = _familyLinkState.value
        prefs.edit()
            .putBoolean("family_is_linked", state.isLinked)
            .putString("family_link_code", state.linkCode)
            .putString("family_child_name", state.childName)
            .putString("family_parent_name", state.parentName)
            .putLong("family_linked_time", state.linkedSinceTimestamp)
            .putBoolean("family_share_tides", state.shareTidesSummary)
            .putBoolean("family_share_island", state.shareIslandProgress)
            .putBoolean("family_share_activities", state.shareActivitiesSummary)
            .apply()
    }

    private fun saveAccompanimentRequests() {
        val arr = JSONArray()
        for (acc in _accompanimentRequests.value.take(20)) {
            val obj = JSONObject()
            obj.put("id", acc.id)
            obj.put("timestamp", acc.timestamp)
            obj.put("note", acc.note)
            obj.put("status", acc.status.name)
            arr.put(obj)
        }
        prefs.edit().putString("saved_accompaniment_requests", arr.toString()).apply()
    }

    private fun saveLogs() {
        val arr = JSONArray()
        for (log in _logs.value.take(40)) {
            val obj = JSONObject()
            obj.put("id", log.id)
            obj.put("type", log.type.name)
            obj.put("dayOfWeek", log.dayOfWeek)
            obj.put("timestamp", log.timestamp)
            obj.put("situation", log.situation)
            obj.put("timeOfDay", log.timeOfDay)
            obj.put("location", log.location)
            obj.put("note", log.note)
            arr.put(obj)
        }
        prefs.edit().putString("saved_logs", arr.toString()).apply()
    }
}
