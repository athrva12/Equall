package com.example.equall

import com.example.equall.data.*
import com.example.equall.domain.*
import com.example.equall.presentation.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class HomeTests {
    @Test fun indianCurrencyGrouping() {
        val cases = mapOf(0L to "₹0", 999L to "₹999", 1000L to "₹1,000", 12_450L to "₹12,450",
            500_000L to "₹5,00,000", 300_000L to "₹3,00,000", 245_000L to "₹2,45,000", 12_345_678L to "₹1,23,45,678", -1000L to "−₹1,000")
        cases.forEach { (amount, expected) -> assertEquals(expected, formatRupees(amount)) }
    }
    @Test fun onlyExcellentTierQualifiesRegardlessOfScore() {
        val check = CheckEligibilityUseCase()
        TrustTier.entries.forEach { tier -> assertEquals(tier == TrustTier.EXCELLENT, check(TrustScore(900, tier, 0))) }
        assertTrue(check(TrustScore(1, TrustTier.EXCELLENT, -20)))
    }
    @Test fun mapsAllThreeScenarios() = runTest {
        val repository = HomeRepositoryImpl(FakeHomeDataSource())
        val discovery = repository.getHomeData(HomeScenario.DISCOVERY)
        assertEquals(500_000L, assertIs<LoanLifecycle.Discovery>(discovery.loan).offeredAmount)
        assertFalse(discovery.products.single { it.id == "card" }.available)
        val review = assertIs<LoanLifecycle.UnderReview>(repository.getHomeData(HomeScenario.REVIEW).loan)
        assertEquals(300_000L, review.appliedAmount)
        assertEquals(listOf(StepStatus.COMPLETED, StepStatus.COMPLETED, StepStatus.CURRENT, StepStatus.PENDING), review.steps.map { it.status })
        val active = assertIs<LoanLifecycle.Active>(repository.getHomeData(HomeScenario.ACTIVE).loan)
        assertEquals(245_000L, active.outstandingAmount)
        assertEquals(4, active.paidEmis)
        assertEquals(12, active.totalEmis)
    }
    @Test fun warningIncludesThreeDaysTodayAndOverdue() = runTest {
        for (days in listOf(-1, 0, 1, 3, 4, 8)) {
            val loan = assertIs<LoanLifecycle.Active>(HomeRepositoryImpl(FakeHomeDataSource(daysUntilDue = days)).getHomeData(HomeScenario.ACTIVE).loan)
            assertEquals(days <= 3, loan.isDueSoon)
        }
    }
    @Test fun eligibilityTransitionsAndDisabledProduct() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val model = HomeViewModel(GetHomeDataUseCase(HomeRepositoryImpl(FakeHomeDataSource())), CheckEligibilityUseCase())
            advanceUntilIdle()
            model.onEvent(HomeEvent.SelectProduct("card"))
            assertNull(model.state.value.message)
            model.onEvent(HomeEvent.CheckEligibility)
            advanceUntilIdle()
            assertEquals(HomeScenario.REVIEW, model.state.value.scenario)
            assertIs<LoanLifecycle.UnderReview>(model.state.value.data?.loan)
            model.onEvent(HomeEvent.SelectScenario(HomeScenario.ACTIVE))
            advanceUntilIdle()
            assertIs<LoanLifecycle.Active>(model.state.value.data?.loan)
            model.onEvent(HomeEvent.SelectScenario(HomeScenario.DISCOVERY))
            advanceUntilIdle()
            assertIs<LoanLifecycle.Discovery>(model.state.value.data?.loan)
        } finally { Dispatchers.resetMain() }
    }
    @Test fun rejectedEligibilityStaysInDiscoveryAndShowsMessage() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            for (tier in listOf("GOOD", "FAIR")) {
                val model = HomeViewModel(GetHomeDataUseCase(HomeRepositoryImpl(FakeHomeDataSource(tier))), CheckEligibilityUseCase())
                advanceUntilIdle()
                model.onEvent(HomeEvent.CheckEligibility)
                advanceUntilIdle()
                assertEquals(HomeScenario.DISCOVERY, model.state.value.scenario)
                assertTrue(model.state.value.message.orEmpty().contains("aren't eligible yet"))
                model.onEvent(HomeEvent.DismissMessage)
                assertNull(model.state.value.message)
            }
        } finally { Dispatchers.resetMain() }
    }
    @Test fun loadFailureCanBeRetried() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            var fail = true
            val repository = object : HomeRepository {
                override suspend fun getHomeData(scenario: HomeScenario): HomeData {
                    if (fail) error("Offline")
                    return FakeHomeDataSource().getHome(scenario).toDomain()
                }
            }
            val model = HomeViewModel(GetHomeDataUseCase(repository), CheckEligibilityUseCase())
            advanceUntilIdle()
            assertNotNull(model.state.value.error)
            fail = false
            model.onEvent(HomeEvent.Retry)
            advanceUntilIdle()
            assertNull(model.state.value.error)
            assertNotNull(model.state.value.data)
        } finally { Dispatchers.resetMain() }
    }
}
