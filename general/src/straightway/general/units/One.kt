package straightway.general.units

class One internal constructor(scale: UnitScale): QuantityBase("", scale, { One(it) })

val one = One(uni)