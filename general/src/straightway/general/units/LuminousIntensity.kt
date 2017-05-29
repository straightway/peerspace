package straightway.general.units

class LuminousIntensity internal constructor(scale: UnitScale): QuantityBase("cd", scale, { LuminousIntensity(it) })

val candela = LuminousIntensity(uni)