# Fianzapp

Group project, app 5. An app for tenants that records the state of the flat with dated photos, room by room, when you move in and when you move out. The tenant and the landlord sign a report on the phone, and at the end the app puts the before and the after side by side and reminds you of the deadline to get the deposit back. It is not legal advice.

This README is yours. Fill it in as you go: it is part of your submission, and the rubric marks it. If this README and the rubric ever disagree, the rubric wins. How to open the project, what the skeleton already does and a route to build the app are in the skeleton's guide, `MAI_SAP_Skeleton_App5_Fianzapp.docx`.

## How to run it

Besides the steps in the skeleton's guide:

1. Open the project in Android Studio and run the `app` configuration. A7 (notification permission) and A8 (camera) need an emulator or phone with **Android 13 or later**; we test on a Pixel 10a emulator with API 37.
2. `app/google-services.json` is in the repository and points to the Firebase project `fianzapp-2df15` (free Spark plan). Nothing else is needed to build.
3. Start on Home: **Lease** first (address, landlord, move-in date, deposit). Every other screen reads the lease.
4. **Rooms → Add room**, open the room and tap the **Move-in** or **Move-out** slot of an item to take its photo. The **Report** tab of a phase can be signed once that phase has at least one photo.
5. **Location (A9):** in the emulator, open *Extended controls (⋯) → Location*, pick a point and press *Set location* before taking photos. Approximate location is enough; without it the photo is still saved, with no position.
6. **Firebase accounts (A6):** the project has two invented test accounts, one for the tenant and one for the landlord, created in the console under *Authentication → Users*. Ask the group for their passwords; they are never written in the repository. To recreate them in another project:
   - In *Authentication → Sign-in method*, enable Email/Password, then add the two users and copy each **User UID**.
   - In *Firestore Database → Data*, create collection `leases`, document `fianzapp-lease`, with two string fields `tenantUid` and `landlordUid` holding those UIDs.
   - In *Firestore Database → Rules*, publish the content of `firestore.rules`.
7. To try A6: **Report** → sign the report → sign in as the tenant → **Send**. Then **Sign out**, open **Landlord** and sign in as the landlord: the report appears, live.
8. **Reminder (A7):** in **Settings**, set the days of warning, turn the reminder on (the app asks for permission to notify) and use **Check now** to run the worker once. WorkManager does not run at an exact time, so the daily check can arrive later in the day.

## Real API or recorded response (A5)

The switch is the constant `USE_RECORDED_CHECKLIST` in `app/src/main/java/com/example/fianzapp/data/ChecklistRepository.kt`:

- `true`: the app reads the recorded response, `app/src/main/assets/recorded/checklist.json`, which works with no network.
- `false`: Retrofit calls `https://cunef-mia-sap-proyecto.vercel.app/data/app5/checklist.json` through the shared `httpClient`.

Both sources are parsed with the same `lenientJson`. **As submitted, the app uses the recorded response (`true`).**

## Coverage table

One row for each row of block A. A row that is not in this table scores 0.

