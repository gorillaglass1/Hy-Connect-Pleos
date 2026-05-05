package com.x.myapplication.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.x.myapplication.data.repository.RecommendationRepository
import com.x.myapplication.data.state.RecommendationUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecommendationViewModel(
    private val repository: RecommendationRepository = RecommendationRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow<RecommendationUiState>(RecommendationUiState.Loading)
    val uiState: StateFlow<RecommendationUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadRecommendations()
    }

    fun loadRecommendations() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = RecommendationUiState.Loading
            _uiState.value = runCatching {
                repository.getRecommendations()
            }.fold(
                onSuccess = { cards -> RecommendationUiState.Success(cards) },
                onFailure = { error ->
                    RecommendationUiState.Error(error.message ?: "추천 정보를 불러오지 못했습니다.")
                },
            )
        }
    }
}
