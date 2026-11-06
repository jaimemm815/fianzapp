package com.example.fianzapp.data

import android.content.Context
import com.example.fianzapp.httpClient
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET

/**
 * A5: switch between the real call and the recorded response.
 *
 * - `true`: the app reads `assets/recorded/checklist.json` (use it in class: the class shares
 *   one internet address).
 * - `false`: the app calls the real file on the internet.
 */
const val USE_RECORDED_CHECKLIST = true

private const val BASE_URL = "https://cunef-mia-sap-proyecto.vercel.app/"
private const val RECORDED_FILE = "recorded/checklist.json"

interface ChecklistApi {
    @GET("data/app5/checklist.json")
    suspend fun getChecklist(): Checklist
}

private val checklistApi: ChecklistApi by lazy {
    Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(lenientJson.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(ChecklistApi::class.java)
}

class ChecklistRepository(private val context: Context) {

    suspend fun loadChecklist(): Checklist =
        if (USE_RECORDED_CHECKLIST) loadRecorded() else checklistApi.getChecklist()

    private suspend fun loadRecorded(): Checklist = withContext(Dispatchers.IO) {
        val text = context.assets.open(RECORDED_FILE).bufferedReader().use { it.readText() }
        parseChecklist(text)
    }
}

/** Kept apart so that a unit test can parse the recorded file without Android. */
fun parseChecklist(text: String): Checklist = lenientJson.decodeFromString(text)
