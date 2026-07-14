package dev.ridill.oar.moneyPiles.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.ridill.oar.R

enum class PileIcon(
    val code: String,
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
) {
    BabyFamily(
        code = "baby_family",
        iconRes = R.drawable.ic_pile_icon_baby_family,
        labelRes = R.string.pile_icon_label_baby_family
    ),
    BusinessStartup(
        code = "business_startup",
        iconRes = R.drawable.ic_pile_icon_business_startup,
        labelRes = R.string.pile_icon_label_business_startup
    ),
    DebtPayoff(
        code = "debt_payoff",
        iconRes = R.drawable.ic_pile_icon_debt_payoff,
        labelRes = R.string.pile_icon_label_debt_payoff
    ),
    DownPayment(
        code = "down_payment",
        iconRes = R.drawable.ic_pile_icon_down_payment,
        labelRes = R.string.pile_icon_label_down_payment
    ),
    Education(
        code = "education",
        iconRes = R.drawable.ic_pile_icon_education,
        labelRes = R.string.pile_icon_label_education
    ),
    Electronics(
        code = "electronics",
        iconRes = R.drawable.ic_pile_icon_electronics,
        labelRes = R.string.pile_icon_label_electronics
    ),
    EmergencyFund(
        code = "emergency_fund",
        iconRes = R.drawable.ic_pile_icon_emergency_fund,
        labelRes = R.string.pile_icon_label_emergency_fund
    ),
    FinancialIndependence(
        code = "financial_independence",
        iconRes = R.drawable.ic_pile_icon_financial_independence,
        labelRes = R.string.pile_icon_label_financial_independence
    ),
    Gift(
        code = "gift",
        iconRes = R.drawable.ic_pile_icon_gift,
        labelRes = R.string.pile_icon_label_gift
    ),
    Health(
        code = "health",
        iconRes = R.drawable.ic_pile_icon_health,
        labelRes = R.string.pile_icon_label_health
    ),
    Home(
        code = "home",
        iconRes = R.drawable.ic_pile_icon_home,
        labelRes = R.string.pile_icon_label_home
    ),
    HomeRenovation(
        code = "home_renovation",
        iconRes = R.drawable.ic_pile_icon_home_renovation,
        labelRes = R.string.pile_icon_label_home_renovation
    ),
    Investment(
        code = "investment",
        iconRes = R.drawable.ic_pile_icon_investment,
        labelRes = R.string.pile_icon_label_investment
    ),
    LandProperty(
        code = "land_property",
        iconRes = R.drawable.ic_pile_icon_land_property,
        labelRes = R.string.pile_icon_label_land_property
    ),
    LegacyInheritance(
        code = "legacy_inheritance",
        iconRes = R.drawable.ic_pile_icon_legacy_inheritance,
        labelRes = R.string.pile_icon_label_legacy_inheritance
    ),
    Purchase(
        code = "purchase",
        iconRes = R.drawable.ic_pile_icon_purchase,
        labelRes = R.string.pile_icon_label_purchase
    ),
    Retirement(
        code = "retirement",
        iconRes = R.drawable.ic_pile_icon_retirement,
        labelRes = R.string.pile_icon_label_retirement
    ),
    Savings(
        code = "savings",
        iconRes = R.drawable.ic_pile_icon_savings,
        labelRes = R.string.pile_icon_label_savings
    ),
    Travel(
        code = "travel",
        iconRes = R.drawable.ic_pile_icon_travel,
        labelRes = R.string.pile_icon_label_travel
    ),
    Vehicle(
        code = "vehicle",
        iconRes = R.drawable.ic_pile_icon_vehicle,
        labelRes = R.string.pile_icon_label_vehicle
    ),
    Wedding(
        code = "wedding",
        iconRes = R.drawable.ic_pile_icon_wedding,
        labelRes = R.string.pile_icon_label_wedding
    );

    companion object {
        fun forCode(code: String): PileIcon = entries
            .find { it.code == code }
            ?: throw IllegalArgumentException("Invalid PileIcon code: $code")
    }
}