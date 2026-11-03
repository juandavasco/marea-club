# Cambios

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
