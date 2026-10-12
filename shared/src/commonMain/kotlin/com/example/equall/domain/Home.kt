package com.example.equall.domain

enum class HomeScenario { DISCOVERY, REVIEW, ACTIVE }
enum class TrustTier { EXCELLENT, GOOD, FAIR }
enum class StepStatus { COMPLETED, CURRENT, PENDING }
data class TrustScore(val score: Int, val tier: TrustTier, val monthlyChange: Int)
data class Product(val id: String, val initials: String, val name: String, val description: String, val available: Boolean)
data class ApplicationStep(val title: String, val description: String, val timestamp: String?, val status: StepStatus)
sealed interface LoanLifecycle {
    data class Discovery(val offeredAmount: Long, val maxTenureMonths: Int) : LoanLifecycle
    data class UnderReview(val appliedAmount: Long, val steps: List<ApplicationStep>) : LoanLifecycle
    data class Active(val outstandingAmount: Long, val paidEmis: Int, val totalEmis: Int, val apr: String,
        val emiAmount: Long, val dueDate: String, val bank: String, val daysUntilDue: Int) : LoanLifecycle {
        val isDueSoon: Boolean get() = daysUntilDue <= 3
    }
}
data class HomeData(val firstName: String, val trustScore: TrustScore, val loan: LoanLifecycle, val products: List<Product>)
interface HomeRepository {
    suspend fun getHomeData(scenario: HomeScenario): HomeData
}
class GetHomeDataUseCase(private val repository: HomeRepository) {
    suspend operator fun invoke(scenario: HomeScenario): HomeData = repository.getHomeData(scenario)
}
class CheckEligibilityUseCase {
    operator fun invoke(trustScore: TrustScore): Boolean = trustScore.tier == TrustTier.EXCELLENT
}
