package com.x.myapplication.data.remote

import com.x.myapplication.data.model.RecommendationCard
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class RecommendationApiClient(
    private val baseUrl: String,
    private val timeoutMillis: Int = 10_000,
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun fetchRecommendations(): List<RecommendationCard> = withContext(Dispatchers.IO) {
        withTimeout(timeoutMillis.toLong() + 2_000L) {
            val primaryPath = "/recommendations"
            val primaryResult = requestRecommendations(primaryPath)
            if (primaryResult.statusCode != HttpURLConnection.HTTP_NOT_FOUND) {
                return@withTimeout primaryResult.body.toRecommendationCards()
            }

            val discoveredPath = discoverRecommendationPath()
                ?: throw IOException("Server returned HTTP 404 for $primaryPath and no recommendation endpoint was found in /openapi.json")

            val discoveredResult = requestRecommendations(discoveredPath)
            if (discoveredResult.statusCode !in 200..299) {
                throw IOException(
                    "Server returned HTTP ${discoveredResult.statusCode} for $discoveredPath: ${discoveredResult.body}"
                )
            }
            discoveredResult.body.toRecommendationCards()
        }
    }

    private fun requestRecommendations(path: String): HttpResult {
        val result = requestRaw(path)
        if (result.statusCode !in 200..299 && result.statusCode != HttpURLConnection.HTTP_NOT_FOUND) {
            throw IOException("Server returned HTTP ${result.statusCode} for $path: ${result.body}")
        }
        return result
    }

    private fun discoverRecommendationPath(): String? {
        val result = requestRaw("/openapi.json")
        if (result.statusCode !in 200..299) return null

        val paths = runCatching {
            json.parseToJsonElement(result.body)
                .jsonObject["paths"]
                ?.jsonObject
                .orEmpty()
        }.getOrDefault(emptyMap())

        val keywords = listOf("recommend", "recommendation", "station", "charging", "charger", "hydrogen")
        return paths.entries
            .asSequence()
            .filter { (_, methods) -> methods.hasGetMethod() }
            .map { (path, methods) -> path to methods.searchText().lowercase() }
            .filter { (path, searchText) ->
                val pathText = path.lowercase()
                keywords.any { keyword -> keyword in pathText || keyword in searchText }
            }
            .map { (path, _) -> path }
            .firstOrNull()
    }

    private fun requestRaw(path: String): HttpResult {
        val connection = openConnection(path)
        return try {
            val statusCode = connection.responseCode
            val responseText = if (statusCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }
            HttpResult(statusCode, responseText)
        } finally {
            connection.disconnect()
        }
    }

    private fun openConnection(path: String): HttpURLConnection {
        val url = URL(baseUrl.trimEnd('/') + path)
        return (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = timeoutMillis
            readTimeout = timeoutMillis
            setRequestProperty("Accept", "application/json")
        }
    }

    private fun String.toRecommendationCards(): List<RecommendationCard> {
        val element = json.parseToJsonElement(this)
        return when (element) {
            is JsonObject -> {
                val wrapped = element["recommendations"]
                    ?: element["stations"]
                    ?: element["charging_stations"]
                    ?: element["chargingStations"]
                    ?: element["data"]
                    ?: element["items"]
                    ?: element.values.firstOrNull { it is JsonArray }
                if (wrapped != null) {
                    wrapped.toRecommendationCards()
                } else {
                    listOf(element.toRecommendationCard())
                }
            }
            is JsonArray -> element.mapNotNull { item ->
                (item as? JsonObject)?.toRecommendationCard()
            }
            else -> emptyList()
        }
    }
}

private data class HttpResult(
    val statusCode: Int,
    val body: String,
)

private fun JsonElement.hasGetMethod(): Boolean {
    return (this as? JsonObject)?.containsKey("get") == true
}

private fun JsonElement.searchText(): String {
    return when (this) {
        is JsonObject -> values.joinToString(" ") { it.searchText() }
        is JsonPrimitive -> contentOrNull.orEmpty()
        else -> runCatching { jsonArray.joinToString(" ") { it.searchText() } }.getOrDefault("")
    }
}

private fun JsonElement.toRecommendationCards(): List<RecommendationCard> {
    return when (this) {
        is JsonArray -> mapNotNull { (it as? JsonObject)?.toRecommendationCard() }
        is JsonObject -> listOf(toRecommendationCard())
        else -> emptyList()
    }
}

private fun JsonObject.toRecommendationCard(): RecommendationCard {
    val stationName = stringValue(
        "station_name",
        "stationName",
        "name",
        "station",
        "title",
        "place_name",
        "placeName",
        "charging_station_name",
    ) ?: "이름 없는 충전소"

    val distanceKm = doubleValue(
        "distance_km",
        "distanceKm",
        "distance",
        "dist",
        "km",
    ) ?: 0.0

    val reason = stringValue(
        "reason",
        "recommendation_reason",
        "recommendationReason",
        "description",
        "message",
        "summary",
    ) ?: "서버 응답에 추천 사유가 없습니다."

    val caution = stringValue(
        "caution",
        "warning",
        "notice",
        "note",
    )

    val latitude = doubleValue("latitude", "lat", "y")
    val longitude = doubleValue("longitude", "lng", "lon", "x")
    val poiId = stringValue("poi_id", "poiId", "id", "place_id", "placeId")
    val poiSubId = stringValue("poi_sub_id", "poiSubId", "sub_id", "subId") ?: "0"
    val address = stringValue("address", "addr", "road_address", "roadAddress")

    val isReachable = booleanValue(
        "is_reachable",
        "isReachable",
        "reachable",
        "available",
    ) ?: true

    return RecommendationCard(
        stationName = stationName,
        distanceKm = distanceKm,
        isReachable = isReachable,
        reason = reason,
        caution = caution,
        latitude = latitude,
        longitude = longitude,
        poiId = poiId,
        poiSubId = poiSubId,
        address = address,
    )
}

private fun JsonObject.stringValue(vararg keys: String): String? {
    return keys.firstNotNullOfOrNull { key ->
        (this[key] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }
    }
}

private fun JsonObject.doubleValue(vararg keys: String): Double? {
    return keys.firstNotNullOfOrNull { key ->
        val primitive = this[key] as? JsonPrimitive
        primitive?.doubleOrNull ?: primitive?.contentOrNull?.toDoubleOrNull()
    }
}

private fun JsonObject.booleanValue(vararg keys: String): Boolean? {
    return keys.firstNotNullOfOrNull { key ->
        val primitive = this[key] as? JsonPrimitive
        primitive?.booleanOrNull ?: primitive?.contentOrNull?.toBooleanStrictOrNull()
    }
}
