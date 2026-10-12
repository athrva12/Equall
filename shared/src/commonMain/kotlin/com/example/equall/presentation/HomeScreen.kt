package com.example.equall.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.equall.domain.*

private val Purple = Color(0xFF4D54E4)
private val Ink = Color(0xFF17191D)
private val Muted = Color(0xFF5C6373)
private val Green = Color(0xFF1E7E4D)
private val Background = Color(0xFFEEEFF3)
private val Pale = Color(0xFFEDEDFF)

@Composable
fun HomeScreen(state: HomeUiState, onEvent: (HomeEvent) -> Unit) {
    Scaffold(containerColor = Background,
        topBar = {
            Row(Modifier.fillMaxWidth().background(Color.White).statusBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Equall", color = Purple, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                Box(Modifier.size(44.dp).border(1.dp, Background, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                    Icon(HomeIcons.Notifications, contentDescription = "Notifications", modifier = Modifier.size(23.dp))
                }
            }
        }, bottomBar = { BottomNavigation() }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HomeScenario.entries.forEachIndexed { index, scenario ->
                    val selected = state.scenario == scenario
                    val label = listOf("A · Discovery", "B · Review", "C · Active")[index]
                    Box(Modifier.weight(1f).heightIn(min = 40.dp).background(if (selected) Purple else Color(0xFFE2E4EB), RoundedCornerShape(13.dp))
                        .semantics { this.selected = selected; role = Role.Tab }
                        .clickable { onEvent(HomeEvent.SelectScenario(scenario)) }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                        Text(label, color = if (selected) Color.White else Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Column(Modifier.padding(top = 2.dp, bottom = 2.dp)) {
                Text("Good morning", color = Muted, fontSize = 15.sp)
                Text("Hi, ${state.data?.firstName ?: "Priya"}", fontSize = 29.sp, fontWeight = FontWeight.ExtraBold)
            }
            when {
                state.loading -> Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.error != null -> CardSurface {
                    Text(state.error, color = Muted)
                    Button(onClick = { onEvent(HomeEvent.Retry) }) { Text("Try again") }
                }
                else -> state.data?.let { data ->
                    when (val loan = data.loan) {
                        is LoanLifecycle.Discovery -> {
                            OfferCard(loan) { onEvent(HomeEvent.CheckEligibility) }
                            TrustCard(data.trustScore)
                            Explore(data.products) { onEvent(HomeEvent.SelectProduct(it)) }
                        }
                        is LoanLifecycle.UnderReview -> {
                            CardSurface(Color(0xFF3D42CE)) {
                                CardHeading("APPLICATION UNDER REVIEW", "●  In review")
                                Amount(loan.appliedAmount)
                                Text("We'll notify you at every step.", color = Color.White.copy(alpha = .85f), fontSize = 14.sp)
                            }
                            ProgressCard(loan.steps)
                            TrustCard(data.trustScore, underReview = true)
                        }
                        is LoanLifecycle.Active -> {
                            ActiveCard(loan)
                            DueBanner(loan) { onEvent(HomeEvent.PayNow) }
                            CardSurface {
                                DetailRow("APR", loan.apr)
                                HorizontalDivider(color = Background)
                                DetailRow("EMI amount", formatRupees(loan.emiAmount))
                                HorizontalDivider(color = Background)
                                DetailRow("Due date", loan.dueDate)
                                HorizontalDivider(color = Background)
                                DetailRow("Autodebit", loan.bank)
                            }
                            TrustCard(data.trustScore)
                        }
                    }
                }
            }
        }
    }
    state.message?.let { message ->
        AlertDialog(onDismissRequest = { onEvent(HomeEvent.DismissMessage) }, title = { Text("Equall") }, text = { Text(message) },
            confirmButton = { TextButton(onClick = { onEvent(HomeEvent.DismissMessage) }) { Text("Got it") } })
    }
}

@Composable
private fun CardSurface(color: Color = Color.White, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().background(color, RoundedCornerShape(17.dp)).padding(20.dp), content = content)
}
@Composable
private fun CardHeading(title: String, badge: String) {
    Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, color = Color.White.copy(alpha = .8f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = .8.sp, modifier = Modifier.weight(1f))
        Text(badge, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.background(Color.White.copy(alpha = .17f), RoundedCornerShape(8.dp)).padding(horizontal = 9.dp, vertical = 4.dp))
    }
}
@Composable
private fun Amount(amount: Long) { Text(formatRupees(amount), color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp) }
@Composable
private fun OfferCard(loan: LoanLifecycle.Discovery, onCheck: () -> Unit) {
    CardSurface(Color(0xFF1B7054)) {
        CardHeading("PRE-QUALIFIED OFFER", "Personal loan")
        Amount(loan.offeredAmount)
        Text("Tenure up to ${loan.maxTenureMonths} months", color = Color.White.copy(alpha = .85f), fontSize = 14.sp)
        Button(onClick = onCheck, modifier = Modifier.fillMaxWidth().padding(top = 14.dp).heightIn(min = 48.dp), shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1B7054))) {
            Text("Check eligibility", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
@Composable
private fun TrustCard(score: TrustScore, underReview: Boolean = false) {
    Row(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(17.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(44.dp).background(Color(0xFFE4F3ED), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Text("IQ", color = Color(0xFF1B7054), fontWeight = FontWeight.Bold, fontSize = 13.sp) }
        Column(Modifier.weight(1f)) {
            Text("TrustIQ", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            if (underReview) Text("Score updates after decision", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            else Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(score.score.toString(), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Text("${if (score.monthlyChange >= 0) "▲ +" else "▼ "}${score.monthlyChange} this month", color = if (score.monthlyChange >= 0) Green else Color(0xFFB74939), fontSize = 10.sp)
            }
        }
        Text(score.tier.name, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.background(if (score.tier == TrustTier.EXCELLENT) Green else Muted, RoundedCornerShape(8.dp)).padding(9.dp, 6.dp))
    }
}
@Composable
private fun Explore(products: List<Product>, onProduct: (String) -> Unit) {
    Text("EXPLORE", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(top = 4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        products.forEach { product ->
            Column(Modifier.weight(1f).heightIn(min = 126.dp).background(if (product.available) Color.White else Color(0xFFF5F6F8), RoundedCornerShape(16.dp))
                .clickable(enabled = product.available, role = Role.Button) { onProduct(product.id) }.padding(12.dp)) {
                Box(Modifier.size(36.dp).background(if (product.available) Pale else Color(0xFFE6E8EC), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Text(product.initials, color = if (product.available) Purple else Muted, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(Modifier.height(12.dp))
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 16.sp, color = if (product.available) Ink else Muted)
                Text(product.description, color = if (product.available) Muted else Color(0xFF745819), fontSize = 10.sp, lineHeight = 14.sp,
                    modifier = if (product.available) Modifier else Modifier.background(Color(0xFFFFE7A6), RoundedCornerShape(5.dp)).padding(4.dp, 2.dp))
            }
        }
    }
}
@Composable
private fun ProgressCard(steps: List<ApplicationStep>) {
    CardSurface {
        Text("APPLICATION PROGRESS", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(16.dp))
        steps.forEachIndexed { index, step ->
            val color = when (step.status) { StepStatus.COMPLETED -> Green; StepStatus.CURRENT -> Purple; StepStatus.PENDING -> Color(0xFFC8CED9) }
            Row(Modifier.height(IntrinsicSize.Min).semantics(mergeDescendants = true) { stateDescription = step.status.name.lowercase() }) {
                Column(Modifier.width(30.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(28.dp).background(if (step.status == StepStatus.COMPLETED) Green else Color.White, RoundedCornerShape(50))
                        .border(2.dp, color, RoundedCornerShape(50)), contentAlignment = Alignment.Center) {
                        when (step.status) {
                            StepStatus.COMPLETED -> Icon(HomeIcons.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            StepStatus.CURRENT -> Box(Modifier.size(10.dp).background(Purple, RoundedCornerShape(50)))
                            StepStatus.PENDING -> Unit
                        }
                    }
                    if (index < steps.lastIndex) Box(Modifier.width(1.5.dp).weight(1f).background(if (step.status == StepStatus.COMPLETED) Green else Color(0xFFE0E3EA)))
                }
                Column(Modifier.padding(start = 12.dp, bottom = if (index == steps.lastIndex) 0.dp else 22.dp).weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(step.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = when (step.status) { StepStatus.CURRENT -> Purple; StepStatus.PENDING -> Muted; else -> Ink }, modifier = Modifier.weight(1f, fill = false))
                        if (step.status == StepStatus.CURRENT) Text("CURRENT", fontSize = 8.sp, color = Purple, fontWeight = FontWeight.Bold, modifier = Modifier.background(Pale, RoundedCornerShape(4.dp)).padding(5.dp, 3.dp))
                    }
                    Text(step.description, color = Muted, fontSize = 13.sp, lineHeight = 19.sp)
                    step.timestamp?.let { Text(it, color = Muted, fontSize = 12.sp) }
                }
            }
        }
    }
}
@Composable
private fun ActiveCard(loan: LoanLifecycle.Active) {
    CardSurface(Color(0xFF101721)) {
        CardHeading("OUTSTANDING", "${loan.totalEmis}-month loan")
        Amount(loan.outstandingAmount)
        Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp).semantics { contentDescription = "${loan.paidEmis} of ${loan.totalEmis} EMIs paid" }, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(loan.totalEmis) { index -> Box(Modifier.weight(1f).height(7.dp).background(if (index < loan.paidEmis) Color(0xFF858AF3) else Color(0xFF29333F), RoundedCornerShape(3.dp))) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${loan.paidEmis} of ${loan.totalEmis} EMIs paid", color = Color(0xFFA7AFBD), fontSize = 13.sp)
            Text("${loan.totalEmis - loan.paidEmis} left", color = Color(0xFFA7AFBD), fontSize = 13.sp)
        }
    }
}
@Composable
private fun DueBanner(loan: LoanLifecycle.Active, onPay: () -> Unit) {
    val bg = if (loan.isDueSoon) Color(0xFFFFEAB3) else Pale
    val foreground = if (loan.isDueSoon) Color(0xFF664B15) else Purple
    Row(Modifier.fillMaxWidth().background(bg, RoundedCornerShape(16.dp)).border(1.dp, if (loan.isDueSoon) Color(0xFFF4D77B) else Pale, RoundedCornerShape(16.dp)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(HomeIcons.Schedule, null, tint = foreground, modifier = Modifier.size(20.dp))
        Column(Modifier.weight(1f)) {
            Text(when { loan.daysUntilDue < 0 -> "EMI overdue by ${-loan.daysUntilDue} days"; loan.daysUntilDue == 0 -> "Next EMI due today"; loan.daysUntilDue == 1 -> "Next EMI due in 1 day"; else -> "Next EMI due in ${loan.daysUntilDue} days" }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = foreground)
            Text("${formatRupees(loan.emiAmount)} will be autodebited", fontSize = 11.sp, color = foreground)
        }
        Button(onClick = onPay, shape = RoundedCornerShape(13.dp), contentPadding = PaddingValues(horizontal = 14.dp), modifier = Modifier.heightIn(min = 44.dp)) { Text("Pay now", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
    }
}
@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = Muted, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}
@Composable
private fun BottomNavigation() {
    val items: List<Pair<String, ImageVector>> = listOf("Home" to HomeIcons.Home, "Loans" to HomeIcons.Payments, "Payments" to HomeIcons.CreditCard, "Profile" to HomeIcons.PersonOutline)
    Row(Modifier.fillMaxWidth().background(Color.White).navigationBarsPadding().padding(vertical = 16.dp)) {
        items.forEachIndexed { index, (label, icon) ->
            Column(Modifier.weight(1f).semantics { selected = index == 0 }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Icon(icon, null, tint = if (index == 0) Purple else Muted, modifier = Modifier.size(22.dp))
                Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (index == 0) Purple else Muted)
            }
        }
    }
}
