package com.x.myapplication.data.repository

import com.x.myapplication.BuildConfig
import com.x.myapplication.data.model.RecommendationCard
import com.x.myapplication.data.remote.RecommendationApiClient

class RecommendationRepository(
    private val apiClient: RecommendationApiClient = RecommendationApiClient(
        baseUrl = BuildConfig.EXTERNAL_SERVER_BASE_URL,
    ),
) {
    suspend fun getRecommendations(): List<RecommendationCard> = apiClient.fetchRecommendations()
}
