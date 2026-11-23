# Prompt logbook — Darío Valdemar

## DV-01

- **Date and time:** 2026-11-20 18:26
- **Author:** Darío Valdemar
- **Tool:** Claude Code (terminal; new session)
- **Files touched:** prompts/dario-v.md, CLAUDE.md
- **Result:** pending
- **How I checked it:** pending

### Prompt

```text
Soy Darío Valdemar, miembro del grupo de Fianzapp (app 5 del proyecto de la asignatura MAI SAP de CUNEF). Respóndeme siempre en español; el código, los nombres y los comentarios, en inglés como el resto del repo.

<contexto>
- Proyecto Android en Kotlin + Jetpack Compose, clonado en C:\Users\dario\AndroidStudioProjects\fianzapp, rama main, repo del grupo en GitHub.
- La guía oficial del esqueleto está en C:\Users\dario\Downloads\MAI_SAP_Skeleton_App5_Fianzapp_guia.txt (fuera del repo: léela, NO la copies al repo). Define las filas A1–A10, cómo comprobar cada una y las secciones 10 y 11 (datos que no se pueden adivinar y Firebase).
- README.md es parte de la entrega y su sección "Facts you cannot guess" debe quedarse tal cual.
- prompts/README.md define el registro de prompts: cada prompt que envío a una IA va en mi archivo prompts/dario-v.md, en orden, con fecha y hora anteriores al commit de su resultado. Un registro inventado o retocado es un problema de integridad académica.
- Un compañero (Jaime) ya hizo commit de: carga del checklist (A5), plazo de la fianza (A7), navegación y dos tests unitarios. No sé cuánto de esas filas está realmente completo.
- Emulador disponible: Pixel 10a, Android API 37. Yo ejecuto la app desde Android Studio; tú no ves el emulador.
</contexto>

<reglas_siempre>
- NUNCA cambies versiones en gradle/libs.versions.toml ni añadas el plugin org.jetbrains.kotlin.android, aunque parezca necesario.
- NUNCA escribas contraseñas, claves ni datos personales en ningún archivo ni en el User-Agent de HttpClient.kt.
- En prompts/ solo puedes tocar prompts/dario-v.md, y solo como se indica abajo. Nunca edites el archivo de otro miembro ni prompts/README.md. Nunca cambies el texto de un prompt ya registrado.
- NUNCA hagas push, pull, merge ni cambies de rama. Solo puedes hacer commit cuando se indique explícitamente abajo o yo te lo pida.
- No toques local.properties, .aiexclude, .gitignore ni firestore.rules salvo que te lo pida.
- Solo haz los cambios que te pida en cada tarea: nada de refactors, funciones extra ni librerías nuevas. Si una tarea necesita una dependencia nueva, para y pregúntame.
- Afirma solo lo que hayas comprobado leyendo archivos o ejecutando comandos. Si algo no lo puedes verificar (por ejemplo, cómo se ve en el emulador), dilo.
</reglas_siempre>

<tarea_de_hoy>
Esta primera sesión es de registro, análisis y preparación. No modifiques código de la app.

0. Antes de nada, registra este prompt:
   - Obtén la fecha y hora actuales con `Get-Date -Format "yyyy-MM-dd HH:mm"`.
   - Crea prompts/dario-v.md con un título "# Prompt logbook — Darío Valdemar" y la entrada DV-01 usando exactamente la plantilla de prompts/README.md:
     - Date and time: la hora obtenida.
     - Author: Darío Valdemar.
     - Tool: Claude Code (terminal; new session).
     - Files touched: prompts/dario-v.md, CLAUDE.md.
     - Result y How I checked it: déjalos como "pending" (los relleno yo al final).
     - Prompt: este prompt completo, copiado literalmente, sin cambiar ni un carácter.
     - What happened: "pending".
   - Haz un commit que contenga SOLO prompts/dario-v.md con el mensaje "Add Darío's prompt logbook (DV-01)".
1. Lee README.md, prompts/README.md, la guía (sobre todo las secciones 1–8 y 10–11), CLAUDE.md si existe y todo app/src. Revisa `git log --oneline`.
2. Ejecuta `.\gradlew.bat testDebugUnitTest` y `.\gradlew.bat assembleDebug` y dime si pasan.
3. Dame una tabla con las filas A1–A10: qué pide la guía (1 línea), estado actual (hecho / a medias / no empezado), archivos implicados y qué falta, incluido el "awkward case" de cada fila. Marca como "a medias" todo lo que no puedas confirmar que está completo.
4. Crea en la raíz del repo un CLAUDE.md (máximo ~60 líneas) con: las reglas de arriba, los datos clave de la sección 10 de la guía (URL y campos del checklist, lenientJson único, fotos en filesDir/photos/, FileProvider, Firebase solo plan Spark), la estructura de paquetes actual y el ciclo de trabajo de abajo. NO hagas commit todavía: enséñamelo y espera a que te diga "OK CLAUDE.md". Cuando te lo diga, haz un commit que contenga SOLO CLAUDE.md con el mensaje "Add CLAUDE.md for Claude Code" y dame su hash corto, porque mi siguiente entrada del registro tiene que nombrarlo.
5. Termina con: (a) las preguntas que necesites que te responda (por ejemplo, qué filas me tocan a mí y si trabajamos en ramas), y (b) la siguiente tarea pequeña que me recomiendas, en una frase.

Para ahí y espera mi respuesta.
</tarea_de_hoy>

<ciclo_para_las_siguientes_tareas>
A partir de ahora, cada vez que te envíe un prompt:
1. Primero añade a prompts/dario-v.md una nueva entrada con el siguiente ID (DV-02, DV-03…), la hora de `Get-Date`, mi prompt copiado literalmente y Result / How I checked it / What happened en "pending". Si en la sesión ha habido un commit de CLAUDE.md desde la última entrada, nómbralo en el campo Tool.
2. Antes de editar código, dime en 3–5 líneas qué archivos vas a tocar y qué cambio harás.
3. Haz solo esa tarea pequeña (una fila o parte de una, incluido su awkward case).
4. Ejecuta los tests unitarios y assembleDebug y dime el resultado real.
5. Dime exactamente qué tengo que tocar en el emulador para comprobarlo, según el "Check it yourself" de la guía.
6. Dame un resumen de 2–3 líneas de lo que cambiaste para "What happened" y propón un mensaje de commit. No hagas el commit del código hasta que yo te lo pida; cuando lo haga, el registro va en un commit anterior o en el mismo, nunca después.
</ciclo_para_las_siguientes_tareas>
```

