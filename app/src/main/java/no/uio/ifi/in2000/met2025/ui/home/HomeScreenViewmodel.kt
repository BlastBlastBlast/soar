package no.uio.ifi.in2000.met2025.ui.home

// Make 4 UI states:
// 1. Loading
// 2. Success
// 3. Error
// 4. Idle
class HomeScreenViewmodel {

}

sealed class HomeScreenUiState {
    object Loading : HomeScreenUiState()
    data class Success(val data: String) : HomeScreenUiState()
    data class Error(val message: String) : HomeScreenUiState()
    object Idle : HomeScreenUiState()
}

sealed class HomeScreenUiEvent {

}

