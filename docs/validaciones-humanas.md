# Validaciones humanas pendientes (gratuitas)

Las pruebas automáticas demuestran que la app hace lo que dice. Estas validaciones demuestran que lo que dice es correcto y adecuado. Ninguna requiere pagar.

| Qué | Quién (gratis) | Cómo | Evidencia a guardar |
| --- | --- | --- | --- |
| Textos de mareas, las 6 actividades con propósito (`GuidedActivities.kt`), la Bitácora y Conexión familiar (`AdultGuide.kt`) y mensajes de Muni | Docente o estudiantes de psicología con experiencia en infancia, adolescencia y orientación familiar | Revisar los archivos y las pantallas con la checklist de abajo | Checklist firmada, fecha y versión aprobada de cada experiencia |
| Protocolo de Mis salvavidas (qué pasa si el adulto no responde, no está disponible, hay urgencia o el adulto no es seguro) | Psicólogo/a y consultorio jurídico | Revisar los mensajes de `SalvavidasScreen.kt` y el tiempo de 15 minutos | Protocolo escrito y aprobado |
| Rutas de apoyo y mensajes de riesgo | Mismo revisor | Llamar o consultar cada ruta; revisar `SafetyRules.kt` | Tabla de `rutas-por-verificar.md` actualizada |
| Privacidad y datos de menores | Consultorio jurídico universitario o docente | Checklist Ley 1581 de 2012 y Decreto 1377 de 2013 (art. 12, datos de niños, niñas y adolescentes). Incluir qué se guarda en Firebase (`firebase/firestore.rules`), retención y borrado | Concepto escrito; no es una validación legal formal |
| Usabilidad | 5 o más usuarios de 11 a 14 años con consentimiento del acudiente y asentimiento del menor; si no es posible, compañeros (indicarlo como limitación) | Guion de tareas + Google Forms | % de tareas completadas sin ayuda y comentarios |
| Accesibilidad | El equipo | App gratuita Accessibility Scanner (Google Play) en un teléfono real | Capturas del informe |
| Imágenes | Equipo de diseño | Confirmar versión oficial de Muni y entregar la isla en alta resolución | Archivos fuente en `design-originals/` |

## Checklist para la revisión psicológica

- [ ] Ningún texto diagnostica, etiqueta ni califica al usuario.
- [ ] Ninguna marea se presenta como buena o mala.
- [ ] Los mensajes de Alta y Tormenta muestran ayuda humana y adulto de confianza.
- [ ] Las actividades no prometen resultados clínicos ni tienen respuestas «correctas».
- [ ] La Bitácora no califica a los adultos ni diagnostica al niño.
- [ ] Las guías de Conexión familiar son voluntarias y no sirven para vigilar.
- [ ] Mis salvavidas no promete atención inmediata y siempre ofrece líneas de ayuda.
- [ ] El lenguaje es adecuado para 11 a 14 años (ni infantil ni clínico).

## Guion corto de usabilidad (10 minutos)

1. Registra cómo te sientes hoy.
2. Registra una marea sin decir por qué.
3. Abre una actividad, sal de ella y vuelve a entrar.
4. Termina «Mi mapa de situaciones», elige un objeto y encuéntralo en tu isla.
5. Mira cómo fue tu semana.
6. Encuentra a quién llamar si te sientes muy mal.
7. Borra un registro.
8. (Con un adulto) Vincula tu perfil con el código del adulto y crea tu PIN.
9. Pide acompañamiento desde Mis salvavidas y mira qué responde el adulto.
