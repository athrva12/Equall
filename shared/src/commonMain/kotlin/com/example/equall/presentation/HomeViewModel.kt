package com.example.equall.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.equall.domain.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(val scenario: HomeScenario = HomeScenario.DISCOVERY, val data: HomeData? = null,
    val loading: Boolean = true, val error: String? = null, val message: String? = null)
sealed interface HomeEvent {
    data class SelectScenario(val scenario: HomeScenario) : HomeEvent
    data object CheckEligibility : HomeEvent
    data object PayNow : HomeEvent
    data class SelectProduct(val id: String) : HomeEvent
    data object DismissMessage : HomeEvent
    data object Retry : HomeEvent
}
class HomeViewModel(private val getHomeData: GetHomeDataUseCase, private val checkEligibility: CheckEligibilityUseCase) : ViewModel() {
    private val mutableState = MutableStateFlow(HomeUiState())
    val state = mutableState.asStateFlow()
    private var loadJob: Job? = null
    init { load(HomeScenario.DISCOVERY) }
    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.SelectScenario -> load(event.scenario)
            HomeEvent.Retry -> load(state.value.scenario)
            HomeEvent.CheckEligibility -> {
                val data = state.value.data ?: return
                if (state.value.loading || data.loan !is LoanLifecycle.Discovery) return
                if (checkEligibility(data.trustScore)) load(HomeScenario.REVIEW)
                else message("You aren't eligible yet. An EXCELLENT TrustIQ tier is required.")
            }
            HomeEvent.PayNow -> if (state.value.data?.loan is LoanLifecycle.Active) message("This is a demo. No payment has been taken.")
            is HomeEvent.SelectProduct -> state.value.data?.products?.firstOrNull { it.id == event.id && it.available }?.let {
                message("${it.name}: this demo covers the Home screen only.")
            }
            HomeEvent.DismissMessage -> mutableState.update { it.copy(message = null) }
        }
    }
    private fun message(text: String) = mutableState.update { it.copy(message = text) }
    private fun load(scenario: HomeScenario) {
        loadJob?.cancel()
        mutableState.update { it.copy(scenario = scenario, loading = true, error = null, message = null) }
        loadJob = viewModelScope.launch {
            try {
                val data = getHomeData(scenario)
                mutableState.update { it.copy(data = data, loading = false) }
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(loading = false, error = "We couldn't load your home. Please try again.") }
            }
        }
    }
}

// String grouping is platform independent and also handles Long.MIN_VALUE safely.
fun formatRupees(amount: Long): String {
    val digits = amount.toString().removePrefix("-")
    val grouped = if (digits.length <= 3) digits else {
        digits.dropLast(3).reversed().chunked(2).joinToString(",").reversed() + "," + digits.takeLast(3)
    }
    return (if (amount < 0) "−" else "") + "₹" + grouped
}
