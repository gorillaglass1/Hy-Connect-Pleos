package com.x.myapplication.data.state

import com.x.myapplication.data.model.RecommendationCard

sealed interface RecommendationUiState {
    data object Loading: RecommendationUiState
    data class Success(val cards: List<RecommendationCard>): RecommendationUiState
    data class Error(val message: String): RecommendationUiState
}