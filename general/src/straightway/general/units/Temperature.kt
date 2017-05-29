package straightway.general.units

class Temperature internal constructor(scale: UnitScale): QuantityBase("K", scale, { Temperature(it) })

val kelvin = Temperature(uni)