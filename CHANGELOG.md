# Cambios

## Actualización integral — entregas 3, 4 y 5 (7 nov 2026)

### Cuentas y vínculo familiar (entrega 3)
- Al instalar se elige quién usa el teléfono: **niño o niña** (sin cuenta) o **adulto** (cuenta de Google).
- El adulto crea el espacio de la familia (con un apodo opcional para el niño), genera **códigos de vínculo** para el niño y para un **segundo adulto** (máximo dos), y puede desvincular.
- Códigos aleatorios de 8 caracteres sin caracteres confusos, válidos **10 minutos** según la hora del servidor y de **un solo uso**. En la nube solo se guarda su SHA-256.
- Límite de intentos: tras 5 códigos inválidos en un teléfono, esperas crecientes.
- El niño se vincula desde **menú ⋯ › Mi familia**, ve con quién está vinculado y qué puede y no puede ver su adulto, y puede salir de la familia.
- **PIN del niño** (4 a 6 dígitos, sin secuencias obvias), guardado como hash PBKDF2 con sal; esperas crecientes tras 5 intentos; si lo olvida, el adulto le genera un código nuevo.
- **Firebase** (plan Spark gratuito): Authentication (Google y anónimo) y Firestore. Sin Cloud Functions: toda la validación está en las **reglas de seguridad** (`firebase/firestore.rules`), probadas con el emulador en GitHub Actions.
- La app ahora usa internet **solo** para el vínculo, Mis salvavidas y, si el niño lo activa, el resumen de su semana. Mareas, notas, diario, respuestas e isla siguen cifrados solo en el teléfono.

### Mis salvavidas y perfil del adulto (entrega 4)
- El niño vinculado avisa a **uno o a ambos** adultos con el mensaje «Necesito que estés conmigo. Cuando puedas, ven a acompañarme». Decir qué necesita es opcional (hablar, compañía, ayuda práctica, simplemente estar acompañado).
- Estado real del pedido por adulto: aún no lo ha visto, lo vio, va en camino, no puede. A los **15 minutos** sin respuesta se sugiere buscar a otro adulto o llamar a una línea. Se puede cancelar o cerrar.
- Sin conexión se avisa y el pedido no queda en cola.
- Solo el adulto destinatario puede leer cada pedido.
- **Compartir mi semana**: apagado por defecto; si el niño lo activa, el adulto ve solo cuántas mareas de cada tipo hubo y en cuántos días. Al apagarlo, salir o desvincular se borra de la nube.
- Perfil del adulto con pestañas **Resumen** (pedidos y semana compartida), **Familia** y **Configuración**.

### Bitácora y Conexión familiar (entrega 5)
- **Bitácora del acompañamiento**: 4 recorridos y 5 experiencias con objetivo, situación, reflexión (cada opción con un comentario, sin puntajes), aprendizaje y aplicación cotidiana. Nada de lo elegido se guarda.
- **Conexión familiar**: 5 guías voluntarias para hacer en persona (5 a 15 minutos). El adulto las ve en su pestaña; el niño vinculado, en Actividades › Con mi familia. No se registra la participación.
- Contenidos marcados como borrador pendiente de revisión psicológica.

### Limitaciones conocidas
- **Sin notificaciones push**: en el plan gratuito no hay servidor que las envíe. El adulto ve los pedidos al abrir la app (se dice en pantalla).
- El límite de intentos de códigos es por teléfono; un límite global en el servidor necesitaría Cloud Functions o App Check (plan Blaze).

## Actualización integral — entregas 1 y 2 (7 nov 2026)

Sigue el Prompt Maestro y el Documento Técnico Integral v1.0 (5 nov 2026). Decisiones del 7 nov: Firebase Spark (gratis), respuestas privadas solo en el teléfono del niño, reubicar las herramientas existentes y retirar Autoevaluación y Consulta anónima.

### Navegación del niño
- Cuatro pestañas: **Inicio**, **Mareas**, **Actividades** y **Mi isla**.
- Inicio simplificado: saludo, «¿Cómo viene tu marea?», una actividad sugerida y acceso a Mis salvavidas.
- Mareas reúne **Registrar** y **Mi semana** (antes «Progreso»); la pestaña «Reflexiones» pasa a llamarse «Mi diario».
- Sistema visual común (`ui/components/MareaDesign.kt`): espaciados, tarjetas, botones de 52 dp, opciones con estado seleccionado anunciado al lector de pantalla.

### Mareas
- Descripciones de las 5 mareas tomadas del Documento Técnico §5.
- Dentro de «No sé» se puede elegir «No sé qué siento» o «No encuentro una emoción que describa cómo estoy»; se guarda con el registro.