| Row | Feature | Files | Screen |
|---|---|---|---|
| A1 | Lease form with state hoisting and `rememberSaveable`; checks dates and a deposit with a decimal comma or point | `ui/lease/LeaseScreen.kt`, `ui/lease/LeaseViewModel.kt`, `domain/LeaseValidation.kt` | Lease |
| A2 | List of rooms, one room with its before and after photos (Coil), type-safe routes carrying the room id, share a photo through the FileProvider | `ui/rooms/RoomsScreen.kt`, `ui/rooms/RoomsViewModel.kt`, `ui/room/RoomScreen.kt`, `ui/room/RoomViewModel.kt`, `ui/room/SharePhoto.kt`, `ui/FianzappNavHost.kt` | Rooms, Room |
| A3 | Days of warning before the deadline and the reminder switch, kept in one Preferences DataStore | `data/Settings.kt`, `domain/Reminders.kt`, `ui/settings/SettingsScreen.kt`, `ui/settings/SettingsViewModel.kt` | Settings (also shown on Deposit deadline) |
| A4 | Room database: lease, rooms, photos (with time, position and labels) and signed reports | `data/db/Entities.kt`, `data/db/Daos.kt`, `data/db/FianzappDatabase.kt` | Every screen reads it |
| A5 | Checklist of what to photograph in each room type, from Retrofit or the recorded file, with loading, error and empty states | `data/Checklist.kt`, `data/ChecklistRepository.kt`, `HttpClient.kt`, `ui/checklist/ChecklistScreen.kt`, `ui/checklist/ChecklistViewModel.kt` | Checklist, Add room, Room |
| A6 | Tenant and landlord sign in with Firebase Auth; the signed report (no photos, signatures as text) goes to Firestore, and the landlord reads it live | `ui/account/AccountViewModel.kt`, `ui/account/AccountViews.kt`, `data/remote/Firestore.kt`, `ui/report/ReportScreen.kt`, `ui/report/ReportViewModel.kt`, `ui/landlord/LandlordScreen.kt`, `firestore.rules` | Report, Landlord |
| A7 | Daily worker that notifies the warning and the deadline (`depositDeadline` = keys returned + 1 month), each only once | `reminder/DepositReminderWorker.kt`, `domain/DepositDeadline.kt`, `domain/DeadlineStatus.kt`, `domain/Reminders.kt`, `ui/deadline/DeadlineScreen.kt`, `ui/deadline/DeadlineViewModel.kt` | Notification, Settings, Deposit deadline |
| A8 | Camera permission with its three paths and CameraX; one photo per item and phase, plus damage photos with a note, saved under `filesDir/photos/<roomId>/` | `ui/camera/CameraScreen.kt`, `ui/camera/CameraViewModel.kt` | Camera (from Room) |
| A9 | Approximate location of each photo, distance from the flat and a warning when it is far | `ui/camera/PhotoSensors.kt`, `ui/camera/CameraViewModel.kt`, `domain/PhotoChecks.kt`, `ui/room/RoomViewModel.kt` | Camera, Room |
| A10 | ML Kit image labeling (bundled) on each photo; compares the move-in and move-out pair | `ui/camera/PhotoSensors.kt`, `domain/PhotoChecks.kt`, `ui/room/RoomScreen.kt` | Room |

All paths are under `app/src/main/java/com/example/fianzapp/` except `firestore.rules`.

## Design decisions

1. **The comparison rule (A10).** A pair shows "the same" when both photos share a label with confidence of 0.7 or more, ignoring `Room`, `Wall` and `Monochrome`. If either photo has no such label, the answer is "cannot tell", never "different". Generic labels appear in almost any photo of a flat, so counting them would call every pair "the same". The labeler cannot see damage, so the app never claims something is damaged: it only checks that people compare like with like, and people judge the damage.
2. **A signed report cannot change.** Once both people sign a phase, the report is read only and `sign` never overwrites it. A report that could be edited after signing would prove nothing to the landlord or in a dispute over the deposit. Photos stay in Room and on the phone; only the report goes to Firestore, because Firebase Storage needs the paid Blaze plan.
3. **The reminder never repeats (A7).** The worker runs once a day, but WorkManager does not promise the time, and it can run twice. DataStore keeps the deadline for which the warning, and the "deadline reached" notice, were already sent. Each is therefore sent once per deadline, and if the device missed the exact reminder day the next run still sends the warning. "Far from the flat" (A9) is 5 km on purpose, because approximate location can be about 1 km off.

## Release (C2)

The `.aab` is signed with our own release key (certificate valid until 2051), created in Android Studio with *Build > Generate Signed App Bundle or APK > Android App Bundle > Create new*. **Darío** keeps the `.jks` file on his computer, in a folder outside the repository. Only he knows its passwords; they are never written in any file or prompt. Neither the `.jks` nor the `.aab` is committed.

