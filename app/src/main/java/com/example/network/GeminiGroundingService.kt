package com.example.network

import android.util.Log
import com.example.BuildConfig
import com.example.data.entity.BuildingEntity
import com.example.data.entity.BuildingTypeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class GeminiGroundingService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val TAG = "GeminiGroundingService"
        private const val PRIMARY_MODEL = "gemini-3.8-flash"
        private const val FALLBACK_MODEL = "gemini-2.5-flash"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    }

    suspend fun syncBuildingMaxLevels(
        townHallLevel: Int,
        village: String,
        buildings: List<BuildingEntity>,
        buildingTypes: List<BuildingTypeEntity>
    ): GroundingSyncResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            field.get(null) as? String ?: ""
        } catch (_: Exception) {
            ""
        }
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext GroundingSyncResult.Error(
                "Gemini API key is not configured. Google Search Grounding requires an API key in AI Studio Secrets."
            )
        }

        if (buildings.isEmpty()) {
            return@withContext GroundingSyncResult.Error("No buildings found to synchronize.")
        }

        val typeMap = buildingTypes.associateBy { it.id }
        val buildingListPrompt = buildings.joinToString("\n") { b ->
            val typeName = typeMap[b.typeId]?.name ?: "Building"
            "- ${b.name} (Category: $typeName, Current Max in App: ${b.maxLevel})"
        }

        val villageName = if (village == "HOME") "Home Village Town Hall" else "Builder Base Builder Hall"
        val prompt = """
            You are an authoritative Clash of Clans database synchronizer using Google Search Grounding.
            Search Google for the latest official Clash of Clans maximum building upgrade levels for $villageName level $townHallLevel.
            
            We are tracking these buildings in the app:
            $buildingListPrompt

            TASK:
            1. Search the web for current Clash of Clans Wiki (clashofclans.fandom.com) and official Supercell upgrade tables for $villageName $townHallLevel.
            2. For each tracked building, find its exact maximum upgrade level at $villageName $townHallLevel.
            3. Return a clean JSON array of objects with the exact schema:
            [
              {"buildingName": "Exact Building Name", "maxLevel": 16}
            ]
            
            Rules:
            - Do not guess or extrapolate levels. Only output levels verified by Google Search data.
            - Keep the building names identical to the tracked names above so they can be matched.
            - Output ONLY the JSON array enclosed between ```json and ``` or as raw JSON.
        """.trimIndent()

        // Attempt primary model first, fallback if model unavailable
        val result = executeGenerateContent(PRIMARY_MODEL, apiKey, prompt)
        val finalResponseText = if (result.isSuccess) {
            result.getOrNull()
        } else {
            Log.w(TAG, "Primary model $PRIMARY_MODEL failed: ${result.exceptionOrNull()?.message}. Trying fallback $FALLBACK_MODEL")
            val fallbackResult = executeGenerateContent(FALLBACK_MODEL, apiKey, prompt)
            if (fallbackResult.isSuccess) fallbackResult.getOrNull() else null
        }

        if (finalResponseText == null) {
            val errorMsg = result.exceptionOrNull()?.message ?: "Failed to connect to Google Search Grounding service."
            return@withContext GroundingSyncResult.Error(errorMsg)
        }

        try {
            val (jsonArrayString, sources, searchQueries, summary) = parseGeminiResponse(finalResponseText)
            val parsedLevels = parseBuildingLevelsJson(jsonArrayString)

            if (parsedLevels.isEmpty()) {
                return@withContext GroundingSyncResult.Error(
                    "Google Search Grounding returned no matching building level entries for $villageName $townHallLevel."
                )
            }

            // Calculate Diff
            val diffs = mutableListOf<BuildingDiff>()
            for (building in buildings) {
                val matchedLevel = findMatchedLevel(building.name, parsedLevels)
                val typeName = typeMap[building.typeId]?.name ?: "Building"
                val newMax = matchedLevel ?: building.maxLevel

                diffs.add(
                    BuildingDiff(
                        buildingId = building.id,
                        buildingName = building.name,
                        categoryName = typeName,
                        currentLevel = building.currentLevel,
                        oldMaxLevel = building.maxLevel,
                        newMaxLevel = newMax
                    )
                )
            }

            GroundingSyncResult.Success(
                townHallLevel = townHallLevel,
                village = village,
                diffs = diffs,
                sources = sources,
                searchQueries = searchQueries,
                rawSummary = summary
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Search Grounding response", e)
            GroundingSyncResult.Error("Failed to parse Search Grounding response: ${e.localizedMessage}")
        }
    }

    private fun executeGenerateContent(model: String, apiKey: String, prompt: String): Result<String> {
        return try {
            val url = "$BASE_URL/$model:generateContent?key=$apiKey"

            // Construct payload with googleSearch tool
            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                }
                put("contents", contents)

                val tools = JSONArray().apply {
                    put(JSONObject().apply {
                        put("googleSearch", JSONObject())
                    })
                }
                put("tools", tools)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val errorDetail = try {
                        val json = JSONObject(responseBody)
                        json.optJSONObject("error")?.optString("message") ?: response.message
                    } catch (_: Exception) {
                        response.message
                    }
                    Result.failure(Exception("API Error (${response.code}): $errorDetail"))
                } else {
                    Result.success(responseBody)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private data class ParsedResponse(
        val jsonArrayString: String,
        val sources: List<GroundingSource>,
        val searchQueries: List<String>,
        val summary: String
    )

    private fun parseGeminiResponse(rawResponseBody: String): ParsedResponse {
        val root = JSONObject(rawResponseBody)
        val candidates = root.optJSONArray("candidates")
        if (candidates == null || candidates.length() == 0) {
            throw IllegalStateException("No response candidate returned by Gemini.")
        }

        val candidate = candidates.getJSONObject(0)
        val content = candidate.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val rawText = if (parts != null && parts.length() > 0) {
            parts.getJSONObject(0).optString("text", "")
        } else ""

        // Extract Grounding Metadata
        val sources = mutableListOf<GroundingSource>()
        val searchQueries = mutableListOf<String>()

        val groundingMetadata = candidate.optJSONObject("groundingMetadata")
        if (groundingMetadata != null) {
            // Search queries
            val queries = groundingMetadata.optJSONArray("webSearchQueries")
            if (queries != null) {
                for (i in 0 until queries.length()) {
                    searchQueries.add(queries.getString(i))
                }
            }

            // Web chunks
            val chunks = groundingMetadata.optJSONArray("groundingChunks")
            if (chunks != null) {
                for (i in 0 until chunks.length()) {
                    val chunkObj = chunks.getJSONObject(i)
                    val web = chunkObj.optJSONObject("web")
                    if (web != null) {
                        val uri = web.optString("uri", "")
                        val title = web.optString("title", uri)
                        if (uri.isNotBlank()) {
                            sources.add(GroundingSource(title = title, url = uri))
                        }
                    }
                }
            }
        }

        // Clean duplicates in sources
        val distinctSources = sources.distinctBy { it.url }

        // Extract JSON block from text
        val jsonString = extractJsonFromText(rawText)

        return ParsedResponse(
            jsonArrayString = jsonString,
            sources = distinctSources,
            searchQueries = searchQueries,
            summary = rawText
        )
    }

    private fun extractJsonFromText(text: String): String {
        // Find ```json ... ``` or [ ... ]
        val matcher = Pattern.compile("```(?:json)?\\s*([\\[{].*?[\\]}])\\s*```", Pattern.DOTALL).matcher(text)
        if (matcher.find()) {
            return matcher.group(1) ?: text
        }
        val firstBracket = text.indexOf('[')
        val lastBracket = text.lastIndexOf(']')
        if (firstBracket != -1 && lastBracket != -1 && lastBracket > firstBracket) {
            return text.substring(firstBracket, lastBracket + 1)
        }
        return text
    }

    private fun parseBuildingLevelsJson(jsonArrayStr: String): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        try {
            val array = JSONArray(jsonArrayStr)
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val name = item.optString("buildingName").trim()
                val level = item.optInt("maxLevel", -1)
                if (name.isNotBlank() && level > 0) {
                    map[name.lowercase()] = level
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse JSON array: $jsonArrayStr", e)
        }
        return map
    }

    private fun findMatchedLevel(buildingName: String, parsedLevels: Map<String, Int>): Int? {
        val normalized = buildingName.trim().lowercase()
        // Exact match
        parsedLevels[normalized]?.let { return it }

        // Partial match
        for ((key, value) in parsedLevels) {
            if (normalized.contains(key) || key.contains(normalized)) {
                return value
            }
        }
        return null
    }
}