### Actividades
- **Para explorar**: 6 actividades con propósito (Mi mapa de situaciones, Dos mareas al mismo tiempo, Antes de responder, Lo que necesito y lo que puedo pedir, Lo que no se ve de mí, Mi propio ritmo). Contenido marcado como *borrador pendiente de revisión psicológica*.
- Se puede salir en cualquier paso sin que se guarde nada. Guardar en el diario es opcional y está desmarcado.
- **Para calmarme**: las 8 herramientas que ya existían (respiración, burbujas, sonidos, diario, dibujo, conchas, brújula, constelación). Terminan con «¿Te ayudó?» opcional y no cambian la isla.

### Mi isla
- Una sola escena. Al terminar una actividad con propósito se ofrecen 2 o 3 objetos (hamaca, jardín, puente, faro) y el niño elige uno o «Ahora no».
- Cada objeto muestra qué significa, de qué actividad vino y admite una reflexión privada (cifrada).
- Se retiran niveles, porcentajes, candados, «nuevo» y contadores.

### Mis salvavidas
- Pantalla nueva con «Necesito acompañamiento». El botón queda **deshabilitado** y lo explica hasta que exista un vínculo real con un adulto (entregas 3 y 4); no se simula ningún envío.
- Líneas 106, Línea Amiga y 123 a un toque, y acceso al directorio completo.

### Retirado
- Autoevaluación preliminar y Consulta anónima (decisión del 7 nov 2026).

### Pruebas
- Nuevas pruebas de dominio (objetos de la isla y actividades) y de almacenamiento (ofertas, objetos no ofrecidos, «Ahora no», reflexión cifrada).
- Pruebas de interfaz de punta a punta para las cuatro pestañas, «No sé», actividad con elección de objeto, salir a la mitad y Mis salvavidas sin vínculo.
- Capturas automáticas ampliadas a 15 pantallas.

### Pendiente (entregas 3–5)
- Cuentas y vínculo familiar con Firebase (necesita `google-services.json`), PIN del niño, envío real de Mis salvavidas, perfil del adulto (Resumen, Familia, Bitácora, Conexión familiar, Configuración).
- Ilustraciones definitivas de los objetos de la isla (hoy son íconos provisionales).
- Revisión psicológica de los textos de las 6 actividades.

## Fase 1 (2 nov 2026)

Corrige los hallazgos de la matriz de trazabilidad de la Fase 0. Los números entre paréntesis son filas de la matriz.

### Seguridad del menor
- Al elegir marea Alta o Tormenta aparece una tarjeta con adulto de confianza, botón para llamar a la Línea 106 y acceso a las rutas (5).
- La consulta anónima dice en pantalla que es una demostración y que nadie lee los mensajes; la ayuda humana queda a un toque, se detecte o no riesgo (38, 39).
- Rutas de apoyo con fuente, enlace y fecha de verificación; las no verificadas no se muestran (40). Ver `docs/rutas-por-verificar.md`.
- La autoevaluación ya no muestra puntaje ni nivel; solo orientación y rutas siempre visibles (37).

### Privacidad
- Datos emocionales cifrados con Tink (AES-256-GCM) (33). Se borran los datos sin cifrar de la versión anterior.
- Sin permiso de internet y sin copia en la nube ni transferencia entre dispositivos (34).
- Se elimina el vínculo de acudiente que aceptaba cualquier código «MAR-…» (35).
- Pantalla «Tus datos y privacidad» en la bienvenida y el menú (36, informativa).

### Lógica
- Registro de marea sin valores precargados; el contexto es opcional (2, 3).
- Cierre de actividad con «¿Te ayudó?» opcional (9).
- El Diario del Mar se guarda (cifrado) y se puede borrar (13).
- Resumen semanal por fecha real (lunes a domingo), días vacíos sin marea, aviso de pocos datos y borrado de registros (14, 15).
- Isla: 9 acciones desbloquean 9 elementos, el aviso explica qué acción produjo el cambio, estados «nuevo» y «seleccionado», nivel derivado de los elementos (16–20).
- Reinicio protegido con confirmación y bienvenida en instalación nueva (31, 32).
- Ficha de propósito, pasos y alternativa en cada actividad (11).

### Controles y visual
- Sin controles con acción vacía; las etiquetas informativas ya no parecen botones (30).
- «Sonidos del océano» reproduce audio real (sonidos originales generados con `tools/generar_sonidos.py`) y se completa tras 1 minuto de escucha (12).
- Tonos de marea tomados del mockup del equipo (6); textos con contraste AA (28).
- Muni recortado de nuevo desde el original, sin huecos; isla sin franja gris y con fondo transparente (29).

### Repositorio
- Wrapper de Gradle, GitHub Actions (lint, pruebas y APK), originales archivados, README en español.

### Pendientes conocidos
- Confirmar versión oficial de Muni; la isla original es de baja resolución (249×195 px) y muestra un pulpo, no la medusa Muni.
- Escuchaderos sin horario publicado; consultorios universitarios sin verificar.
- Validaciones humanas: ver `docs/validaciones-humanas.md`.
