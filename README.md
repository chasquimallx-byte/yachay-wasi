# YACHAY WASI — Colegio Virtual (Android)

*Aprender descubriendo, crecer pensando.* App educativa **sin internet** para 1.º y 2.º de primaria (6–8 años), con Tito, un cuy explorador.
Kotlin + Jetpack Compose. Sin permisos, sin publicidad, sin cuentas. El progreso se guarda solo en el celular.

> Plataforma de apoyo al aprendizaje. **No** está autorizada ni acreditada por el Ministerio de Educación ni reemplaza la matrícula escolar.

## Cómo obtener el APK (GitHub Actions, sin instalar nada)

1. Crea un repositorio nuevo en GitHub y sube **todo el contenido de esta carpeta** (incluida `.github/`).
2. Entra a la pestaña **Actions** → se ejecuta solo *Compilar APK Yachay Wasi* (o pulsa **Run workflow**).
3. Al terminar (≈5 min) abre la ejecución → **Artifacts** → descarga `YachayWasi-APK` (zip con `YachayWasi-debug.apk`).
4. Para publicarlo en **Releases**: crea una etiqueta, por ejemplo `git tag v0.1.0 && git push origin v0.1.0`.

## Instalar en el celular

1. Pasa `YachayWasi-debug.apk` al celular (WhatsApp, cable o Drive).
2. Ábrelo y permite *«Instalar apps desconocidas»* para esa app.
3. Para la voz de Tito: Ajustes → Sistema → Idioma → **Texto a voz** → instalar voz en español (y en inglés para las 50 palabras).

## Compilar en tu PC (opcional)

Android Studio (Koala o superior) → *Open* esta carpeta → *Run*. Por línea de comandos (JDK 17 + Android SDK 34 + Gradle 8.9):
`gradle testDebugUnitTest assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`

## Estructura

```
.github/workflows/build.yml   compila y publica el APK
app/src/main/java/pe/yachaywasi/
  MainActivity.kt   navegación        Screens.kt   inicio, tutorial, aventuras, aprender, inglés, progreso, adultos
  Player.kt         motor de actividades (elegir / tocar todas / ordenar, pistas graduadas)
  Lab.kt            memoria, patrones, rompecabezas, clasificación
  MazeLogic.kt      mapas, colisiones, frutas, meta, pista (lógica pura, con pruebas)
  MazeScreen.kt     pantalla de laberintos      Tilt.kt   acelerómetro real
  Content.kt        contenidos y 50 palabras    Store.kt  progreso local   Speaker.kt  voz
app/src/test/     pruebas unitarias (laberintos y contenidos)
```

## Funciones implementadas (Fase 1)

- Pantalla principal con 4 botones, Tito que saluda, sonido on/off, repetir y tutorial (7 pasos, el último con inclinación real).
- Selector de grado (adulto), perfil local, sin registro.
- **Aventuras**: mapa con 9 lugares; funcionales **Mi hogar, El mercado, La chacra, El río** (los demás: «Próximamente»). Lugares que se desbloquean con estrellas.
- **Aprender**: 1.º (vocales, sílabas, lectura, conteo, figuras y patrones, sumas y restas, cuerpo/convivencia) y un primer conjunto de 2.º (oraciones, números hasta 100, ciclos de vida y agua).
- **Laboratorio de la Mente**: memoria visual, patrones, rompecabezas, clasificación (con 3 niveles que se adaptan al desempeño) y **Los laberintos de Tito**.
- **Laberintos** (3): acelerómetro real con filtro, calibración, zona neutral, velocidad ajustable, colisiones por pasos cortos, pausa/reanudación (también si la app pasa a segundo plano), reinicio, pista, 3 frutas, puente; control alternativo con botones; sin tiempo límite ni vidas.
- **Inglés**: 50 palabras en 10 categorías, tarjeta con imagen + voz + significado, y juego «escucha y toca».
- Estrellas, insignias, objetos para la casa de Tito; panel para adultos protegido (aciertos, errores, pistas, refuerzos, historial, borrar datos).
- Sin permisos de Android; funciona en modo avión.

## Pruebas

- Pruebas unitarias (`app/src/test`): mapas solucionables, 3 frutas, puente obligatorio, no atravesar paredes, un «bot» completa los 3 laberintos, zona neutral, 50 palabras, validez de todas las preguntas. Se ejecutan en cada compilación de GitHub Actions.
- La lógica del motor de laberintos también se simuló aparte (Python) antes de entregar: los 3 mapas se resuelven y las paredes no se atraviesan.
- **Pendiente:** probar el acelerómetro, la voz (TTS), el rendimiento y el modo avión en un **celular físico**. No se ha verificado en un teléfono real.

## Pendiente / siguientes fases

- Ilustraciones y audios propios: hoy se usan **emojis del sistema** (aspecto según el celular) y la **voz de Android** (TTS). Sustituir por arte y grabaciones propias con licencia clara; los emojis provisionales (p. ej. «desk» = 💺) deben cambiarse.
- Aventuras: pueblo, bosque, jardín, montaña y laboratorio de inventos.
- Completar la ruta de 2.º y **verificar contra el Currículo Nacional (CNEB)** las competencias de cada actividad (aún no se ha hecho).
- Quechua: elegir variedad y validar con hablantes antes de publicar (no se inventó ninguna traducción).
- Profesor con IA: no existe; Tito usa respuestas programadas.
- Firmar el APK de *release* (el APK de GitHub Actions es *debug*: instalable, pero no apto para Play Store).

## Licencias de recursos

Código propio del proyecto. Sin imágenes ni audios incluidos: emojis del sistema (fuente del dispositivo) y motor TTS de Android. Librerías: AndroidX / Jetpack Compose (Apache 2.0).
