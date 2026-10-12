package com.example.equall.data

import com.example.equall.domain.*

// Transport values deliberately use strings; domain types are introduced by mappers.
data class TrustDto(val score: Int, val tier: String, val change: Int)
data class ProductDto(val id: String, val initials: String, val name: String, val description: String, val available: Boolean)
data class StepDto(val title: String, val description: String, val timestamp: String?, val status: String)
sealed interface LoanDto {
    data class Offer(val amount: Long, val months: Int) : LoanDto
    data class Review(val amount: Long, val steps: List<StepDto>) : LoanDto
    data class Active(val outstanding: Long, val paid: Int, val total: Int, val apr: String, val emi: Long,
        val dueDate: String, val bank: String, val daysUntilDue: Int) : LoanDto
}
data class HomeDto(val name: String, val trust: TrustDto, val loan: LoanDto, val products: List<ProductDto>)
class FakeHomeDataSource(private val tier: String = "EXCELLENT", private val daysUntilDue: Int = 3) {
    suspend fun getHome(scenario: HomeScenario): HomeDto = HomeDto(
        name = "Priya", trust = TrustDto(812, tier, 12),
        loan = when (scenario) {
            HomeScenario.DISCOVERY -> LoanDto.Offer(500_000, 36)
            HomeScenario.REVIEW -> LoanDto.Review(300_000, listOf(
                StepDto("Application submitted", "We've received your application", "Today · 9:14 am", "COMPLETED"),
                StepDto("Documents verified", "KYC and bank statement checked", "Today · 9:32 am", "COMPLETED"),
                StepDto("Credit assessment", "Usually takes under 30 minutes", "Started 9:35 am", "CURRENT"),
                StepDto("Disbursal", "Money sent to your bank account", null, "PENDING")
            ))
            HomeScenario.ACTIVE -> LoanDto.Active(245_000, 4, 12, "14.5%", 12_450, "5th of every month", "HDFC Bank ••4521", daysUntilDue)
        },
        products = listOf(
            ProductDto("personal", "PL", "Personal Loan", "Up to ₹5L", true),
            ProductDto("topup", "TU", "Top-up Loan", "For existing borrowers", true),
            ProductDto("card", "CC", "Credit Card", "Coming soon", false)
        )
    )
}
fun HomeDto.toDomain() = HomeData(name, TrustScore(trust.score, TrustTier.valueOf(trust.tier), trust.change),
    when (val dto = loan) {
        is LoanDto.Offer -> LoanLifecycle.Discovery(dto.amount, dto.months)
        is LoanDto.Review -> LoanLifecycle.UnderReview(dto.amount, dto.steps.map {
            ApplicationStep(it.title, it.description, it.timestamp, StepStatus.valueOf(it.status))
        })
        is LoanDto.Active -> LoanLifecycle.Active(dto.outstanding, dto.paid, dto.total, dto.apr, dto.emi, dto.dueDate, dto.bank, dto.daysUntilDue)
    }, products.map { Product(it.id, it.initials, it.name, it.description, it.available) })
class HomeRepositoryImpl(private val source: FakeHomeDataSource) : HomeRepository {
    override suspend fun getHomeData(scenario: HomeScenario): HomeData = source.getHome(scenario).toDomain()
}