### What happened

pending

## DV-02

- **Date and time:** 2026-11-20 18:38
- **Author:** Darío Valdemar
- **Tool:** Claude Code (desktop app; same session as DV-01)
- **Files touched:** prompts/dario-v.md
- **Result:** pending
- **How I checked it:** pending

### Prompt

```text
1. el proyecto entero se divide entre nosotros dos, y un tercero que ahora mismo no forma parte del repositorio.
2. no te sabria decir.
3. cambialo
4. si, lo tiene sin subir
5. no, pero lo hago yo si es necesario
```

### What happened

pending

## DV-03

- **Date and time:** 2026-11-20 18:40
- **Author:** Darío Valdemar
- **Tool:** Claude Code (desktop app; same session as DV-02)
- **Files touched:** prompts/dario-v.md, CLAUDE.md
- **Result:** pending
- **How I checked it:** pending

### Prompt

```text
OK CLAUDE.md
```

### What happened

pending

## DV-04

- **Date and time:** 2026-11-23 18:42
- **Author:** Darío Valdemar
- **Tool:** Claude Code (desktop app; same session as DV-03; CLAUDE.md committed in 338a897)
- **Files touched:** prompts/dario-v.md
- **Result:** pending
- **How I checked it:** pending

### Prompt

```text
**Role:** Actúa como diseñador/a UI y desarrollador/a front-end con experiencia en páginas de inicio modernas, interactivas y animadas para aplicaciones web.

**Task:** Rediseña solo la estética de la página principal de mi aplicación: ahora son solo 3 opciones sobre fondo blanco y quiero que tenga elementos interactivos y en movimiento relacionados con la temática de la app. No cambies todavía el interior, el funcionamiento, las acciones ni los documentos de la aplicación.

**Context:**
- La aplicación gira en torno a una vivienda, un inquilino, un alquiler y el dinero de la fianza. El diseño visual debe reflejar esa temática.
- La sensación buscada es moderna y profesional.
- Mantén las 3 opciones actuales de la página principal; solo cambia su apariencia y el entorno visual.
- Más adelante, en una segunda fase, entraremos en detalle para ajustar visualmente el interior de cada una de las 3 opciones. Ahora no.
- Tecnología y código actual de la app: [fill this in: framework o lenguaje que usas, y pega el código de la página principal si quieres que lo modifique directamente]
- Nombres de las 3 opciones y colores o logo de marca, si existen: [fill this in: ...]
- Cómo quiero empezar: o bien me das primero ideas y propuestas, o pasas directamente a la acción. Elige tú lo más útil: si tienes el código necesario, pasa directamente a implementar una propuesta y añade brevemente 2 o 3 alternativas; si no, empieza con 3 propuestas de diseño breves y recomienda una.

**Format:**
- Tono claro y directo, en español.
- Si das propuestas: lista corta, con una frase por idea que describa estilo, animaciones e interacciones (por ejemplo, una casa o llaves animadas, efectos al pasar el cursor, transiciones suaves).
- Si implementas: entrega el código completo y listo para usar, con una breve explicación de qué cambia y cómo probarlo.

**Quality check:** Antes de responder, comprueba que:
- Solo has tocado la estética de la página principal, no la lógica ni el interior.
- Las 3 opciones siguen siendo claras y fáciles de usar.
- Las animaciones son sutiles, profesionales y no estorban.
- El diseño funciona en móvil y en escritorio.
- No has inventado datos sobre mi app; lo que falta lo has señalado.
```

