package straightway.units

class LuminousIntensity constructor(scale: UnitScale) : QuantityBase("cd", scale, { LuminousIntensity(it) })

val candela = LuminousIntensity(uni)