Google Play does not accept the debug key because Android Studio creates it automatically on every computer, with a known password and the same "Android Debug" certificate for everyone. It proves nothing about who published the app, and an app signed with it could not be updated later with the same identity. Play requires a private release key that only the developer holds.

Sign it in Android Studio with **Build > Generate Signed Bundle/APK**, then **Android App Bundle**. The `.aab` that Gradle builds on its own in `app/build/outputs` is not signed, and an unsigned `.aab` scores 0 in C2.

## Facts you cannot guess

Keep this section as it is. It is section 10 of the skeleton's guide, copied here so that your AI assistant can read it while it works on your project.

### A5 · The checklist file

No key and no sign-up. The recorded response, `app/src/main/assets/recorded/checklist.json`, answers this exact call. Retrofit splits the address in two: the base address ends with a slash, and the path is the rest.

```text
https://cunef-mia-sap-proyecto.vercel.app/data/app5/checklist.json
```

The data is invented: a suggested checklist, not a legal list. The file has:

| Field | What it holds |
|---|---|
| `note` | A sentence about the data. You do not need it. |
| `roomTypes` | Seven room types. Each has `id`, a short code such as `kitchen` that never repeats; `name`, to show; and `items`, the list of things to photograph. |
| `roomTypes[].items` | Each item has `id`, a number that never repeats anywhere in the file, not even across room types; `name`, such as `Window`; and `hint`, where to look. |

The file has awkward cases on purpose:

- One item has no hint: `"hint": null` (id 30, the floor of the balcony).
- One room type, `storage`, has no items: `"items": []`.

### Fields you do not read

The file brings a field that you will not read, `note`. With the plain `Json`, the app closes on the first field its classes do not have. Section 3.7 of the Session 8 Guide fixes it with `val lenientJson = Json { ignoreUnknownKeys = true }`. Use that one object in both places where JSON is read:

- `lenientJson.asConverterFactory(...)` in your `Retrofit.Builder()`;
- `lenientJson.decodeFromString(...)` where you read the recorded file.

### A2 and A8 · Saving and sharing a photo

A photo saved in the app's internal files has no address that another app can open, so sharing its path does not work. Save each photo under `filesDir/photos/`, for example in `photos/<room id>/`. It is the only folder the skeleton's FileProvider exposes.

To share a photo:

- Build its `content://` address with `FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)`.
- Put it in a sharing `Intent`.
- Add `Intent.FLAG_GRANT_READ_URI_PERMISSION`, so that the other app may read it.

The authority is `${applicationId}.fileprovider`, as declared in `AndroidManifest.xml`. FileProvider is not in any Guide: ask your assistant, and check what it writes against Android's reference, `https://developer.android.com/reference/androidx/core/content/FileProvider`.

### A6 · What goes to Firestore

The report, without its photos. It holds:

- the lease;
- the rooms;
- each item, with when and where its photos were taken and the answer of the comparison;
- the two signatures, as the text that `encodeSignature` gives.