### What happened

pending

## DV-05

- **Date and time:** 2026-11-23 19:13
- **Author:** Darío Valdemar
- **Tool:** Claude Code (desktop app; same session as DV-04)
- **Files touched:** prompts/dario-v.md
- **Result:** pending
- **How I checked it:** pending

### Prompt

```text
Sí, commitea
```

### What happened

pending

## DV-06

- **Date and time:** 2026-11-23 19:41
- **Author:** Darío Valdemar
- **Tool:** Claude Code (desktop app; same session as DV-05)
- **Files touched:** prompts/dario-v.md
- **Result:** pending
- **How I checked it:** pending

### Prompt

```text
ahora mismo la pp no carga, se queda el modulador en una pantalla de inicio con el logotipo de android
```

### What happened

pending

## DV-07

- **Date and time:** 2026-11-23 20:10 (logged at this time; the prompt was sent a few minutes before and I interrupted the answer)
- **Author:** Darío Valdemar
- **Tool:** Claude Code (desktop app; same session as DV-06)
- **Files touched:** none
- **Result:** pending
- **How I checked it:** pending

### Prompt

```text
Ya arranca
```

### What happened

pending

## DV-08

- **Date and time:** 2026-11-23 20:10
- **Author:** Darío Valdemar
- **Tool:** Claude Code (desktop app; same session as DV-07)
- **Files touched:** prompts/dario-v.md
- **Result:** pending
- **How I checked it:** pending

### Prompt

```text
Ya arranca, pero veo la pantalla de inicio excesivamente basica, no quiero una animacion, quiero fotos realistas o simplemente una pantalla de inicio que parezca bastante mas seria. necesito en la zona de checklist mas cosas, si crees que faltan, y la posibilidad de adjuntar una foto en cada cuarto y que sea posible añadir algo con su foto, por si esta roto o dañado. lo de deposit deadline, hazlo mas profesional, que tenga mas cosas, dame ideas para añadir y que quede mejor, ya que ahora esta muy cutre y tiene muy poco, y vamosa hacer lo mismo con la parte de la firma
```

### What happened

pending

## DV-09

- **Date and time:** 2026-11-23 20:13
- **Author:** Darío Valdemar
- **Tool:** Claude Code (desktop app; same session as DV-08)
- **Files touched:** prompts/dario-v.md
- **Result:** pending
- **How I checked it:** pending

### Prompt

```text
Room (A4) primero
```

### What happened

pending

## DV-10

- **Date and time:** 2026-11-23 20:17
- **Author:** Darío Valdemar
- **Tool:** Claude Code (desktop app; same session as DV-09)
- **Files touched:** prompts/dario-v.md
- **Result:** pending
- **How I checked it:** pending

### Prompt

```text
Sí, commitea
```

### What happened

pending

## DV-11

- **Date and time:** 2026-11-23 20:17
- **Author:** Darío Valdemar
- **Tool:** Claude Code (desktop app; same session as DV-10)
- **Files touched:** prompts/dario-v.md
- **Result:** pending
- **How I checked it:** pending

### Prompt

```text
Sí, Rooms
```

### What happened

pending

## DV-12

- **Date and time:** 2026-11-23 20:23
- **Author:** Darío Valdemar
- **Tool:** Claude Code (desktop app; same session as DV-11)
- **Files touched:** prompts/dario-v.md
- **Result:** pending
- **How I checked it:** pending

### Prompt

```text
Probado, commitea
```

### What happened

pending

## DV-13

- **Date and time:** 2026-11-23 20:29
- **Author:** Darío Valdemar
- **Tool:** Claude Code (desktop app; same session as DV-12)
- **Files touched:** prompts/dario-v.md
- **Result:** pending
- **How I checked it:** pending

### Prompt

```text
Sí, Rooms
```

### What happened

pending

## DV-14

- **Date and time:** 2026-11-23 20:39
- **Author:** Darío Valdemar
- **Tool:** Claude Code (desktop app; same session as DV-13)
- **Files touched:** prompts/dario-v.md
- **Result:** pending
- **How I checked it:** pending

### Prompt

```text
Probado, commitea
```

### What happened

pending

## DV-15

- **Date and time:** 2026-11-23 20:40
- **Author:** Darío Valdemar
- **Tool:** Claude Code (desktop app; same session as DV-14)
- **Files touched:** prompts/dario-v.md
- **Result:** pending
- **How I checked it:** pending

### Prompt

```text
Sí, pantalla Room
```

### What happened

pending

## DV-16

- **Date and time:** 2026-11-23 20:47
- **Author:** Darío Valdemar
- **Tool:** Claude Code (desktop app; same session as DV-15)
- **Files touched:** prompts/dario-v.md
- **Result:** pending
- **How I checked it:** pending

### Prompt

```text
Probado, commitea
```

### What happened

pending
