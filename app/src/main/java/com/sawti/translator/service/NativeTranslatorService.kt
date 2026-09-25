package com.sawti.translator.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class NativeTranslatorService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val localDictionary = mapOf(
        "مرحبا" to "Hello",
        "أهلا" to "Welcome",
        "السلام عليكم" to "Peace be upon you",
        "شكرا" to "Thank you",
        "نعم" to "Yes",
        "لا" to "No",
        "صباح الخير" to "Good morning",
        "مساء الخير" to "Good evening",
        "كيف حالك" to "How are you?",
        "أنا بخير" to "I am fine",
        "hello" to "مرحباً",
        "thank you" to "شكراً لك",
        "yes" to "نعم",
        "no" to "لا",
        "good morning" to "صباح الخير",
        "how are you" to "كيف حالك؟"
    )

    private val memoryCache = mutableMapOf<String, String>()

    suspend fun translate(
        phrase: String,
        sourceLang: String = "ar",
        targetLang: String = "en"
    ): String = withContext(Dispatchers.IO) {
        val clean = phrase.trim()
        if (clean.isEmpty()) return@withContext ""

        val cacheKey = "${clean.lowercase()}_${sourceLang}_${targetLang}"
        if (memoryCache.containsKey(cacheKey)) {
            return@withContext memoryCache[cacheKey]!!
        }

        // 1. Instant local dictionary lookup
        val dictMatch = localDictionary[clean.lowercase()]
        if (dictMatch != null) {
            memoryCache[cacheKey] = dictMatch
            return@withContext dictMatch
        }

        // 2. High-speed Cloud API Translation
        try {
            val endpoint = "https://ais-pre-kxhhdqxs3p4htdzfjmdvgz-657272522163.europe-west1.run.app/api/live-translate"
            val jsonPayload = JSONObject().apply {
                put("phrase", clean)
                put("sourceLang", sourceLang)
                put("targetLang", targetLang)
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: ""
                val resObj = JSONObject(bodyStr)
                val translated = resObj.optString("translated", "")
                if (translated.isNotEmpty()) {
                    memoryCache[cacheKey] = translated
                    return@withContext translated
                }
            }
        } catch (e: Exception) {
            // Fallback to secondary translation API
        }

        // 3. Bulletproof Fallback via Global MyMemory Translation
        try {
            val encodedQuery = URLEncoder.encode(clean, "UTF-8")
            val fallbackUrl = "https://api.mymemory.translated.net/get?q=$encodedQuery&langpair=$sourceLang|$targetLang"
            val fallbackRequest = Request.Builder().url(fallbackUrl).build()
            val fallbackRes = client.newCall(fallbackRequest).execute()
            if (fallbackRes.isSuccessful) {
                val resStr = fallbackRes.body?.string() ?: ""
                val json = JSONObject(resStr)
                val resData = json.optJSONObject("responseData")
                val fallbackTranslated = resData?.optString("translatedText", "") ?: ""
                if (fallbackTranslated.isNotEmpty() && !fallbackTranslated.startsWith("MYMEMORY WARNING")) {
                    memoryCache[cacheKey] = fallbackTranslated
                    return@withContext fallbackTranslated
                }
            }
        } catch (e: Exception) {}

        return@withContext clean
    }
}