A signature of a few lines is a few kilobytes of text, and a Firestore document holds up to 1 MiB (Firestore's page on usage and limits). The photo files stay on the phone: Firebase Storage needs the paid Blaze plan, and the project must cost nothing.

### The signatures

`SignaturePad.kt` gives the pad and the two functions that turn a signature into text and back. The points are pixels of the pad where the person signed. So on a screen of another size or density, a signature can look smaller, or its end can fall outside the pad. In the emulator, sign by dragging the mouse.

### A7 · The deposit deadline

Article 36.4 of Spain's Urban Leases Act (Ley 29/1994, de Arrendamientos Urbanos) says that the part of the deposit that must be returned to the tenant earns legal interest once a month has passed since the tenant handed back the keys without it being returned. Article 5 of the Spanish Civil Code says how that month is counted: from date to date. When the month of the end has no such day, the period ends on the last day of that month. Non-working days are not taken out. Both texts are in the official gazette, the BOE:

```text
https://www.boe.es/buscar/act.php?id=BOE-A-1994-26003
https://www.boe.es/buscar/act.php?id=BOE-A-1889-4763
```

In Kotlin, `LocalDate.plusMonths(1)` counts the same way: when the day does not exist in the new month, it picks the last valid day of that month (Java's reference for `LocalDate`, which gives 2007-03-31 plus one month as 2007-04-30). The app shows the date and says that it is not legal advice.

In the text we read in the BOE on 25 October 2026, article 36 also has a paragraph 7, added in October 2026. It is about a document on the state of the flat that both parties sign when the lease ends. We mention it because your move-out report looks like that document. This guide does not ask your app to follow that paragraph, and nothing in the app depends on it.

WorkManager's `PeriodicWorkRequest` runs at most once every 15 minutes (Session 10 Guide, section 3.4), and Android picks the moment of each run (Session 10 Guide, section 1.6). So the reminder may arrive later in the day than you expect.

### A9 · Location

The client is `FusedLocationProviderClient`, from the `play-services-location` library that the skeleton already has. The location is a dangerous permission with two versions: `ACCESS_COARSE_LOCATION` (approximate) and `ACCESS_FINE_LOCATION` (precise).

Session 11 Guide, section 9.2, explains why your app must work with only the approximate one. It is accurate to about 3 square kilometres, so a position can be about a kilometre away from the real one. A rule about distances needs a margin well above that.

The skeleton does not declare the two permissions: adding them to `AndroidManifest.xml` is part of A9. The position can be `null` (Session 11 Guide, section 9.3), so the camera screen needs a plan for it, and also for a denied permission and for the location turned off.

Android already has the distance formula: `Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results)` stores the distance in metres in `results[0]`, where `results` is a `FloatArray(1)`.

### A8 and A10 · Image labeling

Session 12 read text from a saved photo, and image labeling works the same way: take a photo with `ImageCapture` (Session 11) and label the saved photo. The skeleton has the bundled library, `com.google.mlkit:image-labeling:17.0.9`, which carries its model inside the app. The labeler gives you a list of labels, each with its text, an index and a confidence between 0 and 1. ML Kit's guide shows how to set the minimum confidence you accept with `ImageLabelerOptions.Builder().setConfidenceThreshold(0.7f)`.

The default model knows more than 400 labels, and they are fixed (ML Kit's label map). Room, Kitchen, Bathroom, Bedroom, Wall, Tile, Sink, Couch, Countertop, Cabinetry, Curtain and Shelf are in the map. Window, Floor, Door, Toilet, Mirror, Bed, Oven and Refrigerator are not, so do not wait for them: a photo of a window can come back as Room or Wall, or with no label at all. No label describes damage.

### In the emulator

- **Camera.** Edit the virtual device in Device Manager and set the back camera to Webcam0, your laptop's webcam. It must be free, so close Teams or Zoom. If the preview stays black, restart the emulator and keep its window open: a minimized emulator can give a black camera until it restarts.
- **A10 with the webcam.** The webcam sees what is in front of your laptop. To try A10, show it a sink, a tiled wall or a sofa, or a picture of one. If that does not work, test on an Android phone.
- **No webcam.** The emulator's own back camera shows a virtual room, and its labels are in the map (Room, Shelf, Tile, Wall). Two photos of the same view are enough to check "the same thing".
- **Location.** Open the emulator's extended controls, Location, and set any position, for example latitude 40.4168 and longitude -3.7038, in Madrid. To be about 11 km away, change the latitude to 40.5168: a tenth of a degree of latitude is about 11 km.
- Test A7 and A8 on an emulator with Android 13 or later.

### Call the real file from home

The class shares one internet address. In class, use the recorded response.
