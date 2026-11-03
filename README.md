# Marea Club

App Android de bienestar emocional para preadolescentes (11 a 14 años). Proyecto universitario.

Marea Club ayuda a reconocer cómo te sientes con cuatro "mareas" (tranquila, movida, alta, tormenta) y la opción «No sé qué siento», ofrece actividades breves de autocuidado y muestra el avance en una isla personal, acompañado por la mascota Muni.

> **Límite:** Marea Club acompaña, pero **no diagnostica ni reemplaza** atención psicológica.

## Cómo probarla

**Opción 1: descargar el APK (sin instalar nada)**
1. Entra a la pestaña **Actions** de este repositorio y abre la última ejecución en verde.
2. Descarga el artefacto `marea-club-debug-apk` y descomprímelo.
3. Pásalo a un teléfono Android e instálalo (permite "orígenes desconocidos").

**Opción 2: Android Studio (gratis)**
1. Clona el repositorio y ábrelo en Android Studio.
2. Crea la clave de depuración en la carpeta raíz (solo una vez):
   ```
   keytool -genkeypair -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
   ```
3. Ejecuta la app en un emulador o teléfono.
4. Pruebas: `./gradlew testDebugUnitTest` · Lint: `./gradlew lintDebug`

## Privacidad (resumen)

- Todo se guarda **cifrado y solo en el teléfono** (Tink, AES-256-GCM con Android Keystore).
- La app **no tiene permiso de internet**, no usa cuentas y no hace copia de seguridad en la nube.
- El usuario puede borrar un registro o **todos sus datos** desde el menú (con confirmación).
- La consulta anónima es una **demostración**: ninguna persona lee los mensajes, y la app lo dice en pantalla.

## Estructura

| Carpeta | Contenido |
| --- | --- |
| `app/src/main/java/com/example/domain` | Reglas sin Android: isla, resumen semanal, seguridad, orientación |
| `app/src/main/java/com/example/data` | Contenido fijo (`MareaContent`), almacenamiento cifrado (`MareaRepository`) |
| `app/src/main/java/com/example/ui` | Pantallas y diálogos (Jetpack Compose) |
| `app/src/test` | Pruebas automáticas (JUnit, Robolectric, Compose UI Test) |
| `design-originals/` | Archivos originales del equipo (Muni, ilustraciones, manual de marca). No van en la app |
| `docs/` | Referencias, rutas por verificar, validaciones humanas pendientes |
| `tools/` | Scripts para regenerar los sonidos y los recortes de imagen |

## Herramientas (todas gratuitas)

Android Studio, Kotlin, Jetpack Compose, Tink, JUnit, Robolectric, Android Lint y GitHub Actions (repositorio público).

## Estado y pendientes

Ver [CHANGELOG.md](CHANGELOG.md), [docs/validaciones-humanas.md](docs/validaciones-humanas.md) y [docs/rutas-por-verificar.md](docs/rutas-por-verificar.md).
