# CLAUDE.md — Fianzapp (MAI SAP, CUNEF, app 5)

Android app in Kotlin + Jetpack Compose. Reply to Darío in Spanish; code, names and comments in English.
The build guide (rows A1–A10) lives outside the repo; README.md "Facts you cannot guess" mirrors its section 10.

## Rules (always)
- NEVER change versions in gradle/libs.versions.toml, never add the org.jetbrains.kotlin.android plugin.
- NEVER write passwords, keys or personal data in any file, nor in the User-Agent of HttpClient.kt.
- In prompts/ only touch prompts/dario-v.md. Never edit another member's file or prompts/README.md.
  Never change the text of a prompt already logged.
- NEVER push, pull, merge or switch branch. Commit only when Darío asks.
- Do not touch local.properties, .aiexclude, .gitignore or firestore.rules unless asked.
- Only the change asked for: no refactors, extra features or new libraries. A new dependency → stop and ask.
- State only what was checked by reading files or running commands. The emulator is not visible to you: say so.
- README.md section "Facts you cannot guess" stays exactly as it is.

## Key facts (guide section 10)
- Checklist: base URL `https://cunef-mia-sap-proyecto.vercel.app/`, path `data/app5/checklist.json`.
  Recorded copy: `app/src/main/assets/recorded/checklist.json`. Use the recorded one in class.
- Fields: `note` (not read), `roomTypes[]` {`id` code, `name`, `items[]`}, items {`id` Int unique in the
  whole file, `name`, `hint` nullable}. Awkward: item 30 `hint: null`; `storage` has `items: []`. Never use `!!`.
- ONE `lenientJson = Json { ignoreUnknownKeys = true }` (data/Checklist.kt), used by Retrofit and by the
  recorded reader. Every `Retrofit.Builder()` uses `.client(httpClient)`.
- Photos: save under `filesDir/photos/` (e.g. `photos/<roomId>/`), only folder exposed by the FileProvider.
  Share: `FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)` +
  `Intent.FLAG_GRANT_READ_URI_PERMISSION`, via a chooser. Room stores the path, never the image.
- Deadline: `depositDeadline(keysReturned) = plusMonths(1)` (art. 36.4 LAU + art. 5 CC); one function,
  no Android code; screens and the A7 worker call it. Show "not legal advice".
- Firestore gets the report without photos (signatures as `encodeSignature` text).
  Firebase on the free Spark plan only (never Blaze, no Storage). Firestore in production mode.
- A9: coarse location must be enough; position can be null; photo is saved anyway.
- A10: bundled ML Kit image labeling only. No Text Recognition anywhere. No labels for Window/Floor/Door/etc.
- A7, A8: test on Android 13+ (emulator: Pixel 10a, API 37). Darío runs the app from Android Studio.

## Package structure (com.example.fianzapp)
- `MainActivity.kt` (Scaffold + nav host; `SkeletonScreen` signature demo), `HttpClient.kt`, `SignaturePad.kt` (skeleton)
- `data/` Checklist.kt (models + lenientJson), ChecklistRepository.kt (switch `USE_RECORDED_CHECKLIST`)
- `domain/` DepositDeadline.kt
- `ui/` FianzappNavHost.kt (type-safe routes, temporary Home), `checklist/`, `deadline/`, `theme/`
- Tests: `app/src/test/.../ChecklistParsingTest.kt`, `DepositDeadlineTest.kt`
- Build from a terminal: set `JAVA_HOME=C:\Program Files\Android\Android Studio\jbr` for that process only.

## Work cycle (each new prompt from Darío)
1. First append to prompts/dario-v.md the next entry (DV-02, DV-03…): time from
   `Get-Date -Format "yyyy-MM-dd HH:mm"`, the prompt copied literally, Result / How I checked it /
   What happened = "pending". If CLAUDE.md was committed since the last entry, name that commit in Tool.
2. Before editing code, say in 3–5 lines which files and what change.
3. Do only that small task (one row or part of one, including its awkward case).
4. Run `.\gradlew.bat testDebugUnitTest` and `.\gradlew.bat assembleDebug`; report the real result.
5. Say exactly what to tap on the emulator, following the guide's "Check it yourself".
6. Give a 2–3 line summary for "What happened" and propose a commit message. Commit code only when asked;
   the logbook entry goes in an earlier commit or the same one, never later.
