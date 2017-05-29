package straightway.general.units

class AmountOfSubstance internal constructor(scale: UnitScale): QuantityBase("mol", scale, { AmountOfSubstance(it) })

val mol = AmountOfSubstance(uni)