package straightway.units

class AmountOfSubstance constructor(scale: UnitScale) : QuantityBase("mol", scale, { AmountOfSubstance(it) })

val mol = AmountOfSubstance(uni